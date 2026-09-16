-- Endurecimento C1 (security review): desativa contas de seed instaladas por
-- V3/V5/V9 com senhas conhecidas e públicas no repositório (admin123/senha123).
--
-- Em produção estas contas permanecem inativas: acesso só via fluxo de
-- recuperação de senha. Em ambiente de desenvolvimento, o seed repeatable
-- db/seed/R__seed_dados_dev.sql re-ativa essas contas com credenciais locais.
UPDATE cpc_usuario
   SET ativo = FALSE
 WHERE cpf IN ('00000000000', '11111111111', '22222222222', '99999999999', '12345678901', '98765432100');