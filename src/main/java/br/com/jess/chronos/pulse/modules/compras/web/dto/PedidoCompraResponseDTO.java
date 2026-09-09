package br.com.jess.chronos.pulse.modules.compras.web.dto;

import br.com.jess.chronos.pulse.modules.compras.domain.entity.PedidoCompra;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record PedidoCompraResponseDTO(
        UUID id,
        UUID tenantId,
        String numero,
        UUID fornecedorId,
        String fornecedorNome,
        String objeto,
        LocalDate dataEmissao,
        LocalDate prazoEntrega,
        UUID contratoId,
        String empenhoNumero,
        BigDecimal valorTotal,
        String status,
        String observacoes,
        List<PedidoCompraItemResponseDTO> itens,
        Instant criadoEm
) {
    public static PedidoCompraResponseDTO from(PedidoCompra pedido, String fornecedorNome,
                                               List<PedidoCompraItemResponseDTO> itens) {
        return new PedidoCompraResponseDTO(
                pedido.getId(), pedido.getTenantId(), pedido.getNumero(),
                pedido.getFornecedor().getId(), fornecedorNome, pedido.getObjeto(),
                pedido.getDataEmissao(), pedido.getPrazoEntrega(), pedido.getContratoId(),
                pedido.getEmpenhoNumero(), pedido.getValorTotal(), pedido.getStatus().name(),
                pedido.getObservacoes(), itens, pedido.getCriadoEm());
    }
}