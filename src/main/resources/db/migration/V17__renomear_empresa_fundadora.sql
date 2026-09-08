-- ==========================================================
-- V17: Renomeação da empresa fundadora do ecossistema para a
--      nova razão social da marca.
-- CNPJ 49.262.262/0001-13 permanece como identificador neste
-- ambiente até a emissão do novo CNPJ (substituição pendente).
-- ==========================================================
UPDATE tenant
SET nome = 'LJ CHRONOS PULSE TECNOLOGIA E SISTEMAS LTDA'
WHERE cnpj = '49262262000113';