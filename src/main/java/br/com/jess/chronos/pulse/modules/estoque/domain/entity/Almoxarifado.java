package br.com.jess.chronos.pulse.modules.estoque.domain.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "tb_almoxarifado")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Almoxarifado {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "tenant_id", nullable = false)
    private UUID tenantId;

    @Column(nullable = false, length = 100)
    private String nome;

    private String descricao;

    @Column(name = "responsavel_cpc_id")
    private UUID responsavelCpcId;

    @Builder.Default
    private Boolean ativo = true;
}