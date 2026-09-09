package br.com.jess.chronos.pulse.modules.patrimonio.domain.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "tb_transferencia_patrimonio")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TransferenciaPatrimonio {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "tenant_id", nullable = false)
    private UUID tenantId;

    @Column(name = "patrimonio_id", nullable = false)
    private UUID patrimonioId;

    @Column(name = "localizacao_origem", length = 120)
    private String localizacaoOrigem;

    @Column(name = "localizacao_destino", nullable = false, length = 120)
    private String localizacaoDestino;

    @Column(name = "responsavel_origem", length = 120)
    private String responsavelOrigem;

    @Column(name = "responsavel_destino", length = 120)
    private String responsavelDestino;

    @Column(name = "data_solicitacao", nullable = false)
    @Builder.Default
    private OffsetDateTime dataSolicitacao = OffsetDateTime.now();

    @Column(name = "data_prevista")
    private LocalDate dataPrevista;

    @Column(name = "data_efetivacao")
    private OffsetDateTime dataEfetivacao;

    @Column(nullable = false, length = 20)
    @Builder.Default
    private String status = "SOLICITADA";

    @Column(columnDefinition = "TEXT")
    private String justificativa;

    @Column(name = "aprovado_por", length = 120)
    private String aprovadoPor;

    @Column(name = "solicitado_por", length = 120)
    private String solicitadoPor;

    @Column(name = "criado_em", nullable = false)
    @Builder.Default
    private OffsetDateTime criadoEm = OffsetDateTime.now();
}