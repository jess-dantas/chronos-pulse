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
        String status,
        String observacoes,
        Instant criadoEm,
        Instant atualizadoEm
) {
    public static ContratoResponseDTO fromDomain(Contrato c) {
        return new ContratoResponseDTO(
                c.getId(), c.getTenantId(), c.getNumero(), c.getObjeto(),
                c.getDataInicio(), c.getDataFim(), c.getValorMensal(), c.getValorTotal(),
                c.getStatus(), c.getObservacoes(), c.getCriadoEm(), c.getAtualizadoEm()
        );
    }
}
