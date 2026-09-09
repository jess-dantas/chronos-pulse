package br.com.jess.chronos.pulse.modules.licitacoes.web.dto;

import br.com.jess.chronos.pulse.modules.licitacoes.domain.entity.LicitacaoProposta;

import java.math.BigDecimal;
import java.util.UUID;

public record LicitacaoPropostaResponseDTO(
        UUID id,
        UUID fornecedorId,
        String fornecedorNome,
        UUID materialId,
        String materialDescricao,
        BigDecimal valorUnitario,
        Boolean vencedor
) {
    public static LicitacaoPropostaResponseDTO from(LicitacaoProposta proposta, String fornecedorNome,
                                                    String materialDescricao) {
        return new LicitacaoPropostaResponseDTO(
                proposta.getId(), proposta.getFornecedorId(), fornecedorNome,
                proposta.getMaterialId(), materialDescricao, proposta.getValorUnitario(),
                proposta.getVencedor());
    }
}