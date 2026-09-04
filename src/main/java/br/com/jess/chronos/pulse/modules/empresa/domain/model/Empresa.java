package br.com.jess.chronos.pulse.modules.empresa.domain.model;

import java.time.Instant;
import java.util.UUID;

public class Empresa {

    private final UUID id;
    private final String cnpj;
    private String nome;
    private String responsavelNome;
    private String responsavelCpf;
    private String responsavelEmail;
    private String responsavelCelular;
    private boolean ativo;
    private final Instant criadoEm;

    public Empresa(UUID id, String cnpj, String nome) {
        this(id, cnpj, nome, null, null, null, null);
    }

    public Empresa(UUID id, String cnpj, String nome, String responsavelNome,
                   String responsavelCpf, String responsavelEmail, String responsavelCelular) {
        this.id = id != null ? id : UUID.randomUUID();
        this.cnpj = cnpj;
        this.nome = nome;
        this.responsavelNome = responsavelNome;
        this.responsavelCpf = responsavelCpf;
        this.responsavelEmail = responsavelEmail;
        this.responsavelCelular = responsavelCelular;
        this.ativo = true;
        this.criadoEm = Instant.now();
    }

    public UUID getId() { return id; }
    public String getCnpj() { return cnpj; }
    public String getNome() { return nome; }
    public String getResponsavelNome() { return responsavelNome; }
    public String getResponsavelCpf() { return responsavelCpf; }
    public String getResponsavelEmail() { return responsavelEmail; }
    public String getResponsavelCelular() { return responsavelCelular; }
    public boolean isAtivo() { return ativo; }
    public Instant getCriadoEm() { return criadoEm; }
}
