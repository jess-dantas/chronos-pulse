package br.com.jess.chronos.pulse.modules.portal.web.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record PortalAditivoDTO(
        String tipo,
        String descricao,
        String justificativa,
        Integer prazoAdicionadoDias,
        BigDecimal novoValorTotal,
        Boolean aprovado,
        Instant criadoEm
) {}