package br.com.jess.chronos.pulse.modules.admin.infrastructure.adapters.input.rest.dto;

import java.time.Instant;

/// Vínculo de dispositivo confiável do admin: o valor cru só aparece aqui,
/// uma única vez; depois disso só existe o hash no banco.
public record AdminDeviceTokenResponseDTO(String deviceToken, Instant expiraEm) {}
