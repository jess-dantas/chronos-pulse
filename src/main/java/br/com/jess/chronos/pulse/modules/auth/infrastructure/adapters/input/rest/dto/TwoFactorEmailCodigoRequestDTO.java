package br.com.jess.chronos.pulse.modules.auth.infrastructure.adapters.input.rest.dto;

import jakarta.validation.constraints.NotBlank;

public record TwoFactorEmailCodigoRequestDTO(
        @NotBlank String tempToken
) {}
