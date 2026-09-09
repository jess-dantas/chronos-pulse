package br.com.jess.chronos.pulse.modules.licitacoes.web.dto;

import br.com.jess.chronos.pulse.modules.licitacoes.domain.entity.LicitacaoEdital;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

public record EditalResponseDTO(
        UUID id,
        String numeroProcesso,
        String numeroEdital,
        String localSessao,
        LocalDate dataAberturaSessao,
        LocalTime horarioAbertura,
        String formaEntregaPropostas,
        String anexos,
        String observacoes,
        String status,
        Instant dataPublicacao
) {

    public static EditalResponseDTO from(LicitacaoEdital edital) {
        if (edital == null) {
            return null;
        }
        return new EditalResponseDTO(
                edital.getId(),
                edital.getNumeroProcesso(),
                edital.getNumeroEdital(),
                edital.getLocalSessao(),
                edital.getDataAberturaSessao(),
                edital.getHorarioAbertura(),
                edital.getFormaEntregaPropostas(),
                edital.getAnexos(),
                edital.getObservacoes(),
                edital.getStatus().name(),
                edital.getDataPublicacao());
    }
}