-- ==========================================================
-- V25: Patrimônio Avançado — inventário com conferência,
--      depreciação e transferência de bens patrimoniais.
-- ==========================================================

-- 1. Depreciação no bem patrimonial (método linear)
ALTER TABLE tb_patrimonio
    ADD COLUMN vida_util_meses INTEGER,
    ADD COLUMN taxa_depreciacao_mensal NUMERIC(5, 4),
    ADD COLUMN valor_depreciado NUMERIC(15, 2) NOT NULL DEFAULT 0,
    ADD COLUMN data_inicio_depreciacao DATE;

-- 2. Sessões de inventário físico
CREATE TABLE tb_inventario (
    id UUID PRIMARY KEY,
    tenant_id UUID NOT NULL REFERENCES tenant(id),
    descricao VARCHAR(200) NOT NULL,
    data_inicio DATE NOT NULL,
    data_fim DATE,
    status VARCHAR(20) NOT NULL DEFAULT 'EM_ANDAMENTO',
    criado_por VARCHAR(120),
    criado_em TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT chk_inventario_status CHECK (status IN ('EM_ANDAMENTO', 'CONCLUIDO', 'CANCELADO'))
);

CREATE INDEX idx_inventario_tenant ON tb_inventario(tenant_id);
CREATE INDEX idx_inventario_status ON tb_inventario(status);

-- 3. Itens do inventário (um registro por bem, com resultado da conferência)
CREATE TABLE tb_inventario_item (
    id UUID PRIMARY KEY,
    inventario_id UUID NOT NULL REFERENCES tb_inventario(id),
    patrimonio_id UUID NOT NULL REFERENCES tb_patrimonio(id),
    patrimonio_tombamento VARCHAR(30),
    patrimonio_descricao VARCHAR(255),
    conferido BOOLEAN NOT NULL DEFAULT FALSE,
    conferido_por VARCHAR(120),
    data_conferencia TIMESTAMPTZ,
    resultado VARCHAR(20),
    observacao TEXT,
    CONSTRAINT uk_inventario_item UNIQUE (inventario_id, patrimonio_id),
    CONSTRAINT chk_inventario_item_resultado CHECK (resultado IN ('CONFORME', 'DIVERGENCIA'))
);

CREATE INDEX idx_inventario_item_inventario ON tb_inventario_item(inventario_id);
CREATE INDEX idx_inventario_item_patrimonio ON tb_inventario_item(patrimonio_id);

-- 4. Transferência de bens entre localizações/responsáveis
CREATE TABLE tb_transferencia_patrimonio (
    id UUID PRIMARY KEY,
    tenant_id UUID NOT NULL REFERENCES tenant(id),
    patrimonio_id UUID NOT NULL REFERENCES tb_patrimonio(id),
    localizacao_origem VARCHAR(120),
    localizacao_destino VARCHAR(120) NOT NULL,
    responsavel_origem VARCHAR(120),
    responsavel_destino VARCHAR(120),
    data_solicitacao TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    data_prevista DATE,
    data_efetivacao TIMESTAMPTZ,
    status VARCHAR(20) NOT NULL DEFAULT 'SOLICITADA',
    justificativa TEXT,
    aprovado_por VARCHAR(120),
    solicitado_por VARCHAR(120),
    criado_em TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT chk_transferencia_status CHECK (status IN ('SOLICITADA', 'EM_TRANSITO', 'CONFIRMADA', 'CANCELADA'))
);

CREATE INDEX idx_transferencia_tenant ON tb_transferencia_patrimonio(tenant_id);
CREATE INDEX idx_transferencia_patrimonio ON tb_transferencia_patrimonio(patrimonio_id);
CREATE INDEX idx_transferencia_status ON tb_transferencia_patrimonio(status);

-- ==========================================================
-- SEEDS de demonstração (tenant padrão a0eebc99-...)
-- ==========================================================

-- Depreciação de demonstração nos bens seed do V11
UPDATE tb_patrimonio
SET vida_util_meses = 48,
    data_inicio_depreciacao = data_aquisicao,
    valor_depreciado = ROUND(valor_aquisicao * 0.9 / 48 * 30, 2)
WHERE tenant_id = 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11' AND tombamento = 'TOM-0001';

UPDATE tb_patrimonio
SET vida_util_meses = 60,
    data_inicio_depreciacao = data_aquisicao,
    valor_depreciado = ROUND(valor_aquisicao * 0.9 / 60 * 38, 2)
WHERE tenant_id = 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11' AND tombamento = 'TOM-0002';

UPDATE tb_patrimonio
SET vida_util_meses = 120,
    data_inicio_depreciacao = data_aquisicao,
    valor_depreciado = ROUND(valor_aquisicao * 0.9 / 120 * 52, 2)
WHERE tenant_id = 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11' AND tombamento = 'TOM-0003';

-- Sessão de inventário em andamento
INSERT INTO tb_inventario (id, tenant_id, descricao, data_inicio, data_fim, status, criado_por)
VALUES
    ('e1e1e1e1-e1e1-4e11-81e1-e1e1e1e1e1e1', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11',
     'Inventário anual do almoxarifado e salas administrativas', '2026-09-01', '2026-09-30', 'EM_ANDAMENTO', 'Admin Demo')
ON CONFLICT (id) DO NOTHING;

-- Itens do inventário (TOM-0001 conferido conforme; TOM-0002 e TOM-0003 pendentes)
INSERT INTO tb_inventario_item (id, inventario_id, patrimonio_id, patrimonio_tombamento, patrimonio_descricao, conferido, conferido_por, data_conferencia, resultado, observacao)
VALUES
    ('e2e2e2e2-e2e2-4e22-82e2-e2e2e2e2e2e1', 'e1e1e1e1-e1e1-4e11-81e1-e1e1e1e1e1e1',
     '88888888-8888-4888-8888-888888888881', 'TOM-0001', 'Notebook Dell Latitude 7420 (i7, 16GB)', TRUE, 'Admin Demo', '2026-09-05T10:00:00Z', 'CONFORME', NULL),
    ('e2e2e2e2-e2e2-4e22-82e2-e2e2e2e2e2e2', 'e1e1e1e1-e1e1-4e11-81e1-e1e1e1e1e1e1',
     '88888888-8888-4888-8888-888888888882', 'TOM-0002', 'Impressora Multifuncional Laser HP', FALSE, NULL, NULL, NULL, NULL),
    ('e2e2e2e2-e2e2-4e22-82e2-e2e2e2e2e2e3', 'e1e1e1e1-e1e1-4e11-81e1-e1e1e1e1e1e1',
     '88888888-8888-4888-8888-888888888883', 'TOM-0003', 'Veículo Palio Adventure 1.8 Flex', FALSE, NULL, NULL, NULL, NULL)
ON CONFLICT (id) DO NOTHING;

-- Transferência concluída de demonstração
INSERT INTO tb_transferencia_patrimonio (id, tenant_id, patrimonio_id, localizacao_origem, localizacao_destino, responsavel_origem, responsavel_destino, data_prevista, data_efetivacao, status, justificativa, aprovado_por, solicitado_por)
VALUES
    ('e4e4e4e4-e4e4-4e44-84e4-e4e4e4e4e4e1', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11',
     '88888888-8888-4888-8888-888888888883', 'Garagem Municipal', 'Secretaria de Obras',
     'José Oliveira', 'Engº Carlos Mendes', '2026-08-20', '2026-08-22', 'CONFIRMADA',
     'Veículo remanejado para apoio às obras da zona norte', 'Admin Demo', 'Admin Demo')
ON CONFLICT (id) DO NOTHING;

-- Transfere o bem de demonstração para o novo destino (reflete a transferência confirmada)
UPDATE tb_patrimonio
SET localizacao = 'Secretaria de Obras'
WHERE tenant_id = 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11' AND tombamento = 'TOM-0003';