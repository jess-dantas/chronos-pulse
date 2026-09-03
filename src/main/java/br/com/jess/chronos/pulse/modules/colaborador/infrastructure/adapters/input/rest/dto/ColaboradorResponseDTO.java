package br.com.jess.chronos.pulse.modules.colaborador.infrastructure.adapters.input.rest.dto;

import java.util.UUID;

public record ColaboradorResponseDTO(
        UUID id,
        UUID cpcUsuarioId,
        UUID tenantId,
        String matricula,
        String cargo,
        String departamento
) {}
