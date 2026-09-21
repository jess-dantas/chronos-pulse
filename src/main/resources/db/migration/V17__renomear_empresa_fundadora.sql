-- ==========================================================
-- V17: Renomeação da empresa fundadora do ecossistema para a
--      nova razão social da marca.
-- CNPJ 49.262.262/0001-13 permanece como identificador neste
-- ambiente até a emissão do novo CNPJ (substituição pendente).
-- ==========================================================
UPDATE tenant
SET
    nome = 'LJ CODE TECNOLOGIA E SISTEMAS LTDA',
    razao_social = 'LJ CODE TECNOLOGIA E SISTEMAS LTDA',
    nome_fantasia = 'LJ CODE',
    produto = 'Chronos Suite'
WHERE cnpj = '49262262000113';