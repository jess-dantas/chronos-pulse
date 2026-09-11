package br.com.jess.chronos.pulse.modules.licitacoes.domain.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "contrato")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ContratoLicitacao {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "tenant_id", nullable = false)
    private UUID tenantId;

    @Column(nullable = false, length = 50)
    private String numero;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String objeto;

    @Column(name = "data_inicio", nullable = false)
    private LocalDate dataInicio;

    @Column(name = "data_fim", nullable = false)
    private LocalDate dataFim;

    @Column(name = "valor_mensal", nullable = false, precision = 12, scale = 2)
    private BigDecimal valorMensal;

    @Column(name = "valor_total", nullable = false, precision = 12, scale = 2)
    private BigDecimal valorTotal;

    @Column(nullable = false, length = 20)
    private String status;

    @Column(columnDefinition = "TEXT")
    private String observacoes;

    @Column(name = "valor_empenhado", nullable = false, precision = 12, scale = 2)
    private BigDecimal valorEmpenhado;

    @Column(name = "valor_liquidado", nullable = false, precision = 12, scale = 2)
    private BigDecimal valorLiquidado;

    @Column(name = "empenho_numero")
    private String empenhoNumero;

    @Column(name = "vencimento_aviso_dias", nullable = false)
    private Integer vencimentoAvisoDias;

    @Column(name = "licitacao_id")
    private UUID licitacaoId;

    @Column(name = "criado_em", insertable = false, updatable = false)
    private Instant criadoEm;

    @Column(name = "atualizado_em", insertable = false, updatable = false)
    private Instant atualizadoEm;
}