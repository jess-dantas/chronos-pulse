package br.com.jess.chronos.pulse.modules.admin.application.usecases;

import br.com.jess.chronos.pulse.modules.admin.domain.model.AdminPlataforma;
import br.com.jess.chronos.pulse.modules.admin.domain.ports.input.VerificarCodigoEmailAdminUseCase.Comando;
import br.com.jess.chronos.pulse.modules.admin.domain.ports.output.AdminPlataformaRepositoryPort;
import br.com.jess.chronos.pulse.modules.auth.infrastructure.security.JwtService;
import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VerificarCodigoEmailAdminUseCaseImplTest {

    @Mock
    private AdminPlataformaRepositoryPort repositoryPort;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    private VerificarCodigoEmailAdminUseCaseImpl useCase;

    private AdminPlataforma admin;
    private Claims claims;

    @BeforeEach
    void setUp() {
        useCase = new VerificarCodigoEmailAdminUseCaseImpl(repositoryPort, passwordEncoder, jwtService);
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
    void deveEmitirTokensComCodigoEmailValido() {
        admin.definirCodigoEmail("hash-codigo", Instant.now().plusSeconds(600));
        prepararToken();
        when(passwordEncoder.matches("12345678", "hash-codigo")).thenReturn(true);
        when(jwtService.gerarAccessTokenAdmin("Administrator", admin.getId().toString())).thenReturn("access");
        when(jwtService.gerarRefreshTokenAdmin("Administrator", admin.getId().toString())).thenReturn("refresh");

        var resultado = useCase.executar(new Comando("temp", "12345678"));

        assertThat(resultado.accessToken()).isEqualTo("access");
        assertThat(resultado.refreshToken()).isEqualTo("refresh");
        assertThat(admin.getRecuperacaoEmailHash()).isNull();
        assertThat(admin.getUltimoLogin()).isNotNull();
        verify(repositoryPort).salvar(any(AdminPlataforma.class));
    }

    @Test
    void deveContarTentativaComCodigoErrado() {
        admin.definirCodigoEmail("hash-codigo", Instant.now().plusSeconds(600));
        prepararToken();
        when(passwordEncoder.matches("00000000", "hash-codigo")).thenReturn(false);

        assertThatThrownBy(() -> useCase.executar(new Comando("temp", "00000000")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Código inválido");

        assertThat(admin.getRecuperacaoEmailTentativas()).isEqualTo(1);
        assertThat(admin.getRecuperacaoEmailHash()).isEqualTo("hash-codigo");
        verify(repositoryPort).salvar(any(AdminPlataforma.class));
    }

    @Test
    void deveRecusarCodigoNaoSolicitado() {
        prepararToken();

        assertThatThrownBy(() -> useCase.executar(new Comando("temp", "12345678")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Código expirado ou não solicitado");
        verifyNoInteractions(passwordEncoder);
    }

    @Test
    void deveRecusarCodigoExpirado() {
        admin.definirCodigoEmail("hash-codigo", Instant.now().minusSeconds(1));
        prepararToken();

        assertThatThrownBy(() -> useCase.executar(new Comando("temp", "12345678")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Código expirado ou não solicitado");
    }

    @Test
    void deveRecusarQuandoAtingirLimiteDeTentativas() {
        admin.definirCodigoEmail("hash-codigo", Instant.now().plusSeconds(600));
        for (int i = 0; i < AdminPlataforma.MAX_TENTATIVAS_CODIGO_EMAIL; i++) {
            admin.registrarTentativaCodigoEmail();
        }
        prepararToken();

        assertThatThrownBy(() -> useCase.executar(new Comando("temp", "12345678")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Código expirado ou não solicitado");
        verifyNoInteractions(passwordEncoder);
    }
}
