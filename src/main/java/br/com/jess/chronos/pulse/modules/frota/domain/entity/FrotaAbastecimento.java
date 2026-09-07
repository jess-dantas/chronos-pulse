package br.com.jess.chronos.pulse.modules.frota.domain.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "tb_frota_abastecimento")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FrotaAbastecimento {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "tenant_id", nullable = false)
    private UUID tenantId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "veiculo_id", nullable = false)
    private FrotaVeiculo veiculo;

    @Column(name = "data_hora", nullable = false)
    @Builder.Default
    private OffsetDateTime dataHora = OffsetDateTime.now();

    @Column(nullable = false, precision = 12, scale = 3)
    private BigDecimal litros;

    @Column(name = "valor_litro", nullable = false, precision = 15, scale = 4)
    private BigDecimal valorLitro;

    @Column(name = "valor_total", nullable = false, precision = 15, scale = 4)
    private BigDecimal valorTotal;

    @Column(name = "odometro_km", precision = 12, scale = 1)
    private BigDecimal odometroKm;

    @Column(length = 120)
    private String posto;

    @Column(columnDefinition = "TEXT")
    private String observacoes;
}
