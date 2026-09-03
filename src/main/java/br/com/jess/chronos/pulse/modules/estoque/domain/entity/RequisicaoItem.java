package br.com.jess.chronos.pulse.modules.estoque.domain.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "tb_requisicao_item")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RequisicaoItem {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "requisicao_id", nullable = false)
    private Requisicao requisicao;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "material_id", nullable = false)
    private Material material;

    @Column(name = "quantidade_solicitada", nullable = false, precision = 12, scale = 3)
    private BigDecimal quantidadeSolicitada;

    @Column(name = "quantidade_atendida", precision = 12, scale = 3)
    @Builder.Default
    private BigDecimal quantidadeAtendida = BigDecimal.ZERO;
}
