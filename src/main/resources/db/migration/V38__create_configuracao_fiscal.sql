-- Configuração de exportação fiscal (AFD/AEJ) por tenant:
-- nº de registro no INPI, dados do desenvolvedor (PTRP) e CNO da empresa.
-- Usada quando o chamador não informa o parâmetro explicitamente no download.
CREATE TABLE configuracao_fiscal (
    tenant_id UUID PRIMARY KEY REFERENCES tenant(id),
    numero_registro_inpi VARCHAR(30),
    cnpj_desenvolvedor VARCHAR(14),
    prtp_nome VARCHAR(150) NOT NULL DEFAULT 'CHRONOS PULSE',
    prtp_versao VARCHAR(20) NOT NULL DEFAULT '1.0.0',
    prtp_razao_desenv VARCHAR(150),
    prtp_email VARCHAR(50),
    cno VARCHAR(12),
    atualizado_em TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);