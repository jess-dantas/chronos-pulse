package br.com.jess.chronos.pulse.modules.admin.application.usecases;

import br.com.jess.chronos.pulse.modules.admin.domain.model.AdminPlataforma;
import br.com.jess.chronos.pulse.modules.admin.domain.ports.input.BootstrapAdminUseCase.Comando;
import br.com.jess.chronos.pulse.modules.admin.domain.ports.output.AdminPlataformaRepositoryPort;
import br.com.jess.chronos.pulse.modules.auth.infrastructure.security.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BootstrapAdminUseCaseImplTest {

    @Mock
    private AdminPlataformaRepositoryPort repositoryPort;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    private BootstrapAdminUseCaseImpl useCase;

    @BeforeEach
    void setUp() {
        useCase = new BootstrapAdminUseCaseImpl(repositoryPort, passwordEncoder, jwtService);
    }

    private Comando comando() {
        return new Comando("Administrator", "admin1234", "Admin", "admin@example.com");
    }

    @Test
    void disponivelQuandoTabelaVazia() {
        when(repositoryPort.count()).thenReturn(0L);
        assertThat(useCase.disponivel()).isTrue();
    }

    @Test
    void naoDisponivelQuandoJaExisteAdmin() {
        when(repositoryPort.count()).thenReturn(1L);
        assertThat(useCase.disponivel()).isFalse();
    }

    @Test
    void deveRecusarQuandoJaExisteAdmin() {
        when(repositoryPort.count()).thenReturn(1L);

        assertThatThrownBy(() -> useCase.executar(comando()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Provisionamento já concluído");

        verify(repositoryPort, never()).salvar(any());
    }

    @Test
    void deveRecusarUsernameDuplicado() {
        when(repositoryPort.count()).thenReturn(0L);
        when(repositoryPort.existsByUsername("Administrator")).thenReturn(true);

        assertThatThrownBy(() -> useCase.executar(comando()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Username já existe");
    }

    @Test
    void deveCriarAdminERetornarTempToken() {
        UUID id = UUID.randomUUID();
        when(repositoryPort.count()).thenReturn(0L);
        when(repositoryPort.existsByUsername("Administrator")).thenReturn(false);
        when(repositoryPort.buscarPorEmail("admin@example.com")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("admin1234")).thenReturn("hash-bcrypt");
        when(repositoryPort.salvar(any(AdminPlataforma.class))).thenAnswer(invocation -> {
            AdminPlataforma admin = invocation.getArgument(0);
            admin.setId(id);
            return admin;
        });
        when(jwtService.gerarTempTokenTwoFactor(id.toString())).thenReturn("temp-token");

        var resultado = useCase.executar(comando());

        assertThat(resultado.tempToken()).isEqualTo("temp-token");

        ArgumentCaptor<AdminPlataforma> captor = ArgumentCaptor.forClass(AdminPlataforma.class);
        verify(repositoryPort).salvar(captor.capture());
        assertThat(captor.getValue().getUsername()).isEqualTo("Administrator");
        assertThat(captor.getValue().getSenhaHash()).isEqualTo("hash-bcrypt");
        assertThat(captor.getValue().isTwoFactorEnabled()).isFalse();
        assertThat(captor.getValue().getCriadoEm())
                .as("bootstrap deve definir criadoEm antes de salvar (NOT NULL)")
                .isNotNull();
    }
}
