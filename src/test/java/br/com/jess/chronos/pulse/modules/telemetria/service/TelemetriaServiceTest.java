package br.com.jess.chronos.pulse.modules.telemetria.service;

import br.com.jess.chronos.pulse.modules.telemetria.domain.entity.TelemetriaEvento;
import br.com.jess.chronos.pulse.modules.telemetria.domain.entity.TipoEventoTelemetria;
import br.com.jess.chronos.pulse.modules.telemetria.infrastructure.config.TelemetriaProperties;
import br.com.jess.chronos.pulse.modules.telemetria.repository.TelemetriaEventoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.data.domain.Page.empty;

@ExtendWith(MockitoExtension.class)
class TelemetriaServiceTest {

    @Mock
    private TelemetriaEventoRepository repository;

    @Captor
    private ArgumentCaptor<TelemetriaEvento> captor;

    private final TelemetriaProperties properties = new TelemetriaProperties();

    private TelemetriaService service;

    @BeforeEach
    void setUp() {
        service = new TelemetriaService(repository, properties);
    }

    @Test
    void devePersistirEventoEmModoBestEffort() {
        service.registrar(TipoEventoTelemetria.API_REQUEST, "compras", UUID.randomUUID(),
                UUID.randomUUID(), "/api/v1/compras/pedidos", 200, 12L, "OK", null);

        verify(repository).save(captor.capture());
        TelemetriaEvento evento = captor.getValue();

        assertThat(evento.getTipo()).isEqualTo(TipoEventoTelemetria.API_REQUEST);
        assertThat(evento.getModulo()).isEqualTo("COMPRAS");
        assertThat(evento.getEndpoint()).isEqualTo("/api/v1/compras/pedidos");
        assertThat(evento.getStatusHttp()).isEqualTo(200);
        assertThat(evento.getLatencyMs()).isEqualTo(12L);
        assertThat(evento.getAppVersao()).isEqualTo("1.0.0");
        assertThat(evento.getCriadoEm()).isNotNull();
    }

    @Test
    void deveNormalizarModuloVazioParaGeral() {
        service.registrar(TipoEventoTelemetria.UI_ERRO, "", null, null, null, null, null, "boom", null);

        verify(repository).save(captor.capture());
        assertThat(captor.getValue().getModulo()).isEqualTo("GERAL");
    }

    @Test
    void naoDevePersistirQuandoDesabilitado() {
        properties.setEnabled(false);

        service.registrar(TipoEventoTelemetria.API_ERRO, "AUTH", null, null, null, 500, null, "erro", null);

        verify(repository, never()).save(any(TelemetriaEvento.class));
    }

    @Test
    void naoDevePropagarFalhaDePersistencia() {
        when(repository.save(any(TelemetriaEvento.class))).thenThrow(new RuntimeException("db ruim"));

        org.assertj.core.api.Assertions.assertThatCode(() ->
                service.registrar(TipoEventoTelemetria.CONEXAO_BD, "PLATAFORMA", null, null,
                        "/api/v1/telemetria/saude", 200, 1L, "ok", null))
                .doesNotThrowAnyException();
    }

    @Test
    void deveConsultarComSpecification() {
        when(repository.findAll(any(Specification.class), any(PageRequest.class))).thenReturn(empty());

        var pagina = service.consultar(TipoEventoTelemetria.LOGIN_SUCESSO, "AUTH", UUID.randomUUID(),
                Instant.now().minusSeconds(60), Instant.now(), PageRequest.of(0, 20));

        assertThat(pagina.getContent()).isEmpty();
        verify(repository).findAll(any(Specification.class), any(PageRequest.class));
    }

    @Test
    void deveRemoverEventosAntigosNaRetencao() {
        when(repository.excluirAnterioresA(any(Instant.class))).thenReturn(7);

        org.assertj.core.api.Assertions.assertThatCode(service::limparEventosAntigos)
                .doesNotThrowAnyException();

        verify(repository).excluirAnterioresA(any(Instant.class));
    }

    @Test
    void naoDeveLimparQuandoDesabilitado() {
        properties.setEnabled(false);

        service.limparEventosAntigos();

        verify(repository, never()).excluirAnterioresA(any(Instant.class));
    }
}