package br.com.jess.chronos.pulse.modules.auth.domain.model;

import java.time.Instant;
import java.util.UUID;

public class RecuperacaoSenha {

    private final UUID id;
    private final String cpf;
    private final String codigoHash;
    private final Instant expiraEm;
    private boolean usado;
    private final Instant criadoEm;

    public RecuperacaoSenha(UUID id, String cpf, String codigoHash, Instant expiraEm, boolean usado, Instant criadoEm) {
        this.id = id != null ? id : UUID.randomUUID();
        this.cpf = cpf;
        this.codigoHash = codigoHash;
        this.expiraEm = expiraEm;
        this.usado = usado;
        this.criadoEm = criadoEm != null ? criadoEm : Instant.now();
    }

    public boolean isExpirada() {
        return expiraEm != null && expiraEm.isBefore(Instant.now());
    }

    public void marcarComoUsado() {
        this.usado = true;
    }

    public UUID getId() { return id; }
    public String getCpf() { return cpf; }
    public String getCodigoHash() { return codigoHash; }
    public Instant getExpiraEm() { return expiraEm; }
    public boolean isUsado() { return usado; }
    public Instant getCriadoEm() { return criadoEm; }
}