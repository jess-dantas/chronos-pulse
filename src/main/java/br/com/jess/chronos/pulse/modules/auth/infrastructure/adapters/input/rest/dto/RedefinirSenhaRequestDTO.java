package br.com.jess.chronos.pulse.modules.auth.infrastructure.adapters.input.rest.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RedefinirSenhaRequestDTO(
        @NotBlank @Size(min = 11, max = 11) String cpf,
        @NotBlank String codigo,
        @NotBlank @Size(min = 6) String novaSenha
) {}