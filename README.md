# Chronos Pulse

<div align="center">

![Version](https://img.shields.io/badge/version-1.0.0-blue)
![Java](https://img.shields.io/badge/Java-25-orange)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-4.1.1-brightgreen)
![License](https://img.shields.io/badge/license-MIT-green)

Sistema de gestão de controle de ponto com suporte a sincronização offline e exportação fiscal no formato AEJ (Arquivo Eletrônico de Jornada).

</div>

---

## Índice

- [Funcionalidades](#funcionalidades)
- [Arquitetura](#arquitetura)
- [Pré-requisitos](#pré-requisitos)
- [Configuração](#configuração)
- [Executando](#executando)
- [Testes](#testes)
- [Dependências de Terceiros](#dependências-de-terceiros)
- [Licença](#licença)

---

## Funcionalidades

- **Sincronização de lote** — recebe registros de ponto gerados offline pelo dispositivo móvel via `POST /api/v1/pontos/sincronizar`
- **Integridade** — gera hash SHA-256 por registro com base em CPF, colaborador, data/hora, tipo e coordenadas GPS
- **NSR** — atribui Número Sequencial de Registro conforme exigência legal
- **Exportação fiscal** — gera arquivo AEJ para download via `GET /api/v1/fiscal/aej/download`

---

## Arquitetura

O projeto segue a **Arquitetura Hexagonal (Ports & Adapters)**, mantendo o domínio isolado de frameworks e infraestrutura.

```
src/main/java/.../modules/ponto/
├── domain/
│   ├── model/          # Entidades de domínio (RegistroPonto, TipoRegistro)
│   ├── ports/
│   │   ├── input/      # Casos de uso (interfaces)
│   │   └── output/     # Portas de saída (interfaces)
│   └── service/        # Serviços de domínio (GeradorHashService)
├── application/
│   └── usecases/       # Implementações dos casos de uso
└── infrastructure/
    ├── adapters/
    │   ├── input/rest/  # Controllers REST
    │   └── output/
    │       ├── persistence/  # Adapter JPA + MapStruct
    │       └── fiscal/       # Gerador de arquivo AEJ
    └── config/          # Configuração e injeção de dependências
```

---

## Pré-requisitos

- Java 25+
- Maven 3.9+
- PostgreSQL 14+ (para produção)

---

## Configuração

Configure as variáveis de conexão com o banco em `src/main/resources/application.properties`:

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/chronos_pulse
spring.datasource.username=<usuario>
spring.datasource.password=<senha>
spring.jpa.hibernate.ddl-auto=update
```

---

## Executando

```bash
./mvnw spring-boot:run
```

---

## Testes

```bash
./mvnw test
```

18 testes unitários cobrindo todas as camadas da arquitetura hexagonal, sem dependência de contexto Spring ou banco de dados.

| Camada | Classe | Testes |
|---|---|---|
| Domínio | `GeradorHashService` | 4 |
| Aplicação | `RegistrarPontoUseCaseImpl` | 2 |
| Adapter REST | `SincronizacaoPontoController` | 3 |
| Adapter Persistência | `RegistroPontoRepositoryAdapter` | 4 |
| Adapter Fiscal | `GeradorArquivoAEJAdapter` | 4 |
| Smoke Test | `ChronosPulseApplicationTests` | 1 |

---

## Dependências de Terceiros

Este projeto utiliza as seguintes bibliotecas de código aberto:

| Biblioteca | Versão | Licença | Uso |
|---|---|---|---|
| [Spring Boot](https://github.com/spring-projects/spring-boot) | 4.1.1 | Apache 2.0 | Framework principal, web, JPA, validação |
| [Hibernate ORM](https://github.com/hibernate/hibernate-orm) | 7.x | LGPL 2.1 | Mapeamento objeto-relacional |
| [PostgreSQL JDBC Driver](https://github.com/pgjdbc/pgjdbc) | gerenciado | BSD 2-Clause | Driver de conexão com PostgreSQL |
| [MapStruct](https://github.com/mapstruct/mapstruct) | 1.5.5 | Apache 2.0 | Mapeamento entre entidades JPA e modelos de domínio |
| [Lombok](https://github.com/projectlombok/lombok) | gerenciado | MIT | Redução de boilerplate em tempo de compilação |
| [H2 Database](https://github.com/h2database/h2database) | gerenciado | EPL 1.0 / MPL 2.0 | Banco em memória para testes |
| [JUnit 5](https://github.com/junit-team/junit5) | gerenciado | EPL 2.0 | Framework de testes unitários |
| [Mockito](https://github.com/mockito/mockito) | gerenciado | MIT | Mocks para testes unitários |
| [AssertJ](https://github.com/assertj/assertj) | gerenciado | Apache 2.0 | Asserções fluentes em testes |

> Versões marcadas como _gerenciado_ são controladas pelo `spring-boot-starter-parent`.

---

## Licença

Copyright (c) 2025 Chronos Pulse

Distribuído sob a licença MIT. Consulte o arquivo [LICENSE](LICENSE) para mais detalhes.
