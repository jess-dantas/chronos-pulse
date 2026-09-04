package br.com.jess.chronos.pulse.modules.auth.infrastructure.adapters.output.persistence;

import br.com.jess.chronos.pulse.modules.auth.domain.model.Role;
import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "cpc_usuario")
public class CpcUsuarioJpaEntity {

    @Id private UUID id;
    @Column(name = "cpc_id", unique = true, nullable = false) private UUID cpcId;
    @Column(unique = true, nullable = false) private String cpf;
    @Column(nullable = false) private String nome;
    @Column(name = "email_corporativo") private String emailCorporativo;
    @Column(name = "email_pessoal") private String emailPessoal;
    private String apelido;
    private String celular;
    @Column(name = "foto", columnDefinition = "TEXT") private String foto;
    @Column(name = "senha_hash", nullable = false) private String senhaHash;
    @Enumerated(EnumType.STRING) @Column(nullable = false) private Role role;
    @Column(name = "tenant_id") private UUID tenantId;
    @Column(name = "acesso_estoque") private boolean acessoEstoque;
    private boolean ativo;
    @Column(name = "criado_em") private Instant criadoEm;

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public UUID getCpcId() { return cpcId; }
    public void setCpcId(UUID cpcId) { this.cpcId = cpcId; }
    public String getCpf() { return cpf; }
    public void setCpf(String cpf) { this.cpf = cpf; }
    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }
    public String getEmailCorporativo() { return emailCorporativo; }
    public void setEmailCorporativo(String emailCorporativo) { this.emailCorporativo = emailCorporativo; }
    public String getEmailPessoal() { return emailPessoal; }
    public void setEmailPessoal(String emailPessoal) { this.emailPessoal = emailPessoal; }
    public String getApelido() { return apelido; }
    public void setApelido(String apelido) { this.apelido = apelido; }
    public String getCelular() { return celular; }
    public void setCelular(String celular) { this.celular = celular; }
    public String getFoto() { return foto; }
    public void setFoto(String foto) { this.foto = foto; }
    public String getSenhaHash() { return senhaHash; }
    public void setSenhaHash(String senhaHash) { this.senhaHash = senhaHash; }
    public Role getRole() { return role; }
    public void setRole(Role role) { this.role = role; }
    public UUID getTenantId() { return tenantId; }
    public void setTenantId(UUID tenantId) { this.tenantId = tenantId; }
    public boolean isAcessoEstoque() { return acessoEstoque; }
    public void setAcessoEstoque(boolean acessoEstoque) { this.acessoEstoque = acessoEstoque; }
    public boolean isAtivo() { return ativo; }
    public void setAtivo(boolean ativo) { this.ativo = ativo; }
    public Instant getCriadoEm() { return criadoEm; }
    public void setCriadoEm(Instant criadoEm) { this.criadoEm = criadoEm; }
}
