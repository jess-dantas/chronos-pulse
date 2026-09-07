package br.com.jess.chronos.pulse.modules.modulo.web.dto;

import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record AtualizarModulosEmpresaRequestDTO(
        @NotEmpty(message = "Informe ao menos um módulo.")
        List<@jakarta.validation.constraints.NotBlank String> modulos
) {}