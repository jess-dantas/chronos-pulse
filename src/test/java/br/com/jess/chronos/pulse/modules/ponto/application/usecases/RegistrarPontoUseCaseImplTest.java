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
import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
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

    /** Batidas anteriores em ordem cronológica (o tipo delas não importa: a regra é posicional). */
    private List<RegistroPonto> cadeia(Instant... instantes) {
        return Arrays.stream(instantes)
                .sorted(Instant::compareTo)
                .map(this::novoRegistroComData)
                .toList();
    }

    private void stubCadeia(List<RegistroPonto> batidas) {
        when(repositoryPort.listarPorColaboradorEPeriodo(
                eq(colaboradorId), eq(tenantId), any(Instant.class), any(Instant.class)))
                .thenReturn(batidas);
    }

    private void stubPersistencia(long nsrLogico, long nsr) {
        when(repositoryPort.obterProximoNsrLogico(colaboradorId, tenantId)).thenReturn(nsrLogico);
        when(repositoryPort.obterProximoNsr()).thenReturn(nsr);
        when(repositoryPort.salvar(any())).thenAnswer(inv -> inv.getArgument(0));
    }

    @Test
    void deveAtribuirEntradaQuandoNaoHouverBatidaAnteriorEPersistirRegistro() {
        RegistroPonto registro = novoRegistro();
        stubCadeia(List.of());
        stubPersistencia(1L, 10L);

        RegistroPonto resultado = useCase.executar(registro, "12345678901", tenantId);

        assertThat(registro.getTipoRegistro()).isEqualTo(TipoRegistro.ENTRADA);
        assertThat(registro.getNsrLogico()).isEqualTo(1L);
        assertThat(registro.getNsr()).isEqualTo(10L);
        assertThat(registro.getHashIntegridade()).isNotNull().hasSize(64);
        assertThat(resultado).isNotNull();
        verify(repositoryPort).listarPorColaboradorEPeriodo(
                eq(colaboradorId), eq(tenantId), any(Instant.class), any(Instant.class));
        verify(repositoryPort).obterProximoNsrLogico(colaboradorId, tenantId);
        verify(repositoryPort).obterProximoNsr();
        verify(repositoryPort).salvar(registro);
    }

    @Test
    void deveAvancarSequenciaDeBatidasCorretamente() {
        Instant agora = Instant.parse("2026-10-06T11:00:00Z"); // 08:00 SP
        RegistroPonto registro = novoRegistroComData(agora);
        stubCadeia(cadeia(agora.minus(Duration.ofHours(1)))); // uma batida anterior
        stubPersistencia(2L, 11L);

        useCase.executar(registro, "12345678901", tenantId);

        assertThat(registro.getTipoRegistro()).isEqualTo(TipoRegistro.INTERVALO);
        assertThat(registro.getNsrLogico()).isEqualTo(2L);
        assertThat(registro.getNsr()).isEqualTo(11L);
    }

    // TC001 — jornada de 8h com almoço: 08h(E), 12h(I), 13h(R) → 17h = SAIDA.
    @Test
    void tc001JornadaDeOitoHorasFechaComSaida() {
        Instant inicio = Instant.parse("2026-10-06T11:00:00Z"); // 08:00 SP
        Instant agora = Instant.parse("2026-10-06T20:00:00Z"); // 17:00 SP
        RegistroPonto registro = novoRegistroComData(agora);
        stubCadeia(cadeia(inicio,
                inicio.plus(Duration.ofHours(4)), // 12:00 SP
                inicio.plus(Duration.ofHours(5)))); // 13:00 SP
        stubPersistencia(4L, 14L);

        useCase.executar(registro, "12345678901", tenantId);

        assertThat(registro.getTipoRegistro()).isEqualTo(TipoRegistro.SAIDA);
    }

    // TC002 — a 6ª batida é SAIDA (HE) e a 7ª reinicia a jornada em ENTRADA.
    @Test
    void tc002SextaBatidaESaidaDeHoraExtraESetimaReiniciaEntrada() {
        Instant agora = Instant.parse("2026-10-06T23:00:00Z"); // 20:00 SP
        Instant primeira = agora.minus(Duration.ofHours(12)); // 08:00 SP
        Instant[] anteriores = new Instant[5];
        for (int i = 0; i < 5; i++) {
            anteriores[i] = primeira.plus(Duration.ofHours(2L * i));
        }

        RegistroPonto sexta = novoRegistroComData(agora);
        stubCadeia(cadeia(anteriores));
        stubPersistencia(6L, 16L);
        useCase.executar(sexta, "12345678901", tenantId);
        assertThat(sexta.getTipoRegistro())
                .as("a 6ª batida da jornada é a saída de hora extra")
                .isEqualTo(TipoRegistro.SAIDA);

        Instant seguinte = agora.plus(Duration.ofHours(1)); // 21:00 SP, gap 1h
        RegistroPonto setima = novoRegistroComData(seguinte);
        when(repositoryPort.listarPorColaboradorEPeriodo(
                eq(colaboradorId), eq(tenantId), any(Instant.class), any(Instant.class)))
                .thenReturn(cadeia(concat(anteriores, agora)));
        stubPersistencia(7L, 17L);
        useCase.executar(setima, "12345678901", tenantId);

        assertThat(setima.getTipoRegistro())
                .as("a jornada fecha nas 6 batidas; a 7ª começa jornada nova")
                .isEqualTo(TipoRegistro.ENTRADA);
    }

    // TC003 — turno noturno: 22h(E) → 02h → 04h → 06h = 4ª batida = SAIDA
    // (o intervalo ≤10h mantém a virada na mesma jornada).
    @Test
    void tc003TurnoNoturnoQueAtravessaAViradaContinuaASequencia() {
        Instant entrada = Instant.parse("2026-10-06T01:00:00Z"); // 22:00 SP (dia 05)
        Instant agora = Instant.parse("2026-10-06T09:00:00Z"); // 06:00 SP (dia 06)
        RegistroPonto registro = novoRegistroComData(agora);
        stubCadeia(cadeia(entrada,
                entrada.plus(Duration.ofHours(4)), // 02:00 SP
                entrada.plus(Duration.ofHours(6)))); // 04:00 SP
        stubPersistencia(4L, 14L);

        useCase.executar(registro, "12345678901", tenantId);

        assertThat(registro.getTipoRegistro()).isEqualTo(TipoRegistro.SAIDA);
    }

    // TC004 — intervalo >10h entre batidas abre jornada nova: ontem 17h →
    // hoje 08h = ENTRADA, mesmo que a última batida de ontem tenha sido SAIDA.
    @Test
    void tc004IntervaloMaiorQueDezHorasAbreNovaJornadaComEntrada() {
        Instant ontem = Instant.parse("2026-10-05T20:00:00Z"); // 17:00 SP
        Instant agora = Instant.parse("2026-10-06T11:00:00Z"); // 08:00 SP (15h depois)
        RegistroPonto registro = novoRegistroComData(agora);
        stubCadeia(cadeia(ontem));
        stubPersistencia(1L, 10L);

        useCase.executar(registro, "12345678901", tenantId);

        assertThat(registro.getTipoRegistro()).isEqualTo(TipoRegistro.ENTRADA);
    }

    @Test
    void devePropagarExcecaoQuandoRepositorioFalha() {
        RegistroPonto registro = novoRegistro();
        stubCadeia(List.of());
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
        stubCadeia(List.of());
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
    void deveBuscarACadeiaDaJornadaNosUltimos72Horas() {
        Instant agora = Instant.parse("2026-10-06T12:00:00Z");
        RegistroPonto registro = novoRegistroComData(agora);
        stubCadeia(List.of());
        stubPersistencia(1L, 10L);

        useCase.executar(registro, "12345678901", tenantId);

        ArgumentCaptor<Instant> inicio = ArgumentCaptor.forClass(Instant.class);
        ArgumentCaptor<Instant> fim = ArgumentCaptor.forClass(Instant.class);
        verify(repositoryPort).listarPorColaboradorEPeriodo(
                eq(colaboradorId), eq(tenantId), inicio.capture(), fim.capture());
        assertThat(inicio.getValue()).isEqualTo(agora.minus(Duration.ofHours(72)));
        assertThat(fim.getValue()).isEqualTo(agora);
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
        verify(repositoryPort, never()).listarPorColaboradorEPeriodo(any(), any(), any(Instant.class), any(Instant.class));
        verify(repositoryPort, never()).obterProximoNsrLogico(any(), any());
        verify(repositoryPort, never()).obterProximoNsr();
        verify(repositoryPort, never()).salvar(any());
    }

    @Test
    void deveSerializarDerivacaoDeTipoParaOMesmoColaborador() throws Exception {
        Instant fixo = Instant.parse("2026-10-02T12:00:00Z");
        AtomicInteger ativos = new AtomicInteger();
        AtomicInteger maximoConcorrente = new AtomicInteger();

        when(repositoryPort.buscarPorId(any())).thenReturn(Optional.empty());
        when(repositoryPort.listarPorColaboradorEPeriodo(any(), any(), any(Instant.class), any(Instant.class)))
                .thenAnswer(inv -> {
                    int atual = ativos.incrementAndGet();
                    maximoConcorrente.accumulateAndGet(atual, Math::max);
                    Thread.sleep(50);
                    ativos.decrementAndGet();
                    return List.of();
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
                .as("derivacao de tipo nunca deve rodar em paralelo para o mesmo colaborador")
                .isEqualTo(1);
    }

    private static Instant[] concat(Instant[] base, Instant extra) {
        Instant[] resultado = Arrays.copyOf(base, base.length + 1);
        resultado[base.length] = extra;
        return resultado;
    }
}
