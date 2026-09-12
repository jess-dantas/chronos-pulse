package br.com.jess.chronos.pulse.modules.licitacoes.web.dto;

import br.com.jess.chronos.pulse.modules.licitacoes.domain.entity.ContratoAditivo;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record ContratoAditivoResponseDTO(
        UUID id,
        String tipo,
        String descricao,
        String justificativa,
        Integer prazoAdicionadoDias,
        BigDecimal novoValorTotal,
        Boolean aprovado,
        Instant criadoEm
) {
    public static ContratoAditivoResponseDTO from(ContratoAditivo aditivo) {
        return new ContratoAditivoResponseDTO(
                aditivo.getId(), aditivo.getTipo(), aditivo.getDescricao(),
                aditivo.getJustificativa(), aditivo.getPrazoAdicionadoDias(),
                aditivo.getNovoValorTotal(), aditivo.getAprovado(), aditivo.getCriadoEm());
    }
}