# Plataforma Modular (SaaS)

O Chronos Pulse é um SaaS com **módulos contratáveis por empresa (CNPJ/tenant)**. Cada empresa ativa somente os módulos que adquiriu; o servidor bloqueia o acesso aos demais por um interceptor dedicado.

---

## Modelo de Dados

### `modulo_plataforma` — Catálogo comercializável

| Coluna | Descrição |
|---|---|
| `id` | UUID |
| `codigo` | Identificador único do módulo (`PONTO`, `RECURSOS_HUMANOS`, ...) |
| `nome` | Nome comercial |
| `descricao` | Descrição |
| `ativo` | Se o módulo está disponível para contratação |

### `empresa_modulo` — Ativação por tenant

| Coluna | Descrição |
|---|---|
| `id` | UUID |
| `tenant_id` | FK → `tenant(id)` |
| `modulo_id` | FK → `modulo_plataforma(id)` |
| `ativado_em` | Data/hora da ativação |

Única por par `(tenant_id, modulo_id)`.

---

## Catálogo Atual (Seed `V11`)

| Código | Nome | Tipo |
|---|---|---|
| `PONTO` | Ponto Eletrônico | Core |
| `RECURSOS_HUMANOS` | Recursos Humanos | Core |
| `ESTOQUE` | Estoque & Almoxarifado | Core |
| `PATRIMONIO` | Patrimônio Público | Comercializável |
| `FROTA` | Gestão de Frota | Comercializável |
| `PROTOCOLO` | Protocolo & Tramitação | Comercializável |

**Regras de ativação (V11 e cadastro de empresa):**

1. Toda empresa **já existente** recebe automaticamente o trio core (`PONTO`, `RECURSOS_HUMANOS`, `ESTOQUE`).
2. O tenant de demonstração `a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11` recebe também `PATRIMONIO`, `FROTA` e `PROTOCOLO`.
3. Toda **nova empresa** criada via `POST /api/v1/auth/cadastrar-empresa` (ou use case `CadastrarEmpresaCompletoUseCaseImpl`) tem os módulos core ativados automaticamente via `ModuloService.ativarModulosPadrao()`.

---

## Enforço no Servidor: `@RequiresModulo` + `ModuloInterceptor`

Toda rota de um módulo contratável é anotada com:

```java
@RequiresModulo(codigo = "PROTOCOLO")
public class ProtocoloController { ... }
```

O `ModuloInterceptor` (registrado no `WebMvcConfig`):

1. Lê a anotação na **classe** (via `AnnotationUtils.findAnnotation`) ou no **método** do handler.
2. Se não houver anotação, libera (`true`).
3. Se o principal **não** for `CpcUsuario`, ou o `tenantId` for nulo, libera.
4. Se o perfil for **plataforma** (`ADMIN_PLATAFORMA`, `SUPORTE_N1`, `SUPORTE_N2`), libera.
5. Caso contrário, consulta `ModulosPort.isAtivo(tenantId, codigo)`; se inativo → **403** `"O módulo X não está contratado para esta empresa."`

### Porta de saída `ModulosPort`

| Método | Uso |
|---|---|
| `listarCodigosAtivos(UUID tenantId)` | Módulos ativos da empresa (usado no login/perfil) |
| `isAtivo(UUID tenantId, String codigo)` | Interceptor e validações |
| `ativarModulosPadrao(UUID tenantId)` | Cadastro de nova empresa |

---

## Admin Plataforma — Endpoints

Perfis: `ADMIN_PLATAFORMA`, `SUPORTE_N1`, `SUPORTE_N2`.

| Método | Rota | Descrição |
|---|---|---|
| `GET` | `/api/v1/admin/modulos` | Lista o catálogo de módulos ativos |
| `GET` | `/api/v1/admin/empresas/{tenantId}/modulos` | Lista os códigos ativos da empresa |
| `PUT` | `/api/v1/admin/empresas/{tenantId}/modulos` | Substitui os módulos da empresa |

`PUT` — corpo:

```json
{ "modulos": ["PONTO", "RECURSOS_HUMANOS", "ESTOQUE", "PROTOCOLO"] }
```

O serviço valida duplicados (ignora), normaliza maiúsculas, rejeita código inexistente ou módulo inativo, remove as ativações antigas e reinsere as selecionadas. Resposta: lista de códigos ativos.

---

## Autenticação: módulos no payload de login

`List<String> modulos` é incluído em:

- `POST /api/v1/auth/login`
- `POST /api/v1/auth/cadastrar-empresa`
- `POST /api/v1/auth/refresh`
- `GET /api/v1/auth/me`

O app frontend (Flutter) armazena essa lista em sessão e usa para **esconder** ou **mostrar** itens de menu (`temModuloPonto`, `temModuloEstoque`, etc.). O enforço é duplo: UX no cliente + segurança no servidor (nunca confiar apenas no cliente).

---

## Como Criar um Novo Módulo

1. **Banco**: adicionar a migração `V12_...`:
   - linha no `INSERT` de `modulo_plataforma` (novo `codigo`),
   - `CREATE TABLE` da entidade com `tenant_id`,
   - (opcional) seeds de demonstração.
2. **Entidade/Repo/Service/Controller**: seguir o padrão de `patrimonio/` (entity + repository + service + `web/dto`).
3. **Contrato**: anotar o controller com `@RequiresModulo(codigo = "NOVO_MODULO")` e usar `@PreAuthorize` para os perfis com acesso.
4. **Rota no SecurityConfig**: adicionar `.requestMatchers("/api/v1/novo-modulo/**")` com os perfis.
5. **Default (opcional)**: incluir o código em `MODULOS_PADRAO_NOVA_EMPRESA` (`ModuloService`) caso seja core.
6. **Frontend**: criar a feature (datasource/models/repository/provider/screens), registrar o provider no `main.dart`, adicionar getter `temModuloNovo` em `UsuarioModel` e o item no menu com a condição — ver `docs/modulos.md` no app.