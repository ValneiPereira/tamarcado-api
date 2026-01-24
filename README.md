[![Quality Gate](https://sonarcloud.io/api/project_badges/measure?project=ValneiPereira_tamarcado-api&metric=alert_status)](https://sonarcloud.io/summary/new_code?id=ValneiPereira_tamarcado-api)
[![Coverage](https://sonarcloud.io/api/project_badges/measure?project=ValneiPereira_tamarcado-api&metric=coverage)](https://sonarcloud.io/summary/new_code?id=ValneiPereira_tamarcado-api)

# TÁ MARCADO! - API Backend

Backend API desenvolvida com Spring Boot 3.2+ e Java 21 seguindo os princípios de **Clean Architecture** e **Hexagonal Architecture (Ports & Adapters)**.

## 🚀 Tecnologias

- **Java 21** (LTS)
- **Spring Boot 3.2+**
- **PostgreSQL 16**
- **Redis 7**
- **Spring Security + JWT**
- **MapStruct** (Mappers)
- **Lombok**
- **Flyway** (Migrations)
- **Swagger/OpenAPI 3.0**

## 📋 Pré-requisitos

- Java 21 ou superior
- Maven 3.9+
- Docker e Docker Compose (para PostgreSQL e Redis)

## 🔧 Configuração do Ambiente

### 1. Clone o repositório

```bash
git clone <repository-url>
cd tamarcado-api
```

### 2. Suba os serviços com Docker Compose

```bash
docker-compose up -d
```

Isso irá iniciar:
- PostgreSQL na porta **5432**
- Redis na porta **6379**

### 3. Configure o perfil de desenvolvimento

O arquivo `application-dev.yml` já está configurado para desenvolvimento local.

### 4. Execute a aplicação

```bash
mvn spring-boot:run
```

Ou compile e execute:

```bash
mvn clean package
java -jar target/tamarcado-api-1.0.0-SNAPSHOT.jar
```

A aplicação estará disponível em: `http://localhost:8080/api/v1`

## 📚 Documentação da API

Após iniciar a aplicação, acesse:

- **Swagger UI**: http://localhost:8080/api/v1/swagger-ui.html
- **OpenAPI Docs**: http://localhost:8080/api/v1/api-docs

## 🏗️ Estrutura do Projeto

O projeto segue a arquitetura Clean Architecture + Hexagonal:

```
src/main/java/com/tamarcado/
├── adapter/              # Adapters (Hexagonal)
│   ├── in/              # Input Adapters (REST Controllers)
│   └── out/             # Output Adapters (Repositories, External Services)
├── application/         # Application Layer (Use Cases, Services)
│   ├── usecase/
│   ├── service/
│   └── port/
├── domain/              # Domain Layer (Entities, Value Objects, Domain Services)
│   ├── model/
│   ├── valueobject/
│   ├── exception/
│   └── service/
├── infrastructure/      # Infrastructure Layer
│   ├── config/
│   ├── security/
│   ├── persistence/
│   └── messaging/
└── shared/              # Shared (Cross-cutting concerns)
    ├── dto/
    ├── mapper/
    ├── exception/
    ├── util/
    └── constant/
```

## 🔐 Segurança

A API utiliza JWT para autenticação. Configurações de JWT podem ser ajustadas em:

- `application-dev.yml` (desenvolvimento)
- `application-prod.yml` (produção - usa variáveis de ambiente)

### Variáveis de Ambiente (Produção)

```bash
JWT_SECRET=<sua-chave-secreta-minimo-256-bits>
CORS_ALLOWED_ORIGINS=https://app.tamarcado.com.br
GOOGLE_MAPS_API_KEY=<sua-api-key>
```

## 🗄️ Banco de Dados

As migrations do Flyway estão em: `src/main/resources/db/migration/`

As migrations são executadas automaticamente na inicialização da aplicação.

## 🧪 Testes

```bash
# Executar todos os testes
mvn test

# Executar testes de integração
mvn verify
```

## 📦 Build

```bash
# Build do projeto
mvn clean package

# Build sem testes
mvn clean package -DskipTests
```

## 🐳 Docker

### Subir serviços (PostgreSQL + Redis)

```bash
docker-compose up -d
```

### Parar serviços

```bash
docker-compose down
```

### Ver logs

```bash
docker-compose logs -f
```

## 📝 Licença

Este projeto é proprietário.

## 👥 Equipe

Desenvolvido pela equipe Tá Marcado!
