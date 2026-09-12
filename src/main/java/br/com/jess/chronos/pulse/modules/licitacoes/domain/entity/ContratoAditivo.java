package br.com.jess.chronos.pulse.modules.licitacoes.domain.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "contrato_aditivo")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ContratoAditivo {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "tenant_id", nullable = false)
    private UUID tenantId;

    @Column(name = "contrato_id", nullable = false)
    private UUID contratoId;

    @Column(nullable = false, length = 20)
    private String tipo;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String descricao;

    @Column(columnDefinition = "TEXT")
    private String justificativa;

    @Column(name = "prazo_adicionado_dias")
    private Integer prazoAdicionadoDias;

    @Column(name = "novo_valor_total", precision = 12, scale = 2)
    private BigDecimal novoValorTotal;

    @Column(nullable = false)
    private Boolean aprovado;

    @Column(name = "criado_por", nullable = false)
    private UUID criadoPor;

    @Column(name = "criado_em", insertable = false, updatable = false)
    private Instant criadoEm;
}