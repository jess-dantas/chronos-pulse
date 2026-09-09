package br.com.jess.chronos.pulse.modules.compras.domain.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "tb_requisicao_compra")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RequisicaoCompra {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "tenant_id", nullable = false)
    private UUID tenantId;

    @Column(nullable = false, length = 20)
    private String numero;

    @Column(name = "solicitante_cpc_id", nullable = false)
    private UUID solicitanteCpcId;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String justificativa;

    @Column(name = "data_requisicao", nullable = false)
    private LocalDate dataRequisicao;

    @Column(columnDefinition = "TEXT")
    private String observacoes;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private RequisicaoStatus status;

    @OneToMany(mappedBy = "requisicao", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<RequisicaoCompraItem> itens = new ArrayList<>();

    @Column(name = "criado_em", insertable = false, updatable = false)
    private Instant criadoEm;

    @Column(name = "atualizado_em", insertable = false, updatable = false)
    private Instant atualizadoEm;

    public void adicionarItem(RequisicaoCompraItem item) {
        itens.add(item);
        item.setRequisicao(this);
    }
}