package br.com.jess.chronos.pulse.modules.colaborador.application.usecases;

import br.com.jess.chronos.pulse.modules.auth.domain.ports.output.CpcUsuarioRepositoryPort;
import br.com.jess.chronos.pulse.modules.colaborador.domain.ports.input.AtualizarColaboradorUseCase.Comando;
import br.com.jess.chronos.pulse.modules.colaborador.domain.ports.output.ColaboradorRepositoryPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AtualizarColaboradorUseCaseImplTest {

    @Mock
    private ColaboradorRepositoryPort colaboradorRepository;

    @Mock
    private CpcUsuarioRepositoryPort usuarioRepository;

    private AtualizarColaboradorUseCaseImpl useCase;

    @BeforeEach
    void setUp() {
        useCase = new AtualizarColaboradorUseCaseImpl(colaboradorRepository, usuarioRepository);
    }

    @Test
    void deveBloquearAtualizacaoSemTenant() {
        Comando comando = comandoBase(UUID.randomUUID(), null);

        assertThatThrownBy(() -> useCase.executar(comando))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Tenant ID obrigatório");

        verify(colaboradorRepository, never()).atualizar(any());
        verify(usuarioRepository, never()).atualizar(any());
    }

    @Test
    void deveBloquearAtualizacaoQuandoColaboradorNaoPertenceAoTenant() {
        UUID tenantLogado = UUID.randomUUID();
        UUID tenantOutro = UUID.randomUUID();
        UUID colaboradorId = UUID.randomUUID();

        Comando comando = comandoBase(colaboradorId, tenantLogado);

        when(colaboradorRepository.buscarPorIdETenant(colaboradorId, tenantLogado)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.executar(comando))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("não encontrado no seu tenant");

        verify(colaboradorRepository, never()).atualizar(any());
        verify(usuarioRepository, never()).atualizar(any());
    }

    private Comando comandoBase(UUID colaboradorId, UUID tenantId) {
        return new Comando(
                colaboradorId,
                tenantId,
                "Fulano",
                "fulano@empresa.com",
                "MAT001",
                "Cargo",
                "Departamento",
                LocalDate.of(1990, 1, 1),
                LocalDate.of(2020, 1, 1),
                null,
                true, true, true, true
        );
    }
}