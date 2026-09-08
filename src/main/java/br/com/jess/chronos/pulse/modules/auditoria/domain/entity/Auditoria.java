package br.com.jess.chronos.pulse.modules.auditoria.domain.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "tb_auditoria")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Auditoria {

    @Id
    private UUID id;

    @Column(name = "tenant_id")
    private UUID tenantId;

    @Column(name = "usuario_cpc_id")
    private UUID usuarioCpcId;

    @Column(name = "usuario_cpf", length = 20)
    private String usuarioCpf;

    @Column(length = 50)
    private String papel;

    @Column(nullable = false, length = 100)
    private String acao;

    @Column(nullable = false, length = 100)
    private String entidade;

    @Column(name = "entidade_id", length = 100)
    private String entidadeId;

    @Column(length = 1000)
    private String descricao;

    @Column(name = "payload_antes", columnDefinition = "TEXT")
    private String payloadAntes;

    @Column(name = "payload_depois", columnDefinition = "TEXT")
    private String payloadDepois;

    @Column(name = "ip_origem", length = 64)
    private String ipOrigem;

    @Column(name = "data_hora", nullable = false)
    private OffsetDateTime dataHora;

    @Column(name = "hash_anterior", length = 64)
    private String hashAnterior;

    @Column(name = "hash_registro", nullable = false, unique = true, length = 64)
    private String hashRegistro;
}
