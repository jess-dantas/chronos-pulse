package br.com.jess.chronos.pulse.modules.licitacoes.domain.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "contrato_sancao")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ContratoSancao {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "tenant_id", nullable = false)
    private UUID tenantId;

    @Column(name = "contrato_id", nullable = false)
    private UUID contratoId;

    @Column(nullable = false, length = 30)
    private String tipo;

    @Column(name = "base_legal", columnDefinition = "TEXT")
    private String baseLegal;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String descricao;

    @Column(name = "percentual_multa", precision = 6, scale = 3)
    private BigDecimal percentualMulta;

    @Column(name = "valor_multa", precision = 12, scale = 2)
    private BigDecimal valorMulta;

    @Column(name = "aplicada_em", nullable = false)
    private LocalDate aplicadaEm;

    @Column(name = "criado_por", nullable = false)
    private UUID criadoPor;

    @Column(name = "criado_em", insertable = false, updatable = false)
    private Instant criadoEm;
}