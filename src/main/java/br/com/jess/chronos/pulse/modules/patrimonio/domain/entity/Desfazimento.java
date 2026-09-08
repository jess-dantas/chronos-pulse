package br.com.jess.chronos.pulse.modules.patrimonio.domain.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "tb_desfazimento")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Desfazimento {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "tenant_id", nullable = false)
    private UUID tenantId;

    @Column(name = "patrimonio_id", nullable = false)
    private UUID patrimonioId;

    @Column(name = "estado_bem", nullable = false, length = 30)
    private String estadoBem;

    @Column(name = "tipo_desfazimento", nullable = false, length = 30)
    private String tipoDesfazimento;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String justificativa;

    @Column(name = "responsavel_solicitacao", nullable = false, length = 150)
    private String responsavelSolicitacao;

    @Column(name = "data_solicitacao", nullable = false)
    @Builder.Default
    private OffsetDateTime dataSolicitacao = OffsetDateTime.now();

    @Column(name = "parecer_comissao", columnDefinition = "TEXT")
    private String parecerComissao;

    private Boolean aprovado;

    @Column(name = "data_aprovacao")
    private OffsetDateTime dataAprovacao;

    @Column(name = "data_baixa")
    private OffsetDateTime dataBaixa;

    @Column(nullable = false, length = 30)
    @Builder.Default
    private String status = "EM_ANALISE";

    @Column(name = "processo_numero", length = 40)
    private String processoNumero;

    @Column(columnDefinition = "TEXT")
    private String observacoes;

    @Column(name = "criado_em")
    @Builder.Default
    private OffsetDateTime criadoEm = OffsetDateTime.now();

    @OneToMany(mappedBy = "desfazimento", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<DesfazimentoComissao> comissao = new ArrayList<>();
}