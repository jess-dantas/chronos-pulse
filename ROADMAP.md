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

Cada requisito entrega backend + app (Flutter), com testes e migrações. `docs/api.md` e testes (257 no backend, 152 no app) refletem o estado atual.

| Req | Entrega | Status |
|---|---|---|
| **R27** | Telemetria | ✅ |
| **R28** | Planejamento da Contratação (Fase Preparatória): ETP/TR + Editais | ✅ |
| **R29** | Disputa Eletrônica (Pregão): lances por item, abrir disputa e adjudicação derivada | ✅ |
| **R30** | *A definir* | ⏭️ |

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
| CI: build, 257 testes, security (Trivy) com upload SARIF para Code Scanning | ✅ |
| CI app: analyze, 152 testes, build Android (APK+AppBundle), build Web (Netlify) e build iOS | ✅ |
| Credenciais/segredos fora do código (versionadas em `docs/credenciais.md`) | ✅ |
| Documentação sincronizada (9 módulos, 32 migrations, RBAC, API) | ✅ |
| Deploy **staging** (Render) | ✅ |
| Deploy **produção** (infra cloud + secrets `DB_URL`/`DB_USER`/`DB_PASSWORD`/`KUBE_CONFIG`) | ⏭️ guard no-op enquanto a infra não existir |

---

## Próximos passos

1. **R30** — definição do escopo (a preencher).
2. **Infra de produção** — provisionar infra cloud e ativar o guard de deploy quando existir.