-- ============================================================================
-- Auditoria da sequência de batidas (regra posicional — Fase B, 2026-10-03)
-- ----------------------------------------------------------------------------
-- Compara o tipo armazenado em registro_ponto com a jornada canônico de 6
-- batidas ENTRADA -> INTERVALO -> RETORNO -> SAIDA -> ENTRADA -> SAIDA,
-- na ordem cronológica de cada colaborador (a mesma regra de
-- RegistrarPontoUseCaseImpl.determinarProximoTipo):
--
--   * a posição manda, não o tipo anterior: 5ª batida = ENTRADA (HE),
--     6ª = SAIDA (HE), 7ª reinicia a jornada em ENTRADA;
--   * duas batidas com intervalo > 10h abrem jornada nova (o turno noturno
--     22h -> 06h atravessa a virada sem regra própria);
--   * ajustes manuais ENTRAM na cadeia (o backend não filtra), mas só as
--     batidas naturais (ajuste_manual = false) são comparadas — o tipo de um
--     ajuste é decisão do RH.
--
-- Sem backfill (decisão 2026-10-03): dados antigos NÃO são corrigidos. Por
-- isso as consultas 1-2 filtram por LIMITE_AVALIACIO — ajuste a data do
-- deploy da regra nova; antes dela as divergências são esperadas.
--
-- Uso: rodar no console do Postgres (Render). As consultas 3-5 (nsr) são
-- independentes da regra de sequência e continuam valendo.
-- ============================================================================

-- Data a partir da qual a regra posicional vale (deploy da Fase B).
-- Consultas 1 e 2: troque o literal abaixo se necessário.
-- 2026-10-03 00:00 (-03)

-- ----------------------------------------------------------------------------
-- 1) Resumo: batidas naturais fora da jornada canônica (após o deploy).
--    Esperado: 0 linhas.
-- ----------------------------------------------------------------------------
WITH RECURSIVE limite AS (
    SELECT TIMESTAMPTZ '2026-10-03 00:00:00-03' AS desde
),
ordenados AS (
    SELECT
        r.tenant_id,
        r.colaborador_id,
        r.data_hora_dispositivo,
        r.tipo_registro,
        r.ajuste_manual,
        ROW_NUMBER() OVER (
            PARTITION BY r.tenant_id, r.colaborador_id
            ORDER BY r.data_hora_dispositivo, r.nsr
        ) AS rn
    FROM registro_ponto r, limite
    WHERE r.data_hora_dispositivo >= limite.desde
),
cadeia AS (
    SELECT
        o.tenant_id,
        o.colaborador_id,
        o.data_hora_dispositivo,
        o.tipo_registro,
        o.ajuste_manual,
        o.rn,
        1 AS posicao
    FROM ordenados o
    WHERE o.rn = 1

    UNION ALL

    SELECT
        o.tenant_id,
        o.colaborador_id,
        o.data_hora_dispositivo,
        o.tipo_registro,
        o.ajuste_manual,
        o.rn,
        CASE
            WHEN o.data_hora_dispositivo - c.data_hora_dispositivo
                    > INTERVAL '10 hours'
                OR c.posicao >= 6
            THEN 1
            ELSE c.posicao + 1
        END AS posicao
    FROM ordenados o
    JOIN cadeia c
      ON c.tenant_id = o.tenant_id
     AND c.colaborador_id = o.colaborador_id
     AND c.rn = o.rn - 1
),
canonico AS (
    SELECT
        c.*,
        CASE c.posicao
            WHEN 1 THEN 'ENTRADA'
            WHEN 2 THEN 'INTERVALO'
            WHEN 3 THEN 'RETORNO'
            WHEN 4 THEN 'SAIDA'
            WHEN 5 THEN 'ENTRADA'
            WHEN 6 THEN 'SAIDA'
        END AS tipo_esperado
    FROM cadeia c
)
SELECT
    c.colaborador_id,
    c.tipo_esperado,
    c.tipo_registro,
    COUNT(*) AS qtd_divergente
FROM canonico c
WHERE c.ajuste_manual = false
  AND c.tipo_registro <> c.tipo_esperado
GROUP BY c.colaborador_id, c.tipo_esperado, c.tipo_registro
ORDER BY c.colaborador_id, c.tipo_esperado, c.tipo_registro;

-- ----------------------------------------------------------------------------
-- 2) Detalhe das batidas naturais fora da jornada canônica (após o deploy).
--    Esperado: 0 linhas. Ajustes não aparecem no comparado, mas contam na
--    posição — igual ao backend, que não filtra ajuste_manual.
-- ----------------------------------------------------------------------------
WITH RECURSIVE limite AS (
    SELECT TIMESTAMPTZ '2026-10-03 00:00:00-03' AS desde
),
ordenados AS (
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
            PARTITION BY r.tenant_id, r.colaborador_id
            ORDER BY r.data_hora_dispositivo, r.nsr
        ) AS rn
    FROM registro_ponto r, limite
    WHERE r.data_hora_dispositivo >= limite.desde
),
cadeia AS (
    SELECT
        o.id, o.tenant_id, o.colaborador_id, o.data_hora_dispositivo,
        o.data_hora_servidor, o.tipo_registro, o.ajuste_manual,
        o.nsr_logico, o.nsr, o.rn,
        1 AS posicao
    FROM ordenados o
    WHERE o.rn = 1

    UNION ALL

    SELECT
        o.id, o.tenant_id, o.colaborador_id, o.data_hora_dispositivo,
        o.data_hora_servidor, o.tipo_registro, o.ajuste_manual,
        o.nsr_logico, o.nsr, o.rn,
        CASE
            WHEN o.data_hora_dispositivo - c.data_hora_dispositivo
                    > INTERVAL '10 hours'
                OR c.posicao >= 6
            THEN 1
            ELSE c.posicao + 1
        END AS posicao
    FROM ordenados o
    JOIN cadeia c
      ON c.tenant_id = o.tenant_id
     AND c.colaborador_id = o.colaborador_id
     AND c.rn = o.rn - 1
)
SELECT
    c.id,
    c.tenant_id,
    c.colaborador_id,
    (c.data_hora_dispositivo AT TIME ZONE 'America/Sao_Paulo')::date AS dia_sp,
    c.posicao AS posicao_na_jornada,
    CASE c.posicao
        WHEN 1 THEN 'ENTRADA'
        WHEN 2 THEN 'INTERVALO'
        WHEN 3 THEN 'RETORNO'
        WHEN 4 THEN 'SAIDA'
        WHEN 5 THEN 'ENTRADA'
        WHEN 6 THEN 'SAIDA'
    END AS tipo_esperado,
    c.tipo_registro,
    c.nsr_logico,
    c.data_hora_dispositivo,
    c.data_hora_servidor
FROM cadeia c
WHERE c.ajuste_manual = false
  AND c.tipo_registro <>
      CASE c.posicao
          WHEN 1 THEN 'ENTRADA'
          WHEN 2 THEN 'INTERVALO'
          WHEN 3 THEN 'RETORNO'
          WHEN 4 THEN 'SAIDA'
          WHEN 5 THEN 'ENTRADA'
          WHEN 6 THEN 'SAIDA'
      END
ORDER BY c.colaborador_id, c.data_hora_dispositivo;

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
