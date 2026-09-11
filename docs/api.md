# Referência da API

Base: `http://localhost:8080/api/v1` · Formato: JSON · Autenticação: `Authorization: Bearer <accessToken>` (exceto rotas marcadas como públicas).

| Notação | Significado |
|---|---|
| 🔓 | Pública |
| 👤 | Qualquer usuário autenticado (tenant) |
| 🛡️ | Restrito por `@PreAuthorize` (perfis entre parênteses) |
| 🌐 | Perfis de plataforma (`ADMIN_PLATAFORMA`, `SUPORTE_N1`, `SUPORTE_N2`) |

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

## 2. Admin Plataforma (Módulos)

| Método | Rota | Acesso | Descrição |
|---|---|---|---|
| `GET` | `/admin/modulos` | 🌐 | Catálogo de módulos disponíveis |
| `GET` | `/admin/empresas/{tenantId}/modulos` | 🌐 | Módulos ativos da empresa |
| `PUT` | `/admin/empresas/{tenantId}/modulos` | 🌐 | Substitui os módulos da empresa |

`PUT` — corpo: `{ "modulos": ["PONTO", "PROTOCOLO"] }`.

---

## 3. Empresas (Tenants)

| Método | Rota | Acesso | Descrição |
|---|---|---|---|
| `POST` | `/empresas` | 🛡️ (`ADMIN_PLATAFORMA`) | Cadastra uma empresa |

---

## 4. Colaboradores

| Método | Rota | Acesso | Descrição |
|---|---|---|---|
| `POST` | `/colaboradores` | 🛡️ (`ADMIN_EMPRESA`, `GESTOR_RH`, `ADMIN_PLATAFORMA`) | Cadastra colaborador |
| `GET` | `/colaboradores` | 🛡️ (mesmos perfis) | Lista do tenant |
| `PUT` | `/colaboradores/{id}` | 🛡️ (mesmos perfis) | Atualiza dados/acesso ao estoque |
| `DELETE` | `/colaboradores/{id}` | 🛡️ (mesmos perfis) | Soft delete |

---

## 5. Ponto Eletrônico

| Método | Rota | Acesso | Descrição |
|---|---|---|---|
| `POST` | `/pontos/sincronizar` | 🛡️ (`COLABORADOR`, `ADMIN_EMPRESA`, `GESTOR_RH`, `ADMIN_PLATAFORMA`) | Batida(s) online/offline com GPS e hash |
| `GET` | `/pontos/espelho?mes=9&ano=2026` | 🛡️ (mesmos perfis) | Espelho de ponto mensal |
| `POST` | `/pontos/ajustar` | 🛡️ (mesmos perfis) | Ajuste manual com justificativa obrigatória |

---

## 6. Fiscal

| Método | Rota | Acesso | Descrição |
|---|---|---|---|
| `GET` | `/fiscal/aej/download?cnpj=...&razaoSocial=...` | 🛡️ (`ADMIN_EMPRESA`, `GESTOR_RH`, `ADMIN_PLATAFORMA`) | Download do arquivo AEJ (Portaria MTP 671/2021) |

---

## 7. Estoque & Almoxarifado

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

## 8. Patrimônio Público `@RequiresModulo("PATRIMONIO")`

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

## 9. Gestão de Frota `@RequiresModulo("FROTA")`

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

## 10. Protocolo Eletrônico `@RequiresModulo("PROTOCOLO")`

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

## 11. Compras & Fornecedores `@RequiresModulo("COMPRAS")`

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

## 12. Licitações & Contratações `@RequiresModulo("LICITACOES")` (Lei 14.133/2021)

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

---

## 13. Portal da Transparência `@RequiresModulo("TRANSPARENCIA")` (LC 131/2009)

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