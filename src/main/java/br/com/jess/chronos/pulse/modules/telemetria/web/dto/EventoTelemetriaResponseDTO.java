package br.com.jess.chronos.pulse.modules.telemetria.web.dto;

import br.com.jess.chronos.pulse.modules.telemetria.domain.entity.TipoEventoTelemetria;

import java.time.Instant;
import java.util.UUID;

public record EventoTelemetriaResponseDTO(
        UUID id,
        UUID tenantId,
        UUID usuarioId,
        String modulo,
        TipoEventoTelemetria tipo,
        String endpoint,
        Integer statusHttp,
        Long latencyMs,
        String mensagem,
        String detalhe,
        String traceId,
        String appVersao,
        String plataforma,
        Instant criadoEm
) {
}