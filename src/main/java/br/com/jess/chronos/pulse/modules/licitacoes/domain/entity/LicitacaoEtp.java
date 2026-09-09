package br.com.jess.chronos.pulse.modules.licitacoes.domain.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "tb_licitacao_etp")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LicitacaoEtp {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "licitacao_id", nullable = false, unique = true)
    private Licitacao licitacao;

    @Column(name = "tenant_id", nullable = false)
    private UUID tenantId;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String objeto;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String justificativa;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String requisitos;

    @Column(columnDefinition = "TEXT")
    private String alternativas;

    @Column(name = "valor_estimado", precision = 15, scale = 2)
    private BigDecimal valorEstimado;

    @Column(columnDefinition = "TEXT")
    private String riscos;

    @Column(columnDefinition = "TEXT")
    private String conclusao;

    @Column(length = 255)
    private String responsavel;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private EtpStatus status = EtpStatus.RASCUNHO;

    @Column(name = "data_aprovacao")
    private Instant dataAprovacao;

    @Column(name = "criado_em", insertable = false, updatable = false)
    private Instant criadoEm;

    @Column(name = "atualizado_em", insertable = false, updatable = false)
    private Instant atualizadoEm;
}