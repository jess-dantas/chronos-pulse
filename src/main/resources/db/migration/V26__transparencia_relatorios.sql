-- ==========================================================
-- V26: Portal da Transparência (LC 131/2009) e relatórios
--      gerenciais (BI) — publicações oficiais por competência
--      + resumo consolidado da gestão orçamentária/fiscal.
-- ==========================================================

INSERT INTO modulo_plataforma (id, codigo, nome, descricao, ativo) VALUES
    ('11111111-1111-4111-8111-111111111109', 'TRANSPARENCIA', 'Portal da Transparência',
     'Publicação e divulgação das despesas da gestão conforme a LC 131/2009 (transparência ativa) e relatórios gerenciais (BI)', true)
ON CONFLICT (codigo) DO NOTHING;

-- Ativa TRANSPARENCIA para todas as empresas existentes (vem ativo no catálogo)
INSERT INTO empresa_modulo (id, tenant_id, modulo_id)
SELECT gen_random_uuid()::uuid, t.id, m.id
FROM tenant t
JOIN modulo_plataforma m ON m.codigo = 'TRANSPARENCIA'
ON CONFLICT (tenant_id, modulo_id) DO NOTHING;

-- 1. Publicações oficiais (portal da transparência ativa)
CREATE TABLE tb_transparencia_publicacao (
    id UUID PRIMARY KEY,
    tenant_id UUID NOT NULL REFERENCES tenant(id),
    competencia VARCHAR(7) NOT NULL,
    tipo_publicacao VARCHAR(30) NOT NULL,
    valor_total NUMERIC(15, 2) NOT NULL DEFAULT 0,
    itens_count INTEGER NOT NULL DEFAULT 0,
    status VARCHAR(20) NOT NULL DEFAULT 'EM_ELABORACAO',
    data_publicacao DATE,
    observacoes TEXT,
    criado_em TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    atualizado_em TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT uk_publicacao_competencia_tipo UNIQUE (tenant_id, competencia, tipo_publicacao),
    CONSTRAINT chk_publicacao_competencia CHECK (competencia ~ '^\d{4}-(0[1-9]|1[0-2])$'),
    CONSTRAINT chk_publicacao_tipo CHECK (tipo_publicacao IN ('RECEITAS', 'DESPESAS', 'COMPRAS', 'LICITACOES', 'CONTRATOS', 'FROTA', 'PATRIMONIO', 'FOLHA')),
    CONSTRAINT chk_publicacao_status CHECK (status IN ('EM_ELABORACAO', 'PUBLICADO'))
);

CREATE INDEX idx_transparencia_tenant ON tb_transparencia_publicacao(tenant_id);
CREATE INDEX idx_transparencia_competencia ON tb_transparencia_publicacao(tenant_id, competencia);

-- ==========================================================
-- SEEDS de demonstração (tenant padrão a0eebc99-...)
-- ==========================================================

INSERT INTO tb_transparencia_publicacao (id, tenant_id, competencia, tipo_publicacao, valor_total, itens_count, status, data_publicacao, observacoes)
VALUES
    ('f1f1f1f1-f1f1-4f11-81f1-f1f1f1f1f1e1', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', '2026-06', 'DESPESAS', 48250.00, 12, 'PUBLICADO', '2026-07-10', 'Despesas empenhadas e liquidadas em junho/2026'),
    ('f1f1f1f1-f1f1-4f11-81f1-f1f1f1f1f1e2', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', '2026-07', 'DESPESAS', 51890.50, 15, 'PUBLICADO', '2026-08-10', 'Despesas empenhadas e liquidadas em julho/2026'),
    ('f1f1f1f1-f1f1-4f11-81f1-f1f1f1f1f1e3', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', '2026-08', 'DESPESAS', 0.00, 0, 'EM_ELABORACAO', NULL, 'Em elaboração (fechamento fiscal)'),
    ('f1f1f1f1-f1f1-4f11-81f1-f1f1f1f1f1e4', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', '2026-08', 'COMPRAS', 20485.00, 9, 'PUBLICADO', '2026-09-05', 'Compras e contratações de agosto/2026'),
    ('f1f1f1f1-f1f1-4f11-81f1-f1f1f1f1f1e5', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', '2026-08', 'LICITACOES', 53850.00, 2, 'PUBLICADO', '2026-09-05', 'Licitações do período — valor estimado')
ON CONFLICT (id) DO NOTHING;