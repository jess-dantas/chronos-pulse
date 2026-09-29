-- Seed do contrato de demonstração (tenant Demonstração) — tela admin "Contratos",
-- métrica "Contratos ativos" do dashboard e resumos de Transparência/Portal.
--
-- Movido de V001__baseline_chronos_pulse.sql: a migração 001 já tinha sido
-- aplicada em produção e o Flyway valida o checksum do arquivo (migração
-- aplicada é imutável). Conteúdo novo sempre entra em uma migração nova.
INSERT INTO contrato (
    id, tenant_id, numero, objeto, data_inicio, data_fim,
    valor_mensal, valor_total, status, observacoes,
    empenho_numero, valor_empenhado, valor_liquidado, vencimento_aviso_dias
)
VALUES (
    '44444444-4444-4444-8444-444444444401',
    'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11',
    'CP-2026/0001',
    'Prestação de serviços do Chronos Pulse Suite (Ponto Eletrônico, RH e Gestão Administrativa)',
    DATE '2026-01-01',
    DATE '2026-12-31',
    1499.00,
    17988.00,
    'ATIVO',
    'Contrato de demonstração gerado no seed baseline.',
    '2026NE000123',
    17988.00,
    8994.00,
    30
)
ON CONFLICT (id) DO NOTHING;
