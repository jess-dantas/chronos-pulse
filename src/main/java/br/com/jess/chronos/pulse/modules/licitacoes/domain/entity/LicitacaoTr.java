package br.com.jess.chronos.pulse.modules.licitacoes.domain.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "tb_licitacao_tr")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LicitacaoTr {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "licitacao_id", nullable = false, unique = true)
    private Licitacao licitacao;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "etp_id", nullable = false, unique = true)
    private LicitacaoEtp etp;

    @Column(name = "tenant_id", nullable = false)
    private UUID tenantId;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String especificacoes;

    @Column(name = "condicoes_fornecimento", nullable = false, columnDefinition = "TEXT")
    private String condicoesFornecimento;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String obrigacoes;

    @Column(name = "criterios_aceitacao", nullable = false, columnDefinition = "TEXT")
    private String criteriosAceitacao;

    @Column(name = "prazos_entrega", nullable = false, columnDefinition = "TEXT")
    private String prazosEntrega;

    @Column(columnDefinition = "TEXT")
    private String garantia;

    @Column(name = "forma_pagamento", columnDefinition = "TEXT")
    private String formaPagamento;

    @Column(length = 255)
    private String responsavel;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private TrStatus status = TrStatus.RASCUNHO;

    @Column(name = "data_aprovacao")
    private Instant dataAprovacao;

    @Column(name = "criado_em", insertable = false, updatable = false)
    private Instant criadoEm;

    @Column(name = "atualizado_em", insertable = false, updatable = false)
    private Instant atualizadoEm;
}