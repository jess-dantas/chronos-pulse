package br.com.jess.chronos.pulse.modules.portal.web.dto;

import java.math.BigDecimal;

public record PortalLicitacaoItemDTO(
        String descricao,
        BigDecimal quantidade,
        BigDecimal valorEstimadoUnitario,
        BigDecimal valorEstimadoTotal
) {}