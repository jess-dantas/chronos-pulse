# Chronos Pulse

Sistema de gestão de controle de ponto com suporte a sincronização offline e exportação fiscal no formato AEJ (Arquivo Eletrônico de Jornada).

## Tecnologias

- Java 25
- Spring Boot 4.1.1
- Spring Data JPA + Hibernate
- PostgreSQL (produção) / H2 (testes)
- MapStruct 1.5.5
- Lombok
- JUnit 5 + Mockito + AssertJ

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

## Funcionalidades

- **Sincronização de lote**: recebe registros de ponto gerados offline pelo dispositivo móvel via `POST /api/v1/pontos/sincronizar`
- **Integridade**: gera hash SHA-256 por registro com base em CPF, colaborador, data/hora, tipo e coordenadas GPS
- **NSR**: atribui Número Sequencial de Registro conforme exigência legal
- **Exportação fiscal**: gera arquivo AEJ para download via `GET /api/v1/fiscal/aej/download`

## Configuração

Configure as variáveis de conexão com o banco em `application.properties`:

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/chronos_pulse
spring.datasource.username=<usuario>
spring.datasource.password=<senha>
spring.jpa.hibernate.ddl-auto=update
```

## Executando

```bash
./mvnw spring-boot:run
```

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
