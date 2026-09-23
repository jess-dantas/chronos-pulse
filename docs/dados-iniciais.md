# Dados Iniciais, Migrations e Testes

## Seeds

Aplicados integralmente pela **migration única** `V1__baseline_chronos_pulse.sql`
(as credenciais são documentadas em [`credenciais.md`](credenciais.md)).

### Administrator (Admin Plataforma)

- Tabela `admin_plataforma`, username `Administrator`, **sem CPF e sem tenant** (entidade separada de `cpc_usuario`).
- **Produção:** tabela nasce **vazia** (zero-trace) — provisionamento pelo first-run wizard `POST /admin/auth/bootstrap` + setup **obrigatório** de 2FA (`chronos.admin.two-factor-required: true`) + **8 códigos de recuperação** exibidos uma única vez (tabela `admin_recovery_code`).
- **Dev:** seed `db/seed/R__seed_admin_dev.sql` (flyway locations incluem `classpath:db/seed` apenas no profile dev) cria `Administrator` / senha `admin123`, 2FA desabilitado (`two-factor-required: false` no dev).

### Usuários no tenant de demonstração (Chronos Pulse Tech LTDA)

| Usuário | Nome | Perfil | CPF | Acesso Estoque | Módulos (`usuario_modulo`) |
|---|---|---|---|---|---|
| Admin Empresa | Admin Empresa | `ADMIN_EMPRESA` | `11111111111` | Sim (irrestrito) | Herda todos os módulos contratados no 1º consentimento LGPD |
| Gestor de RH | Gestor de RH | `GESTOR_RH` | `22222222222` | Sim (irrestrito) | `PONTO` + `RECURSOS_HUMANOS` + `ESTOQUE` |
| Colaborador 1 | Colaborador 1 | `COLABORADOR` | `12345678901` | Não | `PONTO` |
| Colaborador 2 | Colaborador 2 | `COLABORADOR` | `98765432100` | Sim | `PONTO` + `ESTOQUE` |

> **Tenant de demonstração:** `a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11` (CNPJ `01.001.001/0001-01`, slug **`demonstracao`**) — possui os **9 módulos** ativos.
> **Tenant LJ Code:** `a0eebc99-0009-0009-0009-6bb9bd380a09` (CNPJ `49.262.262/0001-13`, slug **`lj-code`**) — trio core, sem usuários CPF nos seeds (jornada/fiscal ficam neste tenant).

> **Zero-trace:** o usuário CPF `99999999999` (ex-Fundador Red Cape) **não** existe mais em nenhum seed.

**Slugs públicos**: endpoint público do portal usa `slug` do tenant, ex.: `/api/v1/publico/transparencia/demonstracao/...` (demo) e `/api/v1/publico/transparencia/lj-code/...` (LJ Code). Novas empresas herdam `empresa-<cnpj>` automaticamente.

### Dados de demonstração

- **Patrimônio**: 3 bens (notebook, impressora, veículo) — estados variados.
- **Frota**: 2 veículos (Palio Adventure, S10) + 3 abastecimentos.
- **Protocolo**: 2 protocolos (`PROTO-2026-000001` em tramitação, `PROTO-2026-000002` recebido).
- **Compras**: fornecedores, pedidos de compra e entradas de NFe.
- **Licitações**: licitações com planejamento (ETP/TR/edital), publicações PNCP, disputa, formalização e execução de contrato (aditivos, fiscalização, medições, sanções, rescisão).
- **Transparência**: publicações de receitas/despesas (ex.: `2026-08` — Compras R$ 20.485,00 e Licitações R$ 53.850,00).

## Migrations

Local: `src/main/resources/db/migration/` — baseline `V1` + incrementos (`V2+`):

| Migration / Seed | Conteúdo |
|---|---|
| `V1__baseline_chronos_pulse.sql` | Baseline completo consolidado: schema de todas as tabelas (auth, tenant, colaborador, ponto, fiscal, estoque, compras, licitações, patrimônio, frota, protocolo, transparência, privacidade/LGPD, auditoria, telemetria, módulos, **admin_recovery_code**, **titularidade_transferencia/codigo**), catálogo `modulo_plataforma` (9 códigos + `PRIVACIDADE`), `empresa_modulo`, `usuario_modulo` (associação usuário↔módulo com backfill), seeds de tenants/usuários/módulos/dados demo — **sem** linha de `admin_plataforma` (provisionamento via first-run wizard) |
| `V2__consentimento_auditoria_lgpd.sql` | Auditoria do Termo de Ciência (LGPD): `ALTER TABLE consentimento_privacidade` + `tenant_id`, `user_agent`, `hash_termo` (SHA-256 do texto exato do termo) |
| `db/seed/R__seed_admin_dev.sql` | Seed **apenas dev** (profile dev, `classpath:db/seed`): `Administrator` / `admin123`, 2FA desabilitado |

> Histórico `V1`–`V34` foi consolidado neste baseline (squash). Bancos criados
> com o schema antigo devem ser recriados (`docker compose down -v && docker
> compose up --build`).

## Testes Automatizados

```bash
# Linux / macOS / WSL
./mvnw test

# Windows
.\mvnw.cmd test
```

**Total: 363 testes (61 suites), 0 falhas.** Principais coberturas:

| Camada / Módulo | Objetivo |
|---|---|
| **Licitações** (`LicitacaoService`, `Planejamento`, `Pncp*`, `ContratoExecucao`) | Ciclo completo (planejamento ETP/TR/edital, publicação PNCP, lances, contrato, execução contratual) |
| **Portal Público** (`PortalTransparenciaService`) | Resumo, licitações, contratos (aditivos/sanções) e publicações públicas por `slug` |
| **Compras** (`ComprasService`, `RequisicaoCotacao`, `Nfe*`) | Fornecedores, requisições, cotações, pedidos, NFe/XML/SEFAZ |
| **Patrimônio** (`Patrimonio`, `Inventario`, `Desfazimento`, `Transferencia`) | Tombamento, inventário, desfazimento, transferência |
| **Ponto** (registro, hash, sincronização, espelho, ajuste, AEJ, repository) | Ciclo ENTRADA→SAIDA, NSR, hash SHA-256, espelho, AEJ (Portaria 671/2021) |
| **Estoque** (PMP, movimentações, material, requisições) | Cálculo de custo médio, saldos, ciclo de requisições |
| **Auth & Segurança** (`PaswordPolicy`, `Autenticar`, CORS) | JWT, claims, política de senha, CORS |
| **Telemetria** (`TelemetriaService`, controller, login metrics) | Métricas de login e auditoria de acesso |
| **Compartilhado/Util** (`GlobalExceptionHandler`, `CnpjValidator`) | Tratamento de erros e validação de CNPJ |
| **Colaborador** (cadastrar, atualizar, excluir, listar + associação de módulos + **celular**) | CRUD com CPF/celular, tenant, permissões e sincronização `usuario_modulo` |
| **Transparência** (`TransparenciaService`) | Resumo, despesas mensais e publicações |
| **Privacidade** (`PrivacidadeService`) | Consentimento LGPD (inclusive herança de módulos para `ADMIN_EMPRESA`), status do Termo de Ciência (`GET /consentimento/status`), registro idempotente com auditoria reforçada (`tenant_id`/`user_agent`/`hash_termo`) |
| **Módulos** (`ModuloService`, `UsuarioModuloService`) | Catálogo, ativação por tenant e associação por usuário |
| **Fiscal (AEJ)** | Arquivo AEJ + notificação/comprovante |
| **Notificação** (`EmailComprovantePonto`) | Comprovante por e-mail e fallback |
| **Admin** (`AtualizarSaldoContrato`, **bootstrap**, **autenticação com 2FA forçado**, **recuperação por código**) | Saldo de contrato, first-run wizard, 2FA obrigatório e 8 recovery codes |
| **Titularidade** (`TransferirTitularidade`) | Transferência em 3 etapas (biometria → OTP celular → OTP e-mail), TTL 30min transferência/15min OTP, troca de papéis e módulos |
| **Auditoria** (`AuditoriaService`) | Registro de trilha de auditoria |
| **Empresa** (`CadastrarEmpresa`) | Cadastro de tenant |
| **Smoke** (`ChronosPulseApplicationTests`) | Carregamento do contexto |

Frontend (Flutter), no repositório do app:

```bash
flutter analyze
flutter test   # 217 testes, 0 falhas
```

## Coleção Insomnia

`src/test/resources/collections/Insomnia.yaml` — pastas: Autenticação & Acesso, Empresas, Colaboradores, Gestão de Ponto, Fiscal & Auditoria, Estoque & Almoxarifado.
