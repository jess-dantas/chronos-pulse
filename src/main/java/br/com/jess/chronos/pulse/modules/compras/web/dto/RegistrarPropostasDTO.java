package br.com.jess.chronos.pulse.modules.compras.web.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;
import java.util.UUID;

public record RegistrarPropostasDTO(
        @NotNull(message = "Fornecedor é obrigatório")
        UUID fornecedorId,

        @NotEmpty(message = "Informe ao menos um item na proposta")
        @Valid
        List<PropostaItemDTO> itens
) {}