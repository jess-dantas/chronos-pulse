package br.com.jess.chronos.pulse.modules.licitacoes.domain.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "contrato_apontamento")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ContratoApontamento {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "tenant_id", nullable = false)
    private UUID tenantId;

    @Column(name = "contrato_id", nullable = false)
    private UUID contratoId;

    @Column(nullable = false, length = 100)
    private String fiscal;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String descricao;

    @Column(nullable = false, length = 20)
    private String gravidade;

    @Column(nullable = false)
    private Boolean resolvido;

    @Column(name = "resolvido_em")
    private Instant resolvidoEm;

    @Column(name = "criado_por", nullable = false)
    private UUID criadoPor;

    @Column(name = "criado_em", insertable = false, updatable = false)
    private Instant criadoEm;
}