package br.com.jess.chronos.pulse.modules.compras.web.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;
import java.util.UUID;

public record CadastrarRequisicaoDTO(
        @NotNull(message = "Solicitante é obrigatório")
        UUID solicitanteCpcId,

        @NotBlank(message = "Justificativa é obrigatória")
        String justificativa,

        String observacoes,

        @NotEmpty(message = "A requisição deve conter ao menos um item")
        @Valid
        List<RequisicaoItemDTO> itens
) {}