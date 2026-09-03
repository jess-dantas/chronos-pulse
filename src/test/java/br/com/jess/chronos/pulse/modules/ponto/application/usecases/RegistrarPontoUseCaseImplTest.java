package br.com.jess.chronos.pulse.modules.ponto.application.usecases;

import br.com.jess.chronos.pulse.modules.ponto.domain.model.RegistroPonto;
import br.com.jess.chronos.pulse.modules.ponto.domain.model.TipoRegistro;
import br.com.jess.chronos.pulse.modules.ponto.domain.ports.output.RegistroPontoRepositoryPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RegistrarPontoUseCaseImplTest {

    @Mock
    private RegistroPontoRepositoryPort repositoryPort;

    private RegistrarPontoUseCaseImpl useCase;
    private UUID colaboradorId;
    private UUID tenantId;

    @BeforeEach
    void setUp() {
        useCase = new RegistrarPontoUseCaseImpl(repositoryPort);
        colaboradorId = UUID.randomUUID();
        tenantId = UUID.randomUUID();
    }

    private RegistroPonto novoRegistro() {
        return new RegistroPonto(UUID.randomUUID(), colaboradorId, tenantId, Instant.now(),
                null, null, new BigDecimal("-23.5505"), new BigDecimal("-46.6333"),
                new BigDecimal("5.0"), null, false, null);
    }

    @Test
    void deveAtribuirEntradaQuandoNaoHouverBatidaAnteriorEPersistirRegistro() {
        RegistroPonto registro = novoRegistro();
        when(repositoryPort.buscarUltimoTipoPorColaborador(colaboradorId, tenantId)).thenReturn(Optional.empty());
        when(repositoryPort.obterProximoNsr()).thenReturn(1L);
        when(repositoryPort.salvar(any())).thenReturn(registro);

        RegistroPonto resultado = useCase.executar(registro, "12345678901", tenantId);

        assertThat(registro.getTipoRegistro()).isEqualTo(TipoRegistro.ENTRADA);
        assertThat(registro.getNsr()).isEqualTo(1L);
        assertThat(registro.getHashIntegridade()).isNotNull().hasSize(64);
        assertThat(resultado).isNotNull();
        verify(repositoryPort).buscarUltimoTipoPorColaborador(colaboradorId, tenantId);
        verify(repositoryPort).obterProximoNsr();
        verify(repositoryPort).salvar(registro);
    }

    @Test
    void deveAvancarSequenciaDeBatidasCorretamente() {
        RegistroPonto registro = novoRegistro();
        when(repositoryPort.buscarUltimoTipoPorColaborador(colaboradorId, tenantId)).thenReturn(Optional.of(TipoRegistro.ENTRADA));
        when(repositoryPort.obterProximoNsr()).thenReturn(2L);
        when(repositoryPort.salvar(any())).thenReturn(registro);

        useCase.executar(registro, "12345678901", tenantId);

        assertThat(registro.getTipoRegistro()).isEqualTo(TipoRegistro.INTERVALO);
        assertThat(registro.getNsr()).isEqualTo(2L);
    }

    @Test
    void deveReiniciarCicloParaEntradaAposSaida() {
        RegistroPonto registro = novoRegistro();
        when(repositoryPort.buscarUltimoTipoPorColaborador(colaboradorId, tenantId)).thenReturn(Optional.of(TipoRegistro.SAIDA));
        when(repositoryPort.obterProximoNsr()).thenReturn(3L);
        when(repositoryPort.salvar(any())).thenReturn(registro);

        useCase.executar(registro, "12345678901", tenantId);

        assertThat(registro.getTipoRegistro()).isEqualTo(TipoRegistro.ENTRADA);
        assertThat(registro.getNsr()).isEqualTo(3L);
    }

    @Test
    void devePropagaExcecaoQuandoRepositorioFalha() {
        RegistroPonto registro = novoRegistro();
        when(repositoryPort.buscarUltimoTipoPorColaborador(colaboradorId, tenantId)).thenReturn(Optional.empty());
        when(repositoryPort.obterProximoNsr()).thenReturn(1L);
        when(repositoryPort.salvar(any())).thenThrow(new RuntimeException("DB error"));

        assertThatThrownBy(() -> useCase.executar(registro, "12345678901", tenantId))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("DB error");
    }
}
