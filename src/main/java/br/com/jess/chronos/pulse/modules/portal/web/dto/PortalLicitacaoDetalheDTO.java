package br.com.jess.chronos.pulse.modules.portal.web.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record PortalLicitacaoDetalheDTO(
        UUID id,
        String numero,
        String modalidade,
        String tipoJulgamento,
        String status,
        String objeto,
        LocalDate dataAbertura,
        BigDecimal valorEstimado,
        String observacoes,
        Instant pncpPublicadoEm,
        List<PortalLicitacaoItemDTO> itens
) {}