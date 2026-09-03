-- Adiciona campos para controle de ajuste manual e justificativa no espelho de ponto
ALTER TABLE registro_ponto ADD COLUMN ajuste_manual BOOLEAN DEFAULT FALSE;
ALTER TABLE registro_ponto ADD COLUMN justificativa VARCHAR(255);
ALTER TABLE registro_ponto ADD COLUMN observacao VARCHAR(500);

CREATE INDEX idx_registro_ponto_colab_data ON registro_ponto(colaborador_id, tenant_id, data_hora_dispositivo);
