package br.com.jess.chronos.pulse.modules.licitacoes.web.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;
import java.util.UUID;

public record PublicarLicitacaoDTO(
        @NotEmpty(message = "Informe ao menos um fornecedor habilitado")
        List<@NotNull UUID> fornecedoresIds
) {}