package br.com.jess.chronos.pulse.modules.licitacoes.domain.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "tb_licitacao_item")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LicitacaoItem {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "tenant_id", nullable = false)
    private UUID tenantId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "licitacao_id", nullable = false)
    private Licitacao licitacao;

    @Column(name = "material_id", nullable = false)
    private UUID materialId;

    @Column(nullable = false, length = 255)
    private String descricao;

    @Column(nullable = false, precision = 15, scale = 3)
    private BigDecimal quantidade;

    @Column(name = "valor_estimado_unitario", precision = 15, scale = 4)
    private BigDecimal valorEstimadoUnitario;

    public BigDecimal getValorEstimadoTotal() {
        if (valorEstimadoUnitario == null) {
            return BigDecimal.ZERO;
        }
        return valorEstimadoUnitario.multiply(quantidade);
    }
}