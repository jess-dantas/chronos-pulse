# Dados Iniciais, Migrations e Testes

## Seeds (migrations `V3`, `V4`, `V5` e `V11`)

### Usuários

| Usuário | Perfil | CPF | Senha | Acesso Estoque | Tenant |
|---|---|---|---|---|---|
| Admin Empresa | `ADMIN_EMPRESA` | `11111111111` | `admin123` | Sim (irrestrito) | Chronos Pulse Tech LTDA |
| Gestor de RH | `GESTOR_RH` | `22222222222` | `admin123` | Sim (irrestrito) | Chronos Pulse Tech LTDA |
| Colaborador Padrão | `COLABORADOR` | `12345678901` | `senha123` | Não (apenas ponto) | Chronos Pulse Tech LTDA |
| Colaborador Almoxarife | `COLABORADOR` | `98765432100` | `senha123` | Sim (ponto + estoque) | Chronos Pulse Tech LTDA |

> **Tenant de demonstração:** `a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11` — possui os **6 módulos** ativos.

### Dados de demonstração (V11)

- **Patrimônio**: 3 bens (notebook, impressora, veículo) — estados variados.
- **Frota**: 2 veículos (Palio Adventure, S10) + 3 abastecimentos.
- **Protocolo**: 2 protocolos (`PROTO-2026-000001` em tramitação, `PROTO-2026-000002` recebido).

## Migrations

Local: `src/main/resources/db/migration/`

| Migration | Conteúdo |
|---|---|
| `V1`–`V9` | Estrutura base: tenants, usuários, colaboradores, ponto, estoque, notificação |
| `V10` | Email admin, endereço, foto e recuperação de senha |
| `V11` | **Plataforma modular**: `modulo_plataforma`, `empresa_modulo`, tabelas `tb_patrimonio`, `tb_frota_veiculo`, `tb_frota_abastecimento`, `tb_protocolo` + seeds |

## Testes Automatizados

```bash
# Linux / macOS / WSL
./mvnw test

# Windows
.\mvnw.cmd test
```

**Total: 62 testes, 0 falhas.** Principais coberturas:

| Camada / Módulo | Testes | Objetivo |
|---|---|---|
| Smoke (`ChronosPulseApplicationTests`) | 1 | Carregamento do contexto |
| Auth (`AutenticarUsuarioUseCaseImplTest`) | 3 | JWT, claims e `modulos` no login |
| Colaborador (`Cadastrar` + `Listar`) | 4 | Cadastro e listagem (CPF, tenant, permissões) |
| Segurança/CORS | 3 | Regras de CORS e autenticação |
| Ponto (registro, hash, sincronização, espelho, ajuste, repository) | 17 | Ciclo ENTRADA→SAIDA, NSR, hash SHA-256, AEJ |
| Notificação (`EmailComprovantePontoServiceTest`) | 4 | Comprovante por e-mail e fallback |
| Fiscal (`GeradorArquivoAEJAdapterTest`) | 5 | Arquivo AEJ (Portaria 671/2021) |
| Estoque (PMP, movimentações, requisições, materiais) | 16 | Cálculo de custo médio, saldos, ciclo de requisições |

## Coleção Insomnia

`src/test/resources/collections/Insomnia.yaml` — pastas: Autenticação & Acesso, Empresas, Colaboradores, Gestão de Ponto, Fiscal & Auditoria, Estoque & Almoxarifado.