package br.com.jess.chronos.pulse.modules.portal.web.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record PortalContratoDetalheDTO(
        UUID id,
        String numero,
        String objeto,
        LocalDate dataInicio,
        LocalDate dataFim,
        String status,
        BigDecimal valorTotal,
        BigDecimal valorEmpenhado,
        BigDecimal valorLiquidado,
        String empenhoNumero,
        UUID licitacaoId,
        List<PortalAditivoDTO> aditivos,
        List<PortalSancaoDTO> sancoes
) {}