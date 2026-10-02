package br.com.jess.chronos.pulse.modules.ponto.application.usecases;

import br.com.jess.chronos.pulse.modules.ponto.domain.model.RegistroPonto;
import br.com.jess.chronos.pulse.modules.ponto.domain.model.TipoRegistro;
import br.com.jess.chronos.pulse.modules.ponto.domain.ports.output.RegistroPontoRepositoryPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
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
        return novoRegistroComData(Instant.now());
    }

    private RegistroPonto novoRegistroComData(Instant dataHora) {
        return new RegistroPonto(UUID.randomUUID(), colaboradorId, tenantId, dataHora,
                null, null, new BigDecimal("-23.5505"), new BigDecimal("-46.6333"),
                new BigDecimal("5.0"), null, false, null);
    }

    @Test
    void deveAtribuirEntradaQuandoNaoHouverBatidaAnteriorEPersistirRegistro() {
        RegistroPonto registro = novoRegistro();
        when(repositoryPort.buscarUltimoTipoPorColaborador(eq(colaboradorId), eq(tenantId), any(Instant.class), any(Instant.class))).thenReturn(Optional.empty());
        when(repositoryPort.obterProximoNsrLogico(colaboradorId, tenantId)).thenReturn(1L);
        when(repositoryPort.obterProximoNsr()).thenReturn(10L);
        when(repositoryPort.salvar(any())).thenReturn(registro);

        RegistroPonto resultado = useCase.executar(registro, "12345678901", tenantId);

        assertThat(registro.getTipoRegistro()).isEqualTo(TipoRegistro.ENTRADA);
        assertThat(registro.getNsrLogico()).isEqualTo(1L);
        assertThat(registro.getNsr()).isEqualTo(10L);
        assertThat(registro.getHashIntegridade()).isNotNull().hasSize(64);
        assertThat(resultado).isNotNull();
        verify(repositoryPort).buscarUltimoTipoPorColaborador(eq(colaboradorId), eq(tenantId), any(Instant.class), any(Instant.class));
        verify(repositoryPort).obterProximoNsrLogico(colaboradorId, tenantId);
        verify(repositoryPort).obterProximoNsr();
        verify(repositoryPort).salvar(registro);
    }

    @Test
    void deveAvancarSequenciaDeBatidasCorretamente() {
        RegistroPonto registro = novoRegistro();
        when(repositoryPort.buscarUltimoTipoPorColaborador(eq(colaboradorId), eq(tenantId), any(Instant.class), any(Instant.class))).thenReturn(Optional.of(TipoRegistro.ENTRADA));
        when(repositoryPort.obterProximoNsrLogico(colaboradorId, tenantId)).thenReturn(2L);
        when(repositoryPort.obterProximoNsr()).thenReturn(11L);
        when(repositoryPort.salvar(any())).thenReturn(registro);

        useCase.executar(registro, "12345678901", tenantId);

        assertThat(registro.getTipoRegistro()).isEqualTo(TipoRegistro.INTERVALO);
        assertThat(registro.getNsrLogico()).isEqualTo(2L);
        assertThat(registro.getNsr()).isEqualTo(11L);
    }

    @Test
    void deveReiniciarCicloParaEntradaAposSaida() {
        RegistroPonto registro = novoRegistro();
        when(repositoryPort.buscarUltimoTipoPorColaborador(eq(colaboradorId), eq(tenantId), any(Instant.class), any(Instant.class))).thenReturn(Optional.of(TipoRegistro.SAIDA));
        when(repositoryPort.obterProximoNsrLogico(colaboradorId, tenantId)).thenReturn(3L);
        when(repositoryPort.obterProximoNsr()).thenReturn(12L);
        when(repositoryPort.salvar(any())).thenReturn(registro);

        useCase.executar(registro, "12345678901", tenantId);

        assertThat(registro.getTipoRegistro()).isEqualTo(TipoRegistro.ENTRADA);
        assertThat(registro.getNsrLogico()).isEqualTo(3L);
        assertThat(registro.getNsr()).isEqualTo(12L);
    }

    @Test
    void devePropagarExcecaoQuandoRepositorioFalha() {
        RegistroPonto registro = novoRegistro();
        when(repositoryPort.buscarUltimoTipoPorColaborador(eq(colaboradorId), eq(tenantId), any(Instant.class), any(Instant.class))).thenReturn(Optional.empty());
        when(repositoryPort.obterProximoNsrLogico(colaboradorId, tenantId)).thenReturn(1L);
        when(repositoryPort.obterProximoNsr()).thenReturn(10L);
        when(repositoryPort.salvar(any())).thenThrow(new RuntimeException("DB error"));

        assertThatThrownBy(() -> useCase.executar(registro, "12345678901", tenantId))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("DB error");
    }

    @Test
    void deveAtribuirNsrAntesDePersistirParaNaoViolarNotNullDoBanco() {
        RegistroPonto registro = novoRegistro();
        when(repositoryPort.buscarUltimoTipoPorColaborador(eq(colaboradorId), eq(tenantId), any(Instant.class), any(Instant.class))).thenReturn(Optional.empty());
        when(repositoryPort.obterProximoNsrLogico(colaboradorId, tenantId)).thenReturn(1L);
        when(repositoryPort.obterProximoNsr()).thenReturn(77L);
        when(repositoryPort.salvar(any())).thenAnswer(inv -> {
            RegistroPonto salvo = inv.getArgument(0);
            assertThat(salvo.getNsr()).as("nsr deve estar atribuído antes do save").isNotNull();
            return salvo;
        });

        useCase.executar(registro, "12345678901", tenantId);

        assertThat(registro.getNsr()).isEqualTo(77L);
    }

    @Test
    void deveLimitarBuscaDoUltimoTipoAoDiaDeSaoPauloDaBatida() {
        Instant dataHora = Instant.parse("2026-10-02T12:00:00Z");
        RegistroPonto registro = new RegistroPonto(UUID.randomUUID(), colaboradorId, tenantId, dataHora,
                null, null, new BigDecimal("-23.5505"), new BigDecimal("-46.6333"),
                new BigDecimal("5.0"), null, false, null);
        when(repositoryPort.buscarUltimoTipoPorColaborador(eq(colaboradorId), eq(tenantId), any(Instant.class), any(Instant.class)))
                .thenReturn(Optional.empty());
        when(repositoryPort.obterProximoNsrLogico(colaboradorId, tenantId)).thenReturn(1L);
        when(repositoryPort.obterProximoNsr()).thenReturn(10L);
        when(repositoryPort.salvar(any())).thenReturn(registro);

        useCase.executar(registro, "12345678901", tenantId);

        ArgumentCaptor<Instant> inicio = ArgumentCaptor.forClass(Instant.class);
        ArgumentCaptor<Instant> fim = ArgumentCaptor.forClass(Instant.class);
        verify(repositoryPort).buscarUltimoTipoPorColaborador(
                eq(colaboradorId), eq(tenantId), inicio.capture(), fim.capture());
        assertThat(inicio.getValue()).isEqualTo(Instant.parse("2026-10-02T03:00:00Z"));
        assertThat(fim.getValue()).isEqualTo(Instant.parse("2026-10-03T03:00:00Z"));
    }

    @Test
    void deveRetornarRegistroJaPersistidoSemReprocessarQuandoORetryChegarDeNovo() {
        RegistroPonto registro = novoRegistro();
        RegistroPonto persistido = novoRegistroComData(Instant.parse("2026-10-02T12:00:00Z"));
        persistido.atribuirTipo(TipoRegistro.INTERVALO);
        persistido.atribuirNsrLogico(5L);
        persistido.atribuirNsr(42L);
        when(repositoryPort.buscarPorId(registro.getId())).thenReturn(Optional.of(persistido));

        RegistroPonto resultado = useCase.executar(registro, "12345678901", tenantId);

        assertThat(resultado).isSameAs(persistido);
        assertThat(resultado.getTipoRegistro()).isEqualTo(TipoRegistro.INTERVALO);
        assertThat(resultado.getNsrLogico()).isEqualTo(5L);
        assertThat(resultado.getNsr()).isEqualTo(42L);
        verify(repositoryPort, never()).buscarUltimoTipoPorColaborador(any(), any(), any(Instant.class), any(Instant.class));
        verify(repositoryPort, never()).obterProximoNsrLogico(any(), any());
        verify(repositoryPort, never()).obterProximoNsr();
        verify(repositoryPort, never()).salvar(any());
    }

    @Test
    void deveSerializarDerivacaoDeTipoParaOMesmoColaboradorEDia() throws Exception {
        Instant fixo = Instant.parse("2026-10-02T12:00:00Z");
        AtomicInteger ativos = new AtomicInteger();
        AtomicInteger maximoConcorrente = new AtomicInteger();

        when(repositoryPort.buscarPorId(any())).thenReturn(Optional.empty());
        when(repositoryPort.buscarUltimoTipoPorColaborador(any(), any(), any(Instant.class), any(Instant.class)))
                .thenAnswer(inv -> {
                    int atual = ativos.incrementAndGet();
                    maximoConcorrente.accumulateAndGet(atual, Math::max);
                    Thread.sleep(50);
                    ativos.decrementAndGet();
                    return Optional.empty();
                });
        when(repositoryPort.obterProximoNsrLogico(colaboradorId, tenantId)).thenReturn(1L);
        when(repositoryPort.obterProximoNsr()).thenReturn(10L);
        when(repositoryPort.salvar(any())).thenAnswer(inv -> inv.getArgument(0));

        ExecutorService pool = Executors.newFixedThreadPool(2);
        try {
            Future<RegistroPonto> f1 = pool.submit(() -> useCase.executar(novoRegistroComData(fixo), "12345678901", tenantId));
            Future<RegistroPonto> f2 = pool.submit(() -> useCase.executar(novoRegistroComData(fixo), "12345678901", tenantId));
            f1.get(5, TimeUnit.SECONDS);
            f2.get(5, TimeUnit.SECONDS);
        } finally {
            pool.shutdownNow();
        }

        assertThat(maximoConcorrente.get())
                .as("derivacao de tipo nunca deve rodar em paralelo para o mesmo colaborador/dia")
                .isEqualTo(1);
    }
}
