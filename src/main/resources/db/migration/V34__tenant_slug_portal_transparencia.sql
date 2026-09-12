-- ==========================================================
-- V34: Portal da Transparência (R31) — identificador público (slug) no tenant
-- Permite URLs públicas estáveis por órgão/empresa, ex.:
--   /api/v1/publico/transparencia/{slug}/licitacoes
-- ==========================================================

ALTER TABLE tenant ADD COLUMN slug VARCHAR(60);

-- Backfill padrão para tenants existentes (novas empresas recebem slug no cadastro)
UPDATE tenant
SET slug = 'empresa-' || cnpj
WHERE slug IS NULL;

-- Slugs amigáveis para os tenants de demonstração/fundador
UPDATE tenant SET slug = 'chronos-pulse-demo'
WHERE id = 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11';

UPDATE tenant SET slug = 'red-cape'
WHERE id = 'a0eebc99-0009-0009-0009-6bb9bd380a09';

ALTER TABLE tenant ALTER COLUMN slug SET NOT NULL;
ALTER TABLE tenant ADD CONSTRAINT uk_tenant_slug UNIQUE (slug);