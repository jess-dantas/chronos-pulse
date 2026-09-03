-- Atualiza constraint de roles para incluir GESTOR_RH
ALTER TABLE cpc_usuario DROP CONSTRAINT IF EXISTS chk_role;
ALTER TABLE cpc_usuario ADD CONSTRAINT chk_role CHECK (role IN (
    'ADMIN_PLATAFORMA', 'SUPORTE_N1', 'SUPORTE_N2',
    'ADMIN_EMPRESA', 'GESTOR_RH', 'COLABORADOR'
));

-- Adiciona coluna de acesso ao módulo de estoque
ALTER TABLE cpc_usuario ADD COLUMN IF NOT EXISTS acesso_estoque BOOLEAN DEFAULT FALSE;

-- Atualiza roles administrativas para ter acesso total ao estoque por padrão
UPDATE cpc_usuario SET acesso_estoque = TRUE WHERE role IN ('ADMIN_PLATAFORMA', 'ADMIN_EMPRESA', 'GESTOR_RH');

-- Seed Gestor RH (CPF: 22222222222 / Senha: admin123)
INSERT INTO cpc_usuario (id, cpc_id, cpf, nome, email_corporativo, senha_hash, role, tenant_id, acesso_estoque, ativo, criado_em)
VALUES (
    '55555555-5555-5555-5555-555555555555',
    '55555555-5555-5555-5555-555555555555',
    '22222222222',
    'Gestor de RH',
    'gestor.rh@empresa.com.br',
    '$2a$10$reLinwh8IlJzgTjS9RMj.ux/0bUiI0fhT7EnBzqa4LjptHD44B.1K',
    'GESTOR_RH',
    'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11',
    TRUE,
    TRUE,
    NOW()
)
ON CONFLICT (cpf) DO NOTHING;

-- Seed Colaborador com acesso a Estoque (CPF: 98765432100 / Senha: senha123)
INSERT INTO cpc_usuario (id, cpc_id, cpf, nome, email_corporativo, senha_hash, role, tenant_id, acesso_estoque, ativo, criado_em)
VALUES (
    '66666666-6666-6666-6666-666666666666',
    '66666666-6666-6666-6666-666666666666',
    '98765432100',
    'Colaborador Almoxarife',
    'almoxarife@empresa.com.br',
    '$2a$10$z5NHoUWdOVy7WmEBc94PcOEy0ACY2P6v8mVt6KW9yeHVdSr2Tldxu',
    'COLABORADOR',
    'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11',
    TRUE,
    TRUE,
    NOW()
)
ON CONFLICT (cpf) DO NOTHING;

-- Seed Registro Colaborador Almoxarife
INSERT INTO colaborador (id, cpc_usuario_id, tenant_id, matricula, cargo, departamento, data_nascimento, data_admissao, ativo)
VALUES (
    '77777777-7777-7777-7777-777777777777',
    '66666666-6666-6666-6666-666666666666',
    'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11',
    'MAT-002',
    'Almoxarife Chefe',
    'Logística e Patrimônio',
    '1988-05-15',
    '2024-02-01',
    TRUE
)
ON CONFLICT (id) DO NOTHING;
