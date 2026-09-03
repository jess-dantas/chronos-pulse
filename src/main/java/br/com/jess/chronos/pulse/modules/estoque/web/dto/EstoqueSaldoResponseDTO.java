package br.com.jess.chronos.pulse.modules.estoque.web.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record EstoqueSaldoResponseDTO(
        UUID id,
        UUID almoxarifadoId,
        String nomeAlmoxarifado,
        UUID materialId,
        String descricaoMaterial,
        String unidadeMedida,
        String lote,
        LocalDate dataValidade,
        BigDecimal quantidadeAtual,
        BigDecimal estoqueMinimo,
        BigDecimal custoMedioUnitario,
        BigDecimal valorTotalEstoque,
        boolean abaixoEstoqueMinimo
) {}