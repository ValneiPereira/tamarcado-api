[![Quality Gate](https://sonarcloud.io/api/project_badges/measure?project=ValneiPereira_tamarcado-api&metric=alert_status)](https://sonarcloud.io/summary/new_code?id=ValneiPereira_tamarcado-api)
[![Coverage](https://sonarcloud.io/api/project_badges/measure?project=ValneiPereira_tamarcado-api&metric=coverage)](https://sonarcloud.io/summary/new_code?id=ValneiPereira_tamarcado-api)

# TÁ MARCADO! - API Backend

Backend API desenvolvida com Spring Boot 3.2+ e Java 21 seguindo os princípios de **Clean Architecture** e **Hexagonal Architecture (Ports & Adapters)**.

## ☁️ Deploy na AWS (Free Tier)

Esta aplicação está hospedada na **AWS** utilizando o **Free Tier**, para fins de **estudo e portfólio**. A infraestrutura utiliza:

- **EC2 (t3.micro)** - Free Tier, servidor da aplicação
- **PostgreSQL** - Banco de dados relacional
- **Redis** - Cache em memória


> **Nota:** O ambiente na AWS é utilizado exclusivamente para fins educacionais e de portfólio, demonstrando conhecimentos em deploy, infraestrutura cloud e DevOps.

## 🚀 Tecnologias

### Backend
- **Java 21** (LTS)
- **Spring Boot 3.2+**
- **Spring Data JPA** - Persistência de dados
- **Spring Security + JWT** - Autenticação e autorização
- **Spring Boot Actuator** - Monitoramento e métricas
- **Spring Boot Validation** - Validação de dados

### Banco de Dados e Cache
- **PostgreSQL 16** - Banco de dados relacional
- **Redis 7** - Cache em memória
- **Flyway** - Migrations de banco de dados

### Ferramentas e Bibliotecas
- **MapStruct** - Mapeamento de objetos (DTO ↔ Domain)
- **Lombok** - Redução de boilerplate
- **Swagger/OpenAPI 3.0** - Documentação da API
- **JJWT 0.12.3** - Geração e validação de tokens JWT

### Testes
- **JUnit 5** - Framework de testes
- **Mockito** - Mocking para testes unitários
- **Testcontainers** - Testes de integração com containers Docker
- **Rest Assured** - Testes de API REST
- **H2 Database** - Banco de dados em memória para testes
- **JaCoCo** - Cobertura de código

### Qualidade de Código
- **SonarCloud** - Análise estática de código e cobertura de testes
  - [Quality Gate](https://sonarcloud.io/summary/new_code?id=ValneiPereira_tamarcado-api)
  - [Coverage](https://sonarcloud.io/summary/new_code?id=ValneiPereira_tamarcado-api)

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
```
┌─────────────────────────────────────┐
│   PRESENTATION (Adapter In)         │  ← Controllers REST
│   - Recebe requisições HTTP         │
│   - Valida DTOs                     │
│   - Trata exceções                  │
└─────────────────────────────────────┘
              ↓↑
┌─────────────────────────────────────┐
│   APPLICATION LAYER                 │  ← Lógica de negócio
│   - Services                        │
│   - Use Cases                       │
│   - Ports (Interfaces)              │
└─────────────────────────────────────┘
              ↓↑
┌─────────────────────────────────────┐
│   DOMAIN LAYER                      │  ← Core do negócio
│   - Entidades                       │
│   - Value Objects                   │
│   - Regras de negócio               │
└─────────────────────────────────────┘
              ↓↑
┌─────────────────────────────────────┐
│   INFRASTRUCTURE (Adapter Out)      │  ← Implementações técnicas
│   - Repositories (JPA)              │
│   - Cache (Redis)                   │
│   - APIs Externas                   │
│   - Configurações                   │
└─────────────────────────────────────┘
```

---

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

O projeto possui uma suíte completa de testes unitários e de integração. A análise de qualidade de código e cobertura de testes é realizada automaticamente pelo **SonarCloud**.

### Executar Testes Localmente

```bash
# Executar todos os testes
mvn test

# Executar testes de integração
mvn verify

# Executar testes com relatório de cobertura
mvn clean verify
```

### SonarCloud

Os testes e a análise de qualidade de código estão disponíveis no **SonarCloud**:

- 🔗 [Dashboard do Projeto](https://sonarcloud.io/summary/new_code?id=ValneiPereira_tamarcado-api)
- 📊 **Quality Gate**: Status da qualidade do código
- 📈 **Coverage**: Cobertura de testes

Os badges no topo do README mostram o status atual:
- ✅ **Quality Gate**: Indica se o código atende aos padrões de qualidade
- 📊 **Coverage**: Mostra a porcentagem de cobertura de testes

### Estrutura de Testes

```
src/test/java/com/tamarcado/
├── integration/              # Testes de integração
│   ├── controller/          # Testes de controllers
│   ├── repository/          # Testes de repositórios
│   └── service/             # Testes de serviços
├── config/                   # Configurações de teste
└── TestUtils.java           # Utilitários para testes
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

Desenvolvido por Valnei Pereira.
