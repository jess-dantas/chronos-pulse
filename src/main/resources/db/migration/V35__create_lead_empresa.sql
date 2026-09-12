-- ==========================================================
-- V35: Leads de prospecção de empresas (landing/onboarding)
-- Cadastro público em 3 etapas (Empresa -> Endereco -> Contato),
-- SEM CPF e SEM senha. Nao cria conta/login: vira um lead comercial
-- que a equipe (jess) retorna para agendar a reunião de contratação.
-- ==========================================================

CREATE TABLE tb_lead_empresa (
    id                  UUID PRIMARY KEY,
    cnpj                VARCHAR(14) NOT NULL,
    razao_social        VARCHAR(200) NOT NULL,
    contato_nome        VARCHAR(120) NOT NULL,
    contato_email       VARCHAR(160) NOT NULL,
    contato_telefone    VARCHAR(20),
    contato_celular     VARCHAR(20),
    endereco_logradouro VARCHAR(255),
    endereco_numero     VARCHAR(20),
    endereco_complemento VARCHAR(120),
    endereco_bairro     VARCHAR(120),
    endereco_cidade     VARCHAR(120),
    endereco_uf         VARCHAR(2),
    endereco_cep        VARCHAR(8),
    observacao          VARCHAR(500),
    status              VARCHAR(20) NOT NULL DEFAULT 'NOVO',
    criado_em           TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_lead_empresa_criado_em ON tb_lead_empresa (criado_em DESC);
CREATE INDEX idx_lead_empresa_status ON tb_lead_empresa (status);