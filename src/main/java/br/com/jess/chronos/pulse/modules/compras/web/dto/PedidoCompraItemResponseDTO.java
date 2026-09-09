package br.com.jess.chronos.pulse.modules.compras.web.dto;

import br.com.jess.chronos.pulse.modules.compras.domain.entity.PedidoCompraItem;

import java.math.BigDecimal;
import java.util.UUID;

public record PedidoCompraItemResponseDTO(
        UUID id,
        UUID materialId,
        String materialDescricao,
        String materialUnidadeMedida,
        BigDecimal quantidade,
        BigDecimal valorUnitario,
        BigDecimal valorTotalItem,
        BigDecimal quantidadeRecebida
) {
    public static PedidoCompraItemResponseDTO from(PedidoCompraItem item, String descricao, String unidadeMedida) {
        return new PedidoCompraItemResponseDTO(
                item.getId(), item.getMaterialId(), descricao, unidadeMedida,
                item.getQuantidade(), item.getValorUnitario(), item.getValorTotalItem(),
                item.getQuantidadeRecebida());
    }
}