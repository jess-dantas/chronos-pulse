package br.com.jess.chronos.pulse.modules.licitacoes.web.dto;

import br.com.jess.chronos.pulse.modules.licitacoes.domain.entity.LicitacaoLance;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record LanceResponseDTO(
        UUID id,
        UUID licitacaoItemId,
        String itemDescricao,
        UUID fornecedorId,
        String fornecedorNome,
        BigDecimal valorUnitario,
        BigDecimal valorEstimadoUnitario,
        BigDecimal economia,
        String observacao,
        Instant criadoEm,
        Instant atualizadoEm
) {
    public static LanceResponseDTO from(LicitacaoLance lance,
                                        String itemDescricao,
                                        String fornecedorNome,
                                        BigDecimal valorEstimadoUnitario) {
        BigDecimal economia = null;
        if (valorEstimadoUnitario != null && lance.getValorUnitario() != null) {
            economia = valorEstimadoUnitario.subtract(lance.getValorUnitario());
        }
        return new LanceResponseDTO(
                lance.getId(), lance.getLicitacaoItemId(), itemDescricao,
                lance.getFornecedorId(), fornecedorNome,
                lance.getValorUnitario(), valorEstimadoUnitario, economia,
                lance.getObservacao(), lance.getCriadoEm(), lance.getAtualizadoEm());
    }
}
