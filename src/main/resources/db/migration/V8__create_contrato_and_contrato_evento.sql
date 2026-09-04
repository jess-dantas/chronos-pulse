-- Tabela de contratos vinculados a empresas (tenants)
CREATE TABLE IF NOT EXISTS contrato (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL REFERENCES tenant(id),
    numero VARCHAR(50) NOT NULL,
    objeto TEXT NOT NULL,
    data_inicio DATE NOT NULL,
    data_fim DATE NOT NULL,
    valor_mensal NUMERIC(12,2) NOT NULL DEFAULT 0,
    valor_total NUMERIC(12,2) NOT NULL DEFAULT 0,
    status VARCHAR(20) NOT NULL DEFAULT 'ATIVO',
    observacoes TEXT,
    criado_em TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    atualizado_em TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_contrato_tenant_id ON contrato(tenant_id);
CREATE INDEX IF NOT EXISTS idx_contrato_status ON contrato(status);

-- Eventos/ocorrências vinculados a contratos
CREATE TABLE IF NOT EXISTS contrato_evento (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    contrato_id UUID NOT NULL REFERENCES contrato(id) ON DELETE CASCADE,
    tipo VARCHAR(30) NOT NULL,
    descricao TEXT NOT NULL,
    criado_por UUID NOT NULL REFERENCES cpc_usuario(id),
    criado_em TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_contrato_evento_contrato_id ON contrato_evento(contrato_id);
