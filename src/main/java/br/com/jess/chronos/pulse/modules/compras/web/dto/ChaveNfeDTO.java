package br.com.jess.chronos.pulse.modules.compras.web.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ChaveNfeDTO(
        @NotNull(message = "Chave NFe é obrigatória")
        @Size(min = 44, max = 44, message = "A chave NFe deve conter exatamente 44 dígitos")
        String chaveNfe
) {}