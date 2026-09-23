-- ============================================================
-- Seed EXCLUSIVO de desenvolvimento (NÃO roda em produção).
-- Flyway locations incluem classpath:db/seed apenas no profile dev
-- (application-dev.yml); produção usa somente classpath:db/migration,
-- onde o Administrator é provisionado via first-run wizard
-- (POST /admin/auth/bootstrap) — zero-trace, sem senha seed.
--
-- Username: Administrator | Senha: admin123 | 2FA desabilitado
-- (chronos.admin.two-factor-required=false no profile dev).
-- ============================================================

INSERT INTO admin_plataforma (
    id, username, senha_hash, nome_completo, email, ativo,
    two_factor_enabled, two_factor_secret, criado_em
)
VALUES (
    'a0eebc99-1111-4111-8111-6bb9bd380a11',
    'Administrator',
    '$2a$10$reLinwh8IlJzgTjS9RMj.ux/0bUiI0fhT7EnBzqa4LjptHD44B.1K',
    'Administrador da Plataforma',
    'admin@chronospulse.com.br',
    TRUE,
    FALSE,
    NULL,
    NOW()
)
ON CONFLICT (username) DO UPDATE SET
    ativo = TRUE,
    two_factor_enabled = FALSE,
    two_factor_secret = NULL;
