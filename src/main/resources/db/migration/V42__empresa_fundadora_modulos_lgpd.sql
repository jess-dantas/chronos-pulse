-- ==========================================================
-- V42: Ativação total de módulos para a empresa fundadora
--      e configuração de isolamento de dados (LGPD)
-- ==========================================================

-- 1. Ativa TODOS os módulos do catálogo para a empresa fundadora (CNPJ 49262262000113)
-- A empresa fundadora tem acesso a todos os módulos da plataforma
INSERT INTO empresa_modulo (id, tenant_id, modulo_id, ativado_em)
SELECT
    gen_random_uuid()::uuid,
    t.id,
    m.id,
    NOW()
FROM tenant t
JOIN modulo_plataforma m ON m.ativo = true
WHERE t.cnpj = '49262262000113'
ON CONFLICT (tenant_id, modulo_id) DO UPDATE SET
    ativado_em = EXCLUDED.ativado_em;

-- 2. Garante que o usuário fundador tem role ADMIN_PLATAFORMA
UPDATE cpc_usuario
SET role = 'ADMIN_PLATAFORMA',
    tenant_id = (SELECT id FROM tenant WHERE cnpj = '49262262000113')
WHERE cpf = '99999999999';

-- 3. Cria tabela de controle de acesso a dados sensíveis (LGPD)
-- Define quais roles podem acessar dados de clientes/terceiros
CREATE TABLE IF NOT EXISTS acesso_dados_sensiveis (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    role VARCHAR(30) NOT NULL,
    pode_acessar_dados_clientes BOOLEAN DEFAULT FALSE,
    pode_acessar_dados_colaboradores_terceiros BOOLEAN DEFAULT FALSE,
    pode_exportar_dados_pessoais BOOLEAN DEFAULT FALSE,
    observacao VARCHAR(500),
    UNIQUE (role)
);

-- 4. Seed das permissões de acesso a dados sensíveis (LGPD)
INSERT INTO acesso_dados_sensiveis (role, pode_acessar_dados_clientes, pode_acessar_dados_colaboradores_terceiros, pode_exportar_dados_pessoais, observacao) VALUES
    ('ADMIN_PLATAFORMA', true, true, true, 'Fundador da plataforma: acesso total para gestão da plataforma'),
    ('SUPORTE_N1', false, false, false, 'Suporte nível 1: sem acesso a dados sensíveis de clientes'),
    ('SUPORTE_N2', false, false, false, 'Suporte nível 2: sem acesso a dados sensíveis de clientes'),
    ('ADMIN_EMPRESA', true, true, true, 'Admin da empresa: acesso aos dados da própria empresa'),
    ('GESTOR_RH', true, true, true, 'Gestor RH: acesso aos dados de colaboradores da própria empresa'),
    ('COLABORADOR', false, false, false, 'Colaborador: acesso apenas aos próprios dados')
ON CONFLICT (role) DO UPDATE SET
    pode_acessar_dados_clientes = EXCLUDED.pode_acessar_dados_clientes,
    pode_acessar_dados_colaboradores_terceiros = EXCLUDED.pode_acessar_dados_colaboradores_terceiros,
    pode_exportar_dados_pessoais = EXCLUDED.pode_exportar_dados_pessoais,
    observacao = EXCLUDED.observacao;

-- 5. Índices para consultas de isolamento de tenant
CREATE INDEX IF NOT EXISTS idx_tenant_cnpj ON tenant(cnpj);
CREATE INDEX IF NOT EXISTS idx_cpc_usuario_tenant_role ON cpc_usuario(tenant_id, role);

-- 6. Comentários de documentação LGPD
COMMENT ON TABLE acesso_dados_sensiveis IS 'Controle de acesso a dados sensíveis conforme LGPD (Lei 13.709/2018). Define quais roles podem acessar dados de terceiros.';
COMMENT ON COLUMN acesso_dados_sensiveis.pode_acessar_dados_clientes IS 'Permissão para acessar dados cadastrais de empresas clientes da plataforma (multi-tenant)';
COMMENT ON COLUMN acesso_dados_sensiveis.pode_acessar_dados_colaboradores_terceiros IS 'Permissão para acessar dados de colaboradores de outras empresas (cross-tenant)';
COMMENT ON COLUMN acesso_dados_sensiveis.pode_exportar_dados_pessoais IS 'Permissão para exportar dados pessoais (portabilidade LGPD art. 18)';