# Arquitetura

O **Chronos Pulse** adota a **Arquitetura Hexagonal (Ports & Adapters)** organizada em módulos de negócio coesos e desacoplados de frameworks e persistência. Cada módulo expõe:

- `domain/entity` — entidades JPA (persistência) e modelos de domínio
- `repository/` — interfaces Spring Data JPA
- `service/` — regras de negócio
- `web/` — controllers REST e DTOs (`web/dto`)
- `application/usecases/` — casos de uso (módulos legados) com `ports`

## Estrutura de Módulos

```
src/main/java/br/com/jess/chronos/pulse/
├── modules/
│   ├── auth/                # Autenticação, cadastro público, JWT, Security
│   │   ├── application/usecases/
│   │   │   ├── AutenticarUsuarioUseCaseImpl        # Login (JWT + módulos ativos)
│   │   │   ├── CadastrarEmpresaCompletoUseCaseImpl # Empresa + admin + módulos core
│   │   │   ├── RefreshTokenUseCaseImpl             # Renovação do access token
│   │   │   └── BuscarPerfilUseCaseImpl             # Perfil do usuário autenticado
│   │   ├── domain/model/ & ports/
│   │   └── infrastructure/ (Security, JwtAuthFilter, config)
│   ├── empresa/             # Gestão de empresas (tenants)
│   ├── colaborador/         # CRUD de colaboradores + acesso ao estoque
│   ├── ponto/               # Controle de ponto, sincronização e AEJ
│   │   ├── domain/model/ & ports/ & service/
│   │   ├── application/usecases/
│   │   └── infrastructure/adapters/ (REST, persistence, fiscal)
│   ├── estoque/             # Estoque & Almoxarifado (MCASP/PMP)
│   │   ├── domain/entity/ & service/
│   │   ├── repository/
│   │   └── web/ (Controllers e DTOs)
│   ├── patrimonio/          # Patrimônio Público (tombamento)
│   │   ├── domain/entity/ & repository/ & service/ & web/
│   ├── frota/               # Gestão de Frota (veículos + abastecimentos)
│   │   ├── domain/entity/ (FrotaVeiculo, FrotaAbastecimento)
│   │   ├── repository/ & service/ & web/
│   ├── protocolo/           # Protocolo Eletrônico (documentos/processos)
│   │   ├── domain/entity/ & repository/ & service/ & web/
│   ├── modulo/              # Plataforma modular (catálogo + ativação por tenant)
│   │   ├── domain/entity/ (ModuloPlataforma, EmpresaModulo)
│   │   ├── repository/ & service/
│   │   ├── web/             # ModuloAdminController (Admin Plataforma)
│   │   └── infrastructure/security/  # @RequiresModulo + ModuloInterceptor
│   └── notificacao/         # Notificações por e-mail
└── infrastructure/          # Componentes transversais (config, Flyway)
```

## Camadas e Fluxo

1. **Web** → controllers REST recebem a requisição, extraem `tenantId` do principal (`CpcUsuario`).
2. **ModuloInterceptor** → valida se a empresa contratou o módulo exigido pela rota (para perfis de plataforma o acesso é liberado).
3. **Service / UseCase** → regras de negócio, sempre filtrando pelo `tenantId` para garantir isolamento multi-tenant.
4. **Repository** → Spring Data JPA; todas as consultas levam o `tenant_id` como filtro.

## Isolamento Lógico Multi-Tenant

A segregação é **lógica (row-level)** por `tenant_id`: cada entidade principal armazena a coluna `tenant_id` e todos os repositories filtram por ela. Não há separação física de bancos — simplifica o deploy e a operação de um SaaS multi-tenant.

## Migrations

O esquema é versionado com **Flyway** em `src/main/resources/db/migration/` (`V1` a `V11`). A migração `V11__modulos_plataforma_patrimonio_frota_protocolo.sql` introduz o catálogo de módulos (`modulo_plataforma`), a ativação por empresa (`empresa_modulo`) e as tabelas dos três novos módulos, além dos seeds de demonstração.