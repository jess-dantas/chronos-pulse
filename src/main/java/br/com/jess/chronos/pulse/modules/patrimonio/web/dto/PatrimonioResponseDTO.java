package br.com.jess.chronos.pulse.modules.patrimonio.web.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record PatrimonioResponseDTO(
        UUID id,
        String tombamento,
        String descricao,
        String categoria,
        String estado,
        String localizacao,
        LocalDate dataAquisicao,
        BigDecimal valorAquisicao,
        String responsavelNome,
        String numeroNotaFiscal,
        String observacoes,
        Boolean ativo
) {}
