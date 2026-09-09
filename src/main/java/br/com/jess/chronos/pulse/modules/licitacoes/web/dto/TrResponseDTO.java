package br.com.jess.chronos.pulse.modules.licitacoes.web.dto;

import br.com.jess.chronos.pulse.modules.licitacoes.domain.entity.LicitacaoTr;

import java.time.Instant;
import java.util.UUID;

public record TrResponseDTO(
        UUID id,
        String especificacoes,
        String condicoesFornecimento,
        String obrigacoes,
        String criteriosAceitacao,
        String prazosEntrega,
        String garantia,
        String formaPagamento,
        String responsavel,
        String status,
        Instant dataAprovacao
) {

    public static TrResponseDTO from(LicitacaoTr tr) {
        if (tr == null) {
            return null;
        }
        return new TrResponseDTO(
                tr.getId(),
                tr.getEspecificacoes(),
                tr.getCondicoesFornecimento(),
                tr.getObrigacoes(),
                tr.getCriteriosAceitacao(),
                tr.getPrazosEntrega(),
                tr.getGarantia(),
                tr.getFormaPagamento(),
                tr.getResponsavel(),
                tr.getStatus().name(),
                tr.getDataAprovacao());
    }
}