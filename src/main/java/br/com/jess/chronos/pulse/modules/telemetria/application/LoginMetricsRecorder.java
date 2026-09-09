package br.com.jess.chronos.pulse.modules.telemetria.application;

import br.com.jess.chronos.pulse.modules.auditoria.service.AuditoriaService;
import br.com.jess.chronos.pulse.modules.telemetria.domain.entity.TipoEventoTelemetria;
import br.com.jess.chronos.pulse.modules.telemetria.service.TelemetriaService;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Contadores de autenticação (R27): login_sucesso_total e login_falha_total
 * por razão, além do registro de eventos de telemetria (sem dados pessoais).
 */
@Component
public class LoginMetricsRecorder {

    private static final Logger log = LoggerFactory.getLogger(LoginMetricsRecorder.class);

    private final MeterRegistry meterRegistry;
    private final TelemetriaService telemetriaService;
    private final AuditoriaService auditoriaService;

    public LoginMetricsRecorder(MeterRegistry meterRegistry,
                                TelemetriaService telemetriaService,
                                AuditoriaService auditoriaService) {
        this.meterRegistry = meterRegistry;
        this.telemetriaService = telemetriaService;
        this.auditoriaService = auditoriaService;
    }

    public void registrarSucesso(UUID tenantId, UUID usuarioId, String papel) {
        counter("login_sucesso_total", null).increment();
        telemetriaService.registrar(TipoEventoTelemetria.LOGIN_SUCESSO, "AUTH", tenantId, usuarioId,
                "/api/v1/auth/login", 200, null,
                "Login realizado. Papel=" + (papel != null ? papel : "?"), null);
    }

    public void registrarFalha(String cpf, String razao, boolean usuarioExistia,
                               UUID tenantId, UUID usuarioId) {
        counter("login_falha_total", razao).increment();
        telemetriaService.registrar(TipoEventoTelemetria.LOGIN_FALHA, "AUTH", tenantId, usuarioId,
                "/api/v1/auth/login", 401, null, "Falha de login: " + razao, null);

        if (usuarioExistia) {
            // Auditoria em cadeia (hash) preserva a tentativa mesmo sem sucesso.
            auditoriaService.registrar("LOGIN_FALHOU", "USUARIO", usuarioId, "Tentativa de login com falha",
                    tenantId, usuarioId, cpf, "DESCONHECIDO", null, null, null);
        }
        log.warn("Falha de login detectada: {} (cpf com cadastro: {})", razao, usuarioExistia);
    }

    private Counter counter(String nome, String razao) {
        if (razao == null) {
            return Counter.builder(nome)
                    .description("Contador de autenticação")
                    .register(meterRegistry);
        }
        return Counter.builder(nome)
                .tag("razao", razao)
                .register(meterRegistry);
    }
}