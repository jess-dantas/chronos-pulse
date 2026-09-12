package br.com.jess.chronos.pulse.modules.portal.web.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record PortalPublicacaoDTO(
        UUID id,
        String competencia,
        String tipoPublicacao,
        BigDecimal valorTotal,
        int itensCount,
        LocalDate dataPublicacao,
        String observacoes
) {}