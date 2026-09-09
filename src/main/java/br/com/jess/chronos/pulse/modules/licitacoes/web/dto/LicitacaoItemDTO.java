package br.com.jess.chronos.pulse.modules.licitacoes.web.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.UUID;

public record LicitacaoItemDTO(
        @NotNull(message = "Material é obrigatório")
        UUID materialId,

        @NotNull(message = "Quantidade é obrigatória")
        @DecimalMin(value = "0.001", message = "Quantidade deve ser maior que zero")
        BigDecimal quantidade,

        @DecimalMin(value = "0.0001", message = "Valor estimado deve ser maior que zero")
        BigDecimal valorEstimadoUnitario
) {}