-- ==========================================================
-- V18: Desfazimento de bens patrimoniais (Decreto 9.373/2018).
-- Registra solicitação, parecer da comissão, aprovação e baixa.
-- ==========================================================

CREATE TABLE tb_desfazimento (
    id UUID PRIMARY KEY,
    tenant_id UUID NOT NULL REFERENCES tenant(id),
    patrimonio_id UUID NOT NULL REFERENCES tb_patrimonio(id),
    estado_bem VARCHAR(30) NOT NULL,
    tipo_desfazimento VARCHAR(30) NOT NULL,
    justificativa TEXT NOT NULL,
    responsavel_solicitacao VARCHAR(150) NOT NULL,
    data_solicitacao TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    parecer_comissao TEXT,
    aprovado BOOLEAN,
    data_aprovacao TIMESTAMP WITH TIME ZONE,
    data_baixa TIMESTAMP WITH TIME ZONE,
    status VARCHAR(30) NOT NULL DEFAULT 'EM_ANALISE',
    processo_numero VARCHAR(40),
    observacoes TEXT,
    criado_em TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    CONSTRAINT chk_desfazimento_estado_bem CHECK (estado_bem IN ('OCIOSO', 'RECUPERAVEL', 'ANTIECONOMICO', 'IRRECUPERAVEL')),
    CONSTRAINT chk_desfazimento_tipo CHECK (tipo_desfazimento IN ('DOACAO', 'TROCA', 'VENDA', 'CEDENCIA', 'RECICLAGEM', 'OUTROS')),
    CONSTRAINT chk_desfazimento_status CHECK (status IN ('EM_ANALISE', 'APROVADO', 'CANCELADO'))
);

CREATE INDEX idx_desfazimento_tenant ON tb_desfazimento(tenant_id);
CREATE INDEX idx_desfazimento_patrimonio ON tb_desfazimento(patrimonio_id);
CREATE INDEX idx_desfazimento_status ON tb_desfazimento(status);

CREATE TABLE tb_desfazimento_comissao (
    id UUID PRIMARY KEY,
    desfazimento_id UUID NOT NULL REFERENCES tb_desfazimento(id),
    membro_nome VARCHAR(150) NOT NULL,
    membro_cargo VARCHAR(120),
    membro_cpf VARCHAR(20),
    relator BOOLEAN DEFAULT FALSE,
    parecer TEXT,
    criado_em TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

CREATE INDEX idx_desfazimento_comissao_desfazimento ON tb_desfazimento_comissao(desfazimento_id);