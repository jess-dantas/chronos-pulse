package br.com.jess.chronos.pulse.modules.compras.web.dto;

import br.com.jess.chronos.pulse.modules.compras.domain.entity.EntradaNfe;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record EntradaNfeResponseDTO(
        UUID id,
        UUID tenantId,
        String chaveNfe,
        String numeroNfe,
        String serie,
        LocalDate dataEmissao,
        BigDecimal valorNota,
        UUID fornecedorId,
        UUID pedidoId,
        String pedidoNumero,
        UUID contratoId,
        String empenhoNumero,
        UUID almoxarifadoId,
        String tipoTermo,
        String numeroTermo,
        String cnpjEmitente,
        String razaoEmitente,
        String observacoes,
        Instant criadoEm
) {
    public static EntradaNfeResponseDTO from(EntradaNfe e, String pedidoNumero) {
        return new EntradaNfeResponseDTO(
                e.getId(), e.getTenantId(), e.getChaveNfe(), e.getNumeroNfe(), e.getSerie(),
                e.getDataEmissao(), e.getValorNota(), e.getFornecedorId(), e.getPedidoId(),
                pedidoNumero, e.getContratoId(), e.getEmpenhoNumero(), e.getAlmoxarifadoId(),
                e.getTipoTermo(), e.getNumeroTermo(), e.getCnpjEmitente(), e.getRazaoEmitente(),
                e.getObservacoes(), e.getCriadoEm());
    }
}