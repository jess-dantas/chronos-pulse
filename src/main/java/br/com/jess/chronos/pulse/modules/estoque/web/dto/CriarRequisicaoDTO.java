package br.com.jess.chronos.pulse.modules.estoque.web.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import java.util.UUID;

public record CriarRequisicaoDTO(
        @NotNull(message = "Almoxarifado é obrigatório")
        UUID almoxarifadoId,

        String departamento,

        String justificativa,

        @NotEmpty(message = "A requisição deve conter pelo menos um item")
        @Valid
        List<ItemRequisicaoDTO> itens
) {}
