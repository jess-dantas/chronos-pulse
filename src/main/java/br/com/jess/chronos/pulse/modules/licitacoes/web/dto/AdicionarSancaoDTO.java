package br.com.jess.chronos.pulse.modules.licitacoes.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;

public record AdicionarSancaoDTO(
        @NotBlank @Size(max = 30) String tipo,
        String baseLegal,
        @NotBlank @Size(max = 1000) String descricao,
        @PositiveOrZero BigDecimal percentualMulta,
        @PositiveOrZero BigDecimal valorMulta,
        @NotNull LocalDate aplicadaEm
) {}