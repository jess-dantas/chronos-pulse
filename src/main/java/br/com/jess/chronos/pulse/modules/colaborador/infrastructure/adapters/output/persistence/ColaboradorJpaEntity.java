package br.com.jess.chronos.pulse.modules.colaborador.infrastructure.adapters.output.persistence;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "colaborador")
public class ColaboradorJpaEntity {

    @Id private UUID id;
    @Column(name = "cpc_usuario_id", nullable = false) private UUID cpcUsuarioId;
    @Column(name = "tenant_id", nullable = false) private UUID tenantId;
    private String matricula;
    private String cargo;
    private String departamento;
    @Column(name = "data_nascimento") private LocalDate dataNascimento;
    @Column(name = "data_admissao", nullable = false) private LocalDate dataAdmissao;
    @Column(name = "configuracao_jornada_id") private UUID configuracaoJornadaId;
    private boolean ativo;

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public UUID getCpcUsuarioId() { return cpcUsuarioId; }
    public void setCpcUsuarioId(UUID cpcUsuarioId) { this.cpcUsuarioId = cpcUsuarioId; }
    public UUID getTenantId() { return tenantId; }
    public void setTenantId(UUID tenantId) { this.tenantId = tenantId; }
    public String getMatricula() { return matricula; }
    public void setMatricula(String matricula) { this.matricula = matricula; }
    public String getCargo() { return cargo; }
    public void setCargo(String cargo) { this.cargo = cargo; }
    public String getDepartamento() { return departamento; }
    public void setDepartamento(String departamento) { this.departamento = departamento; }
    public LocalDate getDataNascimento() { return dataNascimento; }
    public void setDataNascimento(LocalDate dataNascimento) { this.dataNascimento = dataNascimento; }
    public LocalDate getDataAdmissao() { return dataAdmissao; }
    public void setDataAdmissao(LocalDate dataAdmissao) { this.dataAdmissao = dataAdmissao; }
    public UUID getConfiguracaoJornadaId() { return configuracaoJornadaId; }
    public void setConfiguracaoJornadaId(UUID configuracaoJornadaId) { this.configuracaoJornadaId = configuracaoJornadaId; }
    public boolean isAtivo() { return ativo; }
    public void setAtivo(boolean ativo) { this.ativo = ativo; }
}
