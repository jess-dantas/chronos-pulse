-- ==========================================================
-- V19: Reforço do controle financeiro e de vigência de contratos.
-- Saldo = valor_empenhado - valor_liquidado.
-- Alertas: dias até o vencimento (vencimento_aviso_dias).
-- ==========================================================

ALTER TABLE contrato
    ADD COLUMN valor_empenhado NUMERIC(12, 2) NOT NULL DEFAULT 0,
    ADD COLUMN valor_liquidado NUMERIC(12, 2) NOT NULL DEFAULT 0,
    ADD COLUMN empenho_numero VARCHAR(30),
    ADD COLUMN vencimento_aviso_dias INTEGER NOT NULL DEFAULT 30;