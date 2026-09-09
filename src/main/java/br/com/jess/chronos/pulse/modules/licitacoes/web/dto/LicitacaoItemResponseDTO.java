package br.com.jess.chronos.pulse.modules.licitacoes.web.dto;

import br.com.jess.chronos.pulse.modules.licitacoes.domain.entity.LicitacaoItem;

import java.math.BigDecimal;
import java.util.UUID;

public record LicitacaoItemResponseDTO(
        UUID id,
        UUID materialId,
        String descricao,
        BigDecimal quantidade,
        BigDecimal valorEstimadoUnitario,
        BigDecimal valorEstimadoTotal,
        String unidadeMedida
) {
    public static LicitacaoItemResponseDTO from(LicitacaoItem item, String unidadeMedida) {
        return new LicitacaoItemResponseDTO(
                item.getId(), item.getMaterialId(), item.getDescricao(), item.getQuantidade(),
                item.getValorEstimadoUnitario(), item.getValorEstimadoTotal(), unidadeMedida);
    }
}