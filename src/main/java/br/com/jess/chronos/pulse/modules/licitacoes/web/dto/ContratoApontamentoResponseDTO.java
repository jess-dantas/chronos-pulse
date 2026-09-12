package br.com.jess.chronos.pulse.modules.licitacoes.web.dto;

import br.com.jess.chronos.pulse.modules.licitacoes.domain.entity.ContratoApontamento;

import java.time.Instant;
import java.util.UUID;

public record ContratoApontamentoResponseDTO(
        UUID id,
        String fiscal,
        String descricao,
        String gravidade,
        Boolean resolvido,
        Instant resolvidoEm,
        Instant criadoEm
) {
    public static ContratoApontamentoResponseDTO from(ContratoApontamento apontamento) {
        return new ContratoApontamentoResponseDTO(
                apontamento.getId(), apontamento.getFiscal(), apontamento.getDescricao(),
                apontamento.getGravidade(), apontamento.getResolvido(),
                apontamento.getResolvidoEm(), apontamento.getCriadoEm());
    }
}