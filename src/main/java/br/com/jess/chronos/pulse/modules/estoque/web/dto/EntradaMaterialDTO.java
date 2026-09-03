package br.com.jess.chronos.pulse.modules.estoque.web.dto;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record EntradaMaterialDTO(
        @NotNull(message = "Almoxarifado é obrigatório")
        UUID almoxarifadoId,

        @NotNull(message = "Material é obrigatório")
        UUID materialId,

        @NotNull(message = "Quantidade é obrigatória")
        @DecimalMin(value = "0.001", message = "Quantidade deve ser maior que zero")
        BigDecimal quantidade,

        @NotNull(message = "Valor unitário é obrigatório")
        @DecimalMin(value = "0.0001", message = "Valor unitário deve ser maior que zero")
        BigDecimal valorUnitario,

        String lote,
        LocalDate dataValidade,

        @NotBlank(message = "Documento de referência é obrigatório")
        String documentoReferencia
) {}