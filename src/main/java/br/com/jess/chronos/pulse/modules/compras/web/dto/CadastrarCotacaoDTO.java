package br.com.jess.chronos.pulse.modules.compras.web.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record CadastrarCotacaoDTO(
        @NotNull(message = "Requisição é obrigatória")
        UUID requisicaoId,

        @NotEmpty(message = "Informe ao menos um fornecedor convidado")
        List<@NotNull UUID> fornecedoresIds,

        LocalDate dataLimite,

        String observacoes
) {}