package br.com.jess.chronos.pulse.modules.ponto.infrastructure.adapters.input.rest.dto;

import br.com.jess.chronos.pulse.modules.ponto.domain.model.TipoRegistro;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;
import java.util.UUID;

public record AjustePontoManualDTO(
        UUID colaboradorId,
        @NotNull Instant dataHora,
        @NotNull TipoRegistro tipoRegistro,
        @NotBlank String justificativa,
        String observacao
) {}
