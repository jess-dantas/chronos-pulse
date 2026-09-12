package br.com.jess.chronos.pulse.modules.licitacoes.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record RescindirContratoDTO(
        @NotBlank @Size(max = 20) String tipo,
        @NotBlank @Size(max = 1000) String motivo,
        @NotNull LocalDate dataRescisao
) {}