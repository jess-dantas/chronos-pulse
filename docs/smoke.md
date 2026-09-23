# Checklist de Smoke (Ajustes + Modelagem)

Validação manual ponta a ponta após `docker compose down -v && docker compose up --build -d`
(banco novo — obrigatório: `V001__baseline_chronos_pulse.sql`, arquivo único de migration) e `flutter run -d web-server --web-port 3000 --dart-define=API_URL=http://localhost:3030/api/v1`.

## 1. Login Administrator (CORS + 1º acesso)

- [ ] **Verificar seed dev:** após `docker compose up --build -d`, `docker compose logs app | grep -i flyway` mostra a aplicação de `R__seed_admin_dev.sql` e `SELECT count(*) FROM admin_plataforma;` retorna **1**. Se retornar 0, o wizard fica visível mesmo em dev — investigar o history do Flyway.
- [ ] `POST /admin/auth/login` do browser **não** retorna erro de CORS (A1: `.cors(withDefaults())` em `AdminSecurityConfig`).
- [ ] **Dev:** `GET /admin/auth/bootstrap/status` → `bootstrapAvailable: false` (seed `R__seed_admin_dev.sql` criou `Administrator`); login `Administrator`/`admin123` entra direto (2FA não obrigatório no dev).
- [ ] **Prod-like** (vácuo `admin_plataforma`): status → `bootstrapAvailable: true`; link "Criar primeiro Administrator" aparece em `/admin/auth/login`.
- [ ] `POST /admin/auth/bootstrap` cria a conta e redireciona a `/admin/auth/setup-2fa` (`setupRequired: true` + tempToken). **Regressão:** retornava 409 "Conflito de integridade de dados" (`criado_em` NOT NULL) — agora deve ser **200**.
- [ ] Setup forçado: `2fa/setup` → `2fa/confirm` → dialog com **8 códigos de recuperação** (salvar) → `/admin/dashboard`.
- [ ] Login subsequente com 2FA desligado e `two-factor-required=true` cai no mesmo setup (`setupRequired`).
- [ ] `POST /admin/auth/2fa/recover` com um código usado → tokens + 8 novos códigos; código antigo não reutiliza.
- [ ] `POST /admin/auth/2fa/disable` com required=true → **403**.
- [ ] CPF `99999999999` **não** existe em nenhum seed (login falha).

## 2. Tenants / Portal público

- [ ] Login CPF `11111111111`/`admin123` → tenant slug **`demonstracao`** (9 módulos).
- [ ] `GET /api/v1/publico/transparencia/demonstracao` responde; `.../lj-code` responde; slug antigo `chronos-pulse-demo` → 404.
- [ ] Ex-Red Cape aparece como **LJ Code** (slug `lj-code`, CNPJ 49.262.262/0001-13), sem usuários CPF.

## 3. Rail / Perfil (A4/A5 + B2)

- [ ] Ícones do `NavigationRail` menores (20) e labels 11px; bloco de perfil + logout no **rodapé** do rail (MainShell e AdminShell), rail com **largura fixa (~120px)** à esquerda.
- [ ] **Regressão de layout:** pós-login em tela larga (>800px), o painel de conteúdo carrega com largura normal (bug anterior: footer `double.infinity` consumia a largura toda e o conteúdo ficava com 0px — só o AppBar aparecia). Coberto por `test/shell_layout_test.dart`.
- [ ] Toque no perfil → `/perfil` (dados, alterar senha; AdminEmpresa vê o card **Transferir titularidade**).
- [ ] Sessão Admin root cai em `/perfil` ao redirecionar indevido, nunca no painel de usuário.

## 4. Transferência de titularidade (B3 + B4)

Pré-condição: login `11111111111`/`admin123`, `chronos.mail.enabled=false` (dev) para OTPs no console.

- [ ] `/perfil` → card **Transferir titularidade** → `/perfil/titularidade` (rota bloqueada a não-ADMIN_EMPRESA).
- [ ] Etapa 1: selecionar Colaborador 1 (CPF igual ao do titular é excluído da lista) → Iniciar → biometria (Web: automática) → avança.
- [ ] Etapa 2: "Enviar código" → OTP 6 dígitos no **log do backend** → marcar confirmação do celular → código → avança.
- [ ] Etapa 3: OTP para e-mail do novo titular (log) → **Concluir transferência** → sessão encerrada → `/login`.
- [ ] Re-login `12345678901`/`senha123` → papel **ADMIN_EMPRESA** + módulos contratados; ex-titular `11111111111` agora é **COLABORADOR** (apenas PONTO).
- [ ] Botão ✕ (cancelar) encerra sem trocar papéis; iniciar nova transferência cancela a anterior em aberto.
- [ ] Colaboradores: cadastro/edição aceitam **celular** e a lista devolve o campo.

## 5. Termo de Ciência (LGPD) e 2FA com QR

- [ ] Login painel (ex.: `12345678901`/`senha123`) com `consentimento_privacidade` vazio → modal **bloqueante** "Termo de Ciência de Privacidade" (barreira não dispensável); "Ciente e de acordo (v1.0)" registra (201) e grava `tenant_id`, `user_agent`, `hash_termo` (SHA-256) + auditoria; "Sair" faz logout.
- [ ] Reenvio/reabertura é **idempotente** (sem duplicar registro nem auditoria); `GET /api/v1/privacidade/consentimento/status` reflete `aceitePendente: false` após o aceite; "Ciência registrada — v1.0" aparece em `/painel/privacidade`.
- [ ] `POST /privacidade/consentimento` com `aceito: false` → **400**; versão divergente → **400**.
- [ ] Setup 2FA admin (`/admin/auth/setup-2fa`): QR Code visível (otpauth), chave manual sob demanda, sem link de configuração em texto; após confirm → `/admin/auth/codigos-recuperacao` com 8 códigos, "Concluir" travado até "Li e salvei".
- [ ] Diálogo de logout (rail/NavigationBar): **"Não"** é o botão em destaque (FilledButton primário) e **"Sim"** é texto vermelho.

## 6. Suítes automatizadas

- [ ] Backend: `.\mvnw.cmd test` → **363 testes (61 suites), 0 falhas** (inclui `AdminPlataformaJpaEntityPersistTest` — regressão do `criado_em`; `PrivacidadeServiceTest` com status/auditoria/idempotência do Termo de Ciência).
- [ ] Frontend: `flutter analyze` sem issues · `flutter test` → **217 testes, 0 falhas** (inclui `shell_layout_test.dart` — regressão do layout + grupo "Consentimento bloqueante").
