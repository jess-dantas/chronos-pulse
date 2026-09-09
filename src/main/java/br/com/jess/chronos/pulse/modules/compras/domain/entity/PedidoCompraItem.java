package br.com.jess.chronos.pulse.modules.compras.domain.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "tb_pedido_compra_item")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PedidoCompraItem {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "tenant_id", nullable = false)
    private UUID tenantId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pedido_id", nullable = false)
    private PedidoCompra pedido;

    @Column(name = "material_id", nullable = false)
    private UUID materialId;

    @Column(nullable = false, precision = 15, scale = 3)
    private BigDecimal quantidade;

    @Builder.Default
    @Column(name = "valor_unitario", nullable = false, precision = 15, scale = 4)
    private BigDecimal valorUnitario = BigDecimal.ZERO;

    @Builder.Default
    @Column(name = "quantidade_recebida", nullable = false, precision = 15, scale = 3)
    private BigDecimal quantidadeRecebida = BigDecimal.ZERO;

    public BigDecimal getValorTotalItem() {
        return quantidade.multiply(valorUnitario);
    }
}