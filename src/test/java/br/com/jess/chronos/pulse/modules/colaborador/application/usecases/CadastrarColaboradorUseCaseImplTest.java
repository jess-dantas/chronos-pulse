package br.com.jess.chronos.pulse.modules.colaborador.application.usecases;

import br.com.jess.chronos.pulse.modules.auth.domain.model.CpcUsuario;
import br.com.jess.chronos.pulse.modules.auth.domain.ports.output.CpcUsuarioRepositoryPort;
import br.com.jess.chronos.pulse.modules.colaborador.domain.model.Colaborador;
import br.com.jess.chronos.pulse.modules.colaborador.domain.ports.input.CadastrarColaboradorUseCase.Comando;
import br.com.jess.chronos.pulse.modules.colaborador.domain.ports.output.ColaboradorRepositoryPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CadastrarColaboradorUseCaseImplTest {

    @Mock
    private ColaboradorRepositoryPort colaboradorRepository;

    @Mock
    private CpcUsuarioRepositoryPort usuarioRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    private CadastrarColaboradorUseCaseImpl useCase;

    @BeforeEach
    void setUp() {
        useCase = new CadastrarColaboradorUseCaseImpl(colaboradorRepository, usuarioRepository, passwordEncoder);
    }

    @Test
    void deveCadastrarColaboradorComSucesso() {
        UUID tenantId = UUID.randomUUID();
        UUID jornadaId = UUID.randomUUID();
        UUID usuarioId = UUID.randomUUID();

        Comando comando = new Comando(
                "12345678901", "Fulano de Tal", "fulano@empresa.com", "senhaForte",
                "MAT001", "Desenvolvedor", "TI", LocalDate.of(1990, 1, 1),
                LocalDate.now(), tenantId, jornadaId
        );

        when(usuarioRepository.existePorCpf("12345678901")).thenReturn(false);
        when(passwordEncoder.encode("senhaForte")).thenReturn("hashSenha");

        CpcUsuario usuarioSalvo = mock(CpcUsuario.class);
        when(usuarioSalvo.getId()).thenReturn(usuarioId);
        when(usuarioRepository.salvar(any(CpcUsuario.class))).thenReturn(usuarioSalvo);

        Colaborador colaboradorSalvo = new Colaborador(
                UUID.randomUUID(), usuarioId, tenantId, "MAT001", "Desenvolvedor", "TI",
                LocalDate.of(1990, 1, 1), LocalDate.now(), jornadaId
        );
        when(colaboradorRepository.salvar(any(Colaborador.class))).thenReturn(colaboradorSalvo);

        Colaborador resultado = useCase.executar(comando);

        assertThat(resultado).isNotNull();
        assertThat(resultado.getCpcUsuarioId()).isEqualTo(usuarioId);
        assertThat(resultado.getMatricula()).isEqualTo("MAT001");
        verify(usuarioRepository).salvar(any(CpcUsuario.class));
        verify(colaboradorRepository).salvar(any(Colaborador.class));
    }

    @Test
    void deveLancarExcecaoQuandoCpfJaExiste() {
        Comando comando = new Comando(
                "12345678901", "Fulano", "fulano@empresa.com", "senha",
                "MAT001", "Cargo", "Depto", LocalDate.now(), LocalDate.now(), UUID.randomUUID(), null
        );

        when(usuarioRepository.existePorCpf("12345678901")).thenReturn(true);

        assertThatThrownBy(() -> useCase.executar(comando))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("CPF já cadastrado");

        verify(usuarioRepository, never()).salvar(any());
        verify(colaboradorRepository, never()).salvar(any());
    }
}
