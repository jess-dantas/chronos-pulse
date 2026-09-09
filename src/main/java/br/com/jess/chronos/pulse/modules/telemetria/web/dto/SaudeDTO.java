package br.com.jess.chronos.pulse.modules.telemetria.web.dto;

import java.time.OffsetDateTime;

public record SaudeDTO(
        String status,
        String versaoPostgres,
        int poolAtivo,
        int poolIdle,
        int poolPendentes,
        int poolMax,
        OffsetDateTime timestamp
) {
}