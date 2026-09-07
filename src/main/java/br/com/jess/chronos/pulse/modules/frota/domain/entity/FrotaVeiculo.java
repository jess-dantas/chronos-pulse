package br.com.jess.chronos.pulse.modules.frota.domain.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "tb_frota_veiculo")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FrotaVeiculo {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "tenant_id", nullable = false)
    private UUID tenantId;

    @Column(nullable = false, length = 10)
    private String placa;

    @Column(length = 20)
    private String renavam;

    @Column(length = 60)
    private String marca;

    @Column(length = 60)
    private String modelo;

    @Column(name = "ano_fabricacao")
    private Integer anoFabricacao;

    @Column(name = "ano_modelo")
    private Integer anoModelo;

    @Column(length = 30)
    private String tipo;

    @Column(length = 30)
    private String combustivel;

    @Column(nullable = false, length = 20)
    @Builder.Default
    private String status = "ATIVO";

    @Column(name = "odometro_atual", precision = 12, scale = 1)
    private BigDecimal odometroAtual;

    @Column(columnDefinition = "TEXT")
    private String observacoes;

    @Builder.Default
    private Boolean ativo = true;
}
