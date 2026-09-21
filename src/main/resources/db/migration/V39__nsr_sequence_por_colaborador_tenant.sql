-- NSR sequence por colaborador/tenant (substitui BIGSERIAL global)
-- Cria sequence composta e função para obter próximo NSR lógico

-- 1. Remove a constraint unique do nsr global (vai usar sequence composta)
ALTER TABLE registro_ponto DROP CONSTRAINT IF EXISTS registro_ponto_nsr_key;

-- 2. Cria sequence para NSR por (tenant_id, colaborador_id)
CREATE SEQUENCE IF NOT EXISTS seq_registro_ponto_nsr;

-- 3. Adiciona coluna para NSR lógico por colaborador/tenant
ALTER TABLE registro_ponto ADD COLUMN IF NOT EXISTS nsr_logico BIGINT;

-- 4. Função para obter próximo NSR lógico por colaborador/tenant
CREATE OR REPLACE FUNCTION obter_proximo_nsr_logico(p_tenant_id UUID, p_colaborador_id UUID)
RETURNS BIGINT AS $$
DECLARE
    v_nsr BIGINT;
BEGIN
    -- Tenta obter o último NSR lógico para este colaborador/tenant
    SELECT COALESCE(MAX(nsr_logico), 0) + 1 INTO v_nsr
    FROM registro_ponto
    WHERE tenant_id = p_tenant_id AND colaborador_id = p_colaborador_id;
    
    RETURN v_nsr;
END;
$$ LANGUAGE plpgsql;

-- 5. Função para obter próximo NSR global (para compatibilidade)
CREATE OR REPLACE FUNCTION obter_proximo_nsr_global()
RETURNS BIGINT AS $$
DECLARE
    v_nsr BIGINT;
BEGIN
    SELECT COALESCE(MAX(nsr), 0) + 1 INTO v_nsr FROM registro_ponto;
    RETURN v_nsr;
END;
$$ LANGUAGE plpgsql;

-- 6. Trigger para auto-preencher nsr_logico na inserção (opcional, uso via aplicação)
-- CREATE TRIGGER trigger_nsr_logico
-- BEFORE INSERT ON registro_ponto
-- FOR EACH ROW EXECUTE FUNCTION preencher_nsr_logico();

-- 7. Índice para performance de busca por nsr_logico
CREATE INDEX IF NOT EXISTS idx_registro_ponto_nsr_logico ON registro_ponto(tenant_id, colaborador_id, nsr_logico);

-- 8. Atualiza nsr_logico para registros existentes (backfill)
UPDATE registro_ponto rp
SET nsr_logico = sub.nsr_seq
FROM (
    SELECT id, 
           ROW_NUMBER() OVER (PARTITION BY tenant_id, colaborador_id ORDER BY data_hora_dispositivo) AS nsr_seq
    FROM registro_ponto
    WHERE nsr_logico IS NULL
) sub
WHERE rp.id = sub.id;