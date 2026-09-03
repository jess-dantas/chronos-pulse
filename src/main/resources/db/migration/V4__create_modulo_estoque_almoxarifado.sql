-- ==========================================================
-- Módulo: CP Estoque & Almoxarifado Público
-- Tabelas: tb_material_grupo, tb_material, tb_almoxarifado,
--          tb_estoque_saldo, tb_estoque_movimentacao,
--          tb_requisicao, tb_requisicao_item
-- ==========================================================

-- 1. Grupos de Materiais (CATMAT / Classes de Consumo)
CREATE TABLE tb_material_grupo (
    id UUID PRIMARY KEY,
    tenant_id UUID NOT NULL REFERENCES tenant(id),
    codigo VARCHAR(20) NOT NULL,
    nome VARCHAR(100) NOT NULL,
    ativo BOOLEAN DEFAULT TRUE,
    CONSTRAINT uk_material_grupo_tenant_codigo UNIQUE (tenant_id, codigo)
);

CREATE INDEX idx_material_grupo_tenant ON tb_material_grupo(tenant_id);

-- 2. Almoxarifados (Central e Setoriais)
CREATE TABLE tb_almoxarifado (
    id UUID PRIMARY KEY,
    tenant_id UUID NOT NULL REFERENCES tenant(id),
    nome VARCHAR(100) NOT NULL,
    descricao VARCHAR(255),
    responsavel_cpc_id UUID,
    ativo BOOLEAN DEFAULT TRUE
);

CREATE INDEX idx_almoxarifado_tenant ON tb_almoxarifado(tenant_id);

-- 3. Catálogo de Materiais
CREATE TABLE tb_material (
    id UUID PRIMARY KEY,
    tenant_id UUID NOT NULL REFERENCES tenant(id),
    grupo_id UUID NOT NULL REFERENCES tb_material_grupo(id),
    codigo_catmat VARCHAR(20),
    descricao VARCHAR(255) NOT NULL,
    unidade_medida VARCHAR(20) NOT NULL,
    estoque_minimo NUMERIC(12, 3),
    controla_lote_validade BOOLEAN DEFAULT FALSE,
    ativo BOOLEAN DEFAULT TRUE
);

CREATE INDEX idx_material_tenant ON tb_material(tenant_id);
CREATE INDEX idx_material_grupo ON tb_material(grupo_id);
CREATE INDEX idx_material_catmat ON tb_material(codigo_catmat);

-- 4. Saldos de Estoque por Almoxarifado, Material e Lote
CREATE TABLE tb_estoque_saldo (
    id UUID PRIMARY KEY,
    tenant_id UUID NOT NULL REFERENCES tenant(id),
    almoxarifado_id UUID NOT NULL REFERENCES tb_almoxarifado(id),
    material_id UUID NOT NULL REFERENCES tb_material(id),
    lote VARCHAR(50),
    data_validade DATE,
    quantidade_atual NUMERIC(12, 3) NOT NULL DEFAULT 0,
    custo_medio_unitario NUMERIC(15, 4) NOT NULL DEFAULT 0
);

CREATE INDEX idx_estoque_saldo_tenant ON tb_estoque_saldo(tenant_id);
CREATE INDEX idx_estoque_saldo_almox_mat ON tb_estoque_saldo(tenant_id, almoxarifado_id, material_id);

-- 5. Movimentações Físico-Financeiras Auditáveis (Entradas, Saídas, Baixas)
CREATE TABLE tb_estoque_movimentacao (
    id UUID PRIMARY KEY,
    tenant_id UUID NOT NULL REFERENCES tenant(id),
    almoxarifado_id UUID NOT NULL REFERENCES tb_almoxarifado(id),
    material_id UUID NOT NULL REFERENCES tb_material(id),
    tipo_movimento VARCHAR(30) NOT NULL,
    quantidade NUMERIC(12, 3) NOT NULL,
    valor_unitario NUMERIC(15, 4) NOT NULL,
    valor_total NUMERIC(15, 4) NOT NULL,
    lote VARCHAR(50),
    data_validade DATE,
    documento_referencia VARCHAR(100),
    usuario_cpc_id UUID NOT NULL,
    data_hora_registro TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

CREATE INDEX idx_estoque_mov_tenant ON tb_estoque_movimentacao(tenant_id);
CREATE INDEX idx_estoque_mov_almox_mat ON tb_estoque_movimentacao(almoxarifado_id, material_id);
CREATE INDEX idx_estoque_mov_data ON tb_estoque_movimentacao(data_hora_registro);

-- 6. Requisições de Almoxarifado (Distribuição e Atendimento)
CREATE TABLE tb_requisicao (
    id UUID PRIMARY KEY,
    tenant_id UUID NOT NULL REFERENCES tenant(id),
    almoxarifado_id UUID NOT NULL REFERENCES tb_almoxarifado(id),
    solicitante_cpc_id UUID NOT NULL,
    departamento VARCHAR(100),
    justificativa TEXT,
    status VARCHAR(30) NOT NULL,
    data_solicitacao TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    data_atendimento TIMESTAMP WITH TIME ZONE,
    atendente_cpc_id UUID,
    CONSTRAINT chk_requisicao_status CHECK (status IN (
        'PENDENTE', 'APROVADA', 'ATENDIDA', 'REJEITADA', 'CANCELADA'
    ))
);

CREATE INDEX idx_requisicao_tenant ON tb_requisicao(tenant_id);
CREATE INDEX idx_requisicao_status ON tb_requisicao(status);
CREATE INDEX idx_requisicao_solicitante ON tb_requisicao(solicitante_cpc_id);

-- 7. Itens da Requisição
CREATE TABLE tb_requisicao_item (
    id UUID PRIMARY KEY,
    requisicao_id UUID NOT NULL REFERENCES tb_requisicao(id) ON DELETE CASCADE,
    material_id UUID NOT NULL REFERENCES tb_material(id),
    quantidade_solicitada NUMERIC(12, 3) NOT NULL,
    quantidade_atendida NUMERIC(12, 3) DEFAULT 0
);

CREATE INDEX idx_requisicao_item_req ON tb_requisicao_item(requisicao_id);

-- ==========================================================
-- Seeds Iniciais de Estoque para o Tenant Padrão
-- ==========================================================

-- Grupo de Material
INSERT INTO tb_material_grupo (id, tenant_id, codigo, nome, ativo)
VALUES (
    '55555555-5555-5555-5555-555555555551',
    'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11',
    'EXP-01',
    'Material de Expediente e Escritório',
    true
) ON CONFLICT (tenant_id, codigo) DO NOTHING;

-- Almoxarifado Central
INSERT INTO tb_almoxarifado (id, tenant_id, nome, descricao, responsavel_cpc_id, ativo)
VALUES (
    '66666666-6666-6666-6666-666666666661',
    'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11',
    'Almoxarifado Central - Prefeitura',
    'Almoxarifado principal para suprimento de secretarias',
    '22222222-2222-2222-2222-222222222222',
    true
) ON CONFLICT (id) DO NOTHING;

-- Materiais
INSERT INTO tb_material (id, tenant_id, grupo_id, codigo_catmat, descricao, unidade_medida, estoque_minimo, controla_lote_validade, ativo)
VALUES (
    '77777777-7777-7777-7777-777777777771',
    'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11',
    '55555555-5555-5555-5555-555555555551',
    'CAT-1001',
    'Papel A4 Sulfite 75g (Resma 500 folhas)',
    'RESMA',
    10.000,
    false,
    true
),
(
    '77777777-7777-7777-7777-777777777772',
    'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11',
    '55555555-5555-5555-5555-555555555551',
    'CAT-1002',
    'Caneta Esferográfica Azul 1.0mm (Caixa c/ 50)',
    'CX',
    5.000,
    false,
    true
) ON CONFLICT (id) DO NOTHING;
