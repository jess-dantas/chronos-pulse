package br.com.jess.chronos.pulse.modules.licitacoes.web.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.UUID;

public record LicitacaoPropostaDTO(
        @NotNull(message = "Material é obrigatório")
        UUID materialId,

        @NotNull(message = "Valor unitário é obrigatório")
        @DecimalMin(value = "0.0001", message = "Valor unitário deve ser maior que zero")
        BigDecimal valorUnitario,

        String observacao
) {}