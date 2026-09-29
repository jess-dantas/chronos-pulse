# Chronos Pulse

<div align="center">

![Version](https://img.shields.io/badge/version-1.1.0-blue)
![Java](https://img.shields.io/badge/Java-25-orange)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-4.1.1-brightgreen)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-16-blue)
![Docker Compose](https://img.shields.io/badge/Docker%20Compose-Ready-2496ED)
![Tests](https://img.shields.io/badge/tests-376%20verdes-brightgreen)
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

A ativação por empresa é feita no painel Admin Plataforma (`docs/modulos-saas.md`) e validada no servidor pelo interceptor `@RequiresModulo`.

---

## Stack

Java 25 · Spring Boot 4.1.1 · Spring Security / JJWT 0.12.6 · Spring Data JPA/Hibernate · PostgreSQL 16 · Flyway · MapStruct 1.5.5 · Lombok · Springdoc OpenAPI 3.1.0 · JUnit 5 / Mockito / AssertJ / H2 · Docker Compose.

---

## Documentação

- [`docs/infra.md`](docs/infra.md) — Subida da aplicação (Docker/Maven), variáveis de ambiente, testes e Swagger
- [`docs/arquitetura.md`](docs/arquitetura.md) — Arquitetura hexagonal modular e estrutura de pastas
- [`docs/modulos-saas.md`](docs/modulos-saas.md) — Catálogo, ativação por tenant, interceptor e como criar um novo módulo
- [`docs/api.md`](docs/api.md) — Referência completa de endpoints (todos os módulos)
- [`docs/rbac.md`](docs/rbac.md) — Perfis, permissões e regras de rota

## Licença

MIT — consulte [LICENSE](LICENSE).
