package br.com.jess.chronos.pulse.modules.transparencia.domain.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "tb_transparencia_publicacao")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TransparenciaPublicacao {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "tenant_id", nullable = false)
    private UUID tenantId;

    @Column(nullable = false, length = 7)
    private String competencia;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_publicacao", nullable = false, length = 30)
    private TipoPublicacaoTransparencia tipoPublicacao;

    @Builder.Default
    @Column(name = "valor_total", nullable = false, precision = 15, scale = 2)
    private BigDecimal valorTotal = BigDecimal.ZERO;

    @Builder.Default
    @Column(name = "itens_count", nullable = false)
    private Integer itensCount = 0;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private StatusPublicacaoTransparencia status = StatusPublicacaoTransparencia.EM_ELABORACAO;

    @Column(name = "data_publicacao")
    private LocalDate dataPublicacao;

    @Column(columnDefinition = "TEXT")
    private String observacoes;

    @Column(name = "criado_em", insertable = false, updatable = false)
    private Instant criadoEm;

    @Column(name = "atualizado_em", insertable = false, updatable = false)
    private Instant atualizadoEm;
}