package br.com.jess.chronos.pulse.modules.titularidade.infrastructure.adapters.input.rest.dto;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record IniciarTitularidadeRequestDTO(
        @NotNull(message = "Novo titular é obrigatório")
        UUID novoTitularId
) {}
