-- ==========================================================
-- V21: Módulo Compras — fornecedores, pedidos de compra (AF),
--      entrada por NFe vinculada a contrato/empenho e banco de
--      preços (histórico de valores de aquisição por material).
-- ==========================================================

INSERT INTO modulo_plataforma (id, codigo, nome, descricao, ativo) VALUES
    ('11111111-1111-4111-8111-111111111107', 'COMPRAS', 'Compras & Fornecedores', 'Fornecedores, pedidos de compra, entrada por NFe e banco de preços', true)
ON CONFLICT (codigo) DO NOTHING;

-- Ativa COMPRAS para todas as empresas existentes (vem ativo no catálogo)
INSERT INTO empresa_modulo (id, tenant_id, modulo_id)
SELECT gen_random_uuid()::uuid, t.id, m.id
FROM tenant t
JOIN modulo_plataforma m ON m.codigo = 'COMPRAS'
ON CONFLICT (tenant_id, modulo_id) DO NOTHING;

-- 1. Fornecedores
CREATE TABLE tb_fornecedor (
    id UUID PRIMARY KEY,
    tenant_id UUID NOT NULL REFERENCES tenant(id),
    cnpj VARCHAR(14) NOT NULL,
    razao_social VARCHAR(180) NOT NULL,
    nome_fantasia VARCHAR(120),
    inscricao_estadual VARCHAR(30),
    email VARCHAR(150),
    telefone VARCHAR(30),
    endereco_logradouro VARCHAR(180),
    endereco_numero VARCHAR(20),
    endereco_bairro VARCHAR(80),
    endereco_cidade VARCHAR(80),
    endereco_uf VARCHAR(2),
    endereco_cep VARCHAR(8),
    observacoes TEXT,
    ativo BOOLEAN NOT NULL DEFAULT TRUE,
    criado_em TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    atualizado_em TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT uk_fornecedor_cnpj_tenant UNIQUE (tenant_id, cnpj)
);

CREATE INDEX idx_fornecedor_tenant ON tb_fornecedor(tenant_id);

-- 2. Pedidos de compra / Autorização de Fornecimento
CREATE TABLE tb_pedido_compra (
    id UUID PRIMARY KEY,
    tenant_id UUID NOT NULL REFERENCES tenant(id),
    numero VARCHAR(20) NOT NULL,
    fornecedor_id UUID NOT NULL REFERENCES tb_fornecedor(id),
    objeto VARCHAR(255),
    data_emissao DATE NOT NULL DEFAULT CURRENT_DATE,
    prazo_entrega DATE,
    contrato_id UUID REFERENCES contrato(id),
    empenho_numero VARCHAR(30),
    valor_total NUMERIC(15, 2) NOT NULL DEFAULT 0,
    status VARCHAR(30) NOT NULL DEFAULT 'EMITIDO',
    observacoes TEXT,
    criado_em TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    atualizado_em TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT chk_pedido_compra_status CHECK (status IN ('EMITIDO', 'RECEBIDO_PARCIAL', 'RECEBIDO', 'CANCELADO'))
);

CREATE INDEX idx_pedido_compra_tenant ON tb_pedido_compra(tenant_id);
CREATE INDEX idx_pedido_compra_numero ON tb_pedido_compra(numero);

CREATE TABLE tb_pedido_compra_item (
    id UUID PRIMARY KEY,
    tenant_id UUID NOT NULL REFERENCES tenant(id),
    pedido_id UUID NOT NULL REFERENCES tb_pedido_compra(id),
    material_id UUID NOT NULL REFERENCES tb_material(id),
    quantidade NUMERIC(15, 3) NOT NULL,
    valor_unitario NUMERIC(15, 4) NOT NULL,
    quantidade_recebida NUMERIC(15, 3) NOT NULL DEFAULT 0
);

CREATE INDEX idx_pedido_compra_item_tenant ON tb_pedido_compra_item(tenant_id);
CREATE INDEX idx_pedido_compra_item_pedido ON tb_pedido_compra_item(pedido_id);

-- 3. Entradas por NFe (aproveitamento no estoque)
CREATE TABLE tb_entrada_nfe (
    id UUID PRIMARY KEY,
    tenant_id UUID NOT NULL REFERENCES tenant(id),
    chave_nfe VARCHAR(44) NOT NULL,
    numero_nfe VARCHAR(9),
    serie VARCHAR(3),
    data_emissao DATE,
    valor_nota NUMERIC(15, 2),
    fornecedor_id UUID REFERENCES tb_fornecedor(id),
    pedido_id UUID NOT NULL REFERENCES tb_pedido_compra(id),
    contrato_id UUID REFERENCES contrato(id),
    empenho_numero VARCHAR(30),
    almoxarifado_id UUID NOT NULL REFERENCES tb_almoxarifado(id),
    tipo_termo VARCHAR(20) NOT NULL DEFAULT 'DEFINITIVO',
    numero_termo VARCHAR(30),
    observacoes TEXT,
    criado_em TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT chk_entrada_nfe_tipo_termo CHECK (tipo_termo IN ('PROVISORIO', 'DEFINITIVO'))
);

CREATE INDEX idx_entrada_nfe_tenant ON tb_entrada_nfe(tenant_id);
CREATE INDEX idx_entrada_nfe_chave ON tb_entrada_nfe(chave_nfe);

-- ==========================================================
-- SEEDS de demonstração (tenant padrão a0eebc99-...):
-- 1 fornecedor, 1 pedido emitido e 1 pedido recebido (banco de preços)
-- ==========================================================

INSERT INTO tb_fornecedor (id, tenant_id, cnpj, razao_social, nome_fantasia, email, telefone,
                           endereco_logradouro, endereco_numero, endereco_bairro, endereco_cidade, endereco_uf, endereco_cep, observacoes, ativo)
VALUES
    ('cccccccc-cccc-4ccc-cccc-ccccccccccc1', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', '11222333000181',
     'PAPELARIA E INFORMATICA LTDA', 'PapelCenter', 'contato@papelcenter.com.br', '(11) 3456-7890',
     'Av. Central', '1200', 'Centro', 'São Paulo', 'SP', '01310100', 'Fornecedor cadastrado via seed de demonstração', true)
ON CONFLICT (tenant_id, cnpj) DO NOTHING;

INSERT INTO tb_pedido_compra (id, tenant_id, numero, fornecedor_id, objeto, data_emissao, prazo_entrega,
                              contrato_id, empenho_numero, valor_total, status, observacoes)
VALUES
    ('dddddddd-dddd-4ddd-dddd-ddddddddddd1', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', 'PC-2026-000001',
     'cccccccc-cccc-4ccc-cccc-ccccccccccc1', 'Papel A4 e material de escritório',
     '2026-09-01', '2026-09-20', NULL, NULL, 1750.00, 'RECEBIDO', NULL),
    ('dddddddd-dddd-4ddd-dddd-ddddddddddd2', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', 'PC-2026-000002',
     'cccccccc-cccc-4ccc-cccc-ccccccccccc1', 'Toners para impressoras',
     '2026-09-05', '2026-09-30', NULL, NULL, 1290.00, 'EMITIDO', NULL)
ON CONFLICT (id) DO NOTHING;

INSERT INTO tb_pedido_compra_item (id, tenant_id, pedido_id, material_id, quantidade, valor_unitario, quantidade_recebida)
VALUES
    ('eeeeeeee-eeee-4eee-eeee-eeeeeeeeeee1', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', 'dddddddd-dddd-4ddd-dddd-ddddddddddd1',
     '77777777-7777-7777-7777-777777777771', 100.000, 17.5000, 100.000),
    ('eeeeeeee-eeee-4eee-eeee-eeeeeeeeeee2', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', 'dddddddd-dddd-4ddd-dddd-ddddddddddd2',
     '77777777-7777-7777-7777-777777777772', 10.000, 129.0000, 0.000)
ON CONFLICT (id) DO NOTHING;