package br.com.jess.chronos.pulse.modules.protocolo.web.dto;

import jakarta.validation.constraints.NotBlank;

public record AtualizarStatusProtocoloDTO(
        @NotBlank(message = "Status é obrigatório")
        String status,

        String responsavel,

        String observacoes
) {}
