package br.com.jess.chronos.pulse.modules.compras.web.dto;

import br.com.jess.chronos.pulse.modules.compras.domain.entity.CotacaoProposta;

import java.math.BigDecimal;
import java.util.UUID;

public record PropostaResponseDTO(
        UUID id,
        UUID fornecedorId,
        String fornecedorNome,
        UUID materialId,
        String materialDescricao,
        BigDecimal valorUnitario,
        Boolean vencedor
) {
    public static PropostaResponseDTO from(CotacaoProposta proposta, String fornecedorNome,
                                           String materialDescricao) {
        return new PropostaResponseDTO(
                proposta.getId(), proposta.getFornecedorId(), fornecedorNome,
                proposta.getMaterialId(), materialDescricao, proposta.getValorUnitario(),
                proposta.getVencedor());
    }
}