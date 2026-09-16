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

Cada requisito entrega backend + app (Flutter), com testes e migrações. `docs/api.md` e testes (301 no backend, 204 no app) refletem o estado atual.

| Req | Entrega | Status |
|---|---|---|
| **R27** | Telemetria | ✅ |
| **R28** | Planejamento da Contratação (Fase Preparatória): ETP/TR + Editais | ✅ |
| **R29** | Disputa Eletrônica (Pregão): lances por item, abrir disputa e adjudicação derivada | ✅ |
| **R30** | Gestão da Execução Contratual: aditivos, fiscalização/apontamentos, medições/pagamentos, sanções e rescisão (Lei 14.133/2021) | ✅ |
| **R31** | Portal da Transparência completo (LC 131/2009): publicização automática de licitações, contratos, aditivos, sanções e despesas + endpoints públicos por `slug` | ✅ |
| **R31.1** | Ajustes pós-entrega: robustez da batida de ponto (watchdog global de 30s), voltar nas telas públicas e **onboarding comercial em 3 etapas (lead sem CPF/senha)** | ✅ |
| **R32** | **REP-P (Portaria MTP 671/2021)**: AFD (Anexo V) e AEJ (Anexo VI) com leiaute oficial (CRC-16/CCITT-TRUE, SHA-256 encadeado), endpoints `/fiscal/afd` e `/fiscal/aej`, espelho conforme art. 84. *Pendente de homologação: registro no INPI (art. 91), assinatura ICP-Brasil (art. 88), registros AEJ "02"/"04"/"07", NSR por estabelecimento e validadores do MTE.* | 🚧 |
| **R33** | Acompanhamento de leads: tela admin para listar e avançar o funil (NOVO → AGENDADO → REUNIAO → CONTRATADO/DESCARTADO) — `GET /leads` + `PATCH /leads/{id}/status` | ✅ |

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
| CI: build, 301 testes, security (Trivy) com upload SARIF para Code Scanning | ✅ |
| CI app: analyze, 204 testes, build Android (APK+AppBundle), build Web (Netlify) e build iOS | ✅ |
| Secretos fora do código — `JWT_SECRET` via env (sem default em `docker-compose.yml`); senhas de seed só no perfil dev; CPFs de seed desativados em produção via `V36` | ✅ |
| Segurança (auditoria + correções): lockout de login, reset de senha com código de 8 dígitos, revogação de JWT por troca de senha, `senhaAtual` no alterar-senha, foto validada por magic-bytes, CORS sem wildcard, Swagger/Actuator restritos | ✅ |
| App endurecido: `allowBackup=false`, cleartext só em debug, keystore de release fora do repositório, perfil (nome/CPF/foto/e-mail) no armazenamento seguro, fila offline apagada no logout/LGPD, ajuste de ponto só p/ gestores, erros amigáveis sem `e.toString()` | ✅ |
| Documentação sincronizada (10 módulos, migrations, RBAC, API) | ✅ |
| Deploy **staging** (Render) | ✅ |
| Deploy **produção** (infra cloud + secrets `DB_URL`/`DB_USER`/`DB_PASSWORD`/`KUBE_CONFIG`) | ⏭️ guard no-op enquanto a infra não existir |

---

## Próximos passos

1. **R32 — REP-P (parcialmente entregue)** — AFD (Anexo V) e AEJ (Anexo VI) seguem o leiaute oficial, com testes (CRC-16/CCITT-TRUE validado pelo vetor `"123456789"→2189` do próprio leiaute; SHA-256 encadeado; larguras fixas; sem linhas em branco; trailer com contagem). Para **homologação**: registrar o software no **INPI** (art. 91) e alimentar o campo de registro no cabeçalho do AFD; assinar AFD/AEJ com certificado **ICP-Brasil** (art. 86/88 via PFX); implementar registros AEJ `02` (REPs), `04` (horário contratual) e `07` (ausências/banco de horas); **NSR por estabelecimento (CNPJ)**; roteiro em `docs/inpi.md`. Backend: 301 testes verdes; app: 204.
2. **R33 — Acompanhamento de leads (entregue)** — `GET /leads` + `PATCH /leads/{id}/status` no backend e tela **Acompanhamento de Leads** no painel admin (funil por status, contadores, avanço por dropdown; `docs/api.md` §14).
3. **Espelho conforme art. 84** — conferir campos mínimos no PDF do espelho (empregador, trabalhador, período, jornada contratual, marcações tratadas e duração) e código de verificação.
4. **Infra de produção** — provisionar infra cloud e ativar o guard de deploy quando existir.