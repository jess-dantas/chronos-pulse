-- Empresa fundadora: RED CAPE QUALIDADE E SEGURANCA CIBERNETICA LTDA
-- CNPJ 49.262.262/0001-13 (normalizado: 49262262000113)
INSERT INTO tenant (id, nome, cnpj, ativo, criado_em)
VALUES ('a0eebc99-0009-0009-0009-6bb9bd380a09', 'RED CAPE QUALIDADE E SEGURANCA CIBERNETICA LTDA', '49262262000113', true, NOW())
ON CONFLICT (cnpj) DO NOTHING;

-- Usuário administrador da empresa fundadora (CPF: 99999999999 / Senha: admin123)
INSERT INTO cpc_usuario (id, cpc_id, cpf, nome, email_corporativo, senha_hash, role, tenant_id, ativo, criado_em)
VALUES (
    'a0eebc99-0999-4999-8999-6bb9bd380a09',
    'a0eebc99-0999-4999-8999-6bb9bd380a09',
    '99999999999',
    'Fundador Red Cape',
    'fundador@redcape.com.br',
    '$2a$10$reLinwh8IlJzgTjS9RMj.ux/0bUiI0fhT7EnBzqa4LjptHD44B.1K',
    'ADMIN_EMPRESA',
    'a0eebc99-0009-0009-0009-6bb9bd380a09',
    true,
    NOW()
)
ON CONFLICT (cpf) DO NOTHING;

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

-- Colaborador da empresa fundadora
INSERT INTO colaborador (id, cpc_usuario_id, tenant_id, matricula, cargo, departamento, data_nascimento, data_admissao, ativo)
VALUES (
    'a0eebc99-0049-4049-8049-6bb9bd380a09',
    'a0eebc99-0999-4999-8999-6bb9bd380a09',
    'a0eebc99-0009-0009-0009-6bb9bd380a09',
    'MAT-F001',
    'Fundador',
    'Diretoria',
    '1985-01-01',
    '2023-01-01',
    true
)
ON CONFLICT (id) DO NOTHING;