-- V16: Consentimento de tratamento de dados pessoais (LGPD).
-- Registra a aceitação explícita e a versão da política de privacidade
-- vigente no momento do consentimento de cada usuário.

CREATE TABLE tb_consentimento_privacidade (
    id                 UUID PRIMARY KEY,
    cpc_id             UUID NOT NULL,
    versao_politica    VARCHAR(50)  NOT NULL,
    data_consentimento TIMESTAMP WITH TIME ZONE NOT NULL,
    aceito             BOOLEAN NOT NULL DEFAULT TRUE,
    ip_origem          VARCHAR(64),
    UNIQUE (cpc_id, versao_politica)
);

CREATE INDEX idx_consentimento_privacidade_cpc ON tb_consentimento_privacidade(cpc_id);