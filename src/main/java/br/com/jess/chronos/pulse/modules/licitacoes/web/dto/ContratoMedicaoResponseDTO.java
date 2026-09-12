package br.com.jess.chronos.pulse.modules.licitacoes.web.dto;

import br.com.jess.chronos.pulse.modules.licitacoes.domain.entity.ContratoMedicao;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record ContratoMedicaoResponseDTO(
        UUID id,
        String periodo,
        BigDecimal valorMedido,
        BigDecimal valorPago,
        LocalDate pagoEm,
        String observacao,
        Instant criadoEm
) {
    public static ContratoMedicaoResponseDTO from(ContratoMedicao medicao) {
        return new ContratoMedicaoResponseDTO(
                medicao.getId(), medicao.getPeriodo(), medicao.getValorMedido(),
                medicao.getValorPago(), medicao.getPagoEm(), medicao.getObservacao(),
                medicao.getCriadoEm());
    }
}