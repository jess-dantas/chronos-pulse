# Credenciais (seeds — demonstração)

> Esta página concentra as credenciais de demonstração aplicadas pelas migrations.
> As credenciais de acesso **privilegiado** (fundador da empresa e Admin Plataforma)
> **não são documentadas em texto plano no repositório** — são entregues fora do
> código (ver seção de acessos do time / gestor de segredos). No código, esses
> usuários existem apenas com **hash bcrypt** (`V3`, `V9`).

## Tenants

- **Tenant de demonstração:** `a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11` (Chronos Pulse Tech LTDA) — possui os **9 módulos** ativos (`PONTO`, `RECURSOS_HUMANOS`, `ESTOQUE`, `PATRIMONIO`, `FROTA`, `PROTOCOLO`, `COMPRAS`, `LICITACOES`, `TRANSPARENCIA`).
- **Tenant fundador (Red Cape):** `a0eebc99-0009-0009-0009-6bb9bd380a09` (`V9`).
- Nova empresa criada via `POST /api/v1/auth/cadastrar-empresa` recebe automaticamente o trio core (`PONTO`, `RECURSOS_HUMANOS`, `ESTOQUE`).

## Usuários de demonstração

> **Atenção:** desde a `V36`, os CPFs seedados estão **desativados em produção** (`ativo = false`) —
> o deploy real cria apenas a estrutura e deixa os usuários **offline**. A reativação ocorre
> exclusivamente no perfil `dev` via seed repetível `db/seed/R__seed_dados_dev.sql`
> (`flyway.locations` inclui `classpath:db/seed` somente em `application-dev.yml`;
> produção utiliza apenas `classpath:db/migration`).

| Perfil | CPF | Senha (only dev seed) | Observação |
|---|---|---|---|
| Admin Empresa | `11111111111` | `admin123` | Acesso irrestrito no tenant (todos os módulos) |
| Gestor de RH | `22222222222` | `admin123` | Gestão de RH, ponto, estoque |
| Colaborador Padrão | `12345678901` | `senha123` | Apenas ponto eletrônico |
| Colaborador Almoxarife | `98765432100` | `senha123` | Ponto + estoque (`acessoEstoque=true`, authority `ROLE_ESTOQUE`) |

## Perfis de plataforma (fora do repositório)

- **Admin Plataforma** (CPF `00000000000`): seedado na `V3`; `V13` só atualiza o e-mail corporativo.
  Desativado em produção pela `V36` — senha entregue fora do repositório e reativado apenas no seed dev.
- **Fundador / Admin Empresa da Red Cape** (CPF `99999999999`): seedado na `V9` com hash bcrypt.
  Desativado em produção pela `V36` — senha entregue fora do repositório e reativado apenas no seed dev.

Para saber como subir a aplicação, ver [`README.md`](../README.md).