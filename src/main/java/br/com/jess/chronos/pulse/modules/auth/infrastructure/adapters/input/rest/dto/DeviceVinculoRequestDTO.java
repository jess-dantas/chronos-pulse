package br.com.jess.chronos.pulse.modules.auth.infrastructure.adapters.input.rest.dto;

import jakarta.validation.constraints.Size;

/// Corpo opcional do vínculo de dispositivo ("Modo Ponto").
public record DeviceVinculoRequestDTO(
        @Size(max = 120, message = "Nome do dispositivo deve ter no máximo 120 caracteres")
        String deviceName
) {}
