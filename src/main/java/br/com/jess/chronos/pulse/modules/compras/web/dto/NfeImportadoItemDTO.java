package br.com.jess.chronos.pulse.modules.compras.web.dto;

import java.math.BigDecimal;

public record NfeImportadoItemDTO(
        Integer numeroItem,
        String codigoProduto,
        String descricao,
        String ncm,
        String cfop,
        String unidadeComercial,
        BigDecimal quantidadeComercial,
        BigDecimal valorUnitarioComercial,
        BigDecimal valorTotalProduto
) {}