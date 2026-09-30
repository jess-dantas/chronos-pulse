package br.com.jess.chronos.pulse.modules.auth.infrastructure.adapters.output.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

/// Vinculo de dispositivo ("Modo Ponto"): apenas o hash SHA-256 do token é
/// persistido — o valor cru existe só na resposta do vínculo.
@Entity
@Table(name = "device_token")
public class DeviceTokenJpaEntity {

    @Id
    private UUID id;

    @Column(name = "usuario_id", nullable = false)
    private UUID usuarioId;

    @Column(name = "token_hash", nullable = false, length = 64, unique = true)
    private String tokenHash;

    @Column(name = "device_name", length = 120)
    private String deviceName;

    @Column(name = "criado_em", nullable = false)
    private Instant criadoEm;

    @Column(name = "expira_em", nullable = false)
    private Instant expiraEm;

    @Column(name = "revogado_em")
    private Instant revogadoEm;

    @Column(name = "ultimo_uso_em")
    private Instant ultimoUsoEm;

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public UUID getUsuarioId() { return usuarioId; }
    public void setUsuarioId(UUID usuarioId) { this.usuarioId = usuarioId; }
    public String getTokenHash() { return tokenHash; }
    public void setTokenHash(String tokenHash) { this.tokenHash = tokenHash; }
    public String getDeviceName() { return deviceName; }
    public void setDeviceName(String deviceName) { this.deviceName = deviceName; }
    public Instant getCriadoEm() { return criadoEm; }
    public void setCriadoEm(Instant criadoEm) { this.criadoEm = criadoEm; }
    public Instant getExpiraEm() { return expiraEm; }
    public void setExpiraEm(Instant expiraEm) { this.expiraEm = expiraEm; }
    public Instant getRevogadoEm() { return revogadoEm; }
    public void setRevogadoEm(Instant revogadoEm) { this.revogadoEm = revogadoEm; }
    public Instant getUltimoUsoEm() { return ultimoUsoEm; }
    public void setUltimoUsoEm(Instant ultimoUsoEm) { this.ultimoUsoEm = ultimoUsoEm; }
}
