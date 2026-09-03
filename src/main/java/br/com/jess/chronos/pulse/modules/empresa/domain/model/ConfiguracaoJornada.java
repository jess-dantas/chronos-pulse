package br.com.jess.chronos.pulse.modules.empresa.domain.model;

import java.util.UUID;

public class ConfiguracaoJornada {

    private final UUID id;
    private final UUID tenantId;
    private final String nome;
    private final int cargaHorariaDiariaMinutos;
    private final boolean exigeIntervalo;
    private final Integer intervaloMinimoMinutos;
    private final int toleranciaEntradaMinutos;
    private final int toleranciaSaidaMinutos;
    private final int interjornadaMinimaMinutos;

    public ConfiguracaoJornada(UUID id, UUID tenantId, String nome, int cargaHorariaDiariaMinutos,
                                int toleranciaEntradaMinutos, int toleranciaSaidaMinutos,
                                int interjornadaMinimaMinutos) {
        this.id = id != null ? id : UUID.randomUUID();
        this.tenantId = tenantId;
        this.nome = nome;
        this.cargaHorariaDiariaMinutos = cargaHorariaDiariaMinutos;
        this.exigeIntervalo = cargaHorariaDiariaMinutos > 360; // CLT: acima de 6h exige intervalo
        this.intervaloMinimoMinutos = this.exigeIntervalo ? 60 : null; // CLT: mínimo 1h
        this.toleranciaEntradaMinutos = toleranciaEntradaMinutos;
        this.toleranciaSaidaMinutos = toleranciaSaidaMinutos;
        this.interjornadaMinimaMinutos = interjornadaMinimaMinutos; // CLT Art. 66: mínimo 660min (11h)
    }

    public UUID getId() { return id; }
    public UUID getTenantId() { return tenantId; }
    public String getNome() { return nome; }
    public int getCargaHorariaDiariaMinutos() { return cargaHorariaDiariaMinutos; }
    public boolean isExigeIntervalo() { return exigeIntervalo; }
    public Integer getIntervaloMinimoMinutos() { return intervaloMinimoMinutos; }
    public int getToleranciaEntradaMinutos() { return toleranciaEntradaMinutos; }
    public int getToleranciaSaidaMinutos() { return toleranciaSaidaMinutos; }
    public int getInterjornadaMinimaMinutos() { return interjornadaMinimaMinutos; }
}
