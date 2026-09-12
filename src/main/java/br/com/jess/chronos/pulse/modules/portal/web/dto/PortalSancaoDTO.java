package br.com.jess.chronos.pulse.modules.portal.web.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record PortalSancaoDTO(
        String tipo,
        String descricao,
        String baseLegal,
        BigDecimal percentualMulta,
        BigDecimal valorMulta,
        LocalDate aplicadaEm
) {}