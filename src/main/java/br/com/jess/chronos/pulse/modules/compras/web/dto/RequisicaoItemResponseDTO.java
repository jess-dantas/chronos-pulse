package br.com.jess.chronos.pulse.modules.compras.web.dto;

import br.com.jess.chronos.pulse.modules.compras.domain.entity.RequisicaoCompraItem;

import java.math.BigDecimal;
import java.util.UUID;

public record RequisicaoItemResponseDTO(
        UUID id,
        UUID materialId,
        String materialDescricao,
        String materialUnidadeMedida,
        BigDecimal quantidade,
        String observacao
) {
    public static RequisicaoItemResponseDTO from(RequisicaoCompraItem item, String materialDescricao,
                                                 String materialUnidadeMedida) {
        return new RequisicaoItemResponseDTO(
                item.getId(), item.getMaterialId(), materialDescricao, materialUnidadeMedida,
                item.getQuantidade(), item.getObservacao());
    }
}