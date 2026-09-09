package br.com.jess.chronos.pulse.modules.telemetria.service;

import br.com.jess.chronos.pulse.modules.telemetria.domain.entity.TelemetriaEvento;
import br.com.jess.chronos.pulse.modules.telemetria.domain.entity.TipoEventoTelemetria;
import br.com.jess.chronos.pulse.modules.telemetria.infrastructure.config.TelemetriaProperties;
import br.com.jess.chronos.pulse.modules.telemetria.repository.TelemetriaEventoRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

/**
 * Persistência de eventos de observabilidade de forma best-effort:
 * qualquer falha de gravação é apenas registrada em log e nunca
 * deve interromper a operação de negócio que originou o evento.
 */
@Service
public class TelemetriaService {

    private static final Logger log = LoggerFactory.getLogger(TelemetriaService.class);

    private final TelemetriaEventoRepository repository;
    private final TelemetriaProperties properties;

    public TelemetriaService(TelemetriaEventoRepository repository,
                             TelemetriaProperties properties) {
        this.repository = repository;
        this.properties = properties;
    }

    @Transactional
    public void registrar(TipoEventoTelemetria tipo, String modulo, UUID tenantId, UUID usuarioId,
                          String endpoint, Integer statusHttp, Long latencyMs,
                          String mensagem, String detalhe) {
        if (!properties.isEnabled()) {
            return;
        }
        try {
            TelemetriaEvento evento = TelemetriaEvento.builder()
                    .tenantId(tenantId)
                    .usuarioId(usuarioId)
                    .modulo(normalizarModulo(modulo))
                    .tipo(tipo)
                    .endpoint(truncar(endpoint, 255))
                    .statusHttp(statusHttp)
                    .latencyMs(latencyMs)
                    .mensagem(truncar(mensagem, 500))
                    .detalhe(detalhe)
                    .traceId(org.slf4j.MDC.get("traceId"))
                    .appVersao(properties.getAppVersao())
                    .criadoEm(Instant.now())
                    .build();
            repository.save(evento);
        } catch (Exception ex) {
            log.warn("Falha ao registrar evento de telemetria (tipo={}, modulo={}): {}", tipo, modulo, ex.getMessage());
        }
    }

    public Page<TelemetriaEvento> consultar(TipoEventoTelemetria tipo, String modulo, UUID tenantId,
                                            Instant inicio, Instant fim, Pageable pageable) {
        Specification<TelemetriaEvento> spec = (root, query, cb) -> cb.conjunction();
        if (tipo != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("tipo"), tipo));
        }
        if (modulo != null && !modulo.isBlank()) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("modulo"), modulo.toUpperCase()));
        }
        if (tenantId != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("tenantId"), tenantId));
        }
        if (inicio != null) {
            spec = spec.and((root, query, cb) -> cb.greaterThanOrEqualTo(root.get("criadoEm"), inicio));
        }
        if (fim != null) {
            spec = spec.and((root, query, cb) -> cb.lessThanOrEqualTo(root.get("criadoEm"), fim));
        }
        return repository.findAll(spec, pageable);
    }

    /**
     * Retenção: remove eventos mais antigos que o limite configurado (padrão 30 dias).
     */
    @Scheduled(initialDelay = 60_000, fixedDelay = 3_600_000)
    @Transactional
    public void limparEventosAntigos() {
        if (!properties.isEnabled()) {
            return;
        }
        try {
            Instant corte = Instant.now().minus(Duration.ofDays(properties.getRetentionDias()));
            int removidos = repository.excluirAnterioresA(corte);
            if (removidos > 0) {
                log.info("Telemetria: {} evento(s) anteriores ao corte de retenção removidos.", removidos);
            }
        } catch (Exception ex) {
            log.warn("Falha na limpeza de eventos de telemetria: {}", ex.getMessage());
        }
    }

    private static String normalizarModulo(String modulo) {
        if (modulo == null || modulo.isBlank()) {
            return "GERAL";
        }
        String normalizado = modulo.trim().toUpperCase().replaceAll("[^A-Z0-9_]", "_");
        return truncar(normalizado, 30);
    }

    private static String truncar(String valor, int max) {
        if (valor == null) {
            return null;
        }
        return valor.length() <= max ? valor : valor.substring(0, max);
    }
}