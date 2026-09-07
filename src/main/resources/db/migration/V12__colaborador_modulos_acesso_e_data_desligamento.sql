-- ==========================================================
-- V12: Acesso por módulo por colaborador (Patrimônio, Frota,
--      Protocolo) e data de desligamento do colaborador
-- ==========================================================

-- 1. Acessos por módulo no usuário (por padrão acompanham o estoque)
ALTER TABLE cpc_usuario ADD COLUMN IF NOT EXISTS acesso_patrimonio BOOLEAN DEFAULT FALSE;
ALTER TABLE cpc_usuario ADD COLUMN IF NOT EXISTS acesso_frota BOOLEAN DEFAULT FALSE;
ALTER TABLE cpc_usuario ADD COLUMN IF NOT EXISTS acesso_protocolo BOOLEAN DEFAULT FALSE;

-- Atualiza roles administrativas para ter acesso total a todos os módulos por padrão
UPDATE cpc_usuario SET acesso_patrimonio = TRUE, acesso_frota = TRUE, acesso_protocolo = TRUE
WHERE role IN ('ADMIN_PLATAFORMA', 'ADMIN_EMPRESA', 'GESTOR_RH');

-- Colaboradores que já tinham acesso ao estoque passam a ter acesso aos demais módulos
UPDATE cpc_usuario SET acesso_patrimonio = TRUE, acesso_frota = TRUE, acesso_protocolo = TRUE
WHERE acesso_estoque = TRUE AND role = 'COLABORADOR';

-- 2. Data de desligamento do colaborador
ALTER TABLE colaborador ADD COLUMN IF NOT EXISTS data_desligamento DATE;