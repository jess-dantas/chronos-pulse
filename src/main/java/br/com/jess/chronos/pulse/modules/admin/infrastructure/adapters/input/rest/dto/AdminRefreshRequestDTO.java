package br.com.jess.chronos.pulse.modules.admin.infrastructure.adapters.input.rest.dto;

import jakarta.validation.constraints.NotBlank;

public record AdminRefreshRequestDTO(
        @NotBlank(message = "Refresh token é obrigatório") String refreshToken
) {}
