package br.com.jess.chronos.pulse.modules.compras.web.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.UUID;

public record PedidoCompraItemDTO(
        @NotNull(message = "Material é obrigatório")
        UUID materialId,

        @NotNull(message = "Quantidade é obrigatória")
        @DecimalMin(value = "0.001", message = "Quantidade deve ser maior que zero")
        BigDecimal quantidade,

        @NotNull(message = "Valor unitário é obrigatório")
        @DecimalMin(value = "0.0001", message = "Valor unitário deve ser maior que zero")
        BigDecimal valorUnitario
) {}