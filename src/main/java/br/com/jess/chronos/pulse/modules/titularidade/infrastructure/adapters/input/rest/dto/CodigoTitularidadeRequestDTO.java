package br.com.jess.chronos.pulse.modules.titularidade.infrastructure.adapters.input.rest.dto;

import jakarta.validation.constraints.NotBlank;

public record CodigoTitularidadeRequestDTO(
        @NotBlank(message = "Código é obrigatório")
        String codigo
) {}
