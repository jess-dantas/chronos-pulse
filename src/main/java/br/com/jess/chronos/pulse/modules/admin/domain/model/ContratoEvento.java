package br.com.jess.chronos.pulse.modules.admin.domain.model;

import java.time.Instant;
import java.util.UUID;

public class ContratoEvento {

    private final UUID id;
    private final UUID contratoId;
    private String tipo;
    private String descricao;
    private final UUID criadoPor;
    private final Instant criadoEm;

    public ContratoEvento(UUID id, UUID contratoId, String tipo, String descricao, UUID criadoPor) {
        this.id = id != null ? id : UUID.randomUUID();
        this.contratoId = contratoId;
        this.tipo = tipo;
        this.descricao = descricao;
        this.criadoPor = criadoPor;
        this.criadoEm = Instant.now();
    }

    public UUID getId() { return id; }
    public UUID getContratoId() { return contratoId; }
    public String getTipo() { return tipo; }
    public String getDescricao() { return descricao; }
    public UUID getCriadoPor() { return criadoPor; }
    public Instant getCriadoEm() { return criadoEm; }
}
