package br.com.jess.chronos.pulse.modules.licitacoes.web.dto;

import jakarta.validation.constraints.NotBlank;

public record AprovacaoDocumentoDTO(
        @NotBlank(message = "Responsável pela aprovação é obrigatório")
        String responsavel
) {}