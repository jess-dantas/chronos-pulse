package br.com.jess.chronos.pulse.modules.colaborador.application.usecases;

import br.com.jess.chronos.pulse.modules.auth.domain.ports.output.CpcUsuarioRepositoryPort;
import br.com.jess.chronos.pulse.modules.colaborador.domain.model.Colaborador;
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
class ExcluirColaboradorUseCaseImplTest {

    @Mock
    private ColaboradorRepositoryPort colaboradorRepository;

    @Mock
    private CpcUsuarioRepositoryPort usuarioRepository;

    private ExcluirColaboradorUseCaseImpl useCase;

    @BeforeEach
    void setUp() {
        useCase = new ExcluirColaboradorUseCaseImpl(colaboradorRepository, usuarioRepository);
    }

    @Test
    void deveBloquearExclusaoSemTenant() {
        assertThatThrownBy(() -> useCase.executar(UUID.randomUUID(), null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Tenant ID obrigatório");

        verify(colaboradorRepository, never()).desativarPorId(any());
        verify(usuarioRepository, never()).desativarPorId(any());
    }

    @Test
    void deveBloquearExclusaoQuandoColaboradorNaoPertenceAoTenant() {
        UUID tenantLogado = UUID.randomUUID();
        UUID colaboradorId = UUID.randomUUID();

        when(colaboradorRepository.buscarPorIdETenant(colaboradorId, tenantLogado)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.executar(colaboradorId, tenantLogado))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("não encontrado no seu tenant");

        verify(colaboradorRepository, never()).desativarPorId(any());
        verify(usuarioRepository, never()).desativarPorId(any());
    }

    @Test
    void deveExcluirColaboradorPropioTenant() {
        UUID tenantLogado = UUID.randomUUID();
        UUID colaboradorId = UUID.randomUUID();
        UUID cpcUsuarioId = UUID.randomUUID();

        Colaborador colaborador = new Colaborador(
                colaboradorId, cpcUsuarioId, tenantLogado, "MAT001", "Cargo", "Depto",
                LocalDate.of(1990, 1, 1), LocalDate.of(2020, 1, 1), null);

        when(colaboradorRepository.buscarPorIdETenant(colaboradorId, tenantLogado)).thenReturn(Optional.of(colaborador));

        useCase.executar(colaboradorId, tenantLogado);

        verify(colaboradorRepository).desativarPorId(colaboradorId);
        verify(usuarioRepository).desativarPorId(cpcUsuarioId);
    }
}