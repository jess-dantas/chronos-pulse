package br.com.jess.chronos.pulse.modules.patrimonio.web.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

public record InventarioItemResponseDTO(
        UUID id,
        UUID patrimonioId,
        String patrimonioTombamento,
        String patrimonioDescricao,
        boolean conferido,
        String conferidoPor,
        OffsetDateTime dataConferencia,
        String resultado,
        String observacao
) {}