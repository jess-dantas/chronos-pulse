package br.com.jess.chronos.pulse.modules.licitacoes.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.util.List;

public record CadastrarLicitacaoDTO(
        @NotBlank(message = "Modalidade é obrigatória")
        String modalidade,

        @NotBlank(message = "Critério de julgamento é obrigatório")
        String tipoJulgamento,

        @NotBlank(message = "Objeto é obrigatório")
        String objeto,

        LocalDate dataAbertura,

        String observacoes,

        @NotEmpty(message = "Informe ao menos um item")
        List<LicitacaoItemDTO> itens
) {}