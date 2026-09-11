package br.com.jess.chronos.pulse.modules.licitacoes.web.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.util.UUID;

public record RegistrarLanceDTO(
        @NotNull(message = "Item da licitação é obrigatório")
        UUID licitacaoItemId,

        @NotNull(message = "Fornecedor é obrigatório")
        UUID fornecedorId,

        @NotNull(message = "Valor do lance é obrigatório")
        @Positive(message = "Valor do lance deve ser maior que zero")
        BigDecimal valorUnitario,

        String observacao
) {}
