package br.com.jess.chronos.pulse.modules.estoque.web.dto;

import java.util.UUID;

public record MaterialGrupoResponseDTO(
        UUID id,
        String codigo,
        String nome,
        Boolean ativo
) {}
