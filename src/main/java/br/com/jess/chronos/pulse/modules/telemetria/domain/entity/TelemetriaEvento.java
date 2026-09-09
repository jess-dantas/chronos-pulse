package br.com.jess.chronos.pulse.modules.telemetria.domain.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "tb_telemetria_evento")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TelemetriaEvento {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "tenant_id")
    private UUID tenantId;

    @Column(name = "usuario_id")
    private UUID usuarioId;

    @Column(nullable = false, length = 30)
    private String modulo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private TipoEventoTelemetria tipo;

    @Column(length = 255)
    private String endpoint;

    @Column(name = "status_http")
    private Integer statusHttp;

    @Column(name = "latency_ms")
    private Long latencyMs;

    @Column(length = 500)
    private String mensagem;

    @Column(columnDefinition = "TEXT")
    private String detalhe;

    @Column(name = "trace_id", length = 64)
    private String traceId;

    @Column(name = "app_versao", length = 30)
    private String appVersao;

    @Column(length = 20)
    private String plataforma;

    @Column(name = "criado_em", nullable = false, updatable = false)
    private Instant criadoEm;
}