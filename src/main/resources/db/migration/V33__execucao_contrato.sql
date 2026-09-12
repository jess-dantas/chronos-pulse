-- R30 - Gestão da Execução Contratual (Lei 14.133/2021)
-- Aditivos, fiscalização/apontamentos, medições/pagamentos, sanções e rescisão.

CREATE TABLE IF NOT EXISTS contrato_aditivo (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL REFERENCES tenant(id),
    contrato_id UUID NOT NULL REFERENCES contrato(id) ON DELETE CASCADE,
    tipo VARCHAR(20) NOT NULL,
    descricao TEXT NOT NULL,
    justificativa TEXT,
    prazo_adicionado_dias INTEGER,
    novo_valor_total NUMERIC(12,2),
    aprovado BOOLEAN NOT NULL DEFAULT FALSE,
    criado_por UUID NOT NULL REFERENCES cpc_usuario(id),
    criado_em TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_contrato_aditivo_contrato_id ON contrato_aditivo(contrato_id);

CREATE TABLE IF NOT EXISTS contrato_apontamento (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL REFERENCES tenant(id),
    contrato_id UUID NOT NULL REFERENCES contrato(id) ON DELETE CASCADE,
    fiscal VARCHAR(100) NOT NULL,
    descricao TEXT NOT NULL,
    gravidade VARCHAR(20) NOT NULL DEFAULT 'MEDIA',
    resolvido BOOLEAN NOT NULL DEFAULT FALSE,
    resolvido_em TIMESTAMP WITH TIME ZONE,
    criado_por UUID NOT NULL REFERENCES cpc_usuario(id),
    criado_em TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_contrato_apontamento_contrato_id ON contrato_apontamento(contrato_id);

CREATE TABLE IF NOT EXISTS contrato_medicao (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL REFERENCES tenant(id),
    contrato_id UUID NOT NULL REFERENCES contrato(id) ON DELETE CASCADE,
    periodo VARCHAR(20) NOT NULL,
    valor_medido NUMERIC(12,2) NOT NULL DEFAULT 0,
    valor_pago NUMERIC(12,2) NOT NULL DEFAULT 0,
    pago_em DATE,
    observacao TEXT,
    criado_por UUID NOT NULL REFERENCES cpc_usuario(id),
    criado_em TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_contrato_medicao_contrato_id ON contrato_medicao(contrato_id);

CREATE TABLE IF NOT EXISTS contrato_sancao (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL REFERENCES tenant(id),
    contrato_id UUID NOT NULL REFERENCES contrato(id) ON DELETE CASCADE,
    tipo VARCHAR(30) NOT NULL,
    base_legal TEXT,
    descricao TEXT NOT NULL,
    percentual_multa NUMERIC(6,3),
    valor_multa NUMERIC(12,2),
    aplicada_em DATE NOT NULL,
    criado_por UUID NOT NULL REFERENCES cpc_usuario(id),
    criado_em TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_contrato_sancao_contrato_id ON contrato_sancao(contrato_id);

CREATE TABLE IF NOT EXISTS contrato_rescisao (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL REFERENCES tenant(id),
    contrato_id UUID NOT NULL REFERENCES contrato(id) ON DELETE CASCADE,
    tipo VARCHAR(20) NOT NULL,
    motivo TEXT NOT NULL,
    data_rescisao DATE NOT NULL,
    criado_por UUID NOT NULL REFERENCES cpc_usuario(id),
    criado_em TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_contrato_rescisao_contrato_id ON contrato_rescisao(contrato_id);