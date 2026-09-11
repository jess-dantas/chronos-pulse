-- R30 - Formalização do contrato a partir da licitação homologada
ALTER TABLE contrato ADD COLUMN IF NOT EXISTS licitacao_id UUID REFERENCES tb_licitacao(id) ON DELETE SET NULL;

CREATE INDEX IF NOT EXISTS idx_contrato_licitacao_id ON contrato(licitacao_id);

ALTER TABLE tb_licitacao ADD COLUMN IF NOT EXISTS contrato_gerado BOOLEAN NOT NULL DEFAULT FALSE;