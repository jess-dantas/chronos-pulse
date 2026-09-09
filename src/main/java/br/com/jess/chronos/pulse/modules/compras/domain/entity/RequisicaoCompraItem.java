package br.com.jess.chronos.pulse.modules.compras.domain.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "tb_requisicao_compra_item")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RequisicaoCompraItem {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "tenant_id", nullable = false)
    private UUID tenantId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "requisicao_id", nullable = false)
    private RequisicaoCompra requisicao;

    @Column(name = "material_id", nullable = false)
    private UUID materialId;

    @Column(nullable = false, precision = 15, scale = 3)
    private BigDecimal quantidade;

    @Column(length = 255)
    private String observacao;
}