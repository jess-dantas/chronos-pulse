# Infraestrutura & Operação — Chronos Pulse (backend)

Guia de subida, configuração, testes e documentação interativa da API.
Para o que é o software, seus módulos e a stack, ver o [`README.md`](../README.md).

---

## Como subir a aplicação

### Pré-requisitos

- **Java 25** (JDK)
- **Maven** — usar o wrapper do projeto (`./mvnw`, `.\mvnw.cmd` no Windows); não precisa instalar Maven
- **PostgreSQL 16** — necessário apenas na subida **sem Docker**
- **Docker + Docker Compose** — necessário apenas na subida **com Docker**

Com o perfil `dev` (padrão), as variáveis já têm defaults que funcionam com o Postgres do `docker-compose`.
**`JWT_SECRET` é sempre obrigatória** (não tem default) — sem ela o Spring não sobe.

### Opção A — Sem Docker (Maven + PostgreSQL local)

```bash
# 1. Crie o banco e o usuário (um única vez)
psql -U postgres -c "CREATE USER chronos_user WITH PASSWORD 'chronos_pass';"
psql -U postgres -c "CREATE DATABASE chronos_db OWNER chronos_user;"

# 2. Defina o JWT (Linux/macOS)
export JWT_SECRET="um-valor-longo-e-aleatorio-troque-em-producao"
# Windows (PowerShell):
#   $env:JWT_SECRET="um-valor-longo-e-aleatorio-troque-em-producao"
#   $env:SPRING_DATASOURCE_URL="jdbc:postgresql://localhost:5432/chronos_db"

# 3. Sobe a API
./mvnw spring-boot:run        # Windows: .\mvnw.cmd spring-boot:run

# 4. Teste
curl http://localhost:8080/v3/api-docs
```

O Flyway roda automaticamente na subida (32 migrations) e cria os seeds de demonstração. A documentação interativa fica em `http://localhost:8080/swagger-ui.html`.

### Opção B — Com Docker (API + PostgreSQL containerizados)

```bash
# 1. Crie o .env a partir do exemplo
cp .env.example .env           # Windows (PowerShell): Copy-Item .env.example .env

# 2. Preencha a variável obrigatória (JWT_SECRET) no .env;
#    e-mail fica desativado se SPRING_MAIL_USERNAME/PASSWORD estiverem vazios

# 3. Suba tudo (compila a imagem + cria o Postgres)
docker compose up --build -d

# 4. Acompanhe a subida
docker compose logs -f app     # Ctrl+C encerra o log; o app continua no ar

# 5. Verifique a saúde do Postgres e da API
docker compose ps
curl http://localhost:8080/v3/api-docs
```

> Dica: depois da primeira compilação, use apenas `docker compose up -d` para subir mais rápido.
> Para derrubar: `docker compose down` (mantém o volume de dados). Para apagar também o BD: `docker compose down -v`.

### Variáveis de ambiente

| Variável | Obrigatória | Default | Descrição |
|---|---|---|---|
| `SPRING_DATASOURCE_URL` | não | `jdbc:postgresql://localhost:5432/chronos_db` | Conexão JDBC |
| `SPRING_DATASOURCE_USERNAME` | não | `chronos_user` | Usuário do banco |
| `SPRING_DATASOURCE_PASSWORD` | não | `chronos_pass` | Senha do banco |
| `JWT_SECRET` | **sim** | — | Segredo para assinar JWTs (alto, aleatório) |
| `CHRONOS_ALLOWED_ORIGINS` | não | `http://localhost:3000,http://localhost:5173,...` | Origens CORS permitidas |
| `SPRING_MAIL_HOST` / `PORT` | não | `smtp.gmail.com` / `587` | SMTP (usado só com e-mail habilitado) |
| `SPRING_MAIL_USERNAME` / `PASSWORD` | não | vazio | Credenciais SMTP |
| `CHRONOS_MAIL_ENABLED` | não | `false` | Liga/desliga envio de e-mail |
| `CHRONOS_MAIL_FROM` | não | `no-reply@chronospulse.com.br` | Remetente |
| `JWT_EXPIRATION_MS` / `JWT_REFRESH_EXPIRATION_MS` | não | `3600000` / `28800000` | Expiração dos tokens |
| `SPRING_PROFILES_ACTIVE` | não | `dev` | Perfil (`dev`, `test`, `prod`) |

Exemplo completo com placeholders no arquivo [`.env.example`](../.env.example).

### Reiniciar o banco (dados de demonstração do zero)

Com Docker:

```bash
docker compose down -v && docker compose up --build -d
```

Sem Docker:

```sql
DROP DATABASE chronos_db;
CREATE DATABASE chronos_db OWNER chronos_user;
```

A próxima subida reaplica as 32 migrations e os seeds.

---

## Credenciais de Teste (seeds — tenant de demonstração)

> As credenciais de demonstração (usuários e tenants dos seeds) estão concentradas
> em [`credenciais.md`](credenciais.md). As credenciais de acesso
> **privilegiado** (fundador da empresa e Admin Plataforma) **não são documentadas
> em texto plano no repositório** — são entregues fora do código (ver seção de
> acessos do time / gestor de segredos).

O tenant de demonstração possui todos os 9 módulos ativos; novas empresas recebem automaticamente o trio core (`PONTO`, `RECURSOS_HUMANOS`, `ESTOQUE`).

---

## Testes automatizados

```bash
./mvnw test      # Windows: .\mvnw.cmd test   (376 testes)
```

O ambiente de teste usa H2 em modo PostgreSQL (`application-test.yml`, Flyway desativado).

---

## API / Swagger

- Swagger UI: `http://localhost:8080/swagger-ui.html`
- Spec OpenAPI (JSON): `http://localhost:8080/v3/api-docs`
