package br.com.jess.chronos.pulse.modules.empresa.domain.model;

import java.time.Instant;
import java.util.UUID;

public class Empresa {

    private final UUID id;
    private final String cnpj;
    private String nome;
    private boolean ativo;
    private final Instant criadoEm;

    public Empresa(UUID id, String cnpj, String nome) {
        this.id = id != null ? id : UUID.randomUUID();
        this.cnpj = cnpj;
        this.nome = nome;
        this.ativo = true;
        this.criadoEm = Instant.now();
    }

    public UUID getId() { return id; }
    public String getCnpj() { return cnpj; }
    public String getNome() { return nome; }
    public boolean isAtivo() { return ativo; }
    public Instant getCriadoEm() { return criadoEm; }
}
