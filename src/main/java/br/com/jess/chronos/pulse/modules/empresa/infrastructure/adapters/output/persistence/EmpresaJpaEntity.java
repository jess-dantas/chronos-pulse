package br.com.jess.chronos.pulse.modules.empresa.infrastructure.adapters.output.persistence;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "tenant")
public class EmpresaJpaEntity {

    @Id private UUID id;
    @Column(unique = true, nullable = false) private String cnpj;
    @Column(nullable = false) private String nome;
    private boolean ativo;
    @Column(name = "criado_em") private Instant criadoEm;

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public String getCnpj() { return cnpj; }
    public void setCnpj(String cnpj) { this.cnpj = cnpj; }
    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }
    public boolean isAtivo() { return ativo; }
    public void setAtivo(boolean ativo) { this.ativo = ativo; }
    public Instant getCriadoEm() { return criadoEm; }
    public void setCriadoEm(Instant criadoEm) { this.criadoEm = criadoEm; }
}
