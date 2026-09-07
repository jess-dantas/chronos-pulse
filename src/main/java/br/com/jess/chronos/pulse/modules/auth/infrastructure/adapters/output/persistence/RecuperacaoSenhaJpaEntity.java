package br.com.jess.chronos.pulse.modules.auth.infrastructure.adapters.output.persistence;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "recuperacao_senha")
public class RecuperacaoSenhaJpaEntity {

    @Id private UUID id;
    @Column(nullable = false) private String cpf;
    @Column(name = "codigo_hash", nullable = false) private String codigoHash;
    @Column(name = "expira_em", nullable = false) private Instant expiraEm;
    private boolean usado;
    @Column(name = "criado_em") private Instant criadoEm;

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public String getCpf() { return cpf; }
    public void setCpf(String cpf) { this.cpf = cpf; }
    public String getCodigoHash() { return codigoHash; }
    public void setCodigoHash(String codigoHash) { this.codigoHash = codigoHash; }
    public Instant getExpiraEm() { return expiraEm; }
    public void setExpiraEm(Instant expiraEm) { this.expiraEm = expiraEm; }
    public boolean isUsado() { return usado; }
    public void setUsado(boolean usado) { this.usado = usado; }
    public Instant getCriadoEm() { return criadoEm; }
    public void setCriadoEm(Instant criadoEm) { this.criadoEm = criadoEm; }
}