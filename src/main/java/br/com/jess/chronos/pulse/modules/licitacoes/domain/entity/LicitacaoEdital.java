package br.com.jess.chronos.pulse.modules.licitacoes.domain.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

@Entity
@Table(name = "tb_licitacao_edital")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LicitacaoEdital {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "licitacao_id", nullable = false, unique = true)
    private Licitacao licitacao;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tr_id")
    private LicitacaoTr tr;

    @Column(name = "tenant_id", nullable = false)
    private UUID tenantId;

    @Column(name = "numero_processo", length = 40)
    private String numeroProcesso;

    @Column(name = "numero_edital", length = 40)
    private String numeroEdital;

    @Column(name = "local_sessao", length = 255)
    private String localSessao;

    @Column(name = "data_abertura_sessao")
    private LocalDate dataAberturaSessao;

    @Column(name = "horario_abertura")
    private LocalTime horarioAbertura;

    @Column(name = "forma_entrega_propostas", length = 20)
    private String formaEntregaPropostas;

    @Column(columnDefinition = "TEXT")
    private String anexos;

    @Column(columnDefinition = "TEXT")
    private String observacoes;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private EditalStatus status = EditalStatus.EM_ELABORACAO;

    @Column(name = "data_publicacao")
    private Instant dataPublicacao;

    @Column(name = "criado_em", insertable = false, updatable = false)
    private Instant criadoEm;

    @Column(name = "atualizado_em", insertable = false, updatable = false)
    private Instant atualizadoEm;
}