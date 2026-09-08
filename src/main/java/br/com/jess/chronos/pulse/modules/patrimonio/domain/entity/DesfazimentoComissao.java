package br.com.jess.chronos.pulse.modules.patrimonio.domain.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "tb_desfazimento_comissao")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DesfazimentoComissao {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "desfazimento_id", nullable = false)
    private Desfazimento desfazimento;

    @Column(name = "membro_nome", nullable = false, length = 150)
    private String membroNome;

    @Column(name = "membro_cargo", length = 120)
    private String membroCargo;

    @Column(name = "membro_cpf", length = 20)
    private String membroCpf;

    @Builder.Default
    private Boolean relator = false;

    @Column(columnDefinition = "TEXT")
    private String parecer;

    @Column(name = "criado_em")
    @Builder.Default
    private OffsetDateTime criadoEm = OffsetDateTime.now();
}