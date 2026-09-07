-- ==========================================================
-- V11: Plataforma modular — catálogo de módulos, ativação por
--      CNPJ (tenant) e novos módulos: Patrimônio, Frota, Protocolo
-- ==========================================================

-- 1. Catálogo de módulos comercializáveis
CREATE TABLE modulo_plataforma (
    id UUID PRIMARY KEY,
    codigo VARCHAR(40) NOT NULL UNIQUE,
    nome VARCHAR(120) NOT NULL,
    descricao VARCHAR(500),
    ativo BOOLEAN DEFAULT TRUE
);

-- 2. Módulos ativados por empresa (tenant)
CREATE TABLE empresa_modulo (
    id UUID PRIMARY KEY,
    tenant_id UUID NOT NULL REFERENCES tenant(id),
    modulo_id UUID NOT NULL REFERENCES modulo_plataforma(id),
    ativado_em TIMESTAMPTZ DEFAULT NOW(),
    CONSTRAINT uk_empresa_modulo UNIQUE (tenant_id, modulo_id)
);

CREATE INDEX idx_empresa_modulo_tenant ON empresa_modulo(tenant_id);
CREATE INDEX idx_empresa_modulo_modulo ON empresa_modulo(modulo_id);

-- 3. Seed do catálogo
INSERT INTO modulo_plataforma (id, codigo, nome, descricao, ativo) VALUES
    ('11111111-1111-4111-8111-111111111101', 'PONTO',            'Ponto Eletrônico',              'Registro, espelho e exportação fiscal de ponto eletrônico', true),
    ('11111111-1111-4111-8111-111111111102', 'RECURSOS_HUMANOS', 'Recursos Humanos',              'Cadastro de colaboradores e gestão de equipe',                true),
    ('11111111-1111-4111-8111-111111111103', 'ESTOQUE',          'Estoque & Almoxarifado',        'Controle de saldos, entradas, saídas e requisições',         true),
    ('11111111-1111-4111-8111-111111111104', 'PATRIMONIO',       'Patrimônio Público',            'Tombamento e controle de bens patrimoniais',                 true),
    ('11111111-1111-4111-8111-111111111105', 'FROTA',            'Gestão de Frota',               'Veículos, abastecimentos e quilometragem',                   true),
    ('11111111-1111-4111-8111-111111111106', 'PROTOCOLO',        'Protocolo & Tramitação',        'Protocolo eletrônico de documentos e processos',             true)
ON CONFLICT (codigo) DO NOTHING;

-- 4. Ativa módulos core para TODAS as empresas existentes
INSERT INTO empresa_modulo (id, tenant_id, modulo_id)
SELECT gen_random_uuid()::uuid, t.id, m.id
FROM tenant t
JOIN modulo_plataforma m ON m.codigo IN ('PONTO', 'RECURSOS_HUMANOS', 'ESTOQUE')
ON CONFLICT (tenant_id, modulo_id) DO NOTHING;

-- 5. Ativa todos os módulos novos para o tenant padrão/demonstração
INSERT INTO empresa_modulo (id, tenant_id, modulo_id)
SELECT gen_random_uuid()::uuid, t.id, m.id
FROM tenant t
JOIN modulo_plataforma m ON m.codigo IN ('PATRIMONIO', 'FROTA', 'PROTOCOLO')
WHERE t.id = 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11'
ON CONFLICT (tenant_id, modulo_id) DO NOTHING;

-- ==========================================================
-- MÓDULO: Patrimônio Público
-- ==========================================================
CREATE TABLE tb_patrimonio (
    id UUID PRIMARY KEY,
    tenant_id UUID NOT NULL REFERENCES tenant(id),
    tombamento VARCHAR(30),
    descricao VARCHAR(255) NOT NULL,
    categoria VARCHAR(60),
    estado VARCHAR(30) NOT NULL DEFAULT 'BOM',
    localizacao VARCHAR(120),
    data_aquisicao DATE,
    valor_aquisicao NUMERIC(15, 2),
    responsavel_nome VARCHAR(120),
    numero_nota_fiscal VARCHAR(30),
    observacoes TEXT,
    ativo BOOLEAN DEFAULT TRUE,
    CONSTRAINT chk_patrimonio_estado CHECK (estado IN ('NOVO', 'OTIMO', 'BOM', 'REGULAR', 'INSERVIVEL'))
);

CREATE INDEX idx_patrimonio_tenant ON tb_patrimonio(tenant_id);
CREATE INDEX idx_patrimonio_tombamento ON tb_patrimonio(tombamento);

INSERT INTO tb_patrimonio (id, tenant_id, tombamento, descricao, categoria, estado, localizacao, data_aquisicao, valor_aquisicao, responsavel_nome, numero_nota_fiscal, ativo)
VALUES
    ('88888888-8888-4888-8888-888888888881', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', 'TOM-0001', 'Notebook Dell Latitude 7420 (i7, 16GB)', 'INFORMATICA', 'BOM', 'Gabinete do Prefeito', '2024-03-15', 6850.00, 'Fulano de Tal', 'NF 4521', true),
    ('88888888-8888-4888-8888-888888888882', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', 'TOM-0002', 'Impressora Multifuncional Laser HP', 'INFORMATICA', 'REGULAR', 'Protocolo Geral', '2023-07-01', 1250.00, 'Maria Silva', 'NF 3802', true),
    ('88888888-8888-4888-8888-888888888883', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', 'TOM-0003', 'Veículo Palio Adventure 1.8 Flex', 'VEICULOS', 'BOM', 'Garagem Municipal', '2022-05-10', 88000.00, 'José Oliveira', 'NF 2140', true)
ON CONFLICT (id) DO NOTHING;

-- ==========================================================
-- MÓDULO: Gestão de Frota
-- ==========================================================
CREATE TABLE tb_frota_veiculo (
    id UUID PRIMARY KEY,
    tenant_id UUID NOT NULL REFERENCES tenant(id),
    placa VARCHAR(10) NOT NULL,
    renavam VARCHAR(20),
    marca VARCHAR(60),
    modelo VARCHAR(60),
    ano_fabricacao INTEGER,
    ano_modelo INTEGER,
    tipo VARCHAR(30),
    combustivel VARCHAR(30),
    status VARCHAR(20) NOT NULL DEFAULT 'ATIVO',
    odometro_atual NUMERIC(12, 1),
    observacoes TEXT,
    ativo BOOLEAN DEFAULT TRUE,
    CONSTRAINT chk_frota_status CHECK (status IN ('ATIVO', 'MANUTENCAO', 'INATIVO'))
);

CREATE INDEX idx_frota_veiculo_tenant ON tb_frota_veiculo(tenant_id);
CREATE INDEX idx_frota_veiculo_placa ON tb_frota_veiculo(placa);

CREATE TABLE tb_frota_abastecimento (
    id UUID PRIMARY KEY,
    tenant_id UUID NOT NULL REFERENCES tenant(id),
    veiculo_id UUID NOT NULL REFERENCES tb_frota_veiculo(id),
    data_hora TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    litros NUMERIC(12, 3) NOT NULL,
    valor_litro NUMERIC(15, 4) NOT NULL,
    valor_total NUMERIC(15, 4) NOT NULL,
    odometro_km NUMERIC(12, 1),
    posto VARCHAR(120),
    observacoes TEXT
);

CREATE INDEX idx_frota_abastecimento_tenant ON tb_frota_abastecimento(tenant_id);
CREATE INDEX idx_frota_abastecimento_veiculo ON tb_frota_abastecimento(veiculo_id);

INSERT INTO tb_frota_veiculo (id, tenant_id, placa, renavam, marca, modelo, ano_fabricacao, ano_modelo, tipo, combustivel, status, odometro_atual, ativo)
VALUES
    ('99999999-9999-4999-9999-999999999991', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', 'ABC-1D23', '12345678901', 'Fiat', 'Palio Adventure', 2022, 2023, 'UTILITARIO', 'FLEX', 'ATIVO', 45210.5, true),
    ('99999999-9999-4999-9999-999999999992', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', 'XYZ-9G87', '98765432109', 'Chevrolet', 'S10 LS 2.4', 2021, 2022, 'CAMINHAO', 'FLEX', 'ATIVO', 87110.0, true)
ON CONFLICT (id) DO NOTHING;

INSERT INTO tb_frota_abastecimento (id, tenant_id, veiculo_id, data_hora, litros, valor_litro, valor_total, odometro_km, posto, observacoes)
VALUES
    ('aaaaaaaa-aaaa-4aaa-aaaa-aaaaaaaaaaa1', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', '99999999-9999-4999-9999-999999999991', '2026-09-01T08:15:00Z', 42.500, 6.1490, 261.33, 45110.0, 'Posto Central', NULL),
    ('aaaaaaaa-aaaa-4aaa-aaaa-aaaaaaaaaaa2', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', '99999999-9999-4999-9999-999999999991', '2026-09-05T16:40:00Z', 38.000, 6.2500, 237.50, 45210.5, 'Auto Posto Bela Vista', NULL),
    ('aaaaaaaa-aaaa-4aaa-aaaa-aaaaaaaaaaa3', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', '99999999-9999-4999-9999-999999999992', '2026-09-02T09:00:00Z', 68.000, 6.3200, 429.76, 87000.0, 'Posto Central', 'Abastecimento mensal' )
ON CONFLICT (id) DO NOTHING;

-- ==========================================================
-- MÓDULO: Protocolo Eletrônico
-- ==========================================================
CREATE TABLE tb_protocolo (
    id UUID PRIMARY KEY,
    tenant_id UUID NOT NULL REFERENCES tenant(id),
    numero_protocolo VARCHAR(30) NOT NULL,
    tipo VARCHAR(40) NOT NULL,
    assunto VARCHAR(255) NOT NULL,
    descricao TEXT,
    remetente VARCHAR(150),
    destinatario VARCHAR(150),
    data_protocolo TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    status VARCHAR(30) NOT NULL DEFAULT 'RECEBIDO',
    responsavel VARCHAR(120),
    observacoes TEXT,
    ativo BOOLEAN DEFAULT TRUE,
    CONSTRAINT chk_protocolo_status CHECK (status IN ('RECEBIDO', 'TRIAGEM', 'EM_TRAMITACAO', 'ARQUIVADO', 'CANCELADO'))
);

CREATE INDEX idx_protocolo_tenant ON tb_protocolo(tenant_id);
CREATE INDEX idx_protocolo_numero ON tb_protocolo(numero_protocolo);

INSERT INTO tb_protocolo (id, tenant_id, numero_protocolo, tipo, assunto, descricao, remetente, destinatario, data_protocolo, status, responsavel, ativo)
VALUES
    ('bbbbbbbb-bbbb-4bbb-bbbb-bbbbbbbbbbb1', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', 'PROTO-2026-000001', 'OFICIO', 'Solicitação de manutenção da frota', 'Ofício nº 012/2026 solicitando orçamento para manutenção preventiva dos veículos municipais.', 'Secretaria de Obras', 'Departamento de Compras', '2026-09-03T10:00:00Z', 'EM_TRAMITACAO', 'Maria Silva', true),
    ('bbbbbbbb-bbbb-4bbb-bbbb-bbbbbbbbbbb2', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', 'PROTO-2026-000002', 'REQUERIMENTO', 'Requerimento administrativo de munícipe', 'Requerimento solicitando informação sobre projetos aprovados na zona norte.', 'Cidadão José R.', 'Protocolo Geral', '2026-09-06T09:30:00Z', 'RECEBIDO', NULL, true)
ON CONFLICT (id) DO NOTHING;