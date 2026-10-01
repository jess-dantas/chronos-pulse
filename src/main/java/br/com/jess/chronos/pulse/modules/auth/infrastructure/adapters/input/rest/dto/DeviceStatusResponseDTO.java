package br.com.jess.chronos.pulse.modules.auth.infrastructure.adapters.input.rest.dto;

import java.time.Instant;
import java.util.UUID;

public record DeviceStatusResponseDTO(
        UUID cpcId,
        String nome,
        boolean twoFactorEnabled,
        Instant vinculoExpiraEm
) {}
