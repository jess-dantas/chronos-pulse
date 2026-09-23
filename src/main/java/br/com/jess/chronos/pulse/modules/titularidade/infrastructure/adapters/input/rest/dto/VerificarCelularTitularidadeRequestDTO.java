package br.com.jess.chronos.pulse.modules.titularidade.infrastructure.adapters.input.rest.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record VerificarCelularTitularidadeRequestDTO(
        @NotBlank(message = "Código é obrigatório")
        String codigo,
        @NotNull(message = "Confirmação do celular do novo titular é obrigatória")
        Boolean celularConfirmado
) {}
