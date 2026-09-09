package br.com.jess.chronos.pulse.modules.compras.domain.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "tb_cotacao_compra")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CotacaoCompra {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "tenant_id", nullable = false)
    private UUID tenantId;

    @Column(nullable = false, length = 20)
    private String numero;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "requisicao_id", nullable = false)
    private RequisicaoCompra requisicao;

    @Column(name = "data_limite")
    private LocalDate dataLimite;

    @Column(columnDefinition = "TEXT")
    private String observacoes;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private CotacaoStatus status;

    @Builder.Default
    @Column(name = "pedido_gerado", nullable = false)
    private Boolean pedidoGerado = Boolean.FALSE;

    @Builder.Default
    @OneToMany(mappedBy = "cotacao", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CotacaoFornecedor> fornecedores = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "cotacao", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CotacaoProposta> propostas = new ArrayList<>();

    @Column(name = "criado_em", insertable = false, updatable = false)
    private Instant criadoEm;

    @Column(name = "atualizado_em", insertable = false, updatable = false)
    private Instant atualizadoEm;

    public void adicionarFornecedor(CotacaoFornecedor fornecedor) {
        fornecedores.add(fornecedor);
        fornecedor.setCotacao(this);
    }

    public void adicionarProposta(CotacaoProposta proposta) {
        propostas.add(proposta);
        proposta.setCotacao(this);
    }
}