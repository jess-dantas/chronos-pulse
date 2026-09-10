# Chronos Pulse

<div align="center">

![Version](https://img.shields.io/badge/version-1.1.0-blue)
![Java](https://img.shields.io/badge/Java-25-orange)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-4.1.1-brightgreen)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-16-blue)
![Docker Compose](https://img.shields.io/badge/Docker%20Compose-Ready-2496ED)
![Tests](https://img.shields.io/badge/tests-243%20verdes-brightgreen)
![License](https://img.shields.io/badge/license-MIT-green)

</div>

## Plataforma **multi-tenant e modular** (SaaS) para gestão pública integrada: <br> 
 - Ponto Eletrônico;<br>
 - Colaboradores;<br>
 - Estoque e Almoxarifado (PMP/MCASP);<br>
 - Patrimônio;<br>
 - Frota;<br>
 - Protocolo Eletrônico;<br>
 - Compras & Fornecedores;
 - Licitações (Lei 14.133/2021) e portal da transparência (LC 131/2009) com BI;<br> 
 - Autenticação JWT por perfil (RBAC) e ativação de módulos por empresa (CNPJ).

---

## Módulos da Plataforma

| Código | Módulo | Ativação |
|---|---|---|
| `PONTO` | Ponto Eletrônico | Core (automática no cadastro) |
| `RECURSOS_HUMANOS` | Recursos Humanos / Colaboradores | Core |
| `ESTOQUE` | Estoque & Almoxarifado | Core |
| `PATRIMONIO` | Patrimônio Público (tombamento, depreciação e inventário) | Contratada (Admin Plataforma) |
| `FROTA` | Gestão de Frota | Contratada |
| `PROTOCOLO` | Protocolo & Tramitação | Contratada |
| `COMPRAS` | Compras & Fornecedores (pedidos, NFe, banco de preços) | Contratada |
| `LICITACOES` | Licitações & Contratações (Lei 14.133/2021) | Contratada |
| `TRANSPARENCIA` | Portal da Transparência & BI (LC 131/2009) | Contratada |

A ativação por empresa é feita no painel Admin Plataforma (`docs/modulos-saas.md`) e validada no servidor pelo interceptor `@RequiresModulo`. O catálogo é versionado no Flyway (`V11`, `V21`, `V24` e `V26`).

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

O Flyway roda automaticamente na subida (29 migrations) e cria os seeds de demonstração. A documentação interativa fica em `http://localhost:8080/swagger-ui.html`.

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

Exemplo completo com placeholders no arquivo [`.env.example`](.env.example).

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

A próxima subida reaplica as 29 migrations e os seeds.

---

## Credenciais de Teste (seeds — tenant de demonstração)

> As credenciais de acesso **privilegiado** (fundador da empresa e Admin Plataforma)
> **não ficam no repositório** — são entregues fora dos projetos (ver seção de acessos
> do time / gestor de segredos).

| Perfil | CPF | Senha |
|---|---|---|
| **Admin Empresa** | `11111111111` | `admin123` |
| **Gestor de RH** | `22222222222` | `admin123` |
| **Colaborador** | `12345678901` | `senha123` |
| **Colaborador Almoxarife** (ponto + estoque) | `98765432100` | `senha123` |

O tenant de demonstração possui todos os 9 módulos ativos; novas empresas recebem automaticamente o trio core (`PONTO`, `RECURSOS_HUMANOS`, `ESTOQUE`).

---

## Testes automatizados

```bash
./mvnw test      # Windows: .\mvnw.cmd test   (243 testes)
```

O ambiente de teste usa H2 em modo PostgreSQL (`application-test.yml`, Flyway desativado).

---

## API / Swagger

- Swagger UI: `http://localhost:8080/swagger-ui.html`
- Spec OpenAPI (JSON): `http://localhost:8080/v3/api-docs`

---

## Documentação

- [`docs/arquitetura.md`](docs/arquitetura.md) — Arquitetura hexagonal modular e estrutura de pastas
- [`docs/modulos-saas.md`](docs/modulos-saas.md) — Catálogo, ativação por tenant, interceptor e como criar um novo módulo
- [`docs/api.md`](docs/api.md) — Referência completa de endpoints (todos os módulos)
- [`docs/rbac.md`](docs/rbac.md) — Perfis, permissões e regras de rota
- [`docs/dados-iniciais.md`](docs/dados-iniciais.md) — Seeds, migrations e testes automatizados

---

## Stack

Java 25 · Spring Boot 4.1.1 · Spring Security / JJWT 0.12.6 · Spring Data JPA/Hibernate · PostgreSQL 16 · Flyway · MapStruct 1.5.5 · Lombok · Springdoc OpenAPI 3.1.0 · JUnit 5 / Mockito / AssertJ / H2 · Docker Compose.

## Licença

MIT — consulte [LICENSE](LICENSE).