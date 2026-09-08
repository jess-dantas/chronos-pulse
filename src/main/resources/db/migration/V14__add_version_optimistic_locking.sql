-- V14: Locking otimista (@Version) em estoque saldo e registro de ponto
-- para evitar corridas concorrentes de baixa de saldo/registro simultâneo.

ALTER TABLE tb_estoque_saldo ADD COLUMN version BIGINT NOT NULL DEFAULT 0;
ALTER TABLE registro_ponto ADD COLUMN version BIGINT NOT NULL DEFAULT 0;
