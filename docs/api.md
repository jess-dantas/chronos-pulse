# Referência da API

Base: `http://localhost:8080/api/v1` · Formato: JSON · Autenticação: `Authorization: Bearer <accessToken>` (exceto rotas marcadas como públicas).

| Notação | Significado |
|---|---|
| 🔓 | Pública |
| 👤 | Qualquer usuário autenticado (tenant) |
| 🛡️ | Restrito por `@PreAuthorize` (perfis entre parênteses) |
| 🌐 | Perfis de plataforma (`ADMIN_PLATAFORMA`, `SUPORTE_N1`, `SUPORTE_N2`) |

> **LGPD (Admin Plataforma):** `ADMIN_PLATAFORMA` **não** acessa rotas de
> dados de tenant (colaboradores, pontos, fiscal, estoque, compras,
> licitações, contratos, patrimônio, frota, protocolo, transparência) — a
> camada de URL em `SecurityConfig` nega; a única rota de empresa permitida é
> `POST /empresas`. Anotações `@PreAuthorize` em controllers podem ainda
> mencionar o perfil, mas a **efetividade final é a interseção** URL ×
> `@PreAuthorize` (ver [`rbac.md`](rbac.md)).

---

## 1. Autenticação & Cadastro

| Método | Rota | Acesso | Descrição |
|---|---|---|---|
| `POST` | `/auth/login` | 🔓 | Login por CPF/senha → `accessToken`, `refreshToken`, `role`, `modulos`, `cpcId`, `tenantId`, ... |
| `POST` | `/auth/cadastrar-empresa` | 🔓 | Cadastro público: tenant + admin + colaborador + módulos core → já autentica |
| `POST` | `/auth/refresh` | 🔓 | Renova o access token (limite absoluto: 8h da sessão) |
| `POST` | `/auth/esqueci-senha` | 🔓 | Solicita recuperação de senha |
| `POST` | `/auth/redefinir-senha` | 🔓 | Redefine a senha |
| `GET` | `/auth/me` | 👤 | Perfil completo do usuário (inclui `modulos`) |
| `GET` | `/auth/ping` | 🔓 | Health-check |

Login — corpo e resposta resumida:

```jsonc
// Corpo
{ "cpf": "12345678901", "senha": "senha123" }

// Resposta
{
  "accessToken": "eyJ...", "refreshToken": "eyJ...",
  "role": "COLABORADOR", "nome": "Colaborador Teste",
  "tenantId": "a0eebc99-...",
  "acessoEstoque": false,
  "modulos": ["PONTO", "RECURSOS_HUMANOS", "ESTOQUE"]
}
```

---

## 2. Admin Plataforma — Auth (fora de `/api/v1`)

Base: `http://localhost:3030/admin/auth` (o controller é `@RequestMapping("/admin/auth")`, sem o prefixo `/api/v1`).

| Método | Rota | Acesso | Descrição |
|---|---|---|---|
| `POST` | `/admin/auth/login` | 🔓 | Login `username` (≤20) + `senha` (8–100) → `accessToken`/`refreshToken` **ou** `requiresTwoFactor: true` + `tempToken` (5 min); se o 2FA for obrigatório e estiver desligado → `setupRequired: true` |
| `GET` | `/admin/auth/bootstrap/status` | 🔓 | `{ bootstrapAvailable }` — `true` enquanto `admin_plataforma` estiver vazia (first-run) |
| `POST` | `/admin/auth/bootstrap` | 🔓 | First-run wizard: `{ username, senha, nomeCompleto, email }` → cria o Administrator e responde `requiresTwoFactor: true`, `setupRequired: true` + `tempToken` |
| `POST` | `/admin/auth/2fa/verify` | 🔓 | `{ tempToken, codigo }` (6 dígitos) → troca pelos tokens finais |
| `POST` | `/admin/auth/2fa/recover` | 🔓 | `{ username, senha, recoveryCode }` (`XXXXX-XXXXX`) → tokens + **8 novos** códigos de recuperação |
| `POST` | `/admin/auth/logout` | 🔓 | Logout (best-effort; invalidação de refresh pendente) |
| `GET` | `/admin/auth/2fa/status` | 🛡️ (`ADMIN_PLATAFORMA`) | `{ enabled }` |
| `POST` | `/admin/auth/2fa/setup` | 🛡️ (`ADMIN_PLATAFORMA`) | Gera segredo TOTP → `{ secret, otpauthUri }` (segredo fica pendente até o confirm) |
| `POST` | `/admin/auth/2fa/confirm` | 🛡️ (`ADMIN_PLATAFORMA`) | `{ codigo }` — valida TOTP, **ativa** o 2FA e, no fluxo de bootstrap/setup, emite os tokens finais **e 8 códigos de recuperação** (exibidos uma única vez) |
| `POST` | `/admin/auth/2fa/disable` | 🛡️ (`ADMIN_PLATAFORMA`) | `{ codigo }` — exige código TOTP válido e **desativa** o 2FA; **403** quando `chronos.admin.two-factor-required=true` |
| `POST` | `/admin/auth/alterar-senha` | 🛡️ (`ADMIN_PLATAFORMA`) | `{ senhaAtual, novaSenha }` (nova 8–100) |

Regras de acesso de `/admin/**` ficam em `AdminSecurityConfig` (chain separada com `securityMatcher("/admin/**")`); o token admin tem claim `adminId` e `role=ADMIN_PLATAFORMA` (sem CPF/tenant). O `JwtAuthFilter` autentica tokens com `adminId` direto com `ADMIN_PLATAFORMA`, sem lookup em `cpc_usuario`.

**2FA obrigatório:** `chronos.admin.two-factor-required` (default `true`, `CHRONOS_ADMIN_2FA_REQUIRED` sobrepõe; `false` no profile dev). Em produção o Administrator nasce apenas pelo wizard `bootstrap` (zero-trace — `V1` não grava linha em `admin_plataforma`); no dev o seed `db/seed/R__seed_admin_dev.sql` cria `Administrator`/`admin123` sem 2FA. Tabela `admin_recovery_code` guarda os 8 códigos (hash SHA-256, uso único).

---

## 2.1 Transferência de Titularidade `/api/v1/titularidade`

Base: `/api/v1/titularidade` — 🛡️ (`ADMIN_EMPRESA` em todas as rotas; tenant e solicitante vêm da `CpcUsuario` de sessão, **nunca do corpo**). App: wizard `/perfil/titularidade` (`TransferirTitularidadeScreen`).

| Método | Rota | Descrição |
|---|---|---|
| `POST` | `/titularidade/iniciar` | `{ novoTitularId }` → `{ transferenciaId, novoTitularNome, novoTitularCelular }`; cancela transferências abertas anteriores do tenant |
| `POST` | `/titularidade/{id}/etapa/biometria` | `{ confirmado }` — etapa 1 (biometria do solicitante; no app Web é confirmada automaticamente) |
| `POST` | `/titularidade/{id}/etapa/celular/enviar` | Envia OTP de 6 dígitos ao **e-mail do titular atual** → `{ mensagem, destino }` |
| `POST` | `/titularidade/{id}/etapa/celular/verificar` | `{ codigo, celularConfirmado }` — valida o OTP **e** exige a atestação do celular do novo titular |
| `POST` | `/titularidade/{id}/etapa/email/enviar` | Envia OTP ao **e-mail corporativo do novo titular** → `{ mensagem, destino }` |
| `POST` | `/titularidade/{id}/etapa/email/verificar` | `{ codigo }` — valida o OTP do novo titular |
| `POST` | `/titularidade/{id}/concluir` | Exige as 3 etapas; troca papéis (`novo` → `ADMIN_EMPRESA` + módulos contratados; `antigo` → `COLABORADOR`, acesso `PONTO`) |
| `POST` | `/titularidade/{id}/cancelar` | Cancela a transferência em andamento |

Regras: etapas obrigatoriamente em ordem (biometria → celular → e-mail); transferência expira em **30 minutos** (auto-`CANCELADA`); OTPs vencem em **15 minutos** e são armazenados com `PasswordEncoder`; reenvio invalida o código anterior da mesma etapa; novo titular deve pertencer ao mesmo tenant e ser diferente do solicitante. Com `chronos.mail.enabled=false` (dev) os OTPs são logados no console (INFO) para smoke test. Ao concluir, o app encerra a sessão — o novo titular precisa logar novamente para refletir o novo papel.

---

## 3. Admin Plataforma (Módulos)

| Método | Rota | Acesso | Descrição |
|---|---|---|---|
| `GET` | `/admin/modulos` | 🌐 | Catálogo de módulos disponíveis |
| `GET` | `/admin/empresas/{tenantId}/modulos` | 🌐 | Módulos ativos da empresa |
| `PUT` | `/admin/empresas/{tenantId}/modulos` | 🌐 | Substitui os módulos da empresa |

`PUT` — corpo: `{ "modulos": ["PONTO", "PROTOCOLO"] }`.

---

## 4. Empresas (Tenants)

| Método | Rota | Acesso | Descrição |
|---|---|---|---|
| `POST` | `/empresas` | 🛡️ (`ADMIN_PLATAFORMA`) | Cadastra uma empresa |

---

## 5. Colaboradores

| Método | Rota | Acesso | Descrição |
|---|---|---|---|
| `POST` | `/colaboradores` | 🛡️ (`ADMIN_EMPRESA`, `GESTOR_RH`) | Cadastra colaborador (aceita `celular` opcional) |
| `GET` | `/colaboradores` | 🛡️ (mesmos perfis) | Lista do tenant (inclui `celular`) |
| `PUT` | `/colaboradores/{id}` | 🛡️ (mesmos perfis) | Atualiza dados/acesso ao estoque (`celular` só muda se informado) |
| `DELETE` | `/colaboradores/{id}` | 🛡️ (mesmos perfis) | Soft delete |

### Módulos por usuário (`usuario_modulo`)

Base: `/usuarios/{usuarioId}/modulos` — 🛡️ (`ADMIN_EMPRESA`, `GESTOR_RH`, `ADMIN_PLATAFORMA`).

| Método | Rota | Acesso | Descrição |
|---|---|---|---|
| `GET` | `/usuarios/{usuarioId}/modulos?tenantId=` | 🛡️ (acima) | `{ modulos: [...] }` — códigos associados ao usuário |
| `PUT` | `/usuarios/{usuarioId}/modulos` | 🛡️ (acima) | Corpo `{ tenantId, codigos: [...] }` — substitui a associação **e** sincroniza os flags legados (`acessoEstoque`, `acessoPatrimonio`, `acessoFrota`, `acessoProtocolo`) |

---

## 6. Ponto Eletrônico

| Método | Rota | Acesso | Descrição |
|---|---|---|---|
| `POST` | `/pontos/sincronizar` | 🛡️ (`COLABORADOR`, `ADMIN_EMPRESA`, `GESTOR_RH`) | Batida(s) online/offline com GPS e hash |
| `GET` | `/pontos/espelho?mes=9&ano=2026` | 🛡️ (mesmos perfis) | Espelho de ponto mensal |
| `GET` | `/pontos/espelho/relatorio?colaboradorId=...&mes=9&ano=2026` | 🛡️ (mesmos perfis) | Relatório do espelho conforme art. 84 da Portaria MTP 671/2021: empregador (nome/CNPJ), trabalhador (nome, CPF, admissão, cargo/função, matrícula), data de emissão, período apurado, jornada contratual, marcações tratadas e **código de verificação** SHA-256 |
| `POST` | `/pontos/ajustar` | 🛡️ (`ADMIN_EMPRESA`, `GESTOR_RH`) | Ajuste manual com justificativa obrigatória |
| `POST` | `/pontos/ajustar/solicitar` | 🛡️ (`COLABORADOR`, `ADMIN_EMPRESA`, `GESTOR_RH`) | Colaborador solicita ajuste (vai para fila de aprovação) |
| `GET` | `/pontos/ajustes/pendentes` | 🛡️ (`ADMIN_EMPRESA`, `GESTOR_RH`) | Fila de aprovação de ajustes |
| `PUT` | `/pontos/ajustes/{id}/aprovar` | 🛡️ (`ADMIN_EMPRESA`, `GESTOR_RH`) | Aprova ajuste pendente |
| `PUT` | `/pontos/ajustes/{id}/rejeitar` | 🛡️ (`ADMIN_EMPRESA`, `GESTOR_RH`) | Rejeita ajuste pendente |

---

## 7. Fiscal

| Método | Rota | Acesso | Descrição |
|---|---|---|---|
| `GET` | `/fiscal/afd/download?cnpj=...&inicio=...&fim=...&numeroRegistroInpi=...&cnpjDesenvolvedor=...` | 🛡️ (`ADMIN_EMPRESA`, `GESTOR_RH`, `ADMIN_PLATAFORMA`) | Download do AFD (Anexo V, REP-P): hash SHA-256 encadeado, NSR reenumerado 1..N por CNPJ e campo `assinDigital` (`ASSINATURA_DIGITAL_EM_ARQUIVO_P7S`) na última linha. `numeroRegistroInpi`, `cno` e `cnpjDesenvolvedor` (opcionais) também vêm das **settings do tenant** (`/fiscal/configuracao`) |
| `GET` | `/fiscal/afd/assinatura?...` | 🛡️ (mesmos perfis) | Assinatura **CAdES destacada** `.p7s` do AFD (`application/pkcs7-signature`); `503` sem `FISCAL_PFX_BASE64`/`FISCAL_PFX_SENHA` |
| `GET` | `/fiscal/aej/download?cnpj=...&razaoSocial=...&numeroRegistroInpi=...&horarioContratual=...&cnpjDesenvolvedor=...` | 🛡️ (`ADMIN_EMPRESA`, `GESTOR_RH`, `ADMIN_PLATAFORMA`) | Download do AEJ (Anexo VI, MTP 671/2021). `numeroRegistroInpi` emite o registro `02` (REP-P); `horarioContratual` (`HHmm-HHmm[;...]`) emite o `04`; `codHorarioContratual` (default `1`) referencia o horário na 1ª entrada; `cnpjDesenvolvedor`/`prtpNome`/`prtpVersao`/`prtpRazaoDesenv`/`prtpEmail` alimentam o registro `08` (PTRP) — sempre com precedência **parâmetro explícito > settings do tenant > padrões** ("CHRONOS PULSE" 1.0.0) |
| `GET` | `/fiscal/aej/assinatura?...` | 🛡️ (mesmos perfis) | Assinatura **CAdES destacada** `.p7s` do AEJ (`application/pkcs7-signature`); `503` sem certificado configurado |
| `GET` | `/fiscal/configuracao` | 🛡️ (mesmos perfis) | Configuração de exportação fiscal do tenant (nº INPI, CNPJ desenvolvedor, PTRP, CNO); retorna os padrões se ainda não gravada |
| `PUT` | `/fiscal/configuracao` | 🛡️ (mesmos perfis) | Grava a configuração (JSON parcial: campos ausentes preservam valor atual) |

---

## 8. Estoque & Almoxarifado

| Método | Rota | Acesso | Descrição |
|---|---|---|---|
| `GET` | `/estoque/saldos` | 👤 com `ROLE_ESTOQUE` ou gestor | Saldos físicos/patrimoniais (PMP) |
| `POST` | `/estoque/movimentacoes/entrada` | 👤 com `ROLE_ESTOQUE` ou gestor | Entrada por NF-e/Empenho (recalcula PMP) |
| `POST` | `/estoque/movimentacoes/saida` | 👤 com `ROLE_ESTOQUE` ou gestor | Saída/baixa com validação de saldo |
| `GET` | `/estoque/requisicoes` | 👤 com `ROLE_ESTOQUE` ou gestor | Requisições (paginada, filtrável) |
| `POST` | `/estoque/requisicoes` | 👤 com `ROLE_ESTOQUE` ou gestor | Cria requisição |
| `POST` | `/estoque/requisicoes/{id}/aprovar` | 👤 com `ROLE_ESTOQUE` ou gestor | Aprova requisição |
| `POST` | `/estoque/requisicoes/{id}/atender` | 👤 com `ROLE_ESTOQUE` ou gestor | Atende (baixa em estoque) |

> **Acesso ao estoque:** colaboradores com `acessoEstoque=true` recebem a authority `ROLE_ESTOQUE` no token (`JwtAuthFilter`). Gestores (`ADMIN_EMPRESA`, `GESTOR_RH`, `ADMIN_PLATAFORMA`) acessam diretamente.

---

## 9. Patrimônio Público `@RequiresModulo("PATRIMONIO")`

| Método | Rota | Acesso | Descrição |
|---|---|---|---|
| `GET` | `/patrimonio` | 🛡️ (`ADMIN_PLATAFORMA`, `ADMIN_EMPRESA`, `COLABORADOR`) | Lista paginada |
| `POST` | `/patrimonio` | 🛡️ (`ADMIN_PLATAFORMA`, `ADMIN_EMPRESA`) | Cadastra bem |
| `GET` | `/patrimonio/{id}` | 🛡️ (mesmos perfis de leitura) | Busca por id |
| `GET` | `/patrimonio/ativos` | 🛡️ (leitura) | Lista bens ativos (sem paginação) |

`POST /patrimonio` — corpo:

```jsonc
{
  "tombamento": "TOM-0004",
  "descricao": "Mesa de escritório em madeira",
  "categoria": "MOBILIARIO",
  "estado": "BOM",              // NOVO | OTIMO | BOM | REGULAR | INSERVIVEL
  "localizacao": "Secretaria de Obras",
  "dataAquisicao": "2024-03-15", // ISO LocalDate
  "valorAquisicao": 850.00,      // número (BigDecimal)
  "responsavelNome": "Maria Silva",
  "numeroNotaFiscal": "NF 1200",
  "observacoes": "Adquirido em pregão"
}
```

---

## 10. Gestão de Frota `@RequiresModulo("FROTA")`

| Método | Rota | Acesso | Descrição |
|---|---|---|---|
| `GET` | `/frota/veiculos` | 🛡️ (`ADMIN_PLATAFORMA`, `ADMIN_EMPRESA`, `COLABORADOR`) | Lista paginada de veículos |
| `POST` | `/frota/veiculos` | 🛡️ (`ADMIN_PLATAFORMA`, `ADMIN_EMPRESA`) | Cadastra veículo |
| `GET` | `/frota/veiculos/{id}` | 🛡️ (leitura) | Detalhe do veículo |
| `GET` | `/frota/abastecimentos` | 🛡️ (leitura) | Lista paginada de abastecimentos |
| `POST` | `/frota/abastecimentos` | 🛡️ (`ADMIN_PLATAFORMA`, `ADMIN_EMPRESA`) | Registra abastecimento |

`POST /frota/veiculos` — corpo:

```jsonc
{
  "placa": "ABC-1D23", "renavam": "12345678901",
  "marca": "Fiat", "modelo": "Palio Adventure",
  "anoFabricacao": 2022, "anoModelo": 2023,
  "tipo": "UTILITARIO", "combustivel": "FLEX",
  "status": "ATIVO",          // ATIVO | MANUTENCAO | INATIVO
  "odometroAtual": 45210.5,   // número
  "observacoes": null
}
```

`POST /frota/abastecimentos` — corpo:

```jsonc
{
  "veiculoId": "99999999-9999-4999-9999-999999999991",
  "litros": 42.5,                    // número (obrigatório)
  "valorLitro": 6.149,               // número (obrigatório)
  "odometroKm": 45110.0,             // número
  "posto": "Posto Central",
  "observacoes": null
}
```

O backend calcula `valorTotal` automaticamente (`litros × valorLitro`).

---

## 11. Protocolo Eletrônico `@RequiresModulo("PROTOCOLO")`

| Método | Rota | Acesso | Descrição |
|---|---|---|---|
| `GET` | `/protocolo` | 🛡️ (`ADMIN_PLATAFORMA`, `ADMIN_EMPRESA`, `COLABORADOR`) | Lista paginada |
| `POST` | `/protocolo` | 🛡️ (`ADMIN_PLATAFORMA`, `ADMIN_EMPRESA`) | Cadastra protocolo |
| `GET` | `/protocolo/{id}` | 🛡️ (leitura) | Detalhe |
| `PATCH` | `/protocolo/{id}/status` | 🛡️ (`ADMIN_PLATAFORMA`, `ADMIN_EMPRESA`) | Altera status |

`POST /protocolo` — corpo (`numeroProtocolo`, `tipo` e `assunto` obrigatórios):

```jsonc
{
  "numeroProtocolo": "PROTO-2026-000003",
  "tipo": "OFICIO",
  "assunto": "Solicitação de manutenção da frota",
  "descricao": "Ofício nº 013/2026 ...",
  "remetente": "Secretaria de Obras",
  "destinatario": "Departamento de Compras",
  "status": "RECEBIDO",       // opcional; default RECEBIDO
  "responsavel": "Maria Silva",
  "observacoes": null
}
```

`PATCH /protocolo/{id}/status` — corpo:

```jsonc
{ "status": "EM_TRAMITACAO", "responsavel": "Maria Silva", "observacoes": "..." }
```

**Status válidos:** `RECEBIDO`, `TRIAGEM`, `EM_TRAMITACAO`, `ARQUIVADO`, `CANCELADO`.

---

## 12. Compras & Fornecedores `@RequiresModulo("COMPRAS")`

### Fornecedores e pedidos

| Método | Rota | Acesso | Descrição |
|---|---|---|---|
| `GET` | `/compras/fornecedores` | 🛡️ (`ADMIN_PLATAFORMA`, `ADMIN_EMPRESA`, `GESTOR_RH`, `ESTOQUE`) | Lista fornecedores |
| `GET` | `/compras/fornecedores/{id}` | 🛡️ (mesmos perfis) | Busca fornecedor |
| `POST` | `/compras/fornecedores` | 🛡️ (`ADMIN_PLATAFORMA`, `ADMIN_EMPRESA`) | Cadastra fornecedor |
| `PUT` | `/compras/fornecedores/{id}` | 🛡️ (`ADMIN_PLATAFORMA`, `ADMIN_EMPRESA`) | Atualiza fornecedor |
| `DELETE` | `/compras/fornecedores/{id}` | 🛡️ (`ADMIN_PLATAFORMA`, `ADMIN_EMPRESA`) | Inativa fornecedor |
| `GET` | `/compras/pedidos` | 🛡️ (`ADMIN_PLATAFORMA`, `ADMIN_EMPRESA`, `GESTOR_RH`, `ESTOQUE`) | Lista pedidos de compra |
| `GET` | `/compras/pedidos/{id}` | 🛡️ (mesmos perfis) | Busca pedido |
| `POST` | `/compras/pedidos` | 🛡️ (mesmos perfis) | Cria pedido de compra |
| `POST` | `/compras/pedidos/{id}/cancelar` | 🛡️ (mesmos perfis) | Cancela pedido |

### Recebimento por NFe

| Método | Rota | Acesso | Descrição |
|---|---|---|---|
| `POST` | `/compras/nfe/receber` | 🛡️ (`ADMIN_PLATAFORMA`, `ADMIN_EMPRESA`, `ESTOQUE`) | Recebe NFe vinculada ao pedido (gera entrada) |
| `GET` | `/compras/nfe` | 🛡️ (`ADMIN_PLATAFORMA`, `ADMIN_EMPRESA`, `GESTOR_RH`, `ESTOQUE`) | Lista entradas por NFe |
| `POST` | `/compras/nfe/importar-xml` | 🛡️ (`ADMIN_PLATAFORMA`, `ADMIN_EMPRESA`, `ESTOQUE`) | Importa XML da NFe (≤ 5 MB, `multipart/form-data` — campo `arquivo`) |
| `POST` | `/compras/nfe/consultar-sefaz` | 🛡️ (`ADMIN_PLATAFORMA`, `ADMIN_EMPRESA`, `ESTOQUE`) | Consulta NFe pela chave na SEFAZ (depende de `app.compras.sefaz.consulta-enabled`) |

### Banco de preços

| Método | Rota | Acesso | Descrição |
|---|---|---|---|
| `GET` | `/compras/precos` | 🛡️ (`ADMIN_PLATAFORMA`, `ADMIN_EMPRESA`, `GESTOR_RH`, `ESTOQUE`) | Banco de preços para consulta |

### Requisições e cotações

| Método | Rota | Acesso | Descrição |
|---|---|---|---|
| `GET` | `/compras/requisicoes` | 🛡️ (grupo compras) | Lista requisições |
| `GET` | `/compras/requisicoes/{id}` | 🛡️ (grupo compras) | Busca requisição |
| `POST` | `/compras/requisicoes` | 🛡️ (grupo compras) | Cria requisição |
| `POST` | `/compras/requisicoes/{id}/cancelar` | 🛡️ (gerência) | Cancela requisição |
| `GET` | `/compras/cotacoes` | 🛡️ (grupo compras) | Lista cotações |
| `GET` | `/compras/cotacoes/{id}` | 🛡️ (grupo compras) | Busca cotação |
| `POST` | `/compras/cotacoes` | 🛡️ (gerência) | Cria cotação |
| `PUT` | `/compras/cotacoes/{id}/propostas` | 🛡️ (gerência) | Registra propostas de fornecedor |
| `POST` | `/compras/cotacoes/{id}/concluir` | 🛡️ (gerência) | Conclui cotação (vencedores por item) |
| `POST` | `/compras/cotacoes/{id}/cancelar` | 🛡️ (gerência) | Cancela cotação |
| `POST` | `/compras/cotacoes/{id}/gerar-pedidos` | 🛡️ (gerência) | Gera pedidos a partir da cotação |

**Grupo compras:** `ADMIN_PLATAFORMA`, `ADMIN_EMPRESA`, `GESTOR_RH`, `ESTOQUE`. **Gerência:** `ADMIN_PLATAFORMA`, `ADMIN_EMPRESA`, `GESTOR_RH`.

`POST /compras/cotacoes` — corpo:

```jsonc
{
  "requisicaoId": "11111111-1111-4111-8111-111111111111",
  "fornecedores": ["22222222-2222-4222-8222-222222222222"],
  "prazoRespostaDias": 7,
  "observacoes": null
}
```

---

## 13. Licitações & Contratações `@RequiresModulo("LICITACOES")` (Lei 14.133/2021)

| Método | Rota | Acesso | Descrição |
|---|---|---|---|
| `GET` | `/licitacoes` | 🛡️ (grupo licitações) | Lista licitações |
| `GET` | `/licitacoes/{id}` | 🛡️ (grupo licitações) | Busca licitação |
| `POST` | `/licitacoes` | 🛡️ (gerência) | Cadastra licitação |
| `POST` | `/licitacoes/{id}/publicar` | 🛡️ (gerência) | Publica a licitação (abre prazos) |
| `POST` | `/licitacoes/{id}/publicar-pncp` | 🛡️ (gerência) | Publica aviso no PNCP |
| `PUT` | `/licitacoes/{id}/propostas` | 🛡️ (gerência) | Registra propostas de fornecedor |
| `POST` | `/licitacoes/{id}/abrir-disputa` | 🛡️ (gerência) | Abre disputa eletrônica |
| `POST` | `/licitacoes/{id}/lances` | 🛡️ (gerência) | Registra lance (disputa) |
| `GET` | `/licitacoes/{id}/lances` | 🛡️ (grupo licitações) | Lista lances |
| `POST` | `/licitacoes/{id}/adjudicar` | 🛡️ (gerência) | Adjudica (menor preço ou disputa) |
| `POST` | `/licitacoes/{id}/homologar` | 🛡️ (gerência) | Homologa a licitação |
| `POST` | `/licitacoes/{id}/cancelar` | 🛡️ (gerência) | Cancela a licitação |
| `POST` | `/licitacoes/{id}/gerar-pedidos` | 🛡️ (gerência) | Gera pedidos de compra |
| `POST` | `/licitacoes/{id}/contrato` | 🛡️ (gerência) | Formaliza contrato da licitação |

### Planejamento (ETP / TR / Edital)

| Método | Rota | Acesso | Descrição |
|---|---|---|---|
| `GET` | `/licitacoes/{licitacaoId}/planejamento` | 🛡️ (grupo licitações) | Busca planejamento da licitação |
| `PUT` | `/licitacoes/{licitacaoId}/planejamento/etp` | 🛡️ (gerência) | Salva ETP |
| `POST` | `/licitacoes/{licitacaoId}/planejamento/etp/aprovar` | 🛡️ (gerência) | Aprova ETP |
| `PUT` | `/licitacoes/{licitacaoId}/planejamento/tr` | 🛡️ (gerência) | Salva Termo de Referência |
| `POST` | `/licitacoes/{licitacaoId}/planejamento/tr/aprovar` | 🛡️ (gerência) | Aprova TR |
| `PUT` | `/licitacoes/{licitacaoId}/planejamento/edital` | 🛡️ (gerência) | Salva edital |
| `POST` | `/licitacoes/{licitacaoId}/planejamento/edital/publicar` | 🛡️ (gerência) | Publica edital |

**Grupo licitações:** `ADMIN_PLATAFORMA`, `ADMIN_EMPRESA`, `GESTOR_RH`, `ESTOQUE`. **Gerência:** `ADMIN_PLATAFORMA`, `ADMIN_EMPRESA`, `GESTOR_RH`.

`POST /licitacoes/{id}/contrato` — corpo:

```jsonc
{
  "numero": "CT-2026-0001",
  "dataAssinatura": "2026-09-30",
  "valor": 53850.00,
  "vigenciaInicio": "2026-10-01",
  "vigenciaFim": "2027-10-01"
}
```

### Execução Contratual `@RequiresModulo("LICITACOES")`

| Método | Rota | Acesso | Descrição |
|---|---|---|---|
| `GET` | `/contratos` | 🛡️ (grupo licitações) | Lista contratos em execução do tenant |
| `GET` | `/contratos/{id}` | 🛡️ (grupo licitações) | Busca execução do contrato (situação, aditivos, apontamentos, medições, sanções, rescisão) |
| `POST` | `/contratos/{id}/aditivos` | 🛡️ (gerência) | Registra termo aditivo (VALOR amplia `valorTotal`; PRAZO prorroga `vigenciaFim`) |
| `POST` | `/contratos/{id}/apontamentos` | 🛡️ (gerência) | Registra apontamento de fiscalização |
| `POST` | `/contratos/{id}/apontamentos/{apontamentoId}/resolver` | 🛡️ (gerência) | Resolve apontamento |
| `POST` | `/contratos/{id}/medicoes` | 🛡️ (gerência) | Registra medição/pagamento (credita `valorLiquidado`) |
| `POST` | `/contratos/{id}/sancoes` | 🛡️ (gerência) | Aplica sanção administrativa |
| `POST` | `/contratos/{id}/rescindir` | 🛡️ (gerência) | Rescinde o contrato (status `RESCINDIDO`) |

Situação computada: `RESCINDIDO` > `VENCIDO` (vigência expirada sem aditivo) > `EXPIRANDO` (dentro da janela de aviso de vencimento) > `VIGENTE`.

**Grupo licitações:** `ADMIN_PLATAFORMA`, `ADMIN_EMPRESA`, `GESTOR_RH`, `ESTOQUE`. **Gerência:** `ADMIN_PLATAFORMA`, `ADMIN_EMPRESA`, `GESTOR_RH`.

---

## 14. Portal da Transparência `@RequiresModulo("TRANSPARENCIA")` (LC 131/2009)

| Método | Rota | Acesso | Descrição |
|---|---|---|---|
| `GET` | `/transparencia/resumo` | 🛡️ (leitura) | Resumo do portal por tenant |
| `GET` | `/transparencia/despesas-mensais?ano=2026` | 🛡️ (leitura) | Despesas mensais (default: `2026`) |
| `GET` | `/transparencia/publicacoes` | 🛡️ (leitura) | Lista publicações |
| `POST` | `/transparencia/publicacoes` | 🛡️ (gerência) | Cria publicação (rascunho) |
| `POST` | `/transparencia/publicacoes/{id}/publicar` | 🛡️ (gerência) | Divulga no portal |
| `DELETE` | `/transparencia/publicacoes/{id}` | 🛡️ (gerência) | Remove publicação em elaboração |

**Leitura:** `ADMIN_PLATAFORMA`, `ADMIN_EMPRESA`, `GESTOR_RH`, `ESTOQUE`, `COLABORADOR`. **Gerência:** `ADMIN_PLATAFORMA`, `ADMIN_EMPRESA`, `GESTOR_RH`.

`POST /transparencia/publicacoes` — corpo (resumo):

```jsonc
{
  "competencia": "2026-09",
  "tipoPublicacao": "DESPESAS",   // RECEITAS | DESPESAS | COMPRAS | LICITACOES | CONTRATOS | FROTA | PATRIMONIO | FOLHA
  "descricao": "Despesas de setembro/2026",
  "itens": [ { "descricao": "Material de escritório", "valor": 1500.00 } ]
}
```

---

## 14.1 Portal Público da Transparência (R31) — sem autenticação

Rotas públicas por `slug` do órgão (ex.: `demonstracao`, `lj-code`). Requisitos: empresa ativa + módulo `TRANSPARENCIA` ativo. Fora disso → `404`.

| Método | Rota | Acesso | Descrição |
|---|---|---|---|
| `GET` | `/publico/transparencia/{slug}` | 🌐 pública | Resumo do órgão (licitações, contratos, despesas do ano, publicações) |
| `GET` | `/publico/transparencia/{slug}/licitacoes` | 🌐 pública | Lista licitações com situação pública |
| `GET` | `/publico/transparencia/{slug}/licitacoes/{id}` | 🌐 pública | Detalhe com itens |
| `GET` | `/publico/transparencia/{slug}/contratos` | 🌐 pública | Lista contratos |
| `GET` | `/publico/transparencia/{slug}/contratos/{id}` | 🌐 pública | Detalhe com aditivos e sanções |
| `GET` | `/publico/transparencia/{slug}/despesas-mensais?ano=2026` | 🌐 pública | Despesas mensais do ano |
| `GET` | `/publico/transparencia/{slug}/publicacoes` | 🌐 pública | Publicações divulgadas |

**Situações públicas de licitação:** `PUBLICADA`, `ABERTA`, `ADJUDICADA`, `HOMOLOGADA`. Rascunhos, canceladas e encerradas não aparecem.

Resposta do resumo (trecho):

```jsonc
{
  "orgao": { "slug": "demonstracao", "nome": "Demonstração", "cnpj": "01001001000101" },
  "licitacoesPublicadas": 4,
  "licitacoesEmAndamento": 2,
  "licitacoesHomologadas": 1,
  "contratosAtivos": 3,
  "valorEmpenhado": 285000.00,
  "valorLiquidado": 210000.00,
  "valorDespesasAno": 96000.00,
  "publicacoesDivulgadas": 3,
  "ultimaCompetencia": "2026-08"
}
```

---

## 15. Leads de Prospecção (R31.1) — onboarding comercial

Cadastro público em 3 etapas (Empresa → Endereço → Contato) **sem CPF e sem senha**: não cria conta, apenas registra um lead comercial (`tb_lead_empresa`) que a equipe retorna para agendar a conversa/contratação.

| Método | Rota | Acesso | Descrição |
|---|---|---|---|
| `POST` | `/leads/empresas` | 🌐 pública | Registra lead com dados da empresa, endereço e contato comercial (status inicial `NOVO`) |
| `GET` | `/leads` | 🔒 `ADMIN_PLATAFORMA`/`SUPORTE_N1`/`SUPORTE_N2` | Lista todos os leads, do mais recente ao mais antigo (funil de prospecção) |
| `PATCH` | `/leads/{id}/status` | 🔒 `ADMIN_PLATAFORMA`/`SUPORTE_N1`/`SUPORTE_N2` | Avança o funil do lead: `NOVO` → `AGENDADO` → `REUNIAO` → `CONTRATADO` (ou `DESCARTADO`) |

Body do `PATCH /leads/{id}/status`:

```json
{ "status": "REUNIAO" }
```

O `GET /leads` retorna um array de objetos (mesmo formato da resposta do `POST`, incluindo endereço completo e `observacao`), ordenado por `criadoEm` decrescente.

**Body (POST):**

```jsonc
{
  "cnpj": "11.222.333/0001-81",        // aceita máscara; normalizado para 14 dígitos
  "razaoSocial": "Empresa Exemplo LTDA",
  "contatoNome": "João Silva",
  "contatoEmail": "joao@empresa.com",
  "contatoTelefone": "1123456789",
  "contatoCelular": "11987654321",
  "enderecoLogradouro": "Rua A",
  "enderecoNumero": "100",
  "enderecoComplemento": "Sala 1",
  "enderecoBairro": "Centro",
  "enderecoCidade": "São Paulo",
  "enderecoUf": "SP",
  "enderecoCep": "01001000",
  "observacao": "Indicação via landing"
}
```

**Resposta `201`:**

```jsonc
{
  "id": "…uuid…",
  "cnpj": "11222333000181",
  "razaoSocial": "Empresa Exemplo LTDA",
  "contatoNome": "João Silva",
  "contatoEmail": "joao@empresa.com",
  "contatoTelefone": "1123456789",
  "contatoCelular": "11987654321",
  "enderecoCidade": "São Paulo",
  "enderecoUf": "SP",
  "status": "NOVO",
  "criadoEm": "2026-09-12T..."
}
```

Regras:
- `cnpj` deve conter **14 dígitos** (após remover máscara) → caso contrário `400` (`CNPJ inválido: deve conter 14 dígitos.`).
- `razaoSocial`, `contatoNome` e `contatoEmail` obrigatórios.
- `endereco*`, telefones e `observacao` opcionais.

---

## 16. Privacidade & LGPD `/api/v1/privacidade`

| Método | Rota | Acesso | Descrição |
|---|---|---|---|
| `GET` | `/privacidade/politica` | 👤 | Política/Termo vigente: `{ versao, dataPublicacao, texto, hashTermo }` (`hashTermo` = SHA-256 do texto exato) |
| `GET` | `/privacidade/consentimento/status` | 👤 | `{ versaoAtual, versaoAceita, dataConsentimento, aceitePendente }` — usado pelo `ConsentimentoGate` do app |
| `GET` | `/privacidade/meus-dados` | 👤 | Exportação LGPD (art. 18): dados do usuário, colaborador e histórico de consentimentos |
| `POST` | `/privacidade/consentimento` | 👤 | Registra o aceite do **Termo de Ciência**: corpo `{ versaoPolitica, aceito }` (`aceito` deve ser `true`; versão deve ser a vigente) → `201`. Captura IP (`X-Forwarded-For`/remote), `User-Agent` e grava `tenant_id` + `hashTermo` no registro e na auditoria (`CONSENTIMENTO_PRIVACIDADE`). **Idempotente:** reenvio do mesmo usuário/versão não duplica. No 1º aceite de `ADMIN_EMPRESA`, associa todos os módulos contratados ao usuário. |
| `DELETE` | `/privacidade/meus-dados` | 👤 | Anonimização LGPD (art. 18, VI) → `204` + auditoria |

> **Escopo do gate:** apenas usuários do painel (`cpc_usuario`). O modal do app (`ConsentimentoGate`) só fecha pelo aceite ou por "Sair" (logout); falha de rede em `GET .../status` **não** bloqueia o acesso (fail-open silencioso). Base legal da marcação de ponto é obrigação legal (CLT / Portaria MTP 671/2021) — o aceite é ciência, não consentimento revogável.

---

## Paginação

As listagens retornam `org.springframework.data.domain.Page`:

```jsonc
{
  "content": [ ... ],
  "totalElements": 3,
  "totalPages": 1,
  "number": 0,
  "size": 20,
  ...
}
```

Parâmetros suportados: `page`, `size`, `sort` (ex.: `?page=0&size=20&sort=estado,asc`). Default: `size=20`.

---

## Erros

- `400` — validação de corpo/DTO (mensagens em PT-BR)
- `401` — token ausente/expirado/inválido
- `403` — perfil sem permissão **ou** módulo não contratado para a empresa
- `404` — recurso inexistente
- `500` — erro interno