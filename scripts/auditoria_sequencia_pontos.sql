-- ============================================================================
-- Auditoria da sequência de batidas (pré V006)
-- ----------------------------------------------------------------------------
-- Compara o tipo armazenado em registro_ponto com o ciclo canônico
-- ENTRADA -> INTERVALO -> RETORNO -> SAIDA, na ordem cronológica do dia de
-- São Paulo (a mesma janela que o backend usa em
-- RegistrarPontoUseCaseImpl.determinarProximoTipo).
--
-- Uso: rodar no console do Postgres (Render) antes e depois da migração
-- V006__repara_sequencia_batidas.sql.
--
-- Esperado após a V006: as consultas 1-2 (sequência) e 3 (nsr_logico null)
-- não retornam linhas; consultas 4-5 podem continuar apontando dados legados
-- de nsr que a migração não toca (por decisão: nsr_logico é intocado).
-- ============================================================================

-- ----------------------------------------------------------------------------
-- 1) Resumo: dias com divergência (apenas batidas naturais; ajustes manuais
--    são intocados — o tipo deles é a decisão do RH).
--    Cada posição rn de um (colaborador, dia SP) espera SEQUENCIA[(rn-1) % 4]:
--    primeira batida do dia = ENTRADA, pois o backend deriva o próximo tipo a
--    partir do último e cai em ENTRADA quando o dia está vazio.
-- ----------------------------------------------------------------------------
WITH ordenados AS (
    SELECT
        r.colaborador_id,
        r.data_hora_dispositivo,
        r.tipo_registro,
        r.ajuste_manual,
        ROW_NUMBER() OVER (
            PARTITION BY
                r.colaborador_id,
                (r.data_hora_dispositivo AT TIME ZONE 'America/Sao_Paulo')::date
            ORDER BY r.data_hora_dispositivo, r.nsr
        ) AS rn
    FROM registro_ponto r
),
canonico AS (
    SELECT
        o.colaborador_id,
        (o.data_hora_dispositivo AT TIME ZONE 'America/Sao_Paulo')::date AS dia_sp,
        o.rn,
        o.tipo_registro,
        CASE (o.rn - 1) % 4
            WHEN 0 THEN 'ENTRADA'
            WHEN 1 THEN 'INTERVALO'
            WHEN 2 THEN 'RETORNO'
            WHEN 3 THEN 'SAIDA'
        END AS tipo_esperado
    FROM ordenados o
    WHERE o.ajuste_manual = false
)
SELECT
    dia_sp,
    tipo_esperado,
    tipo_registro,
    COUNT(*) AS qtd_divergente,
    COUNT(DISTINCT colaborador_id) AS qtd_colaboradores
FROM canonico
WHERE tipo_registro <> tipo_esperado
GROUP BY dia_sp, tipo_esperado, tipo_registro
ORDER BY dia_sp, tipo_esperado, tipo_registro;

-- ----------------------------------------------------------------------------
-- 2) Detalhe das batidas naturais fora do ciclo canônico (o que a V006 vai
--    corrigir). Ajustes manuais não aparecem: o tipo deles foi escolhido pelo
--    RH, mas a posição deles continua contando no ciclo (igual ao backend,
--    que não filtra ajuste_manual ao buscar o último tipo do dia).
-- ----------------------------------------------------------------------------
WITH ordenados AS (
    SELECT
        r.id,
        r.tenant_id,
        r.colaborador_id,
        r.data_hora_dispositivo,
        r.data_hora_servidor,
        r.tipo_registro,
        r.ajuste_manual,
        r.nsr_logico,
        r.nsr,
        ROW_NUMBER() OVER (
            PARTITION BY
                r.colaborador_id,
                (r.data_hora_dispositivo AT TIME ZONE 'America/Sao_Paulo')::date
            ORDER BY r.data_hora_dispositivo, r.nsr
        ) AS rn
    FROM registro_ponto r
),
canonico AS (
    SELECT
        o.*,
        CASE (o.rn - 1) % 4
            WHEN 0 THEN 'ENTRADA'
            WHEN 1 THEN 'INTERVALO'
            WHEN 2 THEN 'RETORNO'
            WHEN 3 THEN 'SAIDA'
        END AS tipo_esperado
    FROM ordenados o
)
SELECT
    c.id,
    c.tenant_id,
    c.colaborador_id,
    (c.data_hora_dispositivo AT TIME ZONE 'America/Sao_Paulo')::date AS dia_sp,
    c.rn AS posicao_no_dia,
    c.tipo_esperado,
    c.tipo_registro,
    c.nsr_logico,
    c.data_hora_dispositivo,
    c.data_hora_servidor
FROM canonico c
WHERE c.ajuste_manual = false
  AND c.tipo_registro <> c.tipo_esperado
ORDER BY c.colaborador_id, dia_sp, c.rn;

-- ----------------------------------------------------------------------------
-- 3) nsr_logico ausente (linha não passou pelo use case / legado).
--    Esperado: 0 linhas após a limpeza manual, se houver.
-- ----------------------------------------------------------------------------
SELECT
    r.id,
    r.tenant_id,
    r.colaborador_id,
    r.data_hora_dispositivo,
    r.tipo_registro,
    r.ajuste_manual
FROM registro_ponto r
WHERE r.nsr_logico IS NULL
ORDER BY r.colaborador_id, r.data_hora_dispositivo;

-- ----------------------------------------------------------------------------
-- 4) nsr_logico duplicado dentro do mesmo (tenant, colaborador): o use case
--    atribui MAX(nsr_logico) + 1 — duplicata indica escrita fora do fluxo.
--    Esperado: 0 linhas.
-- ----------------------------------------------------------------------------
SELECT
    r.tenant_id,
    r.colaborador_id,
    r.nsr_logico,
    COUNT(*) AS qtd
FROM registro_ponto r
WHERE r.nsr_logico IS NOT NULL
GROUP BY r.tenant_id, r.colaborador_id, r.nsr_logico
HAVING COUNT(*) > 1
ORDER BY r.colaborador_id, r.nsr_logico;

-- ----------------------------------------------------------------------------
-- 5) nsr_logico fora de ordem: a ordem de atribuição (data_hora_servidor, ou
--    seja quando o registro chegou ao servidor) diverge da ordem numérica do
--    nsr_logico. nsr_logico é ordem de inserção, NÃO cronologia — por isso a
--    comparação é com data_hora_servidor e não com data_hora_dispositivo
--    (batida offline antiga ganha nsr novo quando sincroniza; isso é normal).
--    Esperado: 0 linhas depois que corridas/race writes sumirem.
-- ----------------------------------------------------------------------------
WITH com_ordem AS (
    SELECT
        r.id,
        r.tenant_id,
        r.colaborador_id,
        r.nsr_logico,
        r.data_hora_servidor,
        ROW_NUMBER() OVER (
            PARTITION BY r.tenant_id, r.colaborador_id
            ORDER BY r.data_hora_servidor, r.nsr
        ) AS rn_insercao,
        ROW_NUMBER() OVER (
            PARTITION BY r.tenant_id, r.colaborador_id
            ORDER BY r.nsr_logico
        ) AS rn_nsr
    FROM registro_ponto r
    WHERE r.nsr_logico IS NOT NULL
)
SELECT
    c.id,
    c.tenant_id,
    c.colaborador_id,
    c.nsr_logico,
    c.rn_insercao,
    c.rn_nsr,
    c.data_hora_servidor
FROM com_ordem c
WHERE c.rn_insercao <> c.rn_nsr
ORDER BY c.colaborador_id, c.nsr_logico;
