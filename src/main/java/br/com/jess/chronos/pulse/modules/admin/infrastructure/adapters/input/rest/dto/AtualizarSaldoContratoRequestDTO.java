package br.com.jess.chronos.pulse.modules.admin.infrastructure.adapters.input.rest.dto;

import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;

public record AtualizarSaldoContratoRequestDTO(
        @PositiveOrZero BigDecimal valorEmpenhado,
        @PositiveOrZero BigDecimal valorLiquidado,
        String empenhoNumero,
        @PositiveOrZero Integer vencimentoAvisoDias
) {}