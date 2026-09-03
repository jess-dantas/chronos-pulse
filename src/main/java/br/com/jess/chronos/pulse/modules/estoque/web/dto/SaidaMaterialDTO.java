package br.com.jess.chronos.pulse.modules.estoque.web.dto;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.util.UUID;

public record SaidaMaterialDTO(
        @NotNull(message = "Almoxarifado é obrigatório")
        UUID almoxarifadoId,

        @NotNull(message = "Material é obrigatório")
        UUID materialId,

        @NotNull(message = "Quantidade é obrigatória")
        @DecimalMin(value = "0.001", message = "Quantidade deve ser maior que zero")
        BigDecimal quantidade,

        String lote,

        @NotBlank(message = "Documento de referência ou número da requisição é obrigatório")
        String documentoReferencia,

        String observacao
) {}