package br.com.jess.chronos.pulse.modules.auth.infrastructure.adapters.input.rest.dto;

public record LoginResponseDTO(
        String accessToken,
        String refreshToken,
        String role,
        String cpcId
) {}
