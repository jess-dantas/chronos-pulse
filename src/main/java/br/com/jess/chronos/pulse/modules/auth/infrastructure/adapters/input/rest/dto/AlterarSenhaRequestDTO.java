package br.com.jess.chronos.pulse.modules.auth.infrastructure.adapters.input.rest.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AlterarSenhaRequestDTO(
        @NotBlank @Size(min = 6) String novaSenha
) {}