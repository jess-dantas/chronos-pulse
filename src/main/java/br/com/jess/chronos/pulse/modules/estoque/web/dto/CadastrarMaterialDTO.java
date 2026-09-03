package br.com.jess.chronos.pulse.modules.estoque.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.UUID;

public record CadastrarMaterialDTO(
        @NotNull(message = "Grupo é obrigatório")
        UUID grupoId,

        String codigoCatmat,

        @NotBlank(message = "Descrição é obrigatória")
        String descricao,

        @NotBlank(message = "Unidade de medida é obrigatória")
        String unidadeMedida,

        BigDecimal estoqueMinimo,

        Boolean controlaLoteValidade
) {}
