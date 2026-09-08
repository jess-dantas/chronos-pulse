-- MCASP/almoxarifado (Fase P2, item 4): código de barras do material,
-- termo de recebimento (provisório/definitivo) e provisão para perdas.

ALTER TABLE tb_material
    ADD COLUMN IF NOT EXISTS codigo_barras VARCHAR(32);

ALTER TABLE tb_estoque_movimentacao
    ADD COLUMN IF NOT EXISTS tipo_termo VARCHAR(20);

ALTER TABLE tb_estoque_movimentacao
    ADD COLUMN IF NOT EXISTS numero_termo VARCHAR(30);

ALTER TABLE tb_estoque_movimentacao
    ADD COLUMN IF NOT EXISTS motivo_baixa VARCHAR(30);

ALTER TABLE tb_estoque_movimentacao
    ADD COLUMN IF NOT EXISTS observacao VARCHAR(255);