package br.com.jess.chronos.pulse.modules.estoque.web.dto;

import java.util.UUID;

public record AlmoxarifadoResponseDTO(
        UUID id,
        String nome,
        String descricao,
        UUID responsavelCpcId,
        Boolean ativo
) {}
