package br.com.jess.chronos.pulse.modules.auth.domain.model;

import java.time.Instant;
import java.util.UUID;

public class CpcUsuario {

    private final UUID id;
    private final UUID cpcId;
    private final String cpf;
    private final String nome;
    private String emailCorporativo;
    private String emailPessoal;
    private String apelido;
    private String celular;
    private final String senhaHash;
    private final Role role;
    private final UUID tenantId;
    private final boolean acessoEstoque;
    private final boolean ativo;
    private final Instant criadoEm;

    public CpcUsuario(UUID id, UUID cpcId, String cpf, String nome, String emailCorporativo,
                      String senhaHash, Role role, UUID tenantId) {
        this(id, cpcId, cpf, nome, emailCorporativo, senhaHash, role, tenantId,
                role == Role.ADMIN_PLATAFORMA || role == Role.ADMIN_EMPRESA || role == Role.GESTOR_RH);
    }

    public CpcUsuario(UUID id, UUID cpcId, String cpf, String nome, String emailCorporativo,
                      String senhaHash, Role role, UUID tenantId, boolean acessoEstoque) {
        this.id = id != null ? id : UUID.randomUUID();
        this.cpcId = cpcId != null ? cpcId : UUID.randomUUID();
        this.cpf = cpf;
        this.nome = nome;
        this.emailCorporativo = emailCorporativo;
        this.senhaHash = senhaHash;
        this.role = role;
        this.tenantId = tenantId;
        this.acessoEstoque = (role == Role.ADMIN_PLATAFORMA || role == Role.ADMIN_EMPRESA || role == Role.GESTOR_RH) || acessoEstoque;
        this.ativo = true;
        this.criadoEm = Instant.now();
    }

    public void atualizarDadosPessoais(String apelido, String celular, String emailPessoal) {
        this.apelido = apelido;
        this.celular = celular;
        this.emailPessoal = emailPessoal;
    }

    public UUID getId() { return id; }
    public UUID getCpcId() { return cpcId; }
    public String getCpf() { return cpf; }
    public String getNome() { return nome; }
    public String getEmailCorporativo() { return emailCorporativo; }
    public String getEmailPessoal() { return emailPessoal; }
    public String getApelido() { return apelido; }
    public String getCelular() { return celular; }
    public String getSenhaHash() { return senhaHash; }
    public Role getRole() { return role; }
    public UUID getTenantId() { return tenantId; }
    public boolean isAcessoEstoque() { return acessoEstoque; }
    public boolean isAtivo() { return ativo; }
    public Instant getCriadoEm() { return criadoEm; }
}
