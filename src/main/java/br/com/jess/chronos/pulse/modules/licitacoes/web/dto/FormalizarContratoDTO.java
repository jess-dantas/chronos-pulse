package br.com.jess.chronos.pulse.modules.licitacoes.web.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;
import java.time.LocalDate;

public record FormalizarContratoDTO(
        @NotNull LocalDate dataInicio,
        @NotNull LocalDate dataFim,
        String observacoes,
        BigDecimal valorMensal,
        BigDecimal valorEmpenhado,
        BigDecimal valorLiquidado,
        String empenhoNumero,
        @PositiveOrZero Integer vencimentoAvisoDias
) {}