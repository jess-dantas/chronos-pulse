package br.com.jess.chronos.pulse.modules.admin.application.usecases;

import br.com.jess.chronos.pulse.modules.admin.domain.model.AdminPlataforma;
import br.com.jess.chronos.pulse.modules.admin.domain.ports.input.EnviarCodigoEmailAdminUseCase.Comando;
import br.com.jess.chronos.pulse.modules.admin.domain.ports.output.AdminPlataformaRepositoryPort;
import br.com.jess.chronos.pulse.modules.auth.infrastructure.security.JwtService;
import br.com.jess.chronos.pulse.modules.notificacao.service.EmailRecuperacaoSenhaService;
import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EnviarCodigoEmailAdminUseCaseImplTest {

    @Mock
    private AdminPlataformaRepositoryPort repositoryPort;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @Mock
    private EmailRecuperacaoSenhaService emailRecuperacaoSenhaService;

    private EnviarCodigoEmailAdminUseCaseImpl useCase;

    private AdminPlataforma admin;
    private Claims claims;

    @BeforeEach
    void setUp() {
        useCase = new EnviarCodigoEmailAdminUseCaseImpl(
                repositoryPort, passwordEncoder, jwtService, emailRecuperacaoSenhaService);
        admin = AdminPlataforma.builder()
                .id(UUID.randomUUID())
                .username("Administrator")
                .senhaHash("hash")
                .email("admin@example.com")
                .ativo(true)
                .twoFactorEnabled(true)
                .build();
        claims = mock(Claims.class);
    }

    private void prepararToken() {
        when(jwtService.extrairClaims("temp")).thenReturn(claims);
        when(jwtService.isTwoFactorToken(claims)).thenReturn(true);
        when(claims.get("adminId", String.class)).thenReturn(admin.getId().toString());
        when(repositoryPort.buscarPorId(admin.getId())).thenReturn(Optional.of(admin));
    }

    @Test
    void deveGerarCodigoEEnviarEmail() {
        prepararToken();
        when(passwordEncoder.encode(anyString())).thenReturn("codigo-hash");

        useCase.executar(new Comando("temp"));

        ArgumentCaptor<AdminPlataforma> captor = ArgumentCaptor.forClass(AdminPlataforma.class);
        verify(repositoryPort).salvar(captor.capture());
        AdminPlataforma salvo = captor.getValue();
        assertThat(salvo.getRecuperacaoEmailHash()).isEqualTo("codigo-hash");
        assertThat(salvo.getRecuperacaoEmailExpiraEm()).isAfter(Instant.now());
        assertThat(salvo.getRecuperacaoEmailTentativas()).isZero();
        verify(emailRecuperacaoSenhaService).enviarCodigoRecuperacaoAsync(eq("admin@example.com"), anyString());
    }

    @Test
    void deveRecusarContaBloqueada() {
        prepararToken();
        admin.setBloqueioLoginAte(Instant.now().plusSeconds(600));

        assertThatThrownBy(() -> useCase.executar(new Comando("temp")))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("bloqueada");
        verifyNoInteractions(passwordEncoder, emailRecuperacaoSenhaService);
        verify(repositoryPort, never()).salvar(any(AdminPlataforma.class));
    }

    @Test
    void deveRecusarAdminSemEmail() {
        admin.setEmail(null);
        prepararToken();

        assertThatThrownBy(() -> useCase.executar(new Comando("temp")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("E-mail de recuperação não cadastrado");
        verifyNoInteractions(emailRecuperacaoSenhaService);
    }

    @Test
    void deveRecusarTempTokenExpirado() {
        when(jwtService.extrairClaims("temp")).thenThrow(new RuntimeException("expired"));

        assertThatThrownBy(() -> useCase.executar(new Comando("temp")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Sessão expirada");
    }
}
