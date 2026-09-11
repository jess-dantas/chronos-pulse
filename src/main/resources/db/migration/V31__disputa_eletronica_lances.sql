-- ==========================================================
-- V31: Disputa eletrônica — lances por item (R29)
--      Tabela de lances incremental por fornecedor/item dentro
--      de uma licitação em fase ABERTA (pregão eletrônico).
-- ==========================================================

CREATE TABLE tb_licitacao_lance (
    id UUID PRIMARY KEY,
    tenant_id UUID NOT NULL REFERENCES tenant(id),
    licitacao_id UUID NOT NULL REFERENCES tb_licitacao(id),
    licitacao_item_id UUID NOT NULL REFERENCES tb_licitacao_item(id),
    fornecedor_id UUID NOT NULL REFERENCES tb_fornecedor(id),
    valor_unitario NUMERIC(15, 4) NOT NULL,
    observacao VARCHAR(255),
    criado_em TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    atualizado_em TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT uk_lance_corrente UNIQUE (licitacao_id, licitacao_item_id, fornecedor_id)
);

CREATE INDEX idx_lance_tenant ON tb_licitacao_lance(tenant_id);
CREATE INDEX idx_lance_licitacao ON tb_licitacao_lance(licitacao_id);
CREATE INDEX idx_lance_item ON tb_licitacao_lance(licitacao_item_id);
CREATE INDEX idx_lance_fornecedor ON tb_licitacao_lance(fornecedor_id);

-- Seeds de demonstração — lances para a licitação em disputa (LIC-2026-000001)
INSERT INTO tb_licitacao_lance (id, tenant_id, licitacao_id, licitacao_item_id, fornecedor_id, valor_unitario, observacao)
VALUES
    ('aaaaaaaa-aaaa-4aaa-8aaa-aaaaaaaaaaaa', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11',
     '86868686-8686-4868-8686-868686868681', '87878787-8787-4787-8787-878787878781',
     'cccccccc-cccc-4ccc-cccc-ccccccccccc1', 16.5000, 'Lance inicial Papel A4'),
    ('aaaaaaaa-aaaa-4aaa-8aaa-aaaaaaaaaaab', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11',
     '86868686-8686-4868-8686-868686868681', '87878787-8787-4787-8787-878787878782',
     'cccccccc-cccc-4ccc-cccc-ccccccccccc1', 125.0000, 'Lance inicial Toner'),
    ('aaaaaaaa-aaaa-4aaa-8aaa-aaaaaaaaaaac', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11',
     '86868686-8686-4868-8686-868686868681', '87878787-8787-4787-8787-878787878781',
     'cccccccc-cccc-4ccc-cccc-ccccccccccc2', 17.2000, NULL),
    ('aaaaaaaa-aaaa-4aaa-8aaa-aaaaaaaaaaad', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11',
     '86868686-8686-4868-8686-868686868681', '87878787-8787-4787-8787-878787878782',
     'cccccccc-cccc-4ccc-cccc-ccccccccccc2', 130.0000, NULL)
ON CONFLICT (licitacao_id, licitacao_item_id, fornecedor_id) DO NOTHING;
