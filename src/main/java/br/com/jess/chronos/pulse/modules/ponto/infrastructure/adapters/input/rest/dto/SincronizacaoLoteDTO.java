package br.com.jess.chronos.pulse.modules.ponto.infrastructure.adapters.input.rest.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record SincronizacaoLoteDTO(
        @NotEmpty(message = "O lote de sincronização não pode estar vazio")
        List<@Valid RegistroPontoDTO> registros
) {}
