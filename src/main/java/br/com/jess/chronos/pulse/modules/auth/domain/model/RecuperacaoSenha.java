package br.com.jess.chronos.pulse.modules.auth.domain.model;

import java.time.Instant;
import java.util.UUID;

public class RecuperacaoSenha {

    public static final int MAX_TENTATIVAS = 5;

    private final UUID id;
    private final String cpf;
    private final String codigoHash;
    private final Instant expiraEm;
    private boolean usado;
    private int tentativas;
    private final Instant criadoEm;

    public RecuperacaoSenha(UUID id, String cpf, String codigoHash, Instant expiraEm, boolean usado, Instant criadoEm) {
        this(id, cpf, codigoHash, expiraEm, usado, 0, criadoEm);
    }

    public RecuperacaoSenha(UUID id, String cpf, String codigoHash, Instant expiraEm, boolean usado,
                            int tentativas, Instant criadoEm) {
        this.id = id != null ? id : UUID.randomUUID();
        this.cpf = cpf;
        this.codigoHash = codigoHash;
        this.expiraEm = expiraEm;
        this.usado = usado;
        this.tentativas = tentativas;
        this.criadoEm = criadoEm != null ? criadoEm : Instant.now();
    }

    public boolean isExpirada() {
        return expiraEm != null && expiraEm.isBefore(Instant.now());
    }

    public boolean isTentativasEsgotadas() {
        return this.tentativas >= MAX_TENTATIVAS;
    }

    public void marcarComoUsado() {
        this.usado = true;
    }

    public void registrarTentativa() {
        this.tentativas++;
    }

    public UUID getId() { return id; }
    public String getCpf() { return cpf; }
    public String getCodigoHash() { return codigoHash; }
    public Instant getExpiraEm() { return expiraEm; }
    public boolean isUsado() { return usado; }
    public int getTentativas() { return tentativas; }
    public Instant getCriadoEm() { return criadoEm; }
}