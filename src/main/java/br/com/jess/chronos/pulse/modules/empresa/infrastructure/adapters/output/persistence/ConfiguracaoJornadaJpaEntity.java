package br.com.jess.chronos.pulse.modules.empresa.infrastructure.adapters.output.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.util.UUID;

@Entity
@Table(name = "configuracao_jornada")
public class ConfiguracaoJornadaJpaEntity {

    @Id
    private UUID id;
    @Column(name = "tenant_id", nullable = false)
    private UUID tenantId;
    @Column(nullable = false)
    private String nome;
    @Column(name = "carga_horaria_diaria_minutos", nullable = false)
    private int cargaHorariaDiariaMinutos;
    @Column(name = "exige_intervalo", nullable = false)
    private boolean exigeIntervalo;
    @Column(name = "intervalo_minimo_minutos")
    private Integer intervaloMinimoMinutos;
    @Column(name = "tolerancia_entrada_minutos")
    private int toleranciaEntradaMinutos;
    @Column(name = "tolerancia_saida_minutos")
    private int toleranciaSaidaMinutos;
    @Column(name = "interjornada_minima_minutos")
    private int interjornadaMinimaMinutos;

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public UUID getTenantId() { return tenantId; }
    public void setTenantId(UUID tenantId) { this.tenantId = tenantId; }
    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }
    public int getCargaHorariaDiariaMinutos() { return cargaHorariaDiariaMinutos; }
    public void setCargaHorariaDiariaMinutos(int cargaHorariaDiariaMinutos) { this.cargaHorariaDiariaMinutos = cargaHorariaDiariaMinutos; }
    public boolean isExigeIntervalo() { return exigeIntervalo; }
    public void setExigeIntervalo(boolean exigeIntervalo) { this.exigeIntervalo = exigeIntervalo; }
    public Integer getIntervaloMinimoMinutos() { return intervaloMinimoMinutos; }
    public void setIntervaloMinimoMinutos(Integer intervaloMinimoMinutos) { this.intervaloMinimoMinutos = intervaloMinimoMinutos; }
    public int getToleranciaEntradaMinutos() { return toleranciaEntradaMinutos; }
    public void setToleranciaEntradaMinutos(int toleranciaEntradaMinutos) { this.toleranciaEntradaMinutos = toleranciaEntradaMinutos; }
    public int getToleranciaSaidaMinutos() { return toleranciaSaidaMinutos; }
    public void setToleranciaSaidaMinutos(int toleranciaSaidaMinutos) { this.toleranciaSaidaMinutos = toleranciaSaidaMinutos; }
    public int getInterjornadaMinimaMinutos() { return interjornadaMinimaMinutos; }
    public void setInterjornadaMinimaMinutos(int interjornadaMinimaMinutos) { this.interjornadaMinimaMinutos = interjornadaMinimaMinutos; }
}