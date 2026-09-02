package br.com.jess.chronos.pulse.modules.ponto.domain.model;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public class RegistroPonto {

    private UUID id;
    private UUID colaboradorId;
    private Instant dataHoraDispositivo;
    private Instant dataHoraServidor;
    private TipoRegistro tipoRegistro;
    private BigDecimal latitude;
    private BigDecimal longitude;
    private BigDecimal precisaoGps;
    private String fotoUrl;
    private String hashIntegridade;
    private Boolean sincronizadoOffline;
    private Long nsr;

    public RegistroPonto(UUID id, UUID colaboradorId, Instant dataHoraDispositivo, Instant dataHoraServidor,
                         TipoRegistro tipoRegistro, BigDecimal latitude, BigDecimal longitude,
                         BigDecimal precisaoGps, String fotoUrl, Boolean sincronizadoOffline, Long nsr) {
        this.id = id != null ? id : UUID.randomUUID();
        this.colaboradorId = colaboradorId;
        this.dataHoraDispositivo = dataHoraDispositivo;
        this.dataHoraServidor = dataHoraServidor != null ? dataHoraServidor : Instant.now();
        this.tipoRegistro = tipoRegistro;
        this.latitude = latitude;
        this.longitude = longitude;
        this.precisaoGps = precisaoGps;
        this.fotoUrl = fotoUrl;
        this.sincronizadoOffline = sincronizadoOffline != null ? sincronizadoOffline : false;
        this.nsr = nsr;
    }

    public UUID getId() { return id; }
    public UUID getColaboradorId() { return colaboradorId; }
    public Instant getDataHoraDispositivo() { return dataHoraDispositivo; }
    public Instant getDataHoraServidor() { return dataHoraServidor; }
    public TipoRegistro getTipoRegistro() { return tipoRegistro; }
    public BigDecimal getLatitude() { return latitude; }
    public BigDecimal getLongitude() { return longitude; }
    public BigDecimal getPrecisaoGps() { return precisaoGps; }
    public String getFotoUrl() { return fotoUrl; }
    public String getHashIntegridade() { return hashIntegridade; }
    public Boolean getSincronizadoOffline() { return sincronizadoOffline; }
    public Long getNsr() { return nsr; }

    public void atribuirHash(String hash) {
        this.hashIntegridade = hash;
    }
}