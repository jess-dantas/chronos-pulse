package br.com.jess.chronos.pulse.modules.transparencia.web.dto;

import br.com.jess.chronos.pulse.modules.transparencia.domain.entity.StatusPublicacaoTransparencia;
import br.com.jess.chronos.pulse.modules.transparencia.domain.entity.TipoPublicacaoTransparencia;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record PublicacaoResponseDTO(
        UUID id,
        UUID tenantId,
        String competencia,
        TipoPublicacaoTransparencia tipoPublicacao,
        BigDecimal valorTotal,
        Integer itensCount,
        StatusPublicacaoTransparencia status,
        LocalDate dataPublicacao,
        String observacoes,
        Instant criadoEm,
        Instant atualizadoEm
) {
}