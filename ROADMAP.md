# Roadmap — Chronos Pulse

> SaaS de **Controle de Ponto** com **Jornada de Contratações Públicas** (Lei 14.133/2021).
> Plataforma modular: cada empresa (tenant/CNPJ) contrata apenas os módulos que adquiriu; o servidor bloqueia o acesso aos demais (`@RequiresModulo` + `ModuloInterceptor`).

## Como ler

| Marca | Significado |
|---|---|
| ✅ | Concluído e em produção (CI verde + testes) |
| 🚧 | Em andamento |
| ⏭️ | Próximo / aguardando definição |

---

## Requisitos (R-series)

Cada requisito entrega backend + app (Flutter), com testes e migrações. `docs/api.md` e testes (282 no backend, 166 no app) refletem o estado atual.

| Req | Entrega | Status |
|---|---|---|
| **R27** | Telemetria | ✅ |
| **R28** | Planejamento da Contratação (Fase Preparatória): ETP/TR + Editais | ✅ |
| **R29** | Disputa Eletrônica (Pregão): lances por item, abrir disputa e adjudicação derivada | ✅ |
| **R30** | Gestão da Execução Contratual: aditivos, fiscalização/apontamentos, medições/pagamentos, sanções e rescisão (Lei 14.133/2021) | ✅ |
| **R31** | Portal da Transparência completo (LC 131/2009): publicização automática de licitações, contratos, aditivos, sanções e despesas + endpoints públicos por `slug` | ✅ |
| **R31.1** | Ajustes pós-entrega: robustez da batida de ponto (watchdog global de 30s), voltar nas telas públicas e **onboarding comercial em 3 etapas (lead sem CPF/senha)** | ✅ |

---

## Catálogo de Módulos (SaaS)

Ver detalhes em `docs/modulos-saas.md`. Ativação por tenant com seeds em `V11`, `V21`, `V24` e `V26`.

| Código | Módulo | Tipo |
|---|---|---|
| `PONTO` | Ponto Eletrônico | Core |
| `RECURSOS_HUMANOS` | Recursos Humanos | Core |
| `ESTOQUE` | Estoque & Almoxarifado | Core |
| `PATRIMONIO` | Patrimônio Público | Comercializável |
| `FROTA` | Gestão de Frota | Comercializável |
| `PROTOCOLO` | Protocolo & Tramitação | Comercializável |
| `COMPRAS` | Compras & Fornecedores (pedidos, NFe, banco de preços) | Comercializável |
| `LICITACOES` | Licitações & Contratações (Lei 14.133/2021) | Comercializável |
| `TRANSPARENCIA` | Portal da Transparência & BI (LC 131/2009) | Comercializável |

---

## Marcos Não-Funcionais

| Marco | Status |
|---|---|
| CI: build, 275 testes, security (Trivy) com upload SARIF para Code Scanning | ✅ |
| CI app: analyze, 166 testes, build Android (APK+AppBundle), build Web (Netlify) e build iOS | ✅ |
| Credenciais/segredos fora do código (versionadas em `docs/credenciais.md`) | ✅ |
| Documentação sincronizada (10 módulos, 34 migrations, RBAC, API) | ✅ |
| Deploy **staging** (Render) | ✅ |
| Deploy **produção** (infra cloud + secrets `DB_URL`/`DB_USER`/`DB_PASSWORD`/`KUBE_CONFIG`) | ⏭️ guard no-op enquanto a infra não existir |

---

## Próximos passos

1. **R31.1 (entregue)** — batida de ponto com watchdog de 30s (link: tela "Bater Ponto"); voltar nas telas de login e cadastro; cadastro público em 3 etapas (Empresa → Endereço → Contato) virou **lead** sem CPF/senha (`POST /api/v1/leads/empresas`, tabela `tb_lead_empresa`). Backend: 286 testes verdes; app: 180 testes verdes.
2. **Acompanhamento de leads** — criar tela admin para listar/alterar status dos leads (NOVO → AGENDADO/REUNIAO/CONTRATADO).
3. **Infra de produção** — provisionar infra cloud e ativar o guard de deploy quando existir.