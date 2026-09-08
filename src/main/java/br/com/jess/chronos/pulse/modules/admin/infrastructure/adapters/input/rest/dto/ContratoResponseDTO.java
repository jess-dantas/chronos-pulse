package br.com.jess.chronos.pulse.modules.admin.infrastructure.adapters.input.rest.dto;

import br.com.jess.chronos.pulse.modules.admin.domain.model.Contrato;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record ContratoResponseDTO(
        UUID id,
        UUID tenantId,
        String numero,
        String objeto,
        LocalDate dataInicio,
        LocalDate dataFim,
        BigDecimal valorMensal,
        BigDecimal valorTotal,
        BigDecimal valorEmpenhado,
        BigDecimal valorLiquidado,
        BigDecimal saldo,
        String empenhoNumero,
        Integer vencimentoAvisoDias,
        Long diasParaVencimento,
        String statusVigencia,
        String status,
        String observacoes,
        Instant criadoEm,
        Instant atualizadoEm
) {
    public static ContratoResponseDTO fromDomain(Contrato c) {
        return new ContratoResponseDTO(
                c.getId(), c.getTenantId(), c.getNumero(), c.getObjeto(),
                c.getDataInicio(), c.getDataFim(), c.getValorMensal(), c.getValorTotal(),
                c.getValorEmpenhado(), c.getValorLiquidado(), c.getSaldo(),
                c.getEmpenhoNumero(), c.getVencimentoAvisoDias(), c.getDiasParaVencimento(),
                c.getStatusVigencia(),
                c.getStatus(), c.getObservacoes(), c.getCriadoEm(), c.getAtualizadoEm()
        );
    }
}
