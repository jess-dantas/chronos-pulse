package br.com.jess.chronos.pulse.modules.admin.infrastructure.adapters.output.persistence;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "admin_plataforma")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdminPlataformaJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "username", unique = true, nullable = false, length = 20)
    private String username;

    @Column(name = "senha_hash", nullable = false)
    private String senhaHash;

    @Column(name = "nome_completo", length = 100)
    private String nomeCompleto;

    @Column(unique = true, length = 100)
    private String email;

    @Column(name = "ultimo_login")
    private java.time.Instant ultimoLogin;

    @Column(name = "tentativas_login_falhas")
    private int tentativasLoginFalhas;

    @Column(name = "bloqueio_login_ate")
    private java.time.Instant bloqueioLoginAte;

    private boolean ativo;

    @Column(name = "two_factor_enabled", nullable = false)
    private boolean twoFactorEnabled;

    @Column(name = "two_factor_secret", length = 64)
    private String twoFactorSecret;

    @Column(name = "criado_em", nullable = false, updatable = false)
    private java.time.Instant criadoEm;

    @Column(name = "atualizado_em")
    private java.time.Instant atualizadoEm;

    @PrePersist
    protected void onCreate() {
        if (this.criadoEm == null) {
            this.criadoEm = Instant.now();
        }
        if (this.ativo == false) {
            this.ativo = true;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        // atualizado_em is handled by the domain model
    }
}