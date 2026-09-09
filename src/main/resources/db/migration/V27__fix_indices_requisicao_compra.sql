-- ==========================================================
-- V27: Corrige colisão de nomes de índice da V22 com a V4.
--
-- A V22 criou em tb_requisicao_compra os índices
-- idx_requisicao_tenant e idx_requisicao_solicitante, nomes já
-- utilizados pela V4 na tabela tb_requisicao. No PostgreSQL o
-- nome de índice é único por schema, então a V22 falhava em
-- banco novo (SQLState 42P07). A V22 foi corrigida para usar
-- nomes únicos (idx_req_compra_*).
--
-- Esta migration é uma salvaguarda idempotente para ambientes
-- que tenham aplicado a V22 antiga: renomeia os índices SOMENTE
-- se eles existirem na tabela tb_requisicao_compra (os índices
-- da V4 em tb_requisicao são preservados).
-- ==========================================================
DO $$
DECLARE
    idx_nome TEXT;
BEGIN
    FOREACH idx_nome IN ARRAY ARRAY['idx_requisicao_tenant'::text, 'idx_requisicao_solicitante'::text]
    LOOP
        IF EXISTS (
            SELECT 1
            FROM pg_indexes
            WHERE tablename = 'tb_requisicao_compra'
              AND indexname = idx_nome
        ) THEN
            EXECUTE format(
                'ALTER INDEX %I RENAME TO %I',
                idx_nome,
                CASE idx_nome
                    WHEN 'idx_requisicao_tenant'     THEN 'idx_req_compra_tenant'
                    ELSE 'idx_req_compra_solicitante'
                END
            );
        END IF;
    END LOOP;
END $$;