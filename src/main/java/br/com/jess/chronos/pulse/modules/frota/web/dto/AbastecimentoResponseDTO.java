package br.com.jess.chronos.pulse.modules.frota.web.dto;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

public record AbastecimentoResponseDTO(
        UUID id,
        UUID veiculoId,
        String veiculoPlaca,
        OffsetDateTime dataHora,
        BigDecimal litros,
        BigDecimal valorLitro,
        BigDecimal valorTotal,
        BigDecimal odometroKm,
        String posto,
        String observacoes
) {}
