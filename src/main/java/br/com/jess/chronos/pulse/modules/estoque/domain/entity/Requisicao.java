package br.com.jess.chronos.pulse.modules.estoque.domain.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "tb_requisicao")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Requisicao {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "tenant_id", nullable = false)
    private UUID tenantId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "almoxarifado_id", nullable = false)
    private Almoxarifado almoxarifado;

    @Column(name = "solicitante_cpc_id", nullable = false)
    private UUID solicitanteCpcId;

    @Column(length = 100)
    private String departamento;

    @Column(columnDefinition = "TEXT")
    private String justificativa;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private RequisicaoStatus status;

    @Column(name = "data_solicitacao", insertable = false, updatable = false)
    private OffsetDateTime dataSolicitacao;

    @Column(name = "data_atendimento")
    private OffsetDateTime dataAtendimento;

    @Column(name = "atendente_cpc_id")
    private UUID atendenteCpcId;

    @OneToMany(mappedBy = "requisicao", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<RequisicaoItem> itens = new ArrayList<>();

    public void adicionarItem(RequisicaoItem item) {
        itens.add(item);
        item.setRequisicao(this);
    }
}
