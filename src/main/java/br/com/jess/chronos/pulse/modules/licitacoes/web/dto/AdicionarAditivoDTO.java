package br.com.jess.chronos.pulse.modules.licitacoes.web.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record AdicionarAditivoDTO(
        @NotNull @Size(max = 20) String tipo,
        @NotNull @Size(max = 500) String descricao,
        String justificativa,
        @Min(1) Integer prazoAdicionadoDias,
        BigDecimal novoValorTotal
) {}