package br.com.jess.chronos.pulse.modules.lead.domain.model;

import java.time.Instant;
import java.util.UUID;

public class LeadEmpresa {

    private UUID id;
    private String cnpj;
    private String razaoSocial;
    private String contatoNome;
    private String contatoEmail;
    private String contatoTelefone;
    private String contatoCelular;
    private String enderecoLogradouro;
    private String enderecoNumero;
    private String enderecoComplemento;
    private String enderecoBairro;
    private String enderecoCidade;
    private String enderecoUf;
    private String enderecoCep;
    private String observacao;
    private LeadEmpresaStatus status;
    private Instant criadoEm;

    public LeadEmpresa(UUID id, String cnpj, String razaoSocial, String contatoNome, String contatoEmail,
                       String contatoTelefone, String contatoCelular, String enderecoLogradouro,
                       String enderecoNumero, String enderecoComplemento, String enderecoBairro,
                       String enderecoCidade, String enderecoUf, String enderecoCep, String observacao) {
        this(id, cnpj, razaoSocial, contatoNome, contatoEmail, contatoTelefone, contatoCelular,
                enderecoLogradouro, enderecoNumero, enderecoComplemento, enderecoBairro,
                enderecoCidade, enderecoUf, enderecoCep, observacao, LeadEmpresaStatus.NOVO, null);
    }

    public LeadEmpresa(UUID id, String cnpj, String razaoSocial, String contatoNome, String contatoEmail,
                       String contatoTelefone, String contatoCelular, String enderecoLogradouro,
                       String enderecoNumero, String enderecoComplemento, String enderecoBairro,
                       String enderecoCidade, String enderecoUf, String enderecoCep, String observacao,
                       LeadEmpresaStatus status, Instant criadoEm) {
        this.id = id != null ? id : UUID.randomUUID();
        this.cnpj = cnpj;
        this.razaoSocial = razaoSocial;
        this.contatoNome = contatoNome;
        this.contatoEmail = contatoEmail;
        this.contatoTelefone = contatoTelefone;
        this.contatoCelular = contatoCelular;
        this.enderecoLogradouro = enderecoLogradouro;
        this.enderecoNumero = enderecoNumero;
        this.enderecoComplemento = enderecoComplemento;
        this.enderecoBairro = enderecoBairro;
        this.enderecoCidade = enderecoCidade;
        this.enderecoUf = enderecoUf;
        this.enderecoCep = enderecoCep;
        this.observacao = observacao;
        this.status = status != null ? status : LeadEmpresaStatus.NOVO;
        this.criadoEm = criadoEm != null ? criadoEm : Instant.now();
    }

    public LeadEmpresa criarComCnpj(String cnpj) {
        return new LeadEmpresa(this.id, cnpj, razaoSocial, contatoNome, contatoEmail, contatoTelefone,
                contatoCelular, enderecoLogradouro, enderecoNumero, enderecoComplemento, enderecoBairro,
                enderecoCidade, enderecoUf, enderecoCep, observacao, status, criadoEm);
    }

    public UUID getId() { return id; }
    public String getCnpj() { return cnpj; }
    public String getRazaoSocial() { return razaoSocial; }
    public String getContatoNome() { return contatoNome; }
    public String getContatoEmail() { return contatoEmail; }
    public String getContatoTelefone() { return contatoTelefone; }
    public String getContatoCelular() { return contatoCelular; }
    public String getEnderecoLogradouro() { return enderecoLogradouro; }
    public String getEnderecoNumero() { return enderecoNumero; }
    public String getEnderecoComplemento() { return enderecoComplemento; }
    public String getEnderecoBairro() { return enderecoBairro; }
    public String getEnderecoCidade() { return enderecoCidade; }
    public String getEnderecoUf() { return enderecoUf; }
    public String getEnderecoCep() { return enderecoCep; }
    public String getObservacao() { return observacao; }
    public LeadEmpresaStatus getStatus() { return status; }
    public Instant getCriadoEm() { return criadoEm; }
}