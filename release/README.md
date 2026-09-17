# Chronos Pulse — pacote de instalação on-prem (local + central)

Kit de instalação do servidor **Chronos Pulse** (Controle Eletrônico de Ponto —
REP-P, Portaria MTP 671/2021) em rede local:

- **Windows 10/11** (64 bits)
- **Linux** (64 bits, distribuições com systemd + Docker)

Amplia o deploy centralizado (Docker/Kubernetes) com um modo **local**: o mesmo
produto roda na máquina da empresa e os terminais (app mobile) apontam para ela.

## Conteúdo do pacote

| Arquivo | Função |
|---|---|
| `chronos-pulse.jar` | Servidor (gerado no build; veja `build-release.ps1`) |
| `docker-compose.yml` | Banco **PostgreSQL 16** oficial (o app NUNCA guarda dados fora dele) |
| `start.bat` | Lançador **Windows** (só requer Docker + Java 25) |
| `start.sh` | Lançador **Linux** |
| `stop.bat` / `stop.sh` | Para o banco local (preservando os dados) |
| `chronos-pulse.service` | Unidade `systemd` de exemplo |
| `install-linux.sh` | Instala como serviço no Linux (`/opt/chronos-pulse`) |
| `.env.example` | Modelo de configuração (copie para `.env`) |
| `README.md` | Este manual |

> **Por que Postgres e não banco embutido?** As migrações do produto usam
> recursos específicos do PostgreSQL (`ON CONFLICT`, `gen_random_uuid`, ...).
> Manter o Postgres via Docker preserva a fidelidade do schema, a integridade e
> um único conjunto de migrações — sem passar a reescrevê-las para cada banco.

## Requisitos

- **Docker** rodando:
  - Windows 10/11: **Docker Desktop** (habilita WSL2 na instalação).
  - Linux: `docker` + `compose` (plugin v2) com o serviço iniciado.
- **Java 25** (recomendado: Temurin/Adoptium), no `PATH`:
  - https://adoptium.net → baixe o instalador do Java 25 (x64).
  - Debian/Ubuntu: `sudo apt install openjdk-25-jre-headless`.
- 1 GB RAM livre, ~1 GB em disco para o Postgres.

> Suportado em **Windows 10/11** e **Linux** (64 bits). Não use em sistemas
> legados sem suporte a Java 25.

## Primeiro uso (rápido)

**Windows** — dê dois cliques em `start.bat` (ou rode pelo terminal). Na
primeira execução ele:

1. cria o `.env` (segredo JWT + credenciais do Postgres geradas);
2. sobe o Postgres com `docker compose up -d` e aguarda o `pg_isready`;
3. inicia o servidor em `http://localhost:3030`.

**Linux** — na pasta do pacote:

```bash
chmod +x start.sh stop.sh
./start.sh
```

Saúde/status: `http://localhost:3030/actuator/health` (deve retornar
`{"status":"UP"}`). Na primeira subida, o Flyway cria/migra o schema (V1…V38).

## Configuração (.env)

Copie `.env.example` para `.env` e ajuste (os lançadores também geram um `.env`
mínimo). Variáveis principais:

| Variável | Obrigatória | Descrição |
|---|---|---|
| `JWT_SECRET` | **sim** | Segredo de assinatura dos tokens (mín. 32 bytes) |
| `POSTGRES_USER` / `POSTGRES_PASSWORD` / `POSTGRES_DB` | sim | Credenciais do banco local |
| `POSTGRES_PORT` | não | Porta do Postgres no host (padrão `5432`) |
| `PORT` | não | Porta HTTP do app (padrão `3030`) |
| `CHRONOS_MAIL_ENABLED` | não | Liga envio de e-mails (padrão `false` no on-prem) |
| `MANAGEMENT_HEALTH_MAIL_ENABLED` | não | `false` | Mantém `/actuator/health` "UP" sem SMTP; reative se usar e-mail real |
| `CHRONOS_ALLOWED_ORIGINS` | não | CORS (origens separadas por vírgula) |
| `FISCAL_PFX_BASE64` / `FISCAL_PFX_SENHA` | não | Certificado p/ assinatura CAdES (`.p7s`) do AFD/AEJ |
| `TELEMETRIA_ENABLED` | não | Logs de login/telemetria (padrão `true`) |

> `POSTGRES_PORT` pode precisar mudar se já houver outro Postgres na porta 5432.

## Como serviço

**Linux (systemd):**

```bash
sudo ./install-linux.sh         # depois de gerar o pacote (README abaixo)
```

Instala em `/opt/chronos-pulse`, cria o usuário `chronos` (grupo `docker`),
gera o `.env`, habilita e inicia `chronos-pulse`. Logs:
`journalctl -u chronos-pulse -f`. O serviço sobe o Postgres via
`docker compose up -d` no `ExecStartPre`. Edite `/opt/chronos-pulse/.env` e
reinicie: `sudo systemctl restart chronos-pulse`.

**Windows:** duas opções —

- **NSSM** (`nssm install ChronosPulse` → `java.exe` com
  `-jar C:\...\chronos-pulse.jar`, `WorkingDirectory` na pasta do kit); ou
- **Agendador de Tarefas** executando `start.bat` (a pasta de trabalho = kit).

Em ambos, garanta que o Docker Desktop inicie junto com o Windows.

## Parar o servidor local

- `stop.bat` / `stop.sh` — para o Postgres **preservando os dados** (volume).
- Para apagar **todos** os dados: `docker compose down -v` (só se for a última opção).

## Atualização

1. Pare o serviço/janela (`sudo systemctl stop chronos-pulse` ou Ctrl+C).
2. Substitua o `chronos-pulse.jar` (mantenha `.env`; o volume do Postgres fica
   intacto).
3. `docker compose up -d postgres` (puxa a imagem, se houver nova) e suba o app.
   As migrações Flyway rodam automaticamente.

## Backup

O banco vive no volume Docker `chronos_pgdata`. Faça `pg_dump`:

```bash
docker compose exec postgres pg_dump -U chronos_user -d chronos_db -Fc -f /tmp/db.dump
```

e copie o arquivo para fora do container
(`docker compose cp postgres:/tmp/db.dump ./backup-<data>.dump`). Agende isso
diariamente (ou use um volume/`bind mount` em disco sincronizado).

## Segurança

- **Não exponha a porta `3030` na internet sem um *reverse proxy* com TLS
  (Caddy/nginx); em LAN, mantenha firewall liberando apenas a rede interna.
- Mantenha `JWT_SECRET` e `POSTGRES_PASSWORD` fortes e privados (o `.env` não é
  versionado).
- `.env` e backups não devem sair da rede da organização sem necessidade.

## Deploy centralizado

Para multi-local com servidor central, o produto também roda via Docker
(`docker-compose.yml` na raiz do repositório) e há o histórico em `main`
(Kubernetes planejado). O app (Flutter) aponta para a URL do servidor na tela
de login — os terminais usam o mesmo apk.

## Gerar o pacote

Na raiz do repositório:

```powershell
pwsh .\release\build-release.ps1 -SkipTests
```

Gera `release\chronos-pulse-<versão>-onprem.zip` com jar + docker-compose +
lançadores + manuais (roda `mvn package`; omita `-SkipTests` para rodar os
testes).