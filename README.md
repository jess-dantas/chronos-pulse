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

## Uso da API

### `POST /api/v1/pontos/sincronizar`

Recebe um lote de registros gerados offline pelo dispositivo móvel. O header `X-CPF-Colaborador` é obrigatório.

Cada jornada completa exige 4 batidas nesta ordem:

| Sequência | `tipoRegistro` | Descrição |
|---|---|---|
| 1 | `ENTRADA` | Início do expediente |
| 2 | `PAUSA_INICIO` | Saída para intervalo |
| 3 | `PAUSA_FIM` | Retorno do intervalo |
| 4 | `SAIDA` | Fim do expediente |

```bash
curl --request POST \
  --url http://localhost:8080/api/v1/pontos/sincronizar \
  --header 'Content-Type: application/json' \
  --header 'X-CPF-Colaborador: 12345678901' \
  --data '{
  "registros": [
    {
      "idLocal": "b1f45c88-7a1a-4d22-921e-123456789011",
      "colaboradorId": "a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11",
      "dataHoraDispositivo": "2026-09-02T08:00:00Z",
      "tipoRegistro": "ENTRADA",
      "latitude": -23.550520,
      "longitude": -46.633308,
      "precisaoGps": 4.2,
      "sincronizadoOffline": true
    },
    {
      "idLocal": "c2f45c88-7a1a-4d22-921e-123456789022",
      "colaboradorId": "a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11",
      "dataHoraDispositivo": "2026-09-02T12:00:00Z",
      "tipoRegistro": "PAUSA_INICIO",
      "latitude": -23.550520,
      "longitude": -46.633308,
      "precisaoGps": 3.8,
      "sincronizadoOffline": true
    },
    {
      "idLocal": "d3f45c88-7a1a-4d22-921e-123456789033",
      "colaboradorId": "a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11",
      "dataHoraDispositivo": "2026-09-02T13:00:00Z",
      "tipoRegistro": "PAUSA_FIM",
      "latitude": -23.550520,
      "longitude": -46.633308,
      "precisaoGps": 4.0,
      "sincronizadoOffline": true
    },
    {
      "idLocal": "e4f45c88-7a1a-4d22-921e-123456789044",
      "colaboradorId": "a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11",
      "dataHoraDispositivo": "2026-09-02T17:00:00Z",
      "tipoRegistro": "SAIDA",
      "latitude": -23.550520,
      "longitude": -46.633308,
      "precisaoGps": 4.5,
      "sincronizadoOffline": true
    }
  ]
}'
```

Resposta:

```json
{
  "idsSucesso": [
    "b1f45c88-7a1a-4d22-921e-123456789011",
    "c2f45c88-7a1a-4d22-921e-123456789022",
    "d3f45c88-7a1a-4d22-921e-123456789033",
    "e4f45c88-7a1a-4d22-921e-123456789044"
  ],
  "idsFalha": []
}
```

### `GET /api/v1/fiscal/aej/download`

Gera e faz o download do arquivo AEJ (Arquivo Eletrônico de Jornada) no formato exigido pela legislação.

```bash
curl --request GET \
  --url 'http://localhost:8080/api/v1/fiscal/aej/download?cnpj=12345678000195&razaoSocial=Chronos%20Pulse%20Tech%20LTDA'
```

Retorna o arquivo `AEJ_<cnpj>.txt` como download.

---

## Pré-requisitos

**Com Docker (recomendado)**
- Docker 24+
- Docker Compose 2.x

**Sem Docker**
- Java 25+
- Maven 3.9+
- PostgreSQL 14+

---

## Configuração

**Com Docker** — as variáveis já estão definidas no `docker-compose.yml`:

| Variável | Valor padrão |
|---|---|
| `POSTGRES_DB` | `chronos_db` |
| `POSTGRES_USER` | `chronos_user` |
| `POSTGRES_PASSWORD` | `chronos_pass` |

**Sem Docker** — configure em `src/main/resources/application.properties`:

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/chronos_db
spring.datasource.username=<usuario>
spring.datasource.password=<senha>
spring.jpa.hibernate.ddl-auto=update
```

---

## Executando

**Com Docker:**

```bash
docker compose up --build -d
```

A aplicação estará disponível em `http://localhost:8080`.
O PostgreSQL ficará exposto na porta `5432`.

Para encerrar:

```bash
docker compose down
```

**Sem Docker:**

```bash
./mvnw spring-boot:run
```

---

## Coleção Insomnia

A coleção de requisições está disponível em `src/test/resources/collections/Insomnia.yaml`.

Para importar: abra o Insomnia → **Import** → selecione o arquivo.

As variáveis de ambiente já estão configuradas na coleção:

| Variável | Valor padrão |
|---|---|
| `base_url` | `http://localhost:8080` |
| `cpf_teste` | `12345678901` |
| `colaborador_id` | `a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11` |
| `cnpj_teste` | `12345678000195` |

Requisições disponíveis:

| Pasta | Requisição |
|---|---|
| Gestão de Ponto | Registrar Ponto Individual — `ENTRADA` |
| Gestão de Ponto | Registrar Ponto Individual — `PAUSA_INICIO` |
| Gestão de Ponto | Registrar Ponto Individual — `PAUSA_FIM` |
| Gestão de Ponto | Registrar Ponto Individual — `SAIDA` |
| Gestão de Ponto | Sincronizar Lote Offline — `ENTRADA` + `PAUSA_INICIO` |
| Gestão de Ponto | Sincronizar Lote Offline — `PAUSA_FIM` + `SAIDA` |
| Fiscal & Auditoria | Download Arquivo Fiscal AEJ |

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
