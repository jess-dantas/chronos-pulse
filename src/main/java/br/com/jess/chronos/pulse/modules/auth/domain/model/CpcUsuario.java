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
    private String foto;
    private final String senhaHash;
    private final Role role;
    private final UUID tenantId;
    private final boolean acessoEstoque;
    private final boolean acessoPatrimonio;
    private final boolean acessoFrota;
    private final boolean acessoProtocolo;
    private final boolean ativo;
    private final Instant criadoEm;

    public CpcUsuario(UUID id, UUID cpcId, String cpf, String nome, String emailCorporativo,
                      String senhaHash, Role role, UUID tenantId) {
        this(id, cpcId, cpf, nome, emailCorporativo, senhaHash, role, tenantId,
                role == Role.ADMIN_PLATAFORMA || role == Role.ADMIN_EMPRESA || role == Role.GESTOR_RH);
    }

    public CpcUsuario(UUID id, UUID cpcId, String cpf, String nome, String emailCorporativo,
                      String senhaHash, Role role, UUID tenantId, boolean acessoEstoque) {
        this(id, cpcId, cpf, nome, emailCorporativo, senhaHash, role, tenantId, acessoEstoque, null);
    }

    public CpcUsuario(UUID id, UUID cpcId, String cpf, String nome, String emailCorporativo,
                      String senhaHash, Role role, UUID tenantId, boolean acessoEstoque, String foto) {
        this(id, cpcId, cpf, nome, emailCorporativo, senhaHash, role, tenantId,
                acessoEstoque, false, false, false, foto);
    }

    public CpcUsuario(UUID id, UUID cpcId, String cpf, String nome, String emailCorporativo,
                      String senhaHash, Role role, UUID tenantId,
                      boolean acessoEstoque, boolean acessoPatrimonio,
                      boolean acessoFrota, boolean acessoProtocolo, String foto) {
        boolean admin = role == Role.ADMIN_PLATAFORMA || role == Role.ADMIN_EMPRESA || role == Role.GESTOR_RH;
        this.id = id != null ? id : UUID.randomUUID();
        this.cpcId = cpcId != null ? cpcId : UUID.randomUUID();
        this.cpf = cpf;
        this.nome = nome;
        this.emailCorporativo = emailCorporativo;
        this.senhaHash = senhaHash;
        this.role = role;
        this.tenantId = tenantId;
        this.acessoEstoque = admin || acessoEstoque;
        this.acessoPatrimonio = admin || acessoPatrimonio;
        this.acessoFrota = admin || acessoFrota;
        this.acessoProtocolo = admin || acessoProtocolo;
        this.foto = foto;
        this.ativo = true;
        this.criadoEm = Instant.now();
    }

    /**
     * Construtor completo usado pela anonimização LGPD (art. 18, VI) para
     * preservar a identidade da entidade (cpf/hashes) removendo os dados
     * pessoais extensos e desativando a conta.
     */
    public CpcUsuario(UUID id, UUID cpcId, String cpf, String nome, String emailCorporativo,
                      String emailPessoal, String apelido, String celular, String foto,
                      String senhaHash, Role role, UUID tenantId,
                      boolean acessoEstoque, boolean acessoPatrimonio,
                      boolean acessoFrota, boolean acessoProtocolo,
                      boolean ativo, Instant criadoEm) {
        this.id = id;
        this.cpcId = cpcId;
        this.cpf = cpf;
        this.nome = nome;
        this.emailCorporativo = emailCorporativo;
        this.emailPessoal = emailPessoal;
        this.apelido = apelido;
        this.celular = celular;
        this.foto = foto;
        this.senhaHash = senhaHash;
        this.role = role;
        this.tenantId = tenantId;
        this.acessoEstoque = acessoEstoque;
        this.acessoPatrimonio = acessoPatrimonio;
        this.acessoFrota = acessoFrota;
        this.acessoProtocolo = acessoProtocolo;
        this.ativo = ativo;
        this.criadoEm = criadoEm;
    }

    public CpcUsuario anonimizar() {
        return new CpcUsuario(id, cpcId, cpf, "Usuário Removido (LGPD)", null,
                null, null, null, null,
                senhaHash, role, tenantId,
                false, false, false, false,
                false, criadoEm);
    }

    public void atualizarDadosPessoais(String apelido, String celular, String emailPessoal) {
        this.apelido = apelido;
        this.celular = celular;
        this.emailPessoal = emailPessoal;
    }

    public void atualizarFoto(String foto) {
        this.foto = foto;
    }

    public CpcUsuario comSenha(String novaSenhaHash) {
        CpcUsuario copia = new CpcUsuario(id, cpcId, cpf, nome, emailCorporativo,
                novaSenhaHash, role, tenantId,
                acessoEstoque, acessoPatrimonio, acessoFrota, acessoProtocolo, foto);
        copia.emailPessoal = this.emailPessoal;
        copia.apelido = this.apelido;
        copia.celular = this.celular;
        return copia;
    }

    public CpcUsuario comFoto(String novaFoto) {
        CpcUsuario copia = new CpcUsuario(id, cpcId, cpf, nome, emailCorporativo,
                senhaHash, role, tenantId,
                acessoEstoque, acessoPatrimonio, acessoFrota, acessoProtocolo, novaFoto);
        copia.emailPessoal = this.emailPessoal;
        copia.apelido = this.apelido;
        copia.celular = this.celular;
        return copia;
    }

    public UUID getId() { return id; }
    public UUID getCpcId() { return cpcId; }
    public String getCpf() { return cpf; }
    public String getNome() { return nome; }
    public String getEmailCorporativo() { return emailCorporativo; }
    public String getEmailPessoal() { return emailPessoal; }
    public String getApelido() { return apelido; }
    public String getCelular() { return celular; }
    public String getFoto() { return foto; }
    public String getSenhaHash() { return senhaHash; }
    public Role getRole() { return role; }
    public UUID getTenantId() { return tenantId; }
    public boolean isAcessoEstoque() { return acessoEstoque; }
    public boolean isAcessoPatrimonio() { return acessoPatrimonio; }
    public boolean isAcessoFrota() { return acessoFrota; }
    public boolean isAcessoProtocolo() { return acessoProtocolo; }
    public boolean isAtivo() { return ativo; }
    public Instant getCriadoEm() { return criadoEm; }
}