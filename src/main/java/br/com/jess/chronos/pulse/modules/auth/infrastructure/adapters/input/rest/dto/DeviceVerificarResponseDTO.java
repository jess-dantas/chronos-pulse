package br.com.jess.chronos.pulse.modules.auth.infrastructure.adapters.input.rest.dto;

import java.time.Instant;

public record DeviceVerificarResponseDTO(
        boolean verificado,
        boolean enviado,
        Instant expiraEm
) {}
