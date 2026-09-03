package br.com.jess.chronos.pulse.modules.estoque.web.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.UUID;

public record ItemRequisicaoDTO(
        @NotNull(message = "Material é obrigatório")
        UUID materialId,

        @NotNull(message = "Quantidade solicitada é obrigatória")
        @DecimalMin(value = "0.001", message = "Quantidade deve ser maior que zero")
        BigDecimal quantidadeSolicitada
) {}
