package br.com.jess.chronos.pulse.modules.compras.web.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record CadastrarPedidoCompraDTO(
        @NotNull(message = "Fornecedor é obrigatório")
        UUID fornecedorId,

        @Size(max = 255)
        String objeto,

        LocalDate prazoEntrega,

        UUID contratoId,

        @Size(max = 30)
        String empenhoNumero,

        String observacoes,

        @NotEmpty(message = "O pedido deve conter ao menos um item")
        @Valid
        List<PedidoCompraItemDTO> itens
) {}