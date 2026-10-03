-- Dispositivo confiável do admin da plataforma (login "biometria-first"):
-- o deviceToken emitido por POST /admin/auth/dispositivo autentica o login
-- direto, pulando senha e 2FA (a biometria é confirmada no aparelho antes
-- de o cliente enviar o token). TTL de 30 dias
-- (chronos.admin.device-expiration-ms) e revogável a qualquer momento.
-- Só o hash SHA-256 é persistido — o valor cru é devolvido uma única vez.
CREATE TABLE admin_device_token (
    id UUID PRIMARY KEY,
    admin_id UUID NOT NULL REFERENCES admin_plataforma(id) ON DELETE CASCADE,
    token_hash VARCHAR(64) NOT NULL UNIQUE,
    device_name VARCHAR(120),
    criado_em TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    expira_em TIMESTAMP WITH TIME ZONE NOT NULL,
    revogado_em TIMESTAMP WITH TIME ZONE,
    ultimo_uso_em TIMESTAMP WITH TIME ZONE
);

CREATE INDEX idx_admin_device_token_admin ON admin_device_token(admin_id);
