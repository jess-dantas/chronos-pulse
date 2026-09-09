package br.com.jess.chronos.pulse.modules.telemetria.application;

import br.com.jess.chronos.pulse.modules.auditoria.service.AuditoriaService;
import br.com.jess.chronos.pulse.modules.telemetria.domain.entity.TipoEventoTelemetria;
import br.com.jess.chronos.pulse.modules.telemetria.service.TelemetriaService;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class LoginMetricsRecorderTest {

    @Mock
    private TelemetriaService telemetriaService;

    @Mock
    private AuditoriaService auditoriaService;

    private final MeterRegistry meterRegistry = new SimpleMeterRegistry();

    private LoginMetricsRecorder recorder;

    @BeforeEach
    void setUp() {
        recorder = new LoginMetricsRecorder(meterRegistry, telemetriaService, auditoriaService);
    }

    @Test
    void deveIncrementarContadorDeSucessoEPersistirEvento() {
        UUID tenantId = UUID.randomUUID();
        UUID cpcId = UUID.randomUUID();

        recorder.registrarSucesso(tenantId, cpcId, "ADMIN_EMPRESA");

        assertThat(meterRegistry.get("login_sucesso_total").counter().count()).isEqualTo(1);
        verify(telemetriaService).registrar(TipoEventoTelemetria.LOGIN_SUCESSO, "AUTH", tenantId, cpcId,
                "/api/v1/auth/login", 200, null,
                "Login realizado. Papel=ADMIN_EMPRESA", null);
    }

    @Test
    void deveIncrementarContadorDeFalhaPorRazaoEEvitarAuditoriaParaUsuarioDesconhecido() {
        recorder.registrarFalha("12345678901", "USUARIO_NAO_ENCONTRADO", false, null, null);

        assertThat(meterRegistry.get("login_falha_total").tags("razao", "USUARIO_NAO_ENCONTRADO")
                .counter().count()).isEqualTo(1);
        verify(telemetriaService).registrar(TipoEventoTelemetria.LOGIN_FALHA, "AUTH", null, null,
                "/api/v1/auth/login", 401, null,
                "Falha de login: USUARIO_NAO_ENCONTRADO", null);
        verify(auditoriaService, never()).registrar(org.mockito.ArgumentMatchers.anyString(),
                org.mockito.ArgumentMatchers.anyString(),
                org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.anyString(),
                org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any(),
                org.mockito.ArgumentMatchers.anyString(), org.mockito.ArgumentMatchers.anyString(),
                org.mockito.ArgumentMatchers.anyString(), org.mockito.ArgumentMatchers.anyString(),
                org.mockito.ArgumentMatchers.anyString());
    }

    @Test
    void deveAuditarFalhaQuandoUsuarioExiste() {
        UUID tenantId = UUID.randomUUID();
        UUID cpcId = UUID.randomUUID();

        recorder.registrarFalha("12345678901", "SENHA_INVALIDA", true, tenantId, cpcId);

        assertThat(meterRegistry.get("login_falha_total").tags("razao", "SENHA_INVALIDA")
                .counter().count()).isEqualTo(1);
        verify(auditoriaService).registrar("LOGIN_FALHOU", "USUARIO", cpcId,
                "Tentativa de login com falha", tenantId, cpcId, "12345678901", "DESCONHECIDO",
                null, null, null);
    }
}