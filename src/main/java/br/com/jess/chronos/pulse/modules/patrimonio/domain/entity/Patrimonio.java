package br.com.jess.chronos.pulse.modules.patrimonio.domain.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "tb_patrimonio")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Patrimonio {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "tenant_id", nullable = false)
    private UUID tenantId;

    @Column(length = 30)
    private String tombamento;

    @Column(nullable = false)
    private String descricao;

    @Column(length = 60)
    private String categoria;

    @Column(nullable = false, length = 30)
    @Builder.Default
    private String estado = "BOM";

    @Column(length = 120)
    private String localizacao;

    @Column(name = "data_aquisicao")
    private LocalDate dataAquisicao;

    @Column(name = "valor_aquisicao", precision = 15, scale = 2)
    private BigDecimal valorAquisicao;

    @Column(name = "responsavel_nome", length = 120)
    private String responsavelNome;

    @Column(name = "numero_nota_fiscal", length = 30)
    private String numeroNotaFiscal;

    @Column(columnDefinition = "TEXT")
    private String observacoes;

    @Column(name = "vida_util_meses")
    private Integer vidaUtilMeses;

    @Column(name = "taxa_depreciacao_mensal", precision = 5, scale = 4)
    private BigDecimal taxaDepreciacaoMensal;

    @Column(name = "valor_depreciado", precision = 15, scale = 2)
    @Builder.Default
    private BigDecimal valorDepreciado = BigDecimal.ZERO;

    @Column(name = "data_inicio_depreciacao")
    private LocalDate dataInicioDepreciacao;

    @Builder.Default
    private Boolean ativo = true;
}
