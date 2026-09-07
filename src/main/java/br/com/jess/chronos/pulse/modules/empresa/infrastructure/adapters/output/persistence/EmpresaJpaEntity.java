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
    @Column(name = "responsavel_nome") private String responsavelNome;
    @Column(name = "responsavel_cpf") private String responsavelCpf;
    @Column(name = "responsavel_email") private String responsavelEmail;
    @Column(name = "responsavel_celular") private String responsavelCelular;
    @Column(name = "responsavel_telefone") private String responsavelTelefone;
    @Column(name = "endereco_logradouro") private String enderecoLogradouro;
    @Column(name = "endereco_numero") private String enderecoNumero;
    @Column(name = "endereco_complemento") private String enderecoComplemento;
    @Column(name = "endereco_bairro") private String enderecoBairro;
    @Column(name = "endereco_cidade") private String enderecoCidade;
    @Column(name = "endereco_uf") private String enderecoUf;
    @Column(name = "endereco_cep") private String enderecoCep;
    private boolean ativo;
    @Column(name = "criado_em") private Instant criadoEm;

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public String getCnpj() { return cnpj; }
    public void setCnpj(String cnpj) { this.cnpj = cnpj; }
    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }
    public String getResponsavelNome() { return responsavelNome; }
    public void setResponsavelNome(String responsavelNome) { this.responsavelNome = responsavelNome; }
    public String getResponsavelCpf() { return responsavelCpf; }
    public void setResponsavelCpf(String responsavelCpf) { this.responsavelCpf = responsavelCpf; }
    public String getResponsavelEmail() { return responsavelEmail; }
    public void setResponsavelEmail(String responsavelEmail) { this.responsavelEmail = responsavelEmail; }
    public String getResponsavelCelular() { return responsavelCelular; }
    public void setResponsavelCelular(String responsavelCelular) { this.responsavelCelular = responsavelCelular; }
    public String getResponsavelTelefone() { return responsavelTelefone; }
    public void setResponsavelTelefone(String responsavelTelefone) { this.responsavelTelefone = responsavelTelefone; }
    public String getEnderecoLogradouro() { return enderecoLogradouro; }
    public void setEnderecoLogradouro(String enderecoLogradouro) { this.enderecoLogradouro = enderecoLogradouro; }
    public String getEnderecoNumero() { return enderecoNumero; }
    public void setEnderecoNumero(String enderecoNumero) { this.enderecoNumero = enderecoNumero; }
    public String getEnderecoComplemento() { return enderecoComplemento; }
    public void setEnderecoComplemento(String enderecoComplemento) { this.enderecoComplemento = enderecoComplemento; }
    public String getEnderecoBairro() { return enderecoBairro; }
    public void setEnderecoBairro(String enderecoBairro) { this.enderecoBairro = enderecoBairro; }
    public String getEnderecoCidade() { return enderecoCidade; }
    public void setEnderecoCidade(String enderecoCidade) { this.enderecoCidade = enderecoCidade; }
    public String getEnderecoUf() { return enderecoUf; }
    public void setEnderecoUf(String enderecoUf) { this.enderecoUf = enderecoUf; }
    public String getEnderecoCep() { return enderecoCep; }
    public void setEnderecoCep(String enderecoCep) { this.enderecoCep = enderecoCep; }
    public boolean isAtivo() { return ativo; }
    public void setAtivo(boolean ativo) { this.ativo = ativo; }
    public Instant getCriadoEm() { return criadoEm; }
    public void setCriadoEm(Instant criadoEm) { this.criadoEm = criadoEm; }
}
