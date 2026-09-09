package br.com.jess.chronos.pulse.modules.compras.web.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record PrecoConsultaResponseDTO(
        UUID materialId,
        String materialDescricao,
        String unidadeMedida,
        LocalDate ultimaCompraEm,
        BigDecimal ultimoValorUnitario,
        BigDecimal valorMedioUnitario,
        long totalEntradas
) {}