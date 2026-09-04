package br.com.jess.chronos.pulse.modules.admin.infrastructure.adapters.input.rest.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

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
        String observacoes
) {}
