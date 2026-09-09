package br.com.jess.chronos.pulse.modules.patrimonio.web.dto;

import jakarta.validation.constraints.NotBlank;

import java.time.LocalDate;

public record CriarInventarioDTO(
        @NotBlank(message = "Descrição é obrigatória")
        String descricao,

        LocalDate dataInicio,

        LocalDate dataFim
) {}