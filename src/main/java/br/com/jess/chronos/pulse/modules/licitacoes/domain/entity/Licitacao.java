package br.com.jess.chronos.pulse.modules.licitacoes.domain.entity;

import br.com.jess.chronos.pulse.modules.compras.domain.entity.Fornecedor;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "tb_licitacao")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Licitacao {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "tenant_id", nullable = false)
    private UUID tenantId;

    @Column(nullable = false, length = 20)
    private String numero;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private LicitacaoModalidade modalidade;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_julgamento", nullable = false, length = 30)
    private LicitacaoTipoJulgamento tipoJulgamento;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String objeto;

    @Column(name = "data_abertura")
    private java.time.LocalDate dataAbertura;

    @Column(name = "valor_estimado", precision = 15, scale = 2)
    private BigDecimal valorEstimado;

    @Column(columnDefinition = "TEXT")
    private String observacoes;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    @Builder.Default
    private LicitacaoStatus status = LicitacaoStatus.EM_ELABORACAO;

    @Builder.Default
    @Column(name = "pedido_gerado", nullable = false)
    private Boolean pedidoGerado = Boolean.FALSE;

    @OneToMany(mappedBy = "licitacao", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<LicitacaoItem> itens = new ArrayList<>();

    @OneToMany(mappedBy = "licitacao", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<LicitacaoParticipante> participantes = new ArrayList<>();

    @OneToMany(mappedBy = "licitacao", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<LicitacaoProposta> propostas = new ArrayList<>();

    @Column(name = "criado_em", insertable = false, updatable = false)
    private Instant criadoEm;

    public void adicionarItem(LicitacaoItem item) {
        itens.add(item);
        item.setLicitacao(this);
    }

    public void adicionarParticipante(LicitacaoParticipante participante) {
        participantes.add(participante);
        participante.setLicitacao(this);
    }

    public void adicionarProposta(LicitacaoProposta proposta) {
        propostas.add(proposta);
        proposta.setLicitacao(this);
    }
}