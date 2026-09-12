package br.com.jess.chronos.pulse.modules.licitacoes.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;

public record RegistrarMedicaoDTO(
        @NotBlank @Size(max = 20) String periodo,
        @PositiveOrZero BigDecimal valorMedido,
        @PositiveOrZero BigDecimal valorPago,
        LocalDate pagoEm,
        String observacao
) {}