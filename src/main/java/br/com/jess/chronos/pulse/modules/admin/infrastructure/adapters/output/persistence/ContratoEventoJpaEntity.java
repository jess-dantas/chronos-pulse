package br.com.jess.chronos.pulse.modules.admin.infrastructure.adapters.output.persistence;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "contrato_evento")
public class ContratoEventoJpaEntity {

    @Id private UUID id;
    @Column(name = "contrato_id", nullable = false) private UUID contratoId;
    @Column(nullable = false) private String tipo;
    @Column(nullable = false) private String descricao;
    @Column(name = "criado_por", nullable = false) private UUID criadoPor;
    @Column(name = "criado_em", nullable = false) private Instant criadoEm;

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public UUID getContratoId() { return contratoId; }
    public void setContratoId(UUID contratoId) { this.contratoId = contratoId; }
    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }
    public String getDescricao() { return descricao; }
    public void setDescricao(String descricao) { this.descricao = descricao; }
    public UUID getCriadoPor() { return criadoPor; }
    public void setCriadoPor(UUID criadoPor) { this.criadoPor = criadoPor; }
    public Instant getCriadoEm() { return criadoEm; }
    public void setCriadoEm(Instant criadoEm) { this.criadoEm = criadoEm; }
}
