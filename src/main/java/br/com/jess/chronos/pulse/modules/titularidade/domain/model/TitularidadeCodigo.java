package br.com.jess.chronos.pulse.modules.titularidade.domain.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

/**
 * Código OTP (6 dígitos) de uma etapa da transferência de titularidade:
 * hash bcrypt, validade 15 minutos, uso único.
 */
@Entity
@Table(name = "titularidade_codigo")
@Getter
@Setter
@NoArgsConstructor
public class TitularidadeCodigo {

    public static final String ETAPA_CELULAR = "CELULAR";
    public static final String ETAPA_EMAIL = "EMAIL";

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "transferencia_id", nullable = false)
    private UUID transferenciaId;

    @Column(nullable = false, length = 20)
    private String etapa;

    @Column(name = "codigo_hash", nullable = false, length = 255)
    private String codigoHash;

    @Column(name = "expira_em", nullable = false)
    private Instant expiraEm;

    @Column(name = "usado_em")
    private Instant usadoEm;

    @Column(name = "criado_em", nullable = false, updatable = false)
    private Instant criadoEm;

    @PrePersist
    protected void onCreate() {
        this.criadoEm = Instant.now();
    }

    public boolean isUsado() {
        return usadoEm != null;
    }

    public boolean isExpirado() {
        return expiraEm != null && expiraEm.isBefore(Instant.now());
    }

    public void marcarComoUsado() {
        this.usadoEm = Instant.now();
    }
}
