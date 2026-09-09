package br.com.jess.chronos.pulse.modules.transparencia.web.dto;

import java.math.BigDecimal;
import java.util.List;

public record DespesasMensaisDTO(
        int ano,
        List<DespesaMensalDTO> meses
) {

    public record DespesaMensalDTO(
            int mes,
            BigDecimal despesasNfe,
            long notasFiscais,
            BigDecimal combustivel,
            long abastecimentos,
            BigDecimal pedidosEmitidos,
            long quantidadePedidos
    ) {
    }
}