# Perfis e Permissões (RBAC)

O sistema usa **RBAC** baseado em roles extraídas do token JWT. Cada perfil recebe a authority `ROLE_<ROLE>` (o `JwtAuthFilter` também adiciona `ROLE_ESTOQUE` para colaboradores com `acessoEstoque`).

| Perfil | Escopo | Ações principais |
|---|---|---|
| `ADMIN_PLATAFORMA` | Global / SaaS | Catálogo de módulos, ativação por empresa, todas as rotas |
| `SUPORTE_N1` | Global / SaaS | Suporte nível 1 (`/api/v1/suporte`, módulos, acesso de leitura) |
| `SUPORTE_N2` | Global / SaaS | Suporte nível 2 |
| `ADMIN_EMPRESA` | Tenant | Gestão completa (colaboradores, ponto, estoque, patrimônio, frota, protocolo) |
| `GESTOR_RH` | Tenant | Colaboradores, ponto, estoque e relatórios RH |
| `COLABORADOR` | Individual | Ponto eletrônico e leitura (estoque/frota/patrimônio/protocolo quando o módulo estiver ativo); estoque com `acessoEstoque` |

## Regras de Rota (SecurityConfig)

| Rota | Perfis permitidos |
|---|---|
| `/api/v1/admin/**` | `ADMIN_PLATAFORMA`, `SUPORTE_N1`, `SUPORTE_N2` |
| `/api/v1/suporte/**` | `SUPORTE_N1`, `SUPORTE_N2` |
| `/api/v1/empresas/**` | `ADMIN_PLATAFORMA` |
| `/api/v1/colaboradores/**` | `ADMIN_PLATAFORMA`, `ADMIN_EMPRESA`, `GESTOR_RH` |
| `/api/v1/pontos/**` | `COLABORADOR`, `ADMIN_EMPRESA`, `GESTOR_RH`, `ADMIN_PLATAFORMA` |
| `/api/v1/fiscal/**` | `ADMIN_EMPRESA`, `ADMIN_PLATAFORMA`, `GESTOR_RH` |
| `/api/v1/estoque/**` | `ADMIN_PLATAFORMA`, `ADMIN_EMPRESA`, `GESTOR_RH`, ou `ROLE_ESTOQUE` |
| `/api/v1/patrimonio/**` | `ADMIN_PLATAFORMA`, `ADMIN_EMPRESA`, `GESTOR_RH`, `COLABORADOR` |
| `/api/v1/frota/**` | `ADMIN_PLATAFORMA`, `ADMIN_EMPRESA`, `GESTOR_RH`, `COLABORADOR` |
| `/api/v1/protocolo/**` | `ADMIN_PLATAFORMA`, `ADMIN_EMPRESA`, `GESTOR_RH`, `COLABORADOR` |
| `/v3/api-docs/**`, `/swagger-ui/**`, `/swagger-ui.html` | Público |

> A efetividade final é a **interseção** entre a regra de URL do `SecurityConfig` e o `@PreAuthorize` do controller. Nos módulos verticais (patrimônio/frota/protocolo), **cadastros e alterações de status** restringem-se a `ADMIN_PLATAFORMA`/`ADMIN_EMPRESA`; **leituras** aceitam também `COLABORADOR`.

## Enforço por Módulo

Perfis de plataforma (`ADMIN_PLATAFORMA`, `SUPORTE_N1`, `SUPORTE_N2`) **ignoram** o `ModuloInterceptor` (acesso liberado a qualquer módulo). Para os demais perfis, a empresa precisa ter o módulo ativado para `tenantId` do usuário — ver [`modulos-saas.md`](modulos-saas.md).

## Sessão

- `accessToken`: 1 hora · `refreshToken`: máximo de 8 horas desde o login (limite absoluto da sessão).
- Idle timeout de 15 minutos monitorado pelo app; o backend também expira o refresh token após 8h.