package br.com.jess.chronos.pulse.modules.estoque.domain.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "tb_estoque_saldo")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EstoqueSaldo {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "tenant_id", nullable = false)
    private UUID tenantId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "almoxarifado_id", nullable = false)
    private Almoxarifado almoxarifado;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "material_id", nullable = false)
    private Material material;

    private String lote;

    @Column(name = "data_validade")
    private LocalDate dataValidade;

    @Column(name = "quantidade_atual", nullable = false, precision = 12, scale = 3)
    private BigDecimal quantidadeAtual;

    @Column(name = "custo_medio_unitario", nullable = false, precision = 15, scale = 4)
    private BigDecimal custoMedioUnitario;
}