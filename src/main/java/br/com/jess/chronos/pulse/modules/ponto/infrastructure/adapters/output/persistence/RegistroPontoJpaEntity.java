package br.com.jess.chronos.pulse.modules.ponto.infrastructure.adapters.output.persistence;

import br.com.jess.chronos.pulse.modules.ponto.domain.model.TipoRegistro;
import br.com.jess.chronos.pulse.modules.ponto.domain.model.AjusteStatus;
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
    private Long nsr;
    private Long nsrLogico;
    @Column(name = "ajuste_manual")
    private Boolean ajusteManual;
    private String justificativa;
    private String observacao;
    @Enumerated(EnumType.STRING)
    @Column(name = "ajuste_status")
    private AjusteStatus ajusteStatus;
    @Column(name = "ajuste_motivo_rejeicao")
    private String ajusteMotivoRejeicao;
    @Column(name = "aprovado_por")
    private UUID aprovadoPor;
    @Column(name = "aprovado_em")
    private Instant aprovadoEm;

    @Version
    private Long version;

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
    public Long getNsrLogico() { return nsrLogico; }
    public void setNsrLogico(Long v) { this.nsrLogico = v; }
    public Boolean getAjusteManual() { return ajusteManual; }
    public void setAjusteManual(Boolean ajusteManual) { this.ajusteManual = ajusteManual; }
    public String getJustificativa() { return justificativa; }
    public void setJustificativa(String justificativa) { this.justificativa = justificativa; }
    public String getObservacao() { return observacao; }
    public void setObservacao(String observacao) { this.observacao = observacao; }
    public AjusteStatus getAjusteStatus() { return ajusteStatus; }
    public void setAjusteStatus(AjusteStatus v) { this.ajusteStatus = v; }
    public String getAjusteMotivoRejeicao() { return ajusteMotivoRejeicao; }
    public void setAjusteMotivoRejeicao(String v) { this.ajusteMotivoRejeicao = v; }
    public UUID getAprovadoPor() { return aprovadoPor; }
    public void setAprovadoPor(UUID v) { this.aprovadoPor = v; }
    public Instant getAprovadoEm() { return aprovadoEm; }
    public void setAprovadoEm(Instant v) { this.aprovadoEm = v; }
}
