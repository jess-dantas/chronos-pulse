package br.com.jess.chronos.pulse.modules.licitacoes.web.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;
import java.util.UUID;

public record RegistrarPropostasLicitacaoDTO(
        @NotNull(message = "Fornecedor é obrigatório")
        UUID fornecedorId,

        @NotEmpty(message = "Informe ao menos um valor de proposta")
        List<LicitacaoPropostaDTO> itens
) {}