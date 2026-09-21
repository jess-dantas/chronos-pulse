-- Empresa fundadora da plataforma: LJ CODE TECNOLOGIA E SISTEMAS LTDA
-- CNPJ 49.262.262/0001-13 (normalizado: 49262262000113)
-- Produto: Chronos Suite
-- Esta empresa é a fundadora da plataforma e tem papel ADMIN_PLATAFORMA

INSERT INTO tenant (id, nome, razao_social, nome_fantasia, produto, cnpj, ativo, criado_em)
VALUES (
    'a0eebc99-0009-0009-0009-6bb9bd380a09',
    'LJ CODE TECNOLOGIA E SISTEMAS LTDA',
    'LJ CODE TECNOLOGIA E SISTEMAS LTDA',
    'LJ CODE',
    'Chronos Suite',
    '49262262000113',
    true,
    NOW()
)
ON CONFLICT (cnpj) DO UPDATE SET
    nome = EXCLUDED.nome,
    razao_social = EXCLUDED.razao_social,
    nome_fantasia = EXCLUDED.nome_fantasia,
    produto = EXCLUDED.produto,
    ativo = EXCLUDED.ativo;

-- Usuário administrador da plataforma (fundador) - ADMIN_PLATAFORMA
-- CPF: 99999999999 / Senha: admin123 (hash BCrypt)
-- E-mail provisório: ljcod3@gmail.com
INSERT INTO cpc_usuario (id, cpc_id, cpf, nome, email_corporativo, senha_hash, role, tenant_id, ativo, criado_em)
VALUES (
    'a0eebc99-0999-4999-8999-6bb9bd380a09',
    'a0eebc99-0999-4999-8999-6bb9bd380a09',
    '99999999999',
    'Fundador LJ CODE',
    'ljcod3@gmail.com',
    '$2a$10$reLinwh8IlJzgTjS9RMj.ux/0bUiI0fhT7EnBzqa4LjptHD44B.1K',
    'ADMIN_PLATAFORMA',
    'a0eebc99-0009-0009-0009-6bb9bd380a09',
    true,
    NOW()
)
ON CONFLICT (cpf) DO UPDATE SET
    nome = EXCLUDED.nome,
    email_corporativo = EXCLUDED.email_corporativo,
    role = EXCLUDED.role,
    tenant_id = EXCLUDED.tenant_id,
    ativo = EXCLUDED.ativo;

-- Configuração de jornada padrão da empresa fundadora
INSERT INTO configuracao_jornada (id, tenant_id, nome, carga_horaria_diaria_minutos, exige_intervalo, intervalo_minimo_minutos, tolerancia_entrada_minutos, tolerancia_saida_minutos, interjornada_minima_minutos)
VALUES (
    'a0eebc99-0019-4019-8019-6bb9bd380a09',
    'a0eebc99-0009-0009-0009-6bb9bd380a09',
    'Padrão 8h',
    480,
    true,
    60,
    10,
    10,
    660
)
ON CONFLICT (id) DO NOTHING;

-- Colaborador da empresa fundadora (vinculado ao usuário fundador)
INSERT INTO colaborador (id, cpc_usuario_id, tenant_id, matricula, cargo, departamento, data_nascimento, data_admissao, ativo)
VALUES (
    'a0eebc99-0049-4049-8049-6bb9bd380a09',
    'a0eebc99-0999-4999-8999-6bb9bd380a09',
    'a0eebc99-0009-0009-0009-6bb9bd380a09',
    'MAT-F001',
    'Fundador / CEO',
    'Diretoria',
    '1985-01-01',
    '2023-01-01',
    true
)
ON CONFLICT (id) DO NOTHING;