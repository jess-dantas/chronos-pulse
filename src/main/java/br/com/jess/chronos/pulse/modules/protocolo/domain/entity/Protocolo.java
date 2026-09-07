package br.com.jess.chronos.pulse.modules.protocolo.domain.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "tb_protocolo")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Protocolo {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "tenant_id", nullable = false)
    private UUID tenantId;

    @Column(name = "numero_protocolo", nullable = false, length = 30)
    private String numeroProtocolo;

    @Column(nullable = false, length = 40)
    private String tipo;

    @Column(nullable = false)
    private String assunto;

    @Column(columnDefinition = "TEXT")
    private String descricao;

    @Column(length = 150)
    private String remetente;

    @Column(length = 150)
    private String destinatario;

    @Column(name = "data_protocolo", nullable = false)
    @Builder.Default
    private OffsetDateTime dataProtocolo = OffsetDateTime.now();

    @Column(nullable = false, length = 30)
    @Builder.Default
    private String status = "RECEBIDO";

    @Column(length = 120)
    private String responsavel;

    @Column(columnDefinition = "TEXT")
    private String observacoes;

    @Builder.Default
    private Boolean ativo = true;
}
