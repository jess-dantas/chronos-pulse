# Dados Iniciais, Migrations e Testes

## Seeds

Aplicados principalmente pelas migrations `V3`, `V5`, `V9`, `V11`, `V21`, `V24` e `V26`
(as credenciais são documentadas em [`credenciais.md`](credenciais.md)).

### Usuários no tenant de demonstração (Chronos Pulse Tech LTDA)

| Usuário | Perfil | CPF | Acesso Estoque | Tenant |
|---|---|---|---|---|
| Admin Empresa | `ADMIN_EMPRESA` | `11111111111` | Sim (irrestrito) | Chronos Pulse Tech LTDA |
| Gestor de RH | `GESTOR_RH` | `22222222222` | Sim (irrestrito) | Chronos Pulse Tech LTDA |
| Colaborador Padrão | `COLABORADOR` | `12345678901` | Não (apenas ponto) | Chronos Pulse Tech LTDA |
| Colaborador Almoxarife | `COLABORADOR` | `98765432100` | Sim (ponto + estoque) | Chronos Pulse Tech LTDA |

> **Tenant de demonstração:** `a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11` — possui os **9 módulos** ativos.
> **Tenant fundador (Red Cape):** `a0eebc99-0009-0009-0009-6bb9bd380a09` — seed `V9` (Admin Empresa com hash bcrypt).

**Slugs públicos (`V34`)**: endpoint público do portal usa `slug` do tenant, ex.: `/api/v1/publico/transparencia/chronos-pulse-demo/...` (demo) e `/api/v1/publico/transparencia/red-cape/...` (fundador). Novas empresas herdam `empresa-<cnpj>` automaticamente.

### Dados de demonstração

- **Patrimônio (`V11`)**: 3 bens (notebook, impressora, veículo) — estados variados.
- **Frota (`V11`)**: 2 veículos (Palio Adventure, S10) + 3 abastecimentos.
- **Protocolo (`V11`)**: 2 protocolos (`PROTO-2026-000001` em tramitação, `PROTO-2026-000002` recebido).
- **Compras (`V21`–`V23`)**: fornecedores, pedidos de compra e entradas de NFe.
- **Licitações (`V24`, `V29`–`V33`)**: licitações com planejamento (ETP/TR/edital), publicações PNCP, disputa, formalização e execução de contrato (aditivos, fiscalização, medições, sanções, rescisão).
- **Transparência (`V26`)**: publicações de receitas/despesas (ex.: `2026-08` — Compras R$ 20.485,00 e Licitações R$ 53.850,00).

## Migrations

Local: `src/main/resources/db/migration/` — total de **34** (`V1`–`V34`).

| Migration | Conteúdo |
|---|---|
| `V1`–`V2` | Estrutura base: registros de ponto e tabelas de auth/empresa/colaborador |
| `V3` | Seed inicial (usuários de demonstração, Admin Plataforma) |
| `V4` | Módulo Estoque & Almoxarifado |
| `V5` | Gestor de RH e acesso ao estoque |
| `V6` | Ajuste manual e espelho de ponto |
| `V7` | Contato da empresa e celular do colaborador |
| `V8` | Contrato e eventos de contrato |
| `V9` | Seed do fundador (Red Cape) e jornada padrão |
| `V10` | E-mail admin, endereço, foto e recuperação de senha |
| `V11` | **Plataforma modular**: `modulo_plataforma`, `empresa_modulo` (trio core) + `tb_patrimonio`, `tb_frota_veiculo`, `tb_frota_abastecimento`, `tb_protocolo` |
| `V12` | Módulos de acesso no colaborador e data de desligamento |
| `V13` | Atualiza e-mail do Admin Plataforma |
| `V14` | Versionamento otimista (`version`) |
| `V15` | Tabela de auditoria |
| `V16` | Consentimento de privacidade (LGPD) |
| `V17` | Renomeia empresa fundadora |
| `V18` | Desfazimento de patrimônio |
| `V19` | Saldo e vigência de contrato |
| `V20` | Estoque: termo de recebimento, motivo de baixa e código de barras |
| `V21` | **Compras**: fornecedores, pedidos, NFe + catálogo `COMPRAS` |
| `V22` | Compras: requisições e cotações |
| `V23` | Compras: importação de XML da NFe e consulta SEFAZ |
| `V24` | **Licitações** (Lei 14.133/2021) + catálogo `LICITACOES` |
| `V25` | Patrimônio avançado (inventário/transferência) |
| `V26` | **Transparência & BI** (LC 131/2009) + catálogo `TRANSPARENCIA` |
| `V27` | Fix de índices de requisição de compra |
| `V28` | Telemetria de eventos de login |
| `V29` | Planejamento de licitação (ETP, TR, edital) |
| `V30` | Publicação de licitação no PNCP |
| `V31` | Disputa eletrônica e lances |
| `V32` | Formalização de contrato a partir da licitação |
| `V33` | Execução contratual: aditivos, apontamentos, medições, sanções e rescisão |
| `V34` | **Portal público (R31)**: `slug` único no tenant (`chronos-pulse-demo`, `red-cape`, padrão `empresa-<cnpj>`) |

## Testes Automatizados

```bash
# Linux / macOS / WSL
./mvnw test

# Windows
.\mvnw.cmd test
```

**Total: 282 testes, 0 falhas.** Principais coberturas:

| Camada / Módulo | Testes | Objetivo |
|---|---|---|
| **Licitações** (`LicitacaoService`, `Planejamento`, `Pncp*`, `ContratoExecucao`) | 72 | Ciclo completo (planejamento ETP/TR/edital, publicação PNCP, lances, contrato, execução contratual) |
| **Portal Público** (`PortalTransparenciaService`) | 7 | Resumo, licitações, contratos (aditivos/sanções) e publicações públicas por `slug` |
| **Compras** (`ComprasService`, `RequisicaoCotacao`, `Nfe*`) | 48 | Fornecedores, requisições, cotações, pedidos, NFe/XML/SEFAZ |
| **Patrimônio** (`Patrimonio`, `Inventario`, `Desfazimento`, `Transferencia`) | 28 | Tombamento, inventário, desfazimento, transferência |
| **Ponto** (registro, hash, sincronização, espelho, ajuste, AEJ, repository) | 25 | Ciclo ENTRADA→SAIDA, NSR, hash SHA-256, espelho, AEJ (Portaria 671/2021) |
| **Estoque** (PMP, movimentações, material, requisições) | 18 | Cálculo de custo médio, saldos, ciclo de requisições |
| **Auth & Segurança** (`PaswordPolicy`, `Autenticar`, CORS) | 14 | JWT, claims, política de senha, CORS |
| **Telemetria** (`TelemetriaService`, controller, login metrics) | 14 | Métricas de login e auditoria de acesso |
| **Compartilhado/Util** (`GlobalExceptionHandler`, `CnpjValidator`) | 13 | Tratamento de erros e validação de CNPJ |
| **Colaborador** (cadastrar, atualizar, excluir, listar) | 8 | CRUD com CPF, tenant e permissões |
| **Transparência** (`TransparenciaService`) | 7 | Resumo, despesas mensais e publicações |
| **Privacidade** (`PrivacidadeService`) | 7 | Consentimento (LGPD) |
| **Módulos** (`ModuloService`) | 7 | Catálogo e ativação por tenant |
| **Fiscal (AEJ)** | 5 | Arquivo AEJ + notificação/comprovante |
| **Notificação** (`EmailComprovantePonto`) | 4 | Comprovante por e-mail e fallback |
| **Admin** (`AtualizarSaldoContrato`) | 4 | Saldo de contrato |
| **Auditoria** (`AuditoriaService`) | 3 | Registro de trilha de auditoria |
| **Empresa** (`CadastrarEmpresa`) | 2 | Cadastro de tenant |
| **Smoke** (`ChronosPulseApplicationTests`) | 1 | Carregamento do contexto |

## Coleção Insomnia

`src/test/resources/collections/Insomnia.yaml` — pastas: Autenticação & Acesso, Empresas, Colaboradores, Gestão de Ponto, Fiscal & Auditoria, Estoque & Almoxarifado.