-- Campos para fluxo de aprovação de ajuste de ponto

-- 1. Enum status de ajuste
CREATE TYPE ajuste_status AS ENUM ('PENDENTE', 'APROVADO', 'REJEITADO');

-- 2. Adiciona colunas na tabela registro_ponto
ALTER TABLE registro_ponto 
ADD COLUMN IF NOT EXISTS ajuste_status ajuste_status DEFAULT 'PENDENTE',
ADD COLUMN IF NOT EXISTS ajuste_motivo_rejeicao VARCHAR(500),
ADD COLUMN IF NOT EXISTS aprovado_por UUID,
ADD COLUMN IF NOT EXISTS aprovado_em TIMESTAMP WITH TIME ZONE;

-- 3. Índices para performance das consultas de aprovação
CREATE INDEX IF NOT EXISTS idx_registro_ponto_ajuste_status ON registro_ponto(ajuste_status) WHERE ajuste_status = 'PENDENTE';
CREATE INDEX IF NOT EXISTS idx_registro_ponto_aprovado_por ON registro_ponto(aprovado_por);

-- 4. Constraint: apenas registros com ajuste_manual=true podem ter ajuste_status
ALTER TABLE registro_ponto 
ADD CONSTRAINT chk_ajuste_status_valido 
CHECK (
    (ajuste_manual = true AND ajuste_status IS NOT NULL) 
    OR (ajuste_manual = false AND ajuste_status IS NULL)
);

-- 5. Backfill: registros existentes com ajuste_manual=true ficam APROVADO (já foram aplicados)
UPDATE registro_ponto 
SET ajuste_status = 'APROVADO' 
WHERE ajuste_manual = true AND ajuste_status IS NULL;