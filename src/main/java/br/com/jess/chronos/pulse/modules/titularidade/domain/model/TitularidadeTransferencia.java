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

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

/**
 * Transferência de titularidade da empresa (wizard /perfil/titularidade).
 * Etapas: biometria → OTP do titular atual + confirmação do celular do novo
 * titular → OTP do e-mail corporativo do novo titular. TTL de 30 minutos.
 */
@Entity
@Table(name = "titularidade_transferencia")
@Getter
@Setter
@NoArgsConstructor
public class TitularidadeTransferencia {

    public static final String STATUS_EM_ANDAMENTO = "EM_ANDAMENTO";
    public static final String STATUS_CONCLUIDA = "CONCLUIDA";
    public static final String STATUS_CANCELADA = "CANCELADA";

    public static final Duration VALIDADE = Duration.ofMinutes(30);

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "tenant_id", nullable = false)
    private UUID tenantId;

    @Column(name = "solicitante_id", nullable = false)
    private UUID solicitanteId;

    @Column(name = "novo_titular_id", nullable = false)
    private UUID novoTitularId;

    @Column(nullable = false, length = 20)
    private String status;

    @Column(name = "etapa_biometria", nullable = false)
    private boolean etapaBiometria;

    @Column(name = "etapa_celular", nullable = false)
    private boolean etapaCelular;

    @Column(name = "etapa_email", nullable = false)
    private boolean etapaEmail;

    @Column(name = "expira_em", nullable = false)
    private Instant expiraEm;

    @Column(name = "concluida_em")
    private Instant concluidaEm;

    @Column(name = "criado_em", nullable = false, updatable = false)
    private Instant criadoEm;

    @PrePersist
    protected void onCreate() {
        this.criadoEm = Instant.now();
        if (this.status == null) {
            this.status = STATUS_EM_ANDAMENTO;
        }
        if (this.expiraEm == null) {
            this.expiraEm = Instant.now().plus(VALIDADE);
        }
    }

    public boolean isExpirada() {
        return expiraEm != null && expiraEm.isBefore(Instant.now());
    }

    public boolean isAberta() {
        return STATUS_EM_ANDAMENTO.equals(status) && !isExpirada();
    }

    public void marcarBiometria() {
        this.etapaBiometria = true;
    }

    public void marcarCelular() {
        this.etapaCelular = true;
    }

    public void marcarEmail() {
        this.etapaEmail = true;
    }

    public void concluir() {
        this.status = STATUS_CONCLUIDA;
        this.concluidaEm = Instant.now();
    }

    public void cancelar() {
        this.status = STATUS_CANCELADA;
    }
}
