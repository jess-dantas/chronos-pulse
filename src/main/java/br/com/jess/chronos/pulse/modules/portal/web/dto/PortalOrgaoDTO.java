package br.com.jess.chronos.pulse.modules.portal.web.dto;

import java.math.BigDecimal;

public record PortalOrgaoDTO(
        String slug,
        String nome,
        String cnpj
) {}