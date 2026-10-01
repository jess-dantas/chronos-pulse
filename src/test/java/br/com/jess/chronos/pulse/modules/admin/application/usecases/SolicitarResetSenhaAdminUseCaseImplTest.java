package br.com.jess.chronos.pulse.modules.admin.application.usecases;

import br.com.jess.chronos.pulse.modules.admin.domain.model.AdminPlataforma;
import br.com.jess.chronos.pulse.modules.admin.domain.ports.input.SolicitarResetSenhaAdminUseCase.Comando;
import br.com.jess.chronos.pulse.modules.admin.domain.ports.output.AdminPlataformaRepositoryPort;
import br.com.jess.chronos.pulse.modules.notificacao.service.EmailRecuperacaoSenhaService;
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
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SolicitarResetSenhaAdminUseCaseImplTest {

    @Mock
    private AdminPlataformaRepositoryPort repositoryPort;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private EmailRecuperacaoSenhaService emailRecuperacaoSenhaService;

    private SolicitarResetSenhaAdminUseCaseImpl useCase;

    private AdminPlataforma admin;

    @BeforeEach
    void setUp() {
        useCase = new SolicitarResetSenhaAdminUseCaseImpl(
                repositoryPort, passwordEncoder, emailRecuperacaoSenhaService);
        admin = AdminPlataforma.builder()
                .id(UUID.randomUUID())
                .username("Administrator")
                .senhaHash("hash")
                .email("admin@example.com")
                .ativo(true)
                .build();
    }

    @Test
    void deveGerarCodigoDe8DigitosESalvarNoAdmin() {
        when(repositoryPort.buscarPorUsername("Administrator")).thenReturn(Optional.of(admin));
        when(passwordEncoder.encode(anyString())).thenReturn("hashCodigo");

        useCase.executar(new Comando("Administrator"));

        ArgumentCaptor<String> codigo = ArgumentCaptor.forClass(String.class);
        verify(passwordEncoder).encode(codigo.capture());
        assertThat(codigo.getValue()).matches("\\d{8}");

        ArgumentCaptor<AdminPlataforma> salvo = ArgumentCaptor.forClass(AdminPlataforma.class);
        verify(repositoryPort).salvar(salvo.capture());
        assertThat(salvo.getValue().getRecuperacaoEmailHash()).isEqualTo("hashCodigo");
        assertThat(salvo.getValue().getRecuperacaoEmailExpiraEm()).isAfter(Instant.now());

        verify(emailRecuperacaoSenhaService)
                .enviarCodigoRecuperacaoAsync(eq("admin@example.com"), anyString());
    }

    @Test
    void deveLancarQuandoUsuarioNaoEncontrado() {
        when(repositoryPort.buscarPorUsername("desconhecido")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.executar(new Comando("desconhecido")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Credenciais inválidas");

        verifyNoInteractions(passwordEncoder, emailRecuperacaoSenhaService);
        verify(repositoryPort, never()).salvar(any());
    }

    @Test
    void deveLancarQuandoContaDesativada() {
        admin.setAtivo(false);
        when(repositoryPort.buscarPorUsername("Administrator")).thenReturn(Optional.of(admin));

        assertThatThrownBy(() -> useCase.executar(new Comando("Administrator")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Credenciais inválidas");

        verifyNoInteractions(emailRecuperacaoSenhaService);
    }

    @Test
    void deveLancarQuandoContaBloqueada() {
        admin.setBloqueioLoginAte(Instant.now().plusSeconds(600));
        when(repositoryPort.buscarPorUsername("Administrator")).thenReturn(Optional.of(admin));

        assertThatThrownBy(() -> useCase.executar(new Comando("Administrator")))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("bloqueada");

        verifyNoInteractions(emailRecuperacaoSenhaService);
    }

    @Test
    void deveLancarQuandoNaoHaEmailCadastrado() {
        admin.setEmail(null);
        when(repositoryPort.buscarPorUsername("Administrator")).thenReturn(Optional.of(admin));

        assertThatThrownBy(() -> useCase.executar(new Comando("Administrator")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("E-mail de recuperação não cadastrado");

        verifyNoInteractions(emailRecuperacaoSenhaService);
        verify(repositoryPort, never()).salvar(any());
    }
}
