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
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.YearMonth;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ConsultarEspelhoPontoUseCaseImplTest {

    @Mock
    private RegistroPontoRepositoryPort repositoryPort;

    private ConsultarEspelhoPontoUseCaseImpl useCase;
    private UUID colaboradorId;
    private UUID tenantId;

    @BeforeEach
    void setUp() {
        useCase = new ConsultarEspelhoPontoUseCaseImpl(repositoryPort);
        colaboradorId = UUID.randomUUID();
        tenantId = UUID.randomUUID();
    }

    @Test
    void deveConsultarPorPeriodoQuandoMesEAnoForemInformados() {
        RegistroPonto r = new RegistroPonto(UUID.randomUUID(), colaboradorId, tenantId, Instant.now(),
                Instant.now(), TipoRegistro.ENTRADA, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO,
                null, false, 1L);

        when(repositoryPort.listarPorColaboradorEPeriodo(eq(colaboradorId), eq(tenantId), any(), any()))
                .thenReturn(List.of(r));

        var resultado = useCase.consultar(colaboradorId, tenantId, 9, 2026);

        assertThat(resultado).hasSize(1);
        verify(repositoryPort).listarPorColaboradorEPeriodo(eq(colaboradorId), eq(tenantId), any(), any());
    }

    @Test
    void deveConsultarTodosQuandoMesEAnoForemNulos() {
        RegistroPonto r = new RegistroPonto(UUID.randomUUID(), colaboradorId, tenantId, Instant.now(),
                Instant.now(), TipoRegistro.ENTRADA, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO,
                null, false, 1L);

        when(repositoryPort.listarPorColaborador(colaboradorId, tenantId)).thenReturn(List.of(r));

        var resultado = useCase.consultar(colaboradorId, tenantId, null, null);

        assertThat(resultado).hasSize(1);
        verify(repositoryPort).listarPorColaborador(colaboradorId, tenantId);
    }

    @Test
    void deveCalcularJanelaDoMesNoFusoBrasileiro() {
        ArgumentCaptor<Instant> inicio = ArgumentCaptor.forClass(Instant.class);
        ArgumentCaptor<Instant> fim = ArgumentCaptor.forClass(Instant.class);
        when(repositoryPort.listarPorColaboradorEPeriodo(eq(colaboradorId), eq(tenantId),
                inicio.capture(), fim.capture())).thenReturn(List.of());

        useCase.consultar(colaboradorId, tenantId, 9, 2026);

        ZoneId brasil = ZoneId.of("America/Sao_Paulo");
        YearMonth setembro = YearMonth.of(2026, 9);
        assertThat(inicio.getValue())
                .isEqualTo(setembro.atDay(1).atStartOfDay(brasil).toInstant());
        assertThat(fim.getValue())
                .isEqualTo(LocalDate.of(2026, 9, 30).atTime(LocalTime.MAX).atZone(brasil).toInstant());
        // Regressão: em UTC as marcações das 21h–23h59 do dia 30 sairiam do mês
        assertThat(inicio.getValue())
                .isNotEqualTo(setembro.atDay(1).atStartOfDay().toInstant(ZoneOffset.UTC));
        assertThat(fim.getValue())
                .isNotEqualTo(LocalDate.of(2026, 9, 30).atTime(LocalTime.MAX).toInstant(ZoneOffset.UTC));
    }
}
