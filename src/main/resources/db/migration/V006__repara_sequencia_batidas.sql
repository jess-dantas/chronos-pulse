-- Fase (c): Reparo da sequência de batidas legadas (backfill V006).
--
-- 1) Backup in-place: a correção abaixo pode ser auditada e desfeita a partir
--    da tabela de backup (scripts/auditoria_sequencia_pontos.sql antes/depois).
--    nsr_logico é intocado — decisão do plano (não se mexe em numeração já
--    emitida; só o auditoria SQL aponta null/duplicado/fora de ordem).
-- 2) Backfill do tipo: batidas NATURAIS (ajuste_manual = false) fora do ciclo
--    canônico ENTRADA -> INTERVALO -> RETORNO -> SAIDA ganham o tipo da posição
--    cronológica do dia de São Paulo (mesma janela e mesma regra do backend em
--    RegistrarPontoUseCaseImpl.determinarProximoTipo — que não filtra
--    ajuste_manual ao buscar o último tipo do dia).
-- 3) Ajustes manuais mantêm o tipo escolhido pelo RH, mas continuam ocupando
--    posição no ciclo: a posição conta todas as linhas do dia, inclusive os
--    ajustes, para o próximo tipo derivar igual ao que o backend deriva.
CREATE TABLE registro_ponto_bkp_v006 AS
SELECT * FROM registro_ponto;

WITH posicoes AS (
    SELECT
        r.id,
        CASE (ROW_NUMBER() OVER (
            PARTITION BY
                r.colaborador_id,
                (r.data_hora_dispositivo AT TIME ZONE 'America/Sao_Paulo')::date
            ORDER BY r.data_hora_dispositivo, r.nsr
        ) - 1) % 4
            WHEN 0 THEN 'ENTRADA'
            WHEN 1 THEN 'INTERVALO'
            WHEN 2 THEN 'RETORNO'
            WHEN 3 THEN 'SAIDA'
        END AS tipo_esperado
    FROM registro_ponto r
)
UPDATE registro_ponto rp
SET tipo_registro = p.tipo_esperado
FROM posicoes p
WHERE rp.id = p.id
  AND rp.ajuste_manual = false
  AND rp.tipo_registro IS DISTINCT FROM p.tipo_esperado;
