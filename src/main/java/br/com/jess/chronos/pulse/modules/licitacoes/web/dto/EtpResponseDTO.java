package br.com.jess.chronos.pulse.modules.licitacoes.web.dto;

import br.com.jess.chronos.pulse.modules.licitacoes.domain.entity.LicitacaoEtp;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record EtpResponseDTO(
        UUID id,
        String objeto,
        String justificativa,
        String requisitos,
        String alternativas,
        BigDecimal valorEstimado,
        String riscos,
        String conclusao,
        String responsavel,
        String status,
        Instant dataAprovacao
) {

    public static EtpResponseDTO from(LicitacaoEtp etp) {
        if (etp == null) {
            return null;
        }
        return new EtpResponseDTO(
                etp.getId(),
                etp.getObjeto(),
                etp.getJustificativa(),
                etp.getRequisitos(),
                etp.getAlternativas(),
                etp.getValorEstimado(),
                etp.getRiscos(),
                etp.getConclusao(),
                etp.getResponsavel(),
                etp.getStatus().name(),
                etp.getDataAprovacao());
    }
}