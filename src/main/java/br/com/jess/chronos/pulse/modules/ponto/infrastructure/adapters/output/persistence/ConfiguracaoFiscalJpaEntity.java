package br.com.jess.chronos.pulse.modules.ponto.infrastructure.adapters.output.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.util.UUID;

@Entity
@Table(name = "configuracao_fiscal")
public class ConfiguracaoFiscalJpaEntity {

    @Id
    @Column(name = "tenant_id", nullable = false)
    private UUID tenantId;
    @Column(name = "numero_registro_inpi")
    private String numeroRegistroInpi;
    @Column(name = "cnpj_desenvolvedor")
    private String cnpjDesenvolvedor;
    @Column(name = "prtp_nome")
    private String prtpNome;
    @Column(name = "prtp_versao")
    private String prtpVersao;
    @Column(name = "prtp_razao_desenv")
    private String prtpRazaoDesenv;
    @Column(name = "prtp_email")
    private String prtpEmail;
    private String cno;

    public UUID getTenantId() { return tenantId; }
    public void setTenantId(UUID tenantId) { this.tenantId = tenantId; }
    public String getNumeroRegistroInpi() { return numeroRegistroInpi; }
    public void setNumeroRegistroInpi(String numeroRegistroInpi) { this.numeroRegistroInpi = numeroRegistroInpi; }
    public String getCnpjDesenvolvedor() { return cnpjDesenvolvedor; }
    public void setCnpjDesenvolvedor(String cnpjDesenvolvedor) { this.cnpjDesenvolvedor = cnpjDesenvolvedor; }
    public String getPrtpNome() { return prtpNome; }
    public void setPrtpNome(String prtpNome) { this.prtpNome = prtpNome; }
    public String getPrtpVersao() { return prtpVersao; }
    public void setPrtpVersao(String prtpVersao) { this.prtpVersao = prtpVersao; }
    public String getPrtpRazaoDesenv() { return prtpRazaoDesenv; }
    public void setPrtpRazaoDesenv(String prtpRazaoDesenv) { this.prtpRazaoDesenv = prtpRazaoDesenv; }
    public String getPrtpEmail() { return prtpEmail; }
    public void setPrtpEmail(String prtpEmail) { this.prtpEmail = prtpEmail; }
    public String getCno() { return cno; }
    public void setCno(String cno) { this.cno = cno; }
}