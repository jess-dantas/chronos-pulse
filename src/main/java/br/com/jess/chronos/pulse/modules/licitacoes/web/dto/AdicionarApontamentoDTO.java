package br.com.jess.chronos.pulse.modules.licitacoes.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AdicionarApontamentoDTO(
        @NotBlank @Size(max = 100) String fiscal,
        @NotBlank @Size(max = 1000) String descricao,
        @NotBlank @Size(max = 20) String gravidade
) {}