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

> **Zero-trace:** nenhum usuário com CPF `99999999999` (ex-Fundador) é criado
> nos seeds — em produção o Administrator nasce apenas pelo first-run wizard.

## Administrator (Admin Plataforma)

- **Username:** `Administrator` (tabela `admin_plataforma` — entidade separada, **sem CPF, sem tenant**). Login na tela "Login Administrator" (`/admin/auth/login`, fora de `/api/v1`).
- **2FA TOTP é obrigatório em produção** (`chronos.admin.two-factor-required`, default `true`; `CHRONOS_ADMIN_2FA_REQUIRED` sobrepõe). A tabela nasce **vazia em produção** — ver abaixo.

### Produção — first-run wizard (zero-trace)

1. `GET /admin/auth/bootstrap/status` → `{ bootstrapAvailable: true }` enquanto `admin_plataforma` estiver vazia; a tela de login exibe o link de criação (`/admin/auth/bootstrap`).
2. `POST /admin/auth/bootstrap` `{ username (≤20), senha (8–100), nomeCompleto, email }` → cria o Administrator e responde `requiresTwoFactor: true`, `setupRequired: true` + `tempToken` (5 min).
3. Setup **obrigatório** do 2FA: `POST /admin/auth/2fa/setup` → `POST /admin/auth/2fa/confirm { codigo }` → emite os tokens finais **e 8 códigos de recuperação** (`XXXXX-XXXXX`, exibidos uma única vez; hash SHA-256, uso único, tabela `admin_recovery_code`).
4. Sem senha seed — nada é gravado em `V1` para o Administrator.

### Desenvolvimento — seed dedicado

- `db/seed/R__seed_admin_dev.sql` (flyway locations incluem `classpath:db/seed` **apenas no profile dev**):
  - **Username:** `Administrator` · **Senha:** `admin123` · 2FA desabilitado.
  - `chronos.admin.two-factor-required: false` em `application-dev.yml` — login direto, sem setup forçado; ativação continua opt-in pela tela **Segurança**.
- Em dev, `chronos.mail.enabled` é `false` por default: os OTPs do wizard de titularidade são logados no console (INFO) para smoke test sem SMTP.

### Recuperação de acesso (qualquer ambiente)

- `POST /admin/auth/2fa/recover` `{ username, senha, recoveryCode }` → tokens + **8 novos códigos** de recuperação (os antigos deixam de valer).
- `POST /admin/auth/2fa/disable` retorna **403** enquanto `chronos.admin.two-factor-required=true` (2FA não pode ser desativado em produção).

> Senhas em texto plano acima são **apenas para demonstração**. Nunca reuse
> essas senhas em ambientes reais; o hash bcrypt correspondente está no seed.

Para saber como subir a aplicação, ver [`README.md`](../README.md). Checklist de smoke: [`smoke.md`](smoke.md).
