# Chronos Pulse

<div align="center">

![Version](https://img.shields.io/badge/version-1.1.0-blue)
![Java](https://img.shields.io/badge/Java-25-orange)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-4.1.1-brightgreen)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-16-blue)
![Docker Compose](https://img.shields.io/badge/Docker%20Compose-Ready-2496ED)
![License](https://img.shields.io/badge/license-MIT-green)

Plataforma **multi-tenant e modular** (SaaS) para gestão pública integrada: ponto eletrônico, colaboradores, estoque e almoxarifado (PMP/MCASP), patrimônio, frota documentada e protocolo eletrônico — com autenticação JWT por perfil (RBAC) e ativação de módulos por empresa (CNPJ).

</div>

---

## Módulos da Plataforma

| Código | Módulo | Ativação |
|---|---|---|
| `PONTO` | Ponto Eletrônico | Core (automática no cadastro) |
| `RECURSOS_HUMANOS` | Recursos Humanos / Colaboradores | Core |
| `ESTOQUE` | Estoque & Almoxarifado | Core |
| `PATRIMONIO` | Patrimônio Público | Contratada (Admin Plataforma) |
| `FROTA` | Gestão de Frota | Contratada |
| `PROTOCOLO` | Protocolo & Tramitação | Contratada |

A ativação por empresa é feita no painel Admin Plataforma (`docs/modulos-saas.md`) e validada no servidor pelo interceptor `@RequiresModulo`.

---

## Início Rápido

```bash
# 1. Containerização completa (API + PostgreSQL)
docker compose up --build -d

# 2. Execução local com Maven (Windows: .\mvnw.cmd)
./mvnw spring-boot:run

# 3. Testes automatizados (62 testes)
./mvnw test
```

### Swagger / OpenAPI

Com a API no ar, acesse a documentação interativa:

- Swagger UI: `http://localhost:8080/swagger-ui.html`
- Spec OpenAPI (JSON): `http://localhost:8080/v3/api-docs`

---

## Credenciais de Teste (Seeds)

| Perfil | CPF | Senha |
|---|---|---|
| **Admin Empresa** | `11111111111` | `admin123` |
| **Gestor de RH** | `22222222222` | `admin123` |
| **Colaborador** | `12345678901` | `senha123` |
| **Colaborador Almoxarife** (ponto + estoque) | `98765432100` | `senha123` |

O tenant de demonstração possui os 6 módulos ativos; os demais tenants recebem automaticamente o trio core (`PONTO`, `RECURSOS_HUMANOS`, `ESTOQUE`).

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