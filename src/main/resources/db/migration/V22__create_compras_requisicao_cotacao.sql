-- ==========================================================
-- V22: Módulo Compras — requisição de compra e cotação de
--      preços (fluxo preparatório que antecede o pedido/AF).
-- ==========================================================

-- 1. Requisições de compra (solicitação de itens)
CREATE TABLE tb_requisicao_compra (
    id UUID PRIMARY KEY,
    tenant_id UUID NOT NULL REFERENCES tenant(id),
    numero VARCHAR(20) NOT NULL,
    solicitante_cpc_id UUID NOT NULL REFERENCES cpc_usuario(id),
    justificativa TEXT NOT NULL,
    data_requisicao DATE NOT NULL DEFAULT CURRENT_DATE,
    observacoes TEXT,
    status VARCHAR(30) NOT NULL DEFAULT 'EM_ABERTO',
    criado_em TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    atualizado_em TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT uk_requisicao_numero_tenant UNIQUE (tenant_id, numero),
    CONSTRAINT chk_requisicao_status CHECK (status IN ('EM_ABERTO', 'COTADA', 'CANCELADA'))
);

CREATE INDEX idx_requisicao_tenant ON tb_requisicao_compra(tenant_id);
CREATE INDEX idx_requisicao_solicitante ON tb_requisicao_compra(solicitante_cpc_id);

CREATE TABLE tb_requisicao_compra_item (
    id UUID PRIMARY KEY,
    tenant_id UUID NOT NULL REFERENCES tenant(id),
    requisicao_id UUID NOT NULL REFERENCES tb_requisicao_compra(id),
    material_id UUID NOT NULL REFERENCES tb_material(id),
    quantidade NUMERIC(15, 3) NOT NULL,
    observacao VARCHAR(255)
);

CREATE INDEX idx_requisicao_item_tenant ON tb_requisicao_compra_item(tenant_id);
CREATE INDEX idx_requisicao_item_requisicao ON tb_requisicao_compra_item(requisicao_id);

-- 2. Cotações de preço (convidam fornecedores e capturam propostas)
CREATE TABLE tb_cotacao_compra (
    id UUID PRIMARY KEY,
    tenant_id UUID NOT NULL REFERENCES tenant(id),
    numero VARCHAR(20) NOT NULL,
    requisicao_id UUID NOT NULL REFERENCES tb_requisicao_compra(id),
    data_limite DATE,
    observacoes TEXT,
    status VARCHAR(30) NOT NULL DEFAULT 'EM_ANDAMENTO',
    pedido_gerado BOOLEAN NOT NULL DEFAULT FALSE,
    criado_em TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    atualizado_em TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT uk_cotacao_numero_tenant UNIQUE (tenant_id, numero),
    CONSTRAINT uk_cotacao_requisicao UNIQUE (requisicao_id),
    CONSTRAINT chk_cotacao_status CHECK (status IN ('EM_ANDAMENTO', 'CONCLUIDA', 'CANCELADA'))
);

CREATE INDEX idx_cotacao_tenant ON tb_cotacao_compra(tenant_id);
CREATE INDEX idx_cotacao_requisicao ON tb_cotacao_compra(requisicao_id);

CREATE TABLE tb_cotacao_fornecedor (
    id UUID PRIMARY KEY,
    tenant_id UUID NOT NULL REFERENCES tenant(id),
    cotacao_id UUID NOT NULL REFERENCES tb_cotacao_compra(id),
    fornecedor_id UUID NOT NULL REFERENCES tb_fornecedor(id),
    CONSTRAINT uk_cotacao_fornecedor UNIQUE (cotacao_id, fornecedor_id)
);

CREATE INDEX idx_cotacao_fornecedor_tenant ON tb_cotacao_fornecedor(tenant_id);
CREATE INDEX idx_cotacao_fornecedor_cotacao ON tb_cotacao_fornecedor(cotacao_id);

CREATE TABLE tb_cotacao_proposta (
    id UUID PRIMARY KEY,
    tenant_id UUID NOT NULL REFERENCES tenant(id),
    cotacao_id UUID NOT NULL REFERENCES tb_cotacao_compra(id),
    fornecedor_id UUID NOT NULL REFERENCES tb_fornecedor(id),
    material_id UUID NOT NULL REFERENCES tb_material(id),
    valor_unitario NUMERIC(15, 4) NOT NULL,
    observacao VARCHAR(255),
    vencedor BOOLEAN NOT NULL DEFAULT FALSE,
    CONSTRAINT uk_cotacao_proposta UNIQUE (cotacao_id, fornecedor_id, material_id)
);

CREATE INDEX idx_cotacao_proposta_tenant ON tb_cotacao_proposta(tenant_id);
CREATE INDEX idx_cotacao_proposta_cotacao ON tb_cotacao_proposta(cotacao_id);

-- ==========================================================
-- SEEDS de demonstração (tenant padrão a0eebc99-...):
-- requisição em aberto + cotação em andamento com propostas.
-- ==========================================================

INSERT INTO tb_fornecedor (id, tenant_id, cnpj, razao_social, nome_fantasia, email, telefone,
                           endereco_logradouro, endereco_numero, endereco_bairro, endereco_cidade, endereco_uf, endereco_cep, observacoes, ativo)
VALUES
    ('cccccccc-cccc-4ccc-cccc-ccccccccccc2', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', '22334455000190',
     'SUPRIMENTOS E UTENSILIOS LTDA', 'Suprimentos Center', 'vendas@suprimentoscenter.com.br', '(11) 2222-3344',
     'Rua das Oficinas', '340', 'Industrial', 'Campinas', 'SP', '13010000', 'Fornecedor cadastrado via seed de demonstração', true)
ON CONFLICT (tenant_id, cnpj) DO NOTHING;

INSERT INTO tb_requisicao_compra (id, tenant_id, numero, solicitante_cpc_id, justificativa, data_requisicao, observacoes, status)
VALUES
    ('88888888-8888-4888-8888-888888888881', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', 'RC-2026-000001',
     '33333333-3333-3333-3333-333333333333', 'Reposição de material de escritório para o setor administrativo',
     '2026-09-10', 'Solicitar propostas de ao menos dois fornecedores', 'EM_ABERTO')
ON CONFLICT (tenant_id, numero) DO NOTHING;

INSERT INTO tb_requisicao_compra_item (id, tenant_id, requisicao_id, material_id, quantidade, observacao)
VALUES
    ('99999999-9999-4999-9999-999999999991', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', '88888888-8888-4888-8888-888888888881',
     '77777777-7777-7777-7777-777777777771', 50.000, 'Resma 500 folhas'),
    ('99999999-9999-4999-9999-999999999992', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', '88888888-8888-4888-8888-888888888881',
     '77777777-7777-7777-7777-777777777772', 20.000, 'Azul')
ON CONFLICT (id) DO NOTHING;

INSERT INTO tb_cotacao_compra (id, tenant_id, numero, requisicao_id, data_limite, observacoes, status, pedido_gerado)
VALUES
    ('aaaaaaaa-aaaa-4aaa-aaaa-aaaaaaaaaaa1', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', 'COT-2026-000001',
     '88888888-8888-4888-8888-888888888881', '2026-09-30', 'Cotação de demonstração para a requisição RC-2026-000001', 'EM_ANDAMENTO', FALSE)
ON CONFLICT (tenant_id, numero) DO NOTHING;

INSERT INTO tb_cotacao_fornecedor (id, tenant_id, cotacao_id, fornecedor_id)
VALUES
    ('bbbbbbbb-bbbb-4bbb-bbbb-bbbbbbbbbbb1', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', 'aaaaaaaa-aaaa-4aaa-aaaa-aaaaaaaaaaa1',
     'cccccccc-cccc-4ccc-cccc-ccccccccccc1'),
    ('bbbbbbbb-bbbb-4bbb-bbbb-bbbbbbbbbbb2', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', 'aaaaaaaa-aaaa-4aaa-aaaa-aaaaaaaaaaa1',
     'cccccccc-cccc-4ccc-cccc-ccccccccccc2')
ON CONFLICT (cotacao_id, fornecedor_id) DO NOTHING;

INSERT INTO tb_cotacao_proposta (id, tenant_id, cotacao_id, fornecedor_id, material_id, valor_unitario, observacao, vencedor)
VALUES
    ('cccccccc-1ccc-4ccc-8ccc-ccccccccccc1', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', 'aaaaaaaa-aaaa-4aaa-aaaa-aaaaaaaaaaa1',
     'cccccccc-cccc-4ccc-cccc-ccccccccccc1', '77777777-7777-7777-7777-777777777771', 16.5000, NULL, FALSE),
    ('cccccccc-1ccc-4ccc-8ccc-ccccccccccc2', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', 'aaaaaaaa-aaaa-4aaa-aaaa-aaaaaaaaaaa1',
     'cccccccc-cccc-4ccc-cccc-ccccccccccc1', '77777777-7777-7777-7777-777777777772', 125.0000, NULL, FALSE),
    ('cccccccc-1ccc-4ccc-8ccc-ccccccccccc3', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', 'aaaaaaaa-aaaa-4aaa-aaaa-aaaaaaaaaaa1',
     'cccccccc-cccc-4ccc-cccc-ccccccccccc2', '77777777-7777-7777-7777-777777777771', 17.0000, NULL, FALSE),
    ('cccccccc-1ccc-4ccc-8ccc-ccccccccccc4', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', 'aaaaaaaa-aaaa-4aaa-aaaa-aaaaaaaaaaa1',
     'cccccccc-cccc-4ccc-cccc-ccccccccccc2', '77777777-7777-7777-7777-777777777772', 122.0000, NULL, FALSE)
ON CONFLICT (cotacao_id, fornecedor_id, material_id) DO NOTHING;