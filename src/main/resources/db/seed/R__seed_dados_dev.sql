-- Seed de dados para desenvolvimento (perfil dev apenas).
-- Carregado via spring.flyway.locations = classpath:db/migration,classpath:db/seed
-- em application-dev.yml. Em produção NÃO é carregado.
--
-- Re-ativa e normaliza as contas locais após V36 desativá-las. Idempotente.

INSERT INTO tenant (id, nome, cnpj, ativo, criado_em)
VALUES ('a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', 'Chronos Pulse Tech LTDA', '12345678000195', true, NOW())
ON CONFLICT (cnpj) DO NOTHING;

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
ON CONFLICT (cpf) DO UPDATE
   SET ativo = TRUE, nome = 'Admin Plataforma', email_corporativo = 'admin@chronospulse.com.br',
       senha_hash = EXCLUDED.senha_hash, role = 'ADMIN_PLATAFORMA', tenant_id = NULL,
       tentativas_login_falhas = 0, bloqueio_login_ate = NULL;

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
ON CONFLICT (cpf) DO UPDATE
   SET ativo = TRUE, nome = 'Gestor RH Empresa', email_corporativo = 'rh@empresa.com.br',
       senha_hash = EXCLUDED.senha_hash, role = 'ADMIN_EMPRESA', tenant_id = 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11',
       tentativas_login_falhas = 0, bloqueio_login_ate = NULL;

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
ON CONFLICT (cpf) DO UPDATE
   SET ativo = TRUE, nome = 'Colaborador Teste', email_corporativo = 'colaborador@empresa.com.br',
       senha_hash = EXCLUDED.senha_hash, role = 'COLABORADOR', tenant_id = 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11',
       tentativas_login_falhas = 0, bloqueio_login_ate = NULL;

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
    true,
    NOW()
)
ON CONFLICT (cpf) DO UPDATE
   SET ativo = TRUE, nome = 'Gestor de RH', email_corporativo = 'gestor.rh@empresa.com.br',
       senha_hash = EXCLUDED.senha_hash, role = 'GESTOR_RH', tenant_id = 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11',
       acesso_estoque = TRUE,
       tentativas_login_falhas = 0, bloqueio_login_ate = NULL;

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
    true,
    NOW()
)
ON CONFLICT (cpf) DO UPDATE
   SET ativo = TRUE, nome = 'Colaborador Almoxarife', email_corporativo = 'almoxarife@empresa.com.br',
       senha_hash = EXCLUDED.senha_hash, role = 'COLABORADOR', tenant_id = 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11',
       acesso_estoque = TRUE,
       tentativas_login_falhas = 0, bloqueio_login_ate = NULL;

INSERT INTO tenant (id, nome, cnpj, ativo, criado_em)
VALUES ('a0eebc99-0009-0009-0009-6bb9bd380a09', 'RED CAPE QUALIDADE E SEGURANCA CIBERNETICA LTDA', '49262262000113', true, NOW())
ON CONFLICT (cnpj) DO NOTHING;

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
ON CONFLICT (cpf) DO UPDATE
   SET ativo = TRUE, nome = 'Fundador Red Cape', email_corporativo = 'fundador@redcape.com.br',
       senha_hash = EXCLUDED.senha_hash, role = 'ADMIN_EMPRESA', tenant_id = 'a0eebc99-0009-0009-0009-6bb9bd380a09',
       tentativas_login_falhas = 0, bloqueio_login_ate = NULL;