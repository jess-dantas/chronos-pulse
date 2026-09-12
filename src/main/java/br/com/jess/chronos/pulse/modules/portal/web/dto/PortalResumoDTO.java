package br.com.jess.chronos.pulse.modules.portal.web.dto;

import java.math.BigDecimal;

public record PortalResumoDTO(
        PortalOrgaoDTO orgao,
        long licitacoesPublicadas,
        long licitacoesEmAndamento,
        long licitacoesHomologadas,
        long contratosAtivos,
        BigDecimal valorEmpenhado,
        BigDecimal valorLiquidado,
        BigDecimal valorDespesasAno,
        long publicacoesDivulgadas,
        String ultimaCompetencia
) {}