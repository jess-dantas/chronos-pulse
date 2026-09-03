package br.com.jess.chronos.pulse.modules.estoque.web.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record MaterialResponseDTO(
        UUID id,
        UUID grupoId,
        String grupoNome,
        String codigoCatmat,
        String descricao,
        String unidadeMedida,
        BigDecimal estoqueMinimo,
        Boolean controlaLoteValidade,
        Boolean ativo
) {}
