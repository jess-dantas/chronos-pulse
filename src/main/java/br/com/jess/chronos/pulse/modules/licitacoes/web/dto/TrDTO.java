package br.com.jess.chronos.pulse.modules.licitacoes.web.dto;

import jakarta.validation.constraints.NotBlank;

public record TrDTO(
        @NotBlank(message = "Especificações do objeto são obrigatórias")
        String especificacoes,

        @NotBlank(message = "Condições de fornecimento são obrigatórias")
        String condicoesFornecimento,

        @NotBlank(message = "Obrigações do fornecedor são obrigatórias")
        String obrigacoes,

        @NotBlank(message = "Critérios de aceitação são obrigatórios")
        String criteriosAceitacao,

        @NotBlank(message = "Prazos de entrega são obrigatórios")
        String prazosEntrega,

        String garantia,

        String formaPagamento
) {}