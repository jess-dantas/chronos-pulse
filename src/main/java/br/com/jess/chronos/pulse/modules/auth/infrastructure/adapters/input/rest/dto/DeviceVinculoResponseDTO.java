package br.com.jess.chronos.pulse.modules.auth.infrastructure.adapters.input.rest.dto;

import java.time.Instant;

/// Resposta do vínculo de dispositivo: o valor cru do token é devolvido uma
/// única vez — depois disso só existe o hash SHA-256 no servidor.
public record DeviceVinculoResponseDTO(
        String deviceToken,
        Instant expiraEm
) {}
