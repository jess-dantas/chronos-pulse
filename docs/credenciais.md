# Credenciais (seeds — demonstração)

> Esta página concentra as credenciais de demonstração aplicadas pela migration
> única `V001__baseline_chronos_pulse.sql` (ambiente dev/demo). Em produção os
> mesmos `INSERT ... ON CONFLICT` criam a estrutura e os usuários demo
> **ativos** apenas se o banco for criado do zero com esta migration — revise
> antes de subir um banco real.

## Tenants

- **Tenant de demonstração (Demonstração):** `a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11` (CNPJ `01.001.001/0001-01`, slug **`demonstracao`**) — possui os **9 módulos** ativos (`PONTO`, `RECURSOS_HUMANOS`, `ESTOQUE`, `PATRIMONIO`, `FROTA`, `PROTOCOLO`, `COMPRAS`, `LICITACOES`, `TRANSPARENCIA`). Os 4 usuários demo abaixo pertencem a este tenant.
- **Tenant LJ Code:** `a0eebc99-0009-0009-0009-6bb9bd380a09` (CNPJ `49.262.262/0001-13`, slug **`lj-code`**) — trio core (`PONTO`, `RECURSOS_HUMANOS`, `ESTOQUE`); **sem usuários CPF** nos seeds (dados de jornada e configuração fiscal permanecem neste tenant).
- Nova empresa criada via `POST /api/v1/auth/cadastrar-empresa` recebe automaticamente o trio core (`PONTO`, `RECURSOS_HUMANOS`, `ESTOQUE`).

## Usuários de demonstração (tenant Demonstração)

| Perfil | Nome | CPF | Senha | Observação |
|---|---|---|---|---|
| `ADMIN_EMPRESA` | Admin Empresa | `11111111111` | `admin123` | Gestão completa do tenant; **herda todos os módulos contratados** no primeiro consentimento LGPD |
| `GESTOR_RH` | Gestor de RH | `22222222222` | `admin123` | Colaboradores, ponto, estoque + gerência de compras/licitações/transparência |
| `COLABORADOR` | Colaborador 1 | `12345678901` | `senha123` | Apenas ponto eletrônico |
| `COLABORADOR` | Colaborador 2 | `98765432100` | `senha123` | Ponto + estoque (`acessoEstoque=true`, authority `ROLE_ESTOQUE`) |

> **Administrator (Admin Plataforma):** credenciais, first-run wizard e
> recuperação de acesso movidos para `C:\app\_projeto\admin-plataforma.md`
> (fora do repositório — material sensível).

Para saber como subir a aplicação, ver [`README.md`](../README.md). Checklist de smoke: [`smoke.md`](smoke.md).
