package br.com.jess.chronos.pulse.modules.privacidade.web.dto;

import jakarta.validation.constraints.NotBlank;

public record ConsentimentoRequestDTO(
        @NotBlank String versaoPolitica,
        Boolean aceito
) {}