package br.com.jess.chronos.pulse.modules.protocolo.web.dto;

import jakarta.validation.constraints.NotBlank;

public record CadastrarProtocoloDTO(
        @NotBlank(message = "Número do protocolo é obrigatório")
        String numeroProtocolo,

        @NotBlank(message = "Tipo é obrigatório")
        String tipo,

        @NotBlank(message = "Assunto é obrigatório")
        String assunto,

        String descricao,

        String remetente,

        String destinatario,

        String status,

        String responsavel,

        String observacoes
) {}
