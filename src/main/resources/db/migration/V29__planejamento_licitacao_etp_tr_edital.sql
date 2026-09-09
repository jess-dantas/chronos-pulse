-- ==========================================================
-- V29: Fase preparatória da contratação (Lei 14.133/2021) —
--      Estudo Técnico Preliminar (ETP), Termo de Referência (TR)
--      e Edital vinculados a uma licitação (tb_licitacao do R24).
--      Fluxo: ETP (RASCUNHO -> APROVADO) -> TR (RASCUNHO -> APROVADO)
--             -> Edital (EM_ELABORACAO -> PUBLICADO).
-- ==========================================================

-- 1. Estudo Técnico Preliminar (um por licitação)
CREATE TABLE tb_licitacao_etp (
    id UUID PRIMARY KEY,
    licitacao_id UUID NOT NULL UNIQUE REFERENCES tb_licitacao(id),
    tenant_id UUID NOT NULL REFERENCES tenant(id),
    objeto TEXT NOT NULL,
    justificativa TEXT NOT NULL,
    requisitos TEXT NOT NULL,
    alternativas TEXT,
    valor_estimado NUMERIC(15, 2),
    riscos TEXT,
    conclusao TEXT,
    responsavel VARCHAR(255),
    status VARCHAR(20) NOT NULL DEFAULT 'RASCUNHO',
    data_aprovacao TIMESTAMPTZ,
    criado_em TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    atualizado_em TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT chk_etp_status CHECK (status IN ('RASCUNHO', 'APROVADO'))
);

CREATE INDEX idx_licitacao_etp_tenant ON tb_licitacao_etp(tenant_id);
CREATE INDEX idx_licitacao_etp_licitacao ON tb_licitacao_etp(licitacao_id);

-- 2. Termo de Referência (um por licitação, baseado em um ETP aprovado)
CREATE TABLE tb_licitacao_tr (
    id UUID PRIMARY KEY,
    licitacao_id UUID NOT NULL UNIQUE REFERENCES tb_licitacao(id),
    etp_id UUID NOT NULL UNIQUE REFERENCES tb_licitacao_etp(id),
    tenant_id UUID NOT NULL REFERENCES tenant(id),
    especificacoes TEXT NOT NULL,
    condicoes_fornecimento TEXT NOT NULL,
    obrigacoes TEXT NOT NULL,
    criterios_aceitacao TEXT NOT NULL,
    prazos_entrega TEXT NOT NULL,
    garantia TEXT,
    forma_pagamento TEXT,
    responsavel VARCHAR(255),
    status VARCHAR(20) NOT NULL DEFAULT 'RASCUNHO',
    data_aprovacao TIMESTAMPTZ,
    criado_em TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    atualizado_em TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT chk_tr_status CHECK (status IN ('RASCUNHO', 'APROVADO'))
);

CREATE INDEX idx_licitacao_tr_tenant ON tb_licitacao_tr(tenant_id);
CREATE INDEX idx_licitacao_tr_licitacao ON tb_licitacao_tr(licitacao_id);

-- 3. Edital (um por licitação, referência ao TR e dados da sessão pública)
CREATE TABLE tb_licitacao_edital (
    id UUID PRIMARY KEY,
    licitacao_id UUID NOT NULL UNIQUE REFERENCES tb_licitacao(id),
    tr_id UUID REFERENCES tb_licitacao_tr(id),
    tenant_id UUID NOT NULL REFERENCES tenant(id),
    numero_processo VARCHAR(40),
    numero_edital VARCHAR(40),
    local_sessao VARCHAR(255),
    data_abertura_sessao DATE,
    horario_abertura TIME,
    forma_entrega_propostas VARCHAR(20),
    anexos TEXT,
    observacoes TEXT,
    status VARCHAR(20) NOT NULL DEFAULT 'EM_ELABORACAO',
    data_publicacao TIMESTAMPTZ,
    criado_em TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    atualizado_em TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT chk_edital_status CHECK (status IN ('EM_ELABORACAO', 'PUBLICADO')),
    CONSTRAINT chk_edital_forma_entrega CHECK (
        forma_entrega_propostas IS NULL OR
        forma_entrega_propostas IN ('PRESENCIAL', 'ELETRONICA'))
);

CREATE INDEX idx_licitacao_edital_tenant ON tb_licitacao_edital(tenant_id);
CREATE INDEX idx_licitacao_edital_licitacao ON tb_licitacao_edital(licitacao_id);

-- ==========================================================
-- SEEDS de demonstração: licitação LIC-2026-000001 (PUBLICADA) com
-- planejamento completo (ETP aprovado -> TR aprovado -> Edital publicado).
-- ==========================================================

INSERT INTO tb_licitacao_etp (id, licitacao_id, tenant_id, objeto, justificativa, requisitos,
                              alternativas, valor_estimado, riscos, conclusao, responsavel,
                              status, data_aprovacao)
VALUES ('9a9a9a9a-9a9a-49a9-9a9a-9a9a9a9a9a01',
        '86868686-8686-4868-8686-868686868681',
        'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11',
        'Aquisição de material de escritório e insumos de informática',
        'Suprimento contínuo de papel, toners e insumos básicos para os setores administrativos da Câmara Municipal.',
        'Produtos de fabricação nacional, entregues em lotes parcelados e com validade mínima de 12 meses.',
        'Locação de impressora com suprimentos inclusos; contratação por catálogo eletrônico.',
        3850.00,
        'Variação cambial reduzida (produtos nacionais); risco de entrega mitigado por cronograma de lotes.',
        'Compra direcionada por pregão, dada a padronização do objeto e multiplicidade de fornecedores.',
        'Comissão de Contratação',
        'APROVADO', NOW() - INTERVAL '5 days')
ON CONFLICT (licitacao_id) DO NOTHING;

INSERT INTO tb_licitacao_tr (id, licitacao_id, etp_id, tenant_id, especificacoes,
                             condicoes_fornecimento, obrigacoes, criterios_aceitacao,
                             prazos_entrega, garantia, forma_pagamento, responsavel,
                             status, data_aprovacao)
VALUES ('9a9a9a9a-9a9a-49a9-9a9a-9a9a9a9a9a02',
        '86868686-8686-4868-8686-868686868681',
        '9a9a9a9a-9a9a-49a9-9a9a-9a9a9a9a9a01',
        'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11',
        'Papel A4 sulfite 75g/m² (resma 500 folhas) e toner original compatível com impressora instalada, em conformidade com a ABNT NBR 6183.',
        'Entrega parcelada em até 10 dias úteis após a autorização de fornecimento, nos horários de expediente.',
        'Fornecedor responsável por substituição de produtos com defeito ou avaria no prazo de 5 dias úteis.',
        'Recebimento efetivado mediante conferência quantitativa e qualitativa pela equipe de almoxarifado.',
        'Primeiro lote em até 10 dias úteis; demais lotes conforme cronograma da autorização.',
        'Garantia mínima de 90 dias contra defeitos de fabricação.',
        'Empenho + liquidação após atesto de entrega.',
        'Comissão de Contratação',
        'APROVADO', NOW() - INTERVAL '3 days')
ON CONFLICT (licitacao_id) DO NOTHING;

INSERT INTO tb_licitacao_edital (id, licitacao_id, tr_id, tenant_id, numero_processo,
                                 numero_edital, local_sessao, data_abertura_sessao,
                                 horario_abertura, forma_entrega_propostas, anexos,
                                 observacoes, status, data_publicacao)
VALUES ('9a9a9a9a-9a9a-49a9-9a9a-9a9a9a9a9a03',
        '86868686-8686-4868-8686-868686868681',
        '9a9a9a9a-9a9a-49a9-9a9a-9a9a9a9a9a02',
        'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11',
        'PA-2026-0001',
        'ED-2026-0001',
        'Sala de Sessões da Câmara Municipal — Av. Principal, 100',
        '2026-10-05',
        '09:00:00',
        'ELETRONICA',
        'Anexo I - Termo de Referência; Anexo II - Minuta de contrato.',
        'Licitação de demonstração com planejamento em conformidade com a Lei 14.133/2021.',
        'PUBLICADO', NOW() - INTERVAL '1 day')
ON CONFLICT (licitacao_id) DO NOTHING;