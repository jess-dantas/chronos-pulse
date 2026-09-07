package br.com.jess.chronos.pulse.modules.colaborador.domain.model;

import java.time.LocalDate;
import java.util.UUID;

public class Colaborador {

    private final UUID id;
    private final UUID cpcUsuarioId;
    private final UUID tenantId;
    private String matricula;
    private String cargo;
    private String departamento;
    private final LocalDate dataNascimento;
    private final LocalDate dataAdmissao;
    private final LocalDate dataDesligamento;
    private UUID configuracaoJornadaId;
    private boolean ativo;

    public Colaborador(UUID id, UUID cpcUsuarioId, UUID tenantId, String matricula,
                       String cargo, String departamento, LocalDate dataNascimento,
                       LocalDate dataAdmissao, UUID configuracaoJornadaId) {
        this(id, cpcUsuarioId, tenantId, matricula, cargo, departamento,
                dataNascimento, dataAdmissao, configuracaoJornadaId, null);
    }

    public Colaborador(UUID id, UUID cpcUsuarioId, UUID tenantId, String matricula,
                       String cargo, String departamento, LocalDate dataNascimento,
                       LocalDate dataAdmissao, UUID configuracaoJornadaId,
                       LocalDate dataDesligamento) {
        this.id = id != null ? id : UUID.randomUUID();
        this.cpcUsuarioId = cpcUsuarioId;
        this.tenantId = tenantId;
        this.matricula = matricula;
        this.cargo = cargo;
        this.departamento = departamento;
        this.dataNascimento = dataNascimento;
        this.dataAdmissao = dataAdmissao;
        this.dataDesligamento = dataDesligamento;
        this.configuracaoJornadaId = configuracaoJornadaId;
        this.ativo = true;
    }

    public UUID getId() { return id; }
    public UUID getCpcUsuarioId() { return cpcUsuarioId; }
    public UUID getTenantId() { return tenantId; }
    public String getMatricula() { return matricula; }
    public String getCargo() { return cargo; }
    public String getDepartamento() { return departamento; }
    public LocalDate getDataNascimento() { return dataNascimento; }
    public LocalDate getDataAdmissao() { return dataAdmissao; }
    public LocalDate getDataDesligamento() { return dataDesligamento; }
    public UUID getConfiguracaoJornadaId() { return configuracaoJornadaId; }
    public boolean isAtivo() { return ativo; }
}
