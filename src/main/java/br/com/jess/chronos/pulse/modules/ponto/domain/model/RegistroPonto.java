package br.com.jess.chronos.pulse.modules.ponto.domain.model;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public class RegistroPonto {

    private UUID id;
    private UUID colaboradorId;
    private UUID tenantId;
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
    private Long nsrLogico;
    private Boolean ajusteManual;
    private String justificativa;
    private String observacao;
    private AjusteStatus ajusteStatus;
    private String ajusteMotivoRejeicao;
    private UUID aprovadoPor;
    private Instant aprovadoEm;

    public RegistroPonto(UUID id, UUID colaboradorId, UUID tenantId, Instant dataHoraDispositivo,
                         Instant dataHoraServidor, TipoRegistro tipoRegistro, BigDecimal latitude,
                         BigDecimal longitude, BigDecimal precisaoGps, String fotoUrl,
                         Boolean sincronizadoOffline, Long nsr) {
        this(id, colaboradorId, tenantId, dataHoraDispositivo, dataHoraServidor, tipoRegistro,
                latitude, longitude, precisaoGps, fotoUrl, sincronizadoOffline, nsr, false, null, null);
    }

    public RegistroPonto(UUID id, UUID colaboradorId, UUID tenantId, Instant dataHoraDispositivo,
                         Instant dataHoraServidor, TipoRegistro tipoRegistro, BigDecimal latitude,
                         BigDecimal longitude, BigDecimal precisaoGps, String fotoUrl,
                         Boolean sincronizadoOffline, Long nsr, Boolean ajusteManual,
                         String justificativa, String observacao) {
        this(id, colaboradorId, tenantId, dataHoraDispositivo, dataHoraServidor, tipoRegistro,
                latitude, longitude, precisaoGps, fotoUrl, sincronizadoOffline, nsr, null, ajusteManual,
                justificativa, observacao, null, null, null, null);
    }

    public RegistroPonto(UUID id, UUID colaboradorId, UUID tenantId, Instant dataHoraDispositivo,
                         Instant dataHoraServidor, TipoRegistro tipoRegistro, BigDecimal latitude,
                         BigDecimal longitude, BigDecimal precisaoGps, String fotoUrl,
                         Boolean sincronizadoOffline, Long nsr, Long nsrLogico, Boolean ajusteManual,
                         String justificativa, String observacao, AjusteStatus ajusteStatus,
                         String ajusteMotivoRejeicao, UUID aprovadoPor, Instant aprovadoEm) {
        this.id = id != null ? id : UUID.randomUUID();
        this.colaboradorId = colaboradorId;
        this.tenantId = tenantId;
        this.dataHoraDispositivo = dataHoraDispositivo;
        this.dataHoraServidor = dataHoraServidor != null ? dataHoraServidor : Instant.now();
        this.tipoRegistro = tipoRegistro;
        this.latitude = latitude;
        this.longitude = longitude;
        this.precisaoGps = precisaoGps;
        this.fotoUrl = fotoUrl;
        this.sincronizadoOffline = sincronizadoOffline != null ? sincronizadoOffline : false;
        this.nsr = nsr;
        this.nsrLogico = nsrLogico;
        this.ajusteManual = ajusteManual != null ? ajusteManual : false;
        this.justificativa = justificativa;
        this.observacao = observacao;
        this.ajusteStatus = ajusteStatus;
        this.ajusteMotivoRejeicao = ajusteMotivoRejeicao;
        this.aprovadoPor = aprovadoPor;
        this.aprovadoEm = aprovadoEm;
    }

    public UUID getId() { return id; }
    public UUID getColaboradorId() { return colaboradorId; }
    public UUID getTenantId() { return tenantId; }
    public Instant getDataHoraDispositivo() { return dataHoraDispositivo; }
    public Instant getDataHoraServidor() { return dataHoraServidor; }
    public Instant getDataHora() { return dataHoraDispositivo != null ? dataHoraDispositivo : dataHoraServidor; }
    public TipoRegistro getTipoRegistro() { return tipoRegistro; }
    public BigDecimal getLatitude() { return latitude; }
    public BigDecimal getLongitude() { return longitude; }
    public BigDecimal getPrecisaoGps() { return precisaoGps; }
    public String getFotoUrl() { return fotoUrl; }
    public String getHashIntegridade() { return hashIntegridade; }
    public String getHash() { return hashIntegridade; }
    public Boolean getSincronizadoOffline() { return sincronizadoOffline; }
    public Long getNsr() { return nsr; }
    public Long getNsrLogico() { return nsrLogico; }
    public Boolean getAjusteManual() { return ajusteManual; }
    public String getJustificativa() { return justificativa; }
    public String getObservacao() { return observacao; }
    public AjusteStatus getAjusteStatus() { return ajusteStatus; }
    public String getAjusteMotivoRejeicao() { return ajusteMotivoRejeicao; }
    public UUID getAprovadoPor() { return aprovadoPor; }
    public Instant getAprovadoEm() { return aprovadoEm; }

    public void atribuirHash(String hash) { this.hashIntegridade = hash; }
    public void atribuirTipo(TipoRegistro tipo) { this.tipoRegistro = tipo; }
    public void atribuirNsr(Long nsr) { this.nsr = nsr; }
    public void atribuirNsrLogico(Long nsrLogico) { this.nsrLogico = nsrLogico; }
    public void atribuirAjusteManual(Boolean ajusteManual, String justificativa, String observacao) {
        this.ajusteManual = ajusteManual;
        this.justificativa = justificativa;
        this.observacao = observacao;
    }
    public void atribuirAjusteStatus(AjusteStatus status, String motivoRejeicao, UUID aprovadoPor) {
        this.ajusteStatus = status;
        this.ajusteMotivoRejeicao = motivoRejeicao;
        this.aprovadoPor = aprovadoPor;
        this.aprovadoEm = status == AjusteStatus.APROVADO ? Instant.now() : null;
    }
}