package br.com.jess.chronos.pulse.modules.titularidade.infrastructure.adapters.input.rest.dto;

import jakarta.validation.constraints.NotNull;

public record BiometriaTitularidadeRequestDTO(
        @NotNull(message = "Confirmação de biometria é obrigatória")
        Boolean confirmado
) {}
