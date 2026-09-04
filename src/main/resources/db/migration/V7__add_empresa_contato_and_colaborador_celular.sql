-- Adiciona campos de contato na tabela tenant para cadastro público
ALTER TABLE tenant ADD COLUMN IF NOT EXISTS responsavel_nome VARCHAR(255);
ALTER TABLE tenant ADD COLUMN IF NOT EXISTS responsavel_cpf VARCHAR(11);
ALTER TABLE tenant ADD COLUMN IF NOT EXISTS responsavel_email VARCHAR(255);
ALTER TABLE tenant ADD COLUMN IF NOT EXISTS responsavel_celular VARCHAR(20);

-- Adiciona campo celular na tabela cpc_usuario
ALTER TABLE cpc_usuario ADD COLUMN IF NOT EXISTS celular VARCHAR(20);
