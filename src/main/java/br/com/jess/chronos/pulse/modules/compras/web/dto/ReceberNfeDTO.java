package br.com.jess.chronos.pulse.modules.compras.web.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record ReceberNfeDTO(
        @NotNull(message = "Chave NFe é obrigatória")
        @Size(min = 44, max = 44, message = "A chave NFe deve conter exatamente 44 dígitos")
        String chaveNfe,

        @Size(max = 9)
        String numeroNfe,

        @Size(max = 3)
        String serie,

        LocalDate dataEmissao,

        BigDecimal valorNota,

        UUID fornecedorId,

        @NotNull(message = "Pedido de compra é obrigatório")
        UUID pedidoId,

        @NotNull(message = "Almoxarifado é obrigatório")
        UUID almoxarifadoId,

        @Pattern(regexp = "PROVISORIO|DEFINITIVO", message = "Tipo de termo deve ser PROVISORIO ou DEFINITIVO")
        String tipoTermo,

        @Size(max = 30)
        String numeroTermo,

        String observacoes,

        @Size(max = 14)
        String cnpjEmitente,

        @Size(max = 160)
        String razaoEmitente,

        String xmlNfe,

        @NotEmpty(message = "Informe ao menos um item recebido")
        @Valid
        List<ItemNfeDTO> itens
) {}