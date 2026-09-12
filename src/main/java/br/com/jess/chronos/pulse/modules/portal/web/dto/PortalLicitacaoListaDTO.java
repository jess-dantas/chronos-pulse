package br.com.jess.chronos.pulse.modules.portal.web.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record PortalLicitacaoListaDTO(
        UUID id,
        String numero,
        String modalidade,
        String status,
        String objeto,
        LocalDate dataAbertura,
        BigDecimal valorEstimado,
        Instant pncpPublicadoEm
) {}