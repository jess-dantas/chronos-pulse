package br.com.jess.chronos.pulse.modules.licitacoes.web.dto;

import br.com.jess.chronos.pulse.modules.licitacoes.domain.entity.ContratoRescisao;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record ContratoRescisaoResponseDTO(
        UUID id,
        String tipo,
        String motivo,
        LocalDate dataRescisao,
        Instant criadoEm
) {
    public static ContratoRescisaoResponseDTO from(ContratoRescisao rescisao) {
        return new ContratoRescisaoResponseDTO(
                rescisao.getId(), rescisao.getTipo(), rescisao.getMotivo(),
                rescisao.getDataRescisao(), rescisao.getCriadoEm());
    }
}