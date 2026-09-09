package br.com.jess.chronos.pulse.modules.compras.web.dto;

import java.time.LocalDate;
import java.util.UUID;

public record PrecoMaterialProjecao(
        UUID materialId,
        LocalDate dataEmissao,
        java.math.BigDecimal valorUnitario
) {}