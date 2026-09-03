package br.com.jess.chronos.pulse.modules.estoque.web.dto;

import jakarta.validation.constraints.NotBlank;

public record CadastrarMaterialGrupoDTO(
        @NotBlank(message = "Código é obrigatório")
        String codigo,

        @NotBlank(message = "Nome é obrigatório")
        String nome
) {}
