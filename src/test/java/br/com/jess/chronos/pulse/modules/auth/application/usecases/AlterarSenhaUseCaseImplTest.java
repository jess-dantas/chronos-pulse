package br.com.jess.chronos.pulse.modules.auth.application.usecases;

import br.com.jess.chronos.pulse.modules.auth.domain.model.CpcUsuario;
import br.com.jess.chronos.pulse.modules.auth.domain.model.Role;
import br.com.jess.chronos.pulse.modules.auth.domain.ports.input.AlterarSenhaUseCase.Comando;
import br.com.jess.chronos.pulse.modules.auth.domain.ports.output.CpcUsuarioRepositoryPort;
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
class AlterarSenhaUseCaseImplTest {

    @Mock
    private CpcUsuarioRepositoryPort usuarioRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    private AlterarSenhaUseCaseImpl useCase;

    private CpcUsuario usuario;

    @BeforeEach
    void setUp() {
        useCase = new AlterarSenhaUseCaseImpl(usuarioRepository, passwordEncoder);
        usuario = new CpcUsuario(UUID.randomUUID(), UUID.randomUUID(), "12345678901",
                "Usuario Teste", "teste@empresa.com", "hashAtual", Role.COLABORADOR, UUID.randomUUID());
    }

    @Test
    void deveTrocarSenhaSemInformarSenhaAtual() {
        when(usuarioRepository.buscarPorCpf("12345678901")).thenReturn(Optional.of(usuario));
        when(passwordEncoder.encode("Nova@123")).thenReturn("hashNovo");

        useCase.executar(new Comando("12345678901", null, "Nova@123"));

        ArgumentCaptor<CpcUsuario> captor = ArgumentCaptor.forClass(CpcUsuario.class);
        verify(usuarioRepository).atualizar(captor.capture());
        assertThat(captor.getValue().getSenhaHash()).isEqualTo("hashNovo");
        verify(passwordEncoder, never()).matches(anyString(), anyString());
    }

    @Test
    void deveTrocarSenhaQuandoSenhaAtualEnviadaECorreta() {
        when(usuarioRepository.buscarPorCpf("12345678901")).thenReturn(Optional.of(usuario));
        when(passwordEncoder.matches("senhaAtualCerta", "hashAtual")).thenReturn(true);
        when(passwordEncoder.encode("Nova@123")).thenReturn("hashNovo");

        useCase.executar(new Comando("12345678901", "senhaAtualCerta", "Nova@123"));

        verify(usuarioRepository).atualizar(any(CpcUsuario.class));
    }

    @Test
    void deveRecusarSenhaAtualIncorretaQuandoEnviada() {
        when(usuarioRepository.buscarPorCpf("12345678901")).thenReturn(Optional.of(usuario));
        when(passwordEncoder.matches("senhaErrada", "hashAtual")).thenReturn(false);

        assertThatThrownBy(() -> useCase.executar(new Comando("12345678901", "senhaErrada", "Nova@123")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Senha atual incorreta.");

        verify(usuarioRepository, never()).atualizar(any());
    }

    @Test
    void deveLancarQuandoUsuarioNaoEncontrado() {
        when(usuarioRepository.buscarPorCpf("12345678901")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.executar(new Comando("12345678901", null, "Nova@123")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Usuário não encontrado");
    }

    @Test
    void deveAplicarPasswordPolicyNaNovaSenha() {
        when(usuarioRepository.buscarPorCpf("12345678901")).thenReturn(Optional.of(usuario));

        assertThatThrownBy(() -> useCase.executar(new Comando("12345678901", null, "12345")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("A senha deve ter no mínimo 6 caracteres.");

        verify(usuarioRepository, never()).atualizar(any());
        verify(passwordEncoder, never()).encode(anyString());
    }
}
