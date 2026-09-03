package br.com.jess.chronos.pulse.modules.ponto.infrastructure.adapters.output.persistence;

import br.com.jess.chronos.pulse.modules.ponto.domain.model.TipoRegistro;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "registro_ponto")
public class RegistroPontoJpaEntity {

    @Id
    private UUID id;
    private UUID colaboradorId;
    private UUID tenantId;
    private Instant dataHoraDispositivo;
    private Instant dataHoraServidor;
    @Enumerated(EnumType.STRING)
    private TipoRegistro tipoRegistro;
    private BigDecimal latitude;
    private BigDecimal longitude;
    private BigDecimal precisaoGps;
    private String fotoUrl;
    private String hashIntegridade;
    private Boolean sincronizadoOffline;
    @Column(unique = true)
    private Long nsr;

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public UUID getColaboradorId() { return colaboradorId; }
    public void setColaboradorId(UUID colaboradorId) { this.colaboradorId = colaboradorId; }
    public UUID getTenantId() { return tenantId; }
    public void setTenantId(UUID tenantId) { this.tenantId = tenantId; }
    public Instant getDataHoraDispositivo() { return dataHoraDispositivo; }
    public void setDataHoraDispositivo(Instant v) { this.dataHoraDispositivo = v; }
    public Instant getDataHoraServidor() { return dataHoraServidor; }
    public void setDataHoraServidor(Instant v) { this.dataHoraServidor = v; }
    public TipoRegistro getTipoRegistro() { return tipoRegistro; }
    public void setTipoRegistro(TipoRegistro v) { this.tipoRegistro = v; }
    public BigDecimal getLatitude() { return latitude; }
    public void setLatitude(BigDecimal v) { this.latitude = v; }
    public BigDecimal getLongitude() { return longitude; }
    public void setLongitude(BigDecimal v) { this.longitude = v; }
    public BigDecimal getPrecisaoGps() { return precisaoGps; }
    public void setPrecisaoGps(BigDecimal v) { this.precisaoGps = v; }
    public String getFotoUrl() { return fotoUrl; }
    public void setFotoUrl(String v) { this.fotoUrl = v; }
    public String getHashIntegridade() { return hashIntegridade; }
    public void setHashIntegridade(String v) { this.hashIntegridade = v; }
    public Boolean getSincronizadoOffline() { return sincronizadoOffline; }
    public void setSincronizadoOffline(Boolean v) { this.sincronizadoOffline = v; }
    public Long getNsr() { return nsr; }
    public void setNsr(Long v) { this.nsr = v; }
}
