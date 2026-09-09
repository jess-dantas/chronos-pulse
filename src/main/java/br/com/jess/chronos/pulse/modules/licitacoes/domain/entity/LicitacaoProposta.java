package br.com.jess.chronos.pulse.modules.licitacoes.domain.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "tb_licitacao_proposta")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LicitacaoProposta {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "tenant_id", nullable = false)
    private UUID tenantId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "licitacao_id", nullable = false)
    private Licitacao licitacao;

    @Column(name = "fornecedor_id", nullable = false)
    private UUID fornecedorId;

    @Column(name = "material_id", nullable = false)
    private UUID materialId;

    @Column(name = "valor_unitario", nullable = false, precision = 15, scale = 4)
    private BigDecimal valorUnitario;

    @Column(length = 255)
    private String observacao;

    @Builder.Default
    @Column(nullable = false)
    private Boolean vencedor = Boolean.FALSE;
}