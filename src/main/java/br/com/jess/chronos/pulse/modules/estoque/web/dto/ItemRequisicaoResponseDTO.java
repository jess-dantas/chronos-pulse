package br.com.jess.chronos.pulse.modules.estoque.web.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record ItemRequisicaoResponseDTO(
        UUID id,
        UUID materialId,
        String materialDescricao,
        String unidadeMedida,
        BigDecimal quantidadeSolicitada,
        BigDecimal quantidadeAtendida
) {}
