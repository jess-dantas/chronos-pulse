package br.com.jess.chronos.pulse.modules.compras.web.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.UUID;

public record PropostaItemDTO(
        @NotNull(message = "Material é obrigatório")
        UUID materialId,

        @NotNull(message = "Valor unitário é obrigatório")
        @DecimalMin(value = "0.0", message = "Valor unitário não pode ser negativo")
        BigDecimal valorUnitario
) {}