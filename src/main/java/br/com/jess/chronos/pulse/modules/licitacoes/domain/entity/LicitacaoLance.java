package br.com.jess.chronos.pulse.modules.licitacoes.domain.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "tb_licitacao_lance", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"licitacao_id", "licitacao_item_id", "fornecedor_id"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LicitacaoLance {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "tenant_id", nullable = false)
    private UUID tenantId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "licitacao_id", nullable = false)
    private Licitacao licitacao;

    @Column(name = "licitacao_item_id", nullable = false)
    private UUID licitacaoItemId;

    @Column(name = "fornecedor_id", nullable = false)
    private UUID fornecedorId;

    @Column(name = "valor_unitario", nullable = false, precision = 15, scale = 4)
    private BigDecimal valorUnitario;

    @Column(length = 255)
    private String observacao;

    @Column(name = "criado_em", nullable = false)
    @Builder.Default
    private Instant criadoEm = Instant.now();

    @Column(name = "atualizado_em", nullable = false)
    @Builder.Default
    private Instant atualizadoEm = Instant.now();
}
