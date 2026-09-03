-- Seed Tenant Inicial
INSERT INTO tenant (id, nome, cnpj, ativo, criado_em)
VALUES ('a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', 'Chronos Pulse Tech LTDA', '12345678000195', true, NOW())
ON CONFLICT (cnpj) DO NOTHING;

-- Seed Admin Plataforma (CPF: 00000000000 / Senha: admin123)
INSERT INTO cpc_usuario (id, cpc_id, cpf, nome, email_corporativo, senha_hash, role, tenant_id, ativo, criado_em)
VALUES (
    '11111111-1111-1111-1111-111111111111',
    '11111111-1111-1111-1111-111111111111',
    '00000000000',
    'Admin Plataforma',
    'admin@chronospulse.com.br',
    '$2a$10$reLinwh8IlJzgTjS9RMj.ux/0bUiI0fhT7EnBzqa4LjptHD44B.1K',
    'ADMIN_PLATAFORMA',
    NULL,
    true,
    NOW()
)
ON CONFLICT (cpf) DO NOTHING;

-- Seed Admin Empresa / Gestor RH (CPF: 11111111111 / Senha: admin123)
INSERT INTO cpc_usuario (id, cpc_id, cpf, nome, email_corporativo, senha_hash, role, tenant_id, ativo, criado_em)
VALUES (
    '22222222-2222-2222-2222-222222222222',
    '22222222-2222-2222-2222-222222222222',
    '11111111111',
    'Gestor RH Empresa',
    'rh@empresa.com.br',
    '$2a$10$reLinwh8IlJzgTjS9RMj.ux/0bUiI0fhT7EnBzqa4LjptHD44B.1K',
    'ADMIN_EMPRESA',
    'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11',
    true,
    NOW()
)
ON CONFLICT (cpf) DO NOTHING;

-- Seed Colaborador Usuário (CPF: 12345678901 / Senha: senha123)
INSERT INTO cpc_usuario (id, cpc_id, cpf, nome, email_corporativo, senha_hash, role, tenant_id, ativo, criado_em)
VALUES (
    '33333333-3333-3333-3333-333333333333',
    '33333333-3333-3333-3333-333333333333',
    '12345678901',
    'Colaborador Teste',
    'colaborador@empresa.com.br',
    '$2a$10$z5NHoUWdOVy7WmEBc94PcOEy0ACY2P6v8mVt6KW9yeHVdSr2Tldxu',
    'COLABORADOR',
    'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11',
    true,
    NOW()
)
ON CONFLICT (cpf) DO NOTHING;

-- Seed Colaborador
INSERT INTO colaborador (id, cpc_usuario_id, tenant_id, matricula, cargo, departamento, data_nascimento, data_admissao, ativo)
VALUES (
    '44444444-4444-4444-4444-444444444444',
    '33333333-3333-3333-3333-333333333333',
    'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11',
    'MAT-001',
    'Desenvolvedor',
    'Engenharia',
    '1990-01-01',
    '2024-01-01',
    true
)
ON CONFLICT (id) DO NOTHING;
