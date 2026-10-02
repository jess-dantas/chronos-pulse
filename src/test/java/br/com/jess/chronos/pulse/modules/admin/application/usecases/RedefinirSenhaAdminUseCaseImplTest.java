package br.com.jess.chronos.pulse.modules.admin.application.usecases;

import br.com.jess.chronos.pulse.modules.admin.domain.model.AdminPlataforma;
import br.com.jess.chronos.pulse.modules.admin.domain.ports.input.RedefinirSenhaAdminUseCase.Comando;
import br.com.jess.chronos.pulse.modules.admin.domain.ports.output.AdminPlataformaRepositoryPort;
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
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RedefinirSenhaAdminUseCaseImplTest {

    @Mock
    private AdminPlataformaRepositoryPort repositoryPort;

    @Mock
    private PasswordEncoder passwordEncoder;

    private RedefinirSenhaAdminUseCaseImpl useCase;

    private AdminPlataforma admin;

    @BeforeEach
    void setUp() {
        useCase = new RedefinirSenhaAdminUseCaseImpl(repositoryPort, passwordEncoder);
        admin = AdminPlataforma.builder()
                .id(UUID.randomUUID())
                .username("Administrator")
                .senhaHash("hashAtual")
                .email("admin@example.com")
                .ativo(true)
                .build();
        admin.definirCodigoEmail("hashCodigo", Instant.now().plusSeconds(900));
    }

    @Test
    void deveRedefinirSenhaComCodigoValido() {
        when(repositoryPort.buscarPorUsername("Administrator")).thenReturn(Optional.of(admin));
        when(passwordEncoder.matches("12345678", "hashCodigo")).thenReturn(true);
        when(passwordEncoder.matches("Nova@1234", "hashAtual")).thenReturn(false);
        when(passwordEncoder.encode("Nova@1234")).thenReturn("hashNovo");

        useCase.executar(new Comando("Administrator", "12345678", "Nova@1234"));

        ArgumentCaptor<AdminPlataforma> salvo = ArgumentCaptor.forClass(AdminPlataforma.class);
        verify(repositoryPort).salvar(salvo.capture());
        assertThat(salvo.getValue().getSenhaHash()).isEqualTo("hashNovo");
        assertThat(salvo.getValue().getRecuperacaoEmailHash()).isNull();
    }

    @Test
    void deveContarTentativaQuandoCodigoInvalido() {
        when(repositoryPort.buscarPorUsername("Administrator")).thenReturn(Optional.of(admin));
        when(passwordEncoder.matches("00000000", "hashCodigo")).thenReturn(false);

        assertThatThrownBy(() -> useCase.executar(new Comando("Administrator", "00000000", "Nova@1234")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Código inválido");

        ArgumentCaptor<AdminPlataforma> salvo = ArgumentCaptor.forClass(AdminPlataforma.class);
        verify(repositoryPort).salvar(salvo.capture());
        assertThat(salvo.getValue().getRecuperacaoEmailTentativas()).isEqualTo(1);
        assertThat(salvo.getValue().getSenhaHash()).isEqualTo("hashAtual");
    }

    @Test
    void deveLancarQuandoCodigoExpiradoOuNaoSolicitado() {
        admin.definirCodigoEmail("hashCodigo", Instant.now().minusSeconds(60));
        when(repositoryPort.buscarPorUsername("Administrator")).thenReturn(Optional.of(admin));

        assertThatThrownBy(() -> useCase.executar(new Comando("Administrator", "12345678", "Nova@1234")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Código expirado ou não solicitado");

        verify(repositoryPort, never()).salvar(any());
        verifyNoInteractions(passwordEncoder);
    }

    @Test
    void deveAplicarPasswordPolicyDeGestor() {
        when(repositoryPort.buscarPorUsername("Administrator")).thenReturn(Optional.of(admin));
        when(passwordEncoder.matches("12345678", "hashCodigo")).thenReturn(true);

        assertThatThrownBy(() -> useCase.executar(new Comando("Administrator", "12345678", "Abc@123")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("A senha do gestor deve ter no mínimo 8 caracteres.");

        verify(repositoryPort, never()).salvar(any());
    }

    @Test
    void deveRecusarNovaSenhaIgualAAtual() {
        when(repositoryPort.buscarPorUsername("Administrator")).thenReturn(Optional.of(admin));
        when(passwordEncoder.matches("12345678", "hashCodigo")).thenReturn(true);
        when(passwordEncoder.matches("Nova@1234", "hashAtual")).thenReturn(true);

        assertThatThrownBy(() -> useCase.executar(new Comando("Administrator", "12345678", "Nova@1234")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("A nova senha deve ser diferente da atual");

        verify(repositoryPort, never()).salvar(any());
    }

    @Test
    void deveLancarQuandoUsuarioNaoEncontrado() {
        when(repositoryPort.buscarPorUsername("desconhecido")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.executar(new Comando("desconhecido", "12345678", "Nova@1234")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Credenciais inválidas");

        verify(repositoryPort, never()).salvar(any());
        verify(passwordEncoder, never()).matches(anyString(), anyString());
    }
}
