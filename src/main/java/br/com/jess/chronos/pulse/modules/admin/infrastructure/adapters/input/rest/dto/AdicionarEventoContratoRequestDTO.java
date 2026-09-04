package br.com.jess.chronos.pulse.modules.admin.infrastructure.adapters.input.rest.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record AdicionarEventoContratoRequestDTO(
        @NotNull UUID contratoId,
        @NotBlank String tipo,
        @NotBlank String descricao
) {}
