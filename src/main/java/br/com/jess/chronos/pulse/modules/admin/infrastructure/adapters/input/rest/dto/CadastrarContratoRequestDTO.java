package br.com.jess.chronos.pulse.modules.admin.infrastructure.adapters.input.rest.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record CadastrarContratoRequestDTO(
        @NotNull UUID tenantId,
        @NotBlank String numero,
        @NotBlank String objeto,
        @NotNull LocalDate dataInicio,
        @NotNull LocalDate dataFim,
        @NotNull BigDecimal valorMensal,
        @NotNull BigDecimal valorTotal,
        String observacoes,
        BigDecimal valorEmpenhado,
        BigDecimal valorLiquidado,
        String empenhoNumero,
        @PositiveOrZero Integer vencimentoAvisoDias
) {}
