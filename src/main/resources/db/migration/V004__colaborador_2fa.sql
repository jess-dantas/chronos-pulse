-- 2FA do colaborador: TOTP (Google Authenticator) + OTP por e-mail.
-- Um único código OTP pendente por usuário (definir sobrescreve o anterior),
-- com hash bcrypt, expiração e limite de tentativas (espelha o admin).
ALTER TABLE cpc_usuario ADD COLUMN two_factor_enabled BOOLEAN NOT NULL DEFAULT FALSE;
ALTER TABLE cpc_usuario ADD COLUMN two_factor_secret VARCHAR(64);
ALTER TABLE cpc_usuario ADD COLUMN two_factor_email_hash VARCHAR(255);
ALTER TABLE cpc_usuario ADD COLUMN two_factor_email_expira_em TIMESTAMPTZ;
ALTER TABLE cpc_usuario ADD COLUMN two_factor_email_tentativas INTEGER NOT NULL DEFAULT 0;
