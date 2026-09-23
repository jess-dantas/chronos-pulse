# Perfis e Permissões (RBAC)

O sistema usa **RBAC** baseado em roles extraídas do token JWT. Cada perfil recebe a authority `ROLE_<ROLE>` (o `JwtAuthFilter` também adiciona `ROLE_ESTOQUE` para colaboradores com `acessoEstoque`). As authorities de usuário vêm do **papel atual no banco** (`usuario.getRole()`), não do claim `role` do token — mudanças de papel (ex.: transferência de titularidade) valem **imediatamente**, sem esperar o refresh do JWT.

| Perfil | Escopo | Ações principais |
|---|---|---|
| `ADMIN_PLATAFORMA` | Global / SaaS (LGPD: **sem dados de tenant**) | Catálogo de módulos, ativação por empresa, gestão de empresas (`POST /empresas`), telemetria e actuator — **não** acessa rotas de dados de tenant (ponto, colaboradores, fiscal, estoque, compras, licitações, patrimônio, frota, protocolo, transparência, contratos) |
| `SUPORTE_N1` | Global / SaaS | Suporte nível 1 (`/api/v1/suporte`, módulos, acesso de leitura) |
| `SUPORTE_N2` | Global / SaaS | Suporte nível 2 |
| `ADMIN_EMPRESA` | Tenant | Gestão completa (colaboradores, ponto, estoque, patrimônio, frota, protocolo, compras, licitações, transparência, contratos); **herda todos os módulos contratados** no primeiro consentimento LGPD; aprova/rejeita ajustes de ponto |
| `GESTOR_RH` | Tenant | Colaboradores, ponto (incl. aprovação de ajustes), estoque + gerência de compras/licitações/transparência; associação de módulos por usuário |
| `COLABORADOR` | Individual | Ponto eletrônico e leitura (estoque/frota/patrimônio/protocolo/transparência quando o módulo estiver ativo); estoque e compras com `acessoEstoque` |

> **LGPD (Admin Plataforma):** `ADMIN_PLATAFORMA` é removido de **todas** as
> regras de URL de dados de tenant. A única rota `/api/v1/empresas/**`
> permitida é `POST` (criação de empresa). Anotações `@PreAuthorize` em alguns
> controllers ainda mencionam o perfil, mas a **camada de URL nega antes** — a
> efetividade final é a interseção URL × `@PreAuthorize`.

## Regras de Rota (SecurityConfig)

> A regra de URL libera o acesso; a **efetividade final é a interseção** com `@PreAuthorize` do controller.

| Rota | Perfis permitidos (URL) |
|---|---|
| `/admin/**` (auth Admin Plataforma) | Ver [`api.md` §2](api.md): `login`, `logout`, `2fa/verify`, `bootstrap`, `bootstrap/status`, `2fa/recover` anônimos; demais exigem `ADMIN_PLATAFORMA` (`AdminSecurityConfig`) |
| `/api/v1/admin/**` | `ADMIN_PLATAFORMA`, `SUPORTE_N1`, `SUPORTE_N2` |
| `/api/v1/suporte/**` | `SUPORTE_N1`, `SUPORTE_N2` |
| `POST /api/v1/empresas/**` | `ADMIN_PLATAFORMA` (única rota de empresa permitida ao admin) |
| `/api/v1/colaboradores/**` | `ADMIN_EMPRESA`, `GESTOR_RH` |
| `/api/v1/titularidade/**` | `ADMIN_EMPRESA` (transferência de titularidade; tenant e solicitante vêm da sessão) |
| `/api/v1/pontos/**` | `COLABORADOR`, `ADMIN_EMPRESA`, `GESTOR_RH` |
| `/api/v1/fiscal/**` | `ADMIN_EMPRESA`, `GESTOR_RH` |
| `/api/v1/estoque/**` | `ADMIN_EMPRESA`, `GESTOR_RH`, `ROLE_ESTOQUE` |
| `/api/v1/compras/**` | `ADMIN_EMPRESA`, `GESTOR_RH`, `ROLE_ESTOQUE` |
| `/api/v1/licitacoes/**` | `ADMIN_EMPRESA`, `GESTOR_RH`, `ROLE_ESTOQUE` |
| `/api/v1/contratos/**` | `ADMIN_EMPRESA`, `GESTOR_RH`, `ROLE_ESTOQUE` |
| `/api/v1/patrimonio/**` | `ADMIN_EMPRESA`, `GESTOR_RH`, `COLABORADOR` |
| `/api/v1/frota/**` | `ADMIN_EMPRESA`, `GESTOR_RH`, `COLABORADOR` |
| `/api/v1/protocolo/**` | `ADMIN_EMPRESA`, `GESTOR_RH`, `COLABORADOR` |
| `/api/v1/transparencia/**` | `ADMIN_EMPRESA`, `GESTOR_RH`, `ROLE_ESTOQUE`, `COLABORADOR` |
| `/api/v1/telemetria/**` | `ADMIN_PLATAFORMA` (exceto `POST /eventos`, autenticado) |
| `/v3/api-docs/**`, `/swagger-ui/**`, `/swagger-ui.html` | Público |

### Associação de módulos por usuário (`usuario_modulo`)

`GET/PUT /api/v1/usuarios/{usuarioId}/modulos` (`UsuarioModuloController`):
`ADMIN_EMPRESA`, `GESTOR_RH`, `ADMIN_PLATAFORMA` (URL: `anyRequest().authenticated()`).
`PUT` sincroniza também os 4 flags legados (`acessoEstoque/Patrimonio/Frota/Protocolo`) em `cpc_usuario`.

### `@PreAuthorize` por operação (módulos verticais e de compras/licitações)

> Observação LGPD: as listas abaixo são as anotações dos controllers; para
> `ADMIN_PLATAFORMA` vale a negação da camada de URL acima.

- **Ponto / ajustes** (`EspelhoPontoController`): leitura do espelho e `POST /pontos/ajustar` aceitam `COLABORADOR`/`ADMIN_EMPRESA`/`GESTOR_RH` (+ `ADMIN_PLATAFORMA` na anotação, negado na URL); **aprovação de ajustes** (`GET /pontos/ajustes/pendentes`, `PUT .../aprovar`, `PUT .../rejeitar`) apenas `ADMIN_EMPRESA`/`GESTOR_RH`.
- **Compras** (`@RequiresModulo("COMPRAS")`): leituras (fornecedores, pedidos, NFe, banco de preços), criação de pedido e recebimento por NFe aceitam `ADMIN_EMPRESA`/`GESTOR_RH`/`ROLE_ESTOQUE`. **Cadastro/edição/inativação de fornecedor**: gerência (`ADMIN_EMPRESA`; anotação também `ADMIN_PLATAFORMA`, negado na URL). **Requisições**: leitura/CRUD com o grupo de compras; **cancelamento, cotações (criar/propostas/concluir/cancelar/gerar-pedidos)**: gerência (`ADMIN_EMPRESA`/`GESTOR_RH`).
- **Licitações** (`@RequiresModulo("LICITACOES")`): leituras (listagem, detalhe, lances, planejamento) aceitam `ADMIN_EMPRESA`/`GESTOR_RH`/`ROLE_ESTOQUE`; **todas as escritas** (criar, publicar, PNCP, propostas, disputa, lances, adjudicar, homologar, cancelar, gerar pedidos, contrato, planejamento ETP/TR/edital) restringem-se à **gerência** (`ADMIN_EMPRESA`/`GESTOR_RH`).
- **Transparência** (`@RequiresModulo("TRANSPARENCIA")`): leituras (resumo, despesas mensais, publicações) aceitam `ADMIN_EMPRESA`/`GESTOR_RH`/`ROLE_ESTOQUE`/**`COLABORADOR`**; **publicação/remoção** restringem-se à gerência.
- **Patrimônio/Frota/Protocolo** (módulos verticais): cadastros e alterações de status restringem-se a `ADMIN_EMPRESA` (anotação também `ADMIN_PLATAFORMA`, negado na URL); leituras aceitam também `COLABORADOR`.

## Enforço por Módulo (empresa × usuário)

Duplo enforço:

1. **Empresa** — `ModuloInterceptor` valida se a empresa contratou o módulo exigido pela rota (perfis de plataforma `ADMIN_PLATAFORMA`/`SUPORTE_N1`/`SUPORTE_N2` ignoram) — ver [`modulos-saas.md`](modulos-saas.md).
2. **Usuário** — a associação `usuario_modulo` (códigos por usuário) restringe o menu e o acesso por usuário; `ADMIN_EMPRESA` **não** herda código por papel — todos os módulos contratados são associados automaticamente no **primeiro consentimento LGPD** (`PrivacidadeService.registrarConsentimento`).

## Sessão

- `accessToken`: 1 hora · `refreshToken`: máximo de 8 horas desde o login (limite absoluto da sessão).
- Idle timeout de 15 minutos monitorado pelo app; o backend também expira o refresh token após 8h.
- **Administrator (Admin Plataforma)**: fluxo separado em `/admin/auth/*` com **2FA TOTP obrigatório em produção** (`chronos.admin.two-factor-required`, default `true`; claim `adminId`, `role=ADMIN_PLATAFORMA`, sem CPF/tenant); entre senha OK e código o backend emite um `tempToken` de 5 minutos — ver [`api.md` §2](api.md). Conta criada pelo first-run wizard (`bootstrap`) com **8 códigos de recuperação** (`POST /admin/auth/2fa/recover`); `2fa/disable` retorna 403 enquanto o 2FA for obrigatório. Tela de login no app: "Login Administrator".
