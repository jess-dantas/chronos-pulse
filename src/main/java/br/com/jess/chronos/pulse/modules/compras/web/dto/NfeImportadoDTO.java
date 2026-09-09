package br.com.jess.chronos.pulse.modules.compras.web.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record NfeImportadoDTO(
        String chaveNfe,
        String numero,
        String serie,
        LocalDate dataEmissao,
        BigDecimal valorNota,
        String cnpjEmitente,
        String razaoEmitente,
        String origem,
        List<NfeImportadoItemDTO> itens
) {}