-- Fase (e): Gestor RH com escopo fixo em PONTO + RECURSOS_HUMANOS.
--
-- 1) Os flags legados de acesso (estoque/patrimônio/frota/protocolo) deixam de
--    ser implícitos para o papel: o papel agora É o escopo, decidido no
--    ModuloInterceptor/JwtAuthFilter/SecurityConfig pelo role GESTOR_RH.
--    (O seed do V001 gravava acesso_estoque = TRUE para o Gestor de RH.)
-- 2) Vínculos de usuario_modulo fora do escopo são removidos: o backfill do
--    V001 dava todos os módulos ativos do tenant a todo usuário ativo.
UPDATE cpc_usuario
SET acesso_estoque = FALSE,
    acesso_patrimonio = FALSE,
    acesso_frota = FALSE,
    acesso_protocolo = FALSE
WHERE role = 'GESTOR_RH';

DELETE FROM usuario_modulo
WHERE usuario_id IN (SELECT id FROM cpc_usuario WHERE role = 'GESTOR_RH')
  AND codigo NOT IN ('PONTO', 'RECURSOS_HUMANOS');
