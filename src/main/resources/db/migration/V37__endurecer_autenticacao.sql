-- Endurecimento de autenticação (security review):
--  * tentativas_login_falhas / bloqueio_login_ate: lockout de brute force no login
--  * senha_alterada_em: revoga tokens emitidos antes da última troca de senha
--  * recuperacao_senha.tentativas: limite de tentativas do código de recuperação
ALTER TABLE cpc_usuario ADD COLUMN IF NOT EXISTS tentativas_login_falhas INT NOT NULL DEFAULT 0;
ALTER TABLE cpc_usuario ADD COLUMN IF NOT EXISTS bloqueio_login_ate TIMESTAMP NULL;
ALTER TABLE cpc_usuario ADD COLUMN IF NOT EXISTS senha_alterada_em TIMESTAMP NULL;
ALTER TABLE recuperacao_senha ADD COLUMN IF NOT EXISTS tentativas INT NOT NULL DEFAULT 0;