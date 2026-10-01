-- R3: OTP por e-mail para recuperação de acesso do admin da plataforma.
-- Um único código pendente por admin (definir sobrescreve o anterior),
-- com hash bcrypt, expiração e limite de tentativas.
ALTER TABLE admin_plataforma ADD COLUMN recuperacao_email_hash VARCHAR(255);
ALTER TABLE admin_plataforma ADD COLUMN recuperacao_email_expira_em TIMESTAMPTZ;
ALTER TABLE admin_plataforma ADD COLUMN recuperacao_email_tentativas INTEGER NOT NULL DEFAULT 0;
