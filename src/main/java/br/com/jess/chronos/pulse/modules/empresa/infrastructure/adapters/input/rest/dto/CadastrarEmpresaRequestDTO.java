package br.com.jess.chronos.pulse.modules.empresa.infrastructure.adapters.input.rest.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CadastrarEmpresaRequestDTO(
        @NotBlank @Size(min = 14, max = 14) String cnpj,
        @NotBlank String nome
) {}
