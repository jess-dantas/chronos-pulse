package br.com.jess.chronos.pulse.modules.licitacoes.web.dto;

import br.com.jess.chronos.pulse.modules.licitacoes.domain.entity.ContratoSancao;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record ContratoSancaoResponseDTO(
        UUID id,
        String tipo,
        String baseLegal,
        String descricao,
        BigDecimal percentualMulta,
        BigDecimal valorMulta,
        LocalDate aplicadaEm,
        Instant criadoEm
) {
    public static ContratoSancaoResponseDTO from(ContratoSancao sancao) {
        return new ContratoSancaoResponseDTO(
                sancao.getId(), sancao.getTipo(), sancao.getBaseLegal(),
                sancao.getDescricao(), sancao.getPercentualMulta(),
                sancao.getValorMulta(), sancao.getAplicadaEm(), sancao.getCriadoEm());
    }
}