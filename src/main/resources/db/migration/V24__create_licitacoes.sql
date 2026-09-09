-- ==========================================================
-- V24: Módulo Licitações — processo licitatório conforme a
--      Lei 14.133/2021 (fase preparatória, publicação, propostas,
--      julgamento por menor preço, adjudicação, homologação e
--      emissão de AF/pedido para os vencedores).
-- ==========================================================

INSERT INTO modulo_plataforma (id, codigo, nome, descricao, ativo) VALUES
    ('11111111-1111-4111-8111-111111111108', 'LICITACOES', 'Licitações & Contratações',
     'Licitações conforme a Lei 14.133/2021: modalidades, julgamento por menor preço, adjudicação, homologação e emissão de AF', true)
ON CONFLICT (codigo) DO NOTHING;

-- Ativa LICITACOES para todas as empresas existentes (vem ativo no catálogo)
INSERT INTO empresa_modulo (id, tenant_id, modulo_id)
SELECT gen_random_uuid()::uuid, t.id, m.id
FROM tenant t
JOIN modulo_plataforma m ON m.codigo = 'LICITACOES'
ON CONFLICT (tenant_id, modulo_id) DO NOTHING;

-- 1. Licitações
CREATE TABLE tb_licitacao (
    id UUID PRIMARY KEY,
    tenant_id UUID NOT NULL REFERENCES tenant(id),
    numero VARCHAR(20) NOT NULL,
    modalidade VARCHAR(30) NOT NULL,
    tipo_julgamento VARCHAR(30) NOT NULL,
    objeto TEXT NOT NULL,
    data_abertura DATE,
    valor_estimado NUMERIC(15, 2),
    observacoes TEXT,
    status VARCHAR(30) NOT NULL DEFAULT 'EM_ELABORACAO',
    pedido_gerado BOOLEAN NOT NULL DEFAULT FALSE,
    criado_em TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    atualizado_em TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT uk_licitacao_numero_tenant UNIQUE (tenant_id, numero),
    CONSTRAINT chk_licitacao_modalidade CHECK (modalidade IN ('PREGAO', 'CONCORRENCIA', 'LEILAO', 'CONCURSO', 'DIALOGO_COMPETITIVO')),
    CONSTRAINT chk_licitacao_julgamento CHECK (tipo_julgamento IN ('MENOR_PRECO', 'MAIOR_DESCONTO', 'MELHOR_TECNICA', 'TECNICA_E_PRECO', 'MAIOR_LANCE')),
    CONSTRAINT chk_licitacao_status CHECK (status IN ('EM_ELABORACAO', 'PUBLICADA', 'ABERTA', 'ADJUDICADA', 'HOMOLOGADA', 'CANCELADA'))
);

CREATE INDEX idx_licitacao_tenant ON tb_licitacao(tenant_id);
CREATE INDEX idx_licitacao_numero ON tb_licitacao(numero);

-- 2. Itens da licitação (objeto detalhado com estimativa)
CREATE TABLE tb_licitacao_item (
    id UUID PRIMARY KEY,
    tenant_id UUID NOT NULL REFERENCES tenant(id),
    licitacao_id UUID NOT NULL REFERENCES tb_licitacao(id),
    material_id UUID NOT NULL REFERENCES tb_material(id),
    descricao VARCHAR(255) NOT NULL,
    quantidade NUMERIC(15, 3) NOT NULL,
    valor_estimado_unitario NUMERIC(15, 4)
);

CREATE INDEX idx_licitacao_item_tenant ON tb_licitacao_item(tenant_id);
CREATE INDEX idx_licitacao_item_licitacao ON tb_licitacao_item(licitacao_id);

-- 3. Participantes (fornecedores habilitados a apresentar proposta)
CREATE TABLE tb_licitacao_participante (
    id UUID PRIMARY KEY,
    tenant_id UUID NOT NULL REFERENCES tenant(id),
    licitacao_id UUID NOT NULL REFERENCES tb_licitacao(id),
    fornecedor_id UUID NOT NULL REFERENCES tb_fornecedor(id),
    habilitado BOOLEAN NOT NULL DEFAULT TRUE,
    CONSTRAINT uk_licitacao_participante UNIQUE (licitacao_id, fornecedor_id)
);

CREATE INDEX idx_licitacao_participante_tenant ON tb_licitacao_participante(tenant_id);
CREATE INDEX idx_licitacao_participante_licitacao ON tb_licitacao_participante(licitacao_id);

-- 4. Propostas (uma por fornecedor/item; vencedor definido por menor preço)
CREATE TABLE tb_licitacao_proposta (
    id UUID PRIMARY KEY,
    tenant_id UUID NOT NULL REFERENCES tenant(id),
    licitacao_id UUID NOT NULL REFERENCES tb_licitacao(id),
    fornecedor_id UUID NOT NULL REFERENCES tb_fornecedor(id),
    material_id UUID NOT NULL REFERENCES tb_material(id),
    valor_unitario NUMERIC(15, 4) NOT NULL,
    observacao VARCHAR(255),
    vencedor BOOLEAN NOT NULL DEFAULT FALSE,
    CONSTRAINT uk_licitacao_proposta UNIQUE (licitacao_id, fornecedor_id, material_id)
);

CREATE INDEX idx_licitacao_proposta_tenant ON tb_licitacao_proposta(tenant_id);
CREATE INDEX idx_licitacao_proposta_licitacao ON tb_licitacao_proposta(licitacao_id);

-- ==========================================================
-- SEEDS de demonstração (tenant padrão a0eebc99-...):
-- licitação pública em disputa + licitação adjudicada (resultados)
-- ==========================================================

INSERT INTO tb_licitacao (id, tenant_id, numero, modalidade, tipo_julgamento, objeto, data_abertura, valor_estimado, observacoes, status, pedido_gerado)
VALUES
    ('86868686-8686-4868-8686-868686868681', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', 'LIC-2026-000001',
     'PREGAO', 'MENOR_PRECO', 'Aquisição de material de escritório e insumos de informática',
     '2026-10-05', 3850.00, 'Licitação de demonstração em fase de propostas', 'PUBLICADA', FALSE),
    ('86868686-8686-4868-8686-868686868682', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', 'LIC-2026-000002',
     'CONCORRENCIA', 'MENOR_PRECO', 'Contratação de serviços de manutenção predial preventiva',
     '2026-09-15', 50000.00, 'Licitação de demonstração com vencedores definidos', 'ADJUDICADA', FALSE)
ON CONFLICT (tenant_id, numero) DO NOTHING;

INSERT INTO tb_licitacao_item (id, tenant_id, licitacao_id, material_id, descricao, quantidade, valor_estimado_unitario)
VALUES
    ('87878787-8787-4787-8787-878787878781', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', '86868686-8686-4868-8686-868686868681',
     '77777777-7777-7777-7777-777777777771', 'Papel A4 (resma 500 folhas)', 100.000, 17.5000),
    ('87878787-8787-4787-8787-878787878782', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', '86868686-8686-4868-8686-868686868681',
     '77777777-7777-7777-7777-777777777772', 'Toner para impressora', 10.000, 129.0000),
    ('87878787-8787-4787-8787-878787878783', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', '86868686-8686-4868-8686-868686868682',
     '77777777-7777-7777-7777-777777777771', 'Papel A4 (resma 500 folhas)', 500.000, 18.0000)
ON CONFLICT (id) DO NOTHING;

INSERT INTO tb_licitacao_participante (id, tenant_id, licitacao_id, fornecedor_id, habilitado)
VALUES
    ('85858585-8585-4585-8585-858585858581', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', '86868686-8686-4868-8686-868686868681',
     'cccccccc-cccc-4ccc-cccc-ccccccccccc1', TRUE),
    ('85858585-8585-4585-8585-858585858582', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', '86868686-8686-4868-8686-868686868681',
     'cccccccc-cccc-4ccc-cccc-ccccccccccc2', TRUE),
    ('85858585-8585-4585-8585-858585858583', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', '86868686-8686-4868-8686-868686868682',
     'cccccccc-cccc-4ccc-cccc-ccccccccccc1', TRUE),
    ('85858585-8585-4585-8585-858585858584', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', '86868686-8686-4868-8686-868686868682',
     'cccccccc-cccc-4ccc-cccc-ccccccccccc2', TRUE)
ON CONFLICT (licitacao_id, fornecedor_id) DO NOTHING;

INSERT INTO tb_licitacao_proposta (id, tenant_id, licitacao_id, fornecedor_id, material_id, valor_unitario, observacao, vencedor)
VALUES
    ('89898989-8989-4989-8989-898989898981', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', '86868686-8686-4868-8686-868686868682',
     'cccccccc-cccc-4ccc-cccc-ccccccccccc1', '77777777-7777-7777-7777-777777777771', 17.0000, 'Preço de tabela com desconto', TRUE),
    ('89898989-8989-4989-8989-898989898982', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', '86868686-8686-4868-8686-868686868682',
     'cccccccc-cccc-4ccc-cccc-ccccccccccc2', '77777777-7777-7777-7777-777777777771', 18.5000, NULL, FALSE)
ON CONFLICT (licitacao_id, fornecedor_id, material_id) DO NOTHING;