package br.com.jess.chronos.pulse.modules.licitacoes.web.dto;

import jakarta.validation.constraints.NotBlank;

import java.math.BigDecimal;

public record EtpDTO(
        @NotBlank(message = "Objeto do ETP é obrigatório")
        String objeto,

        @NotBlank(message = "Justificativa é obrigatória")
        String justificativa,

        @NotBlank(message = "Requisitos são obrigatórios")
        String requisitos,

        String alternativas,

        BigDecimal valorEstimado,

        String riscos,

        String conclusao
) {}