package br.com.jess.chronos.pulse.modules.telemetria.web.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record RegistrarEventosDTO(
        @NotEmpty(message = "Informe ao menos um evento")
        @Valid
        List<EventoIngestDTO> eventos
) {
}