package br.com.jess.chronos.pulse.modules.patrimonio.web.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotBlank;

import java.time.LocalDate;
import java.util.UUID;

public record SolicitarTransferenciaDTO(
        @NotNull(message = "Patrimônio é obrigatório")
        UUID patrimonioId,

        @NotBlank(message = "Localização de destino é obrigatória")
        String localizacaoDestino,

        String localizacaoOrigem,

        String responsavelOrigem,

        String responsavelDestino,

        LocalDate dataPrevista,

        String justificativa
) {}