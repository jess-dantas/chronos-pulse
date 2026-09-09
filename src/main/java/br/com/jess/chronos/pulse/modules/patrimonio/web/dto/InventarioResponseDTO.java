package br.com.jess.chronos.pulse.modules.patrimonio.web.dto;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public record InventarioResponseDTO(
        UUID id,
        String descricao,
        LocalDate dataInicio,
        LocalDate dataFim,
        String status,
        String criadoPor,
        OffsetDateTime criadoEm,
        int totalItens,
        int totalConferidos,
        int totalConformes,
        int totalDivergencias,
        List<InventarioItemResponseDTO> itens
) {}