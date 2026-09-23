package br.com.jess.chronos.pulse.modules.admin.domain.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "admin_plataforma")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdminPlataforma {

    public static final int MAX_TENTATIVAS_LOGIN = 5;
    public static final int LOCKOUT_MINUTOS = 15;

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(unique = true, nullable = false, length = 20)
    private String username;

    @Column(name = "senha_hash", nullable = false)
    private String senhaHash;

    @Column(name = "nome_completo", length = 100)
    private String nomeCompleto;

    @Column(unique = true, length = 100)
    private String email;

    @Column(name = "ultimo_login")
    private Instant ultimoLogin;

    @Column(name = "tentativas_login_falhas")
    private int tentativasLoginFalhas;

    @Column(name = "bloqueio_login_ate")
    private Instant bloqueioLoginAte;

    private boolean ativo;

    @Column(name = "two_factor_enabled", nullable = false)
    private boolean twoFactorEnabled;

    @Column(name = "two_factor_secret", length = 64)
    private String twoFactorSecret;

    @Column(name = "criado_em", nullable = false, updatable = false)
    private Instant criadoEm;

    @Column(name = "atualizado_em")
    private Instant atualizadoEm;

    @PrePersist
    protected void onCreate() {
        this.criadoEm = Instant.now();
        this.ativo = true;
        this.tentativasLoginFalhas = 0;
    }

    @PreUpdate
    protected void onUpdate() {
        this.atualizadoEm = Instant.now();
    }

    public void registrarFalhaLogin() {
        this.tentativasLoginFalhas++;
        if (this.tentativasLoginFalhas >= MAX_TENTATIVAS_LOGIN && this.bloqueioLoginAte == null) {
            this.bloqueioLoginAte = Instant.now().plus(Duration.ofMinutes(LOCKOUT_MINUTOS));
        }
    }

    public void registrarLoginSucesso() {
        this.tentativasLoginFalhas = 0;
        this.bloqueioLoginAte = null;
        this.ultimoLogin = Instant.now();
    }

    public boolean isLoginBloqueado() {
        return this.bloqueioLoginAte != null && this.bloqueioLoginAte.isAfter(Instant.now());
    }
}