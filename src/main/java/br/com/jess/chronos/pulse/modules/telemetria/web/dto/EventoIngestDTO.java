package br.com.jess.chronos.pulse.modules.telemetria.web.dto;

import br.com.jess.chronos.pulse.modules.telemetria.domain.entity.TipoEventoTelemetria;

public record EventoIngestDTO(
        String modulo,
        TipoEventoTelemetria tipo,
        String endpoint,
        Integer statusHttp,
        Long latencyMs,
        String mensagem,
        String detalhe,
        String plataforma,
        String traceId
) {
}