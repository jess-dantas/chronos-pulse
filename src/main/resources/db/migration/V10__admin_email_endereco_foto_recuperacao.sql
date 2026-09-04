-- E-mail do Admin Plataforma
UPDATE cpc_usuario SET email_corporativo = 'jess.dantas.it@gmail.com' WHERE cpf = '00000000000';

-- Endereco da empresa (tenant) + telefone do responsavel
ALTER TABLE tenant ADD COLUMN responsavel_telefone VARCHAR(20);
ALTER TABLE tenant ADD COLUMN endereco_logradouro VARCHAR(255);
ALTER TABLE tenant ADD COLUMN endereco_numero VARCHAR(20);
ALTER TABLE tenant ADD COLUMN endereco_complemento VARCHAR(255);
ALTER TABLE tenant ADD COLUMN endereco_bairro VARCHAR(120);
ALTER TABLE tenant ADD COLUMN endereco_cidade VARCHAR(120);
ALTER TABLE tenant ADD COLUMN endereco_uf VARCHAR(2);
ALTER TABLE tenant ADD COLUMN endereco_cep VARCHAR(9);

-- Foto de perfil do usuario (base64)
ALTER TABLE cpc_usuario ADD COLUMN foto TEXT;

-- Recuperacao de senha
CREATE TABLE recuperacao_senha (
    id UUID PRIMARY KEY,
    cpf VARCHAR(11) NOT NULL,
    codigo_hash VARCHAR(255) NOT NULL,
    expira_em TIMESTAMPTZ NOT NULL,
    usado BOOLEAN DEFAULT FALSE,
    criado_em TIMESTAMPTZ DEFAULT NOW()
);
CREATE INDEX idx_recuperacao_senha_cpf ON recuperacao_senha (cpf);