# Perfis e Permissões (RBAC)

O sistema usa **RBAC** baseado em roles extraídas do token JWT. Cada perfil recebe a authority `ROLE_<ROLE>` (o `JwtAuthFilter` também adiciona `ROLE_ESTOQUE` para colaboradores com `acessoEstoque`).

| Perfil | Escopo | Ações principais |
|---|---|---|
| `ADMIN_PLATAFORMA` | Global / SaaS | Catálogo de módulos, ativação por empresa, **todas as rotas** |
| `SUPORTE_N1` | Global / SaaS | Suporte nível 1 (`/api/v1/suporte`, módulos, acesso de leitura) |
| `SUPORTE_N2` | Global / SaaS | Suporte nível 2 |
| `ADMIN_EMPRESA` | Tenant | Gestão completa (colaboradores, ponto, estoque, patrimônio, frota, protocolo, compras, licitações, transparência, contratos) |
| `GESTOR_RH` | Tenant | Colaboradores, ponto, estoque + gerência de compras/licitações/transparência |
| `COLABORADOR` | Individual | Ponto eletrônico e leitura (estoque/frota/patrimônio/protocolo/transparência quando o módulo estiver ativo); estoque e compras com `acessoEstoque` |

## Regras de Rota (SecurityConfig)

> A regra de URL libera o acesso; a **efetividade final é a interseção** com `@PreAuthorize` do controller.

| Rota | Perfis permitidos (URL) |
|---|---|
| `/api/v1/admin/**` | `ADMIN_PLATAFORMA`, `SUPORTE_N1`, `SUPORTE_N2` |
| `/api/v1/suporte/**` | `SUPORTE_N1`, `SUPORTE_N2` |
| `/api/v1/empresas/**` | `ADMIN_PLATAFORMA` |
| `/api/v1/colaboradores/**` | `ADMIN_PLATAFORMA`, `ADMIN_EMPRESA`, `GESTOR_RH` |
| `/api/v1/pontos/**` | `COLABORADOR`, `ADMIN_EMPRESA`, `GESTOR_RH`, `ADMIN_PLATAFORMA` |
| `/api/v1/fiscal/**` | `ADMIN_EMPRESA`, `ADMIN_PLATAFORMA`, `GESTOR_RH` |
| `/api/v1/estoque/**` | `ADMIN_PLATAFORMA`, `ADMIN_EMPRESA`, `GESTOR_RH`, `ROLE_ESTOQUE` |
| `/api/v1/compras/**` | `ADMIN_PLATAFORMA`, `ADMIN_EMPRESA`, `GESTOR_RH`, `ROLE_ESTOQUE` |
| `/api/v1/licitacoes/**` | `ADMIN_PLATAFORMA`, `ADMIN_EMPRESA`, `GESTOR_RH`, `ROLE_ESTOQUE` |
| `/api/v1/patrimonio/**` | `ADMIN_PLATAFORMA`, `ADMIN_EMPRESA`, `GESTOR_RH`, `COLABORADOR` |
| `/api/v1/frota/**` | `ADMIN_PLATAFORMA`, `ADMIN_EMPRESA`, `GESTOR_RH`, `COLABORADOR` |
| `/api/v1/protocolo/**` | `ADMIN_PLATAFORMA`, `ADMIN_EMPRESA`, `GESTOR_RH`, `COLABORADOR` |
| `/api/v1/transparencia/**` | `ADMIN_PLATAFORMA`, `ADMIN_EMPRESA`, `GESTOR_RH`, `ROLE_ESTOQUE`, `COLABORADOR` |
| `/api/v1/telemetria/**` | `ADMIN_PLATAFORMA` (exceto `POST /eventos`, autenticado) |
| `/v3/api-docs/**`, `/swagger-ui/**`, `/swagger-ui.html` | Público |

### `@PreAuthorize` por operação (módulos verticais e de compras/licitações)

- **Compras** (`@RequiresModulo("COMPRAS")`): leituras (fornecedores, pedidos, NFe, banco de preços), criação de pedido e recebimento por NFe aceitam `ADMIN_PLATAFORMA`/`ADMIN_EMPRESA`/`GESTOR_RH`/`ROLE_ESTOQUE`. **Cadastro/edição/inativação de fornecedor**: só `ADMIN_PLATAFORMA`/`ADMIN_EMPRESA`. **Requisições**: leitura/CRUD com o grupo de compras; **cancelamento, cotações (criar/propostas/concluir/cancelar/gerar-pedidos)**: gerência (`ADMIN_PLATAFORMA`/`ADMIN_EMPRESA`/`GESTOR_RH`).
- **Licitações** (`@RequiresModulo("LICITACOES")`): leituras (listagem, detalhe, lances, planejamento) aceitam `ADMIN_PLATAFORMA`/`ADMIN_EMPRESA`/`GESTOR_RH`/`ROLE_ESTOQUE`; **todas as escritas** (criar, publicar, PNCP, propostas, disputa, lances, adjudicar, homologar, cancelar, gerar pedidos, contrato, planejamento ETP/TR/edital) restringem-se à **gerência** (`ADMIN_PLATAFORMA`/`ADMIN_EMPRESA`/`GESTOR_RH`).
- **Transparência** (`@RequiresModulo("TRANSPARENCIA")`): leituras (resumo, despesas mensais, publicações) aceitam `ADMIN_PLATAFORMA`/`ADMIN_EMPRESA`/`GESTOR_RH`/`ROLE_ESTOQUE`/**`COLABORADOR`**; **publicação/remoção** restringem-se à gerência.
- **Patrimônio/Frota/Protocolo** (módulos verticais): cadastros e alterações de status restringem-se a `ADMIN_PLATAFORMA`/`ADMIN_EMPRESA`; leituras aceitam também `COLABORADOR`.

## Enforço por Módulo

Perfis de plataforma (`ADMIN_PLATAFORMA`, `SUPORTE_N1`, `SUPORTE_N2`) **ignoram** o `ModuloInterceptor` (acesso liberado a qualquer módulo). Para os demais perfis, a empresa precisa ter o módulo ativado para `tenantId` do usuário — ver [`modulos-saas.md`](modulos-saas.md).

## Sessão

- `accessToken`: 1 hora · `refreshToken`: máximo de 8 horas desde o login (limite absoluto da sessão).
- Idle timeout de 15 minutos monitorado pelo app; o backend também expira o refresh token após 8h.