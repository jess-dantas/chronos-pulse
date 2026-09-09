package br.com.jess.chronos.pulse.modules.patrimonio.web.dto;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record ConferirItemDTO(
        @NotNull(message = "Patrimônio é obrigatório")
        UUID patrimonioId,

        @NotNull(message = "Resultado é obrigatório")
        String resultado,

        String observacao
) {}