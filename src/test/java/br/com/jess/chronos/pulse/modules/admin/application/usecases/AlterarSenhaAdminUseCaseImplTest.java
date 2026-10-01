package br.com.jess.chronos.pulse.modules.admin.application.usecases;

import br.com.jess.chronos.pulse.modules.admin.domain.model.AdminPlataforma;
import br.com.jess.chronos.pulse.modules.admin.domain.ports.input.AlterarSenhaAdminUseCase.Comando;
import br.com.jess.chronos.pulse.modules.admin.domain.ports.output.AdminPlataformaRepositoryPort;
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
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AlterarSenhaAdminUseCaseImplTest {

    @Mock
    private AdminPlataformaRepositoryPort repositoryPort;

    @Mock
    private PasswordEncoder passwordEncoder;

    private AlterarSenhaAdminUseCaseImpl useCase;

    private AdminPlataforma admin;
    private String adminId;

    @BeforeEach
    void setUp() {
        useCase = new AlterarSenhaAdminUseCaseImpl(repositoryPort, passwordEncoder);
        adminId = UUID.randomUUID().toString();
        admin = AdminPlataforma.builder()
                .id(UUID.fromString(adminId))
                .username("Administrator")
                .senhaHash("hashAtual")
                .ativo(true)
                .twoFactorEnabled(true)
                .build();
    }

    @Test
    void deveTrocarSenhaSemInformarSenhaAtual() {
        when(repositoryPort.buscarPorId(UUID.fromString(adminId))).thenReturn(Optional.of(admin));
        when(passwordEncoder.matches("Nova@1234", "hashAtual")).thenReturn(false);
        when(passwordEncoder.encode("Nova@1234")).thenReturn("hashNovo");

        useCase.executar(new Comando(adminId, null, "Nova@1234"));

        ArgumentCaptor<AdminPlataforma> captor = ArgumentCaptor.forClass(AdminPlataforma.class);
        verify(repositoryPort).salvar(captor.capture());
        assertThat(captor.getValue().getSenhaHash()).isEqualTo("hashNovo");
        verify(passwordEncoder, never()).matches(isNull(), anyString());
    }

    @Test
    void deveRecusarSenhaAtualIncorretaQuandoEnviada() {
        when(repositoryPort.buscarPorId(UUID.fromString(adminId))).thenReturn(Optional.of(admin));
        when(passwordEncoder.matches("senhaErrada", "hashAtual")).thenReturn(false);

        assertThatThrownBy(() -> useCase.executar(new Comando(adminId, "senhaErrada", "Nova@1234")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Senha atual incorreta");

        verify(repositoryPort, never()).salvar(any());
    }

    @Test
    void deveAceitarSenhaAtualCorretaQuandoEnviada() {
        when(repositoryPort.buscarPorId(UUID.fromString(adminId))).thenReturn(Optional.of(admin));
        when(passwordEncoder.matches("senhaAtualCerta", "hashAtual")).thenReturn(true);
        when(passwordEncoder.matches("Nova@1234", "hashAtual")).thenReturn(false);
        when(passwordEncoder.encode("Nova@1234")).thenReturn("hashNovo");

        useCase.executar(new Comando(adminId, "senhaAtualCerta", "Nova@1234"));

        verify(repositoryPort).salvar(any(AdminPlataforma.class));
    }

    @Test
    void deveRecusarNovaSenhaIgualAAtual() {
        when(repositoryPort.buscarPorId(UUID.fromString(adminId))).thenReturn(Optional.of(admin));
        when(passwordEncoder.matches("Nova@1234", "hashAtual")).thenReturn(true);

        assertThatThrownBy(() -> useCase.executar(new Comando(adminId, null, "Nova@1234")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("A nova senha deve ser diferente da atual");

        verify(repositoryPort, never()).salvar(any());
    }

    @Test
    void deveAplicarPasswordPolicyDeGestor() {
        when(repositoryPort.buscarPorId(UUID.fromString(adminId))).thenReturn(Optional.of(admin));

        assertThatThrownBy(() -> useCase.executar(new Comando(adminId, null, "Abc@123")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("A senha do gestor deve ter no mínimo 8 caracteres.");

        verify(repositoryPort, never()).salvar(any());
    }

    @Test
    void deveLancarQuandoAdminNaoEncontrado() {
        when(repositoryPort.buscarPorId(UUID.fromString(adminId))).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.executar(new Comando(adminId, null, "Nova@1234")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Admin não encontrado");
    }
}
