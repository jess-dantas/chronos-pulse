-- Adiciona campos de identificação completa na tabela tenant
ALTER TABLE tenant ADD COLUMN IF NOT EXISTS razao_social VARCHAR(255);
ALTER TABLE tenant ADD COLUMN IF NOT EXISTS nome_fantasia VARCHAR(255);
ALTER TABLE tenant ADD COLUMN IF NOT EXISTS produto VARCHAR(100);

-- Comentários para documentação
COMMENT ON COLUMN tenant.razao_social IS 'Razão social completa da empresa/órgão';
COMMENT ON COLUMN tenant.nome_fantasia IS 'Nome fantasia/marca comercial';
COMMENT ON COLUMN tenant.produto IS 'Produto/Suite principal associado ao tenant (ex.: Chronos Suite)';