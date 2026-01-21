# 📋 TASKS - TÁ MARCADO! API

## 🎯 Progresso Geral

- [x] **TASK-BE-001**: Setup do Projeto Backend ✅
- [x] **TASK-BE-002**: Configurar Segurança e JWT ✅
- [x] **TASK-BE-003**: Modelagem do Banco de Dados
- [x] **TASK-BE-004**: Repositories
- [x] **TASK-BE-005**: Implementar Autenticação ✅
- [x] **TASK-BE-006**: Implementar Geocoding ✅
- [x] **TASK-BE-007**: Implementar UserController ✅
- [x] **TASK-BE-008**: Implementar Busca de Serviços ✅
- [x] **TASK-BE-009**: Implementar Busca de Profissionais ✅
- [x] **TASK-BE-010**: Implementar ProfessionalController ✅
- [ ] **TASK-BE-011**: Implementar AppointmentController - Cliente
- [ ] **TASK-BE-012**: Implementar Gerenciamento de Agendamentos - Profissional
- [ ] **TASK-BE-013**: Implementar Sistema de Notificações
- [ ] **TASK-BE-014**: Implementar Sistema de Avaliações
- [ ] **TASK-BE-015**: Implementar Dashboard - Profissional
- [ ] **TASK-BE-016**: Implementar Dashboard - Cliente
- [ ] **TASK-BE-017**: Testes de Integração
- [ ] **TASK-BE-018**: Performance e Otimizações
- [ ] **TASK-BE-019**: Documentação e Deploy

---

## 📝 SPRINT 1 - FUNDAÇÃO

### ✅ TASK-BE-001: Setup do Projeto Backend
**Status:** ✅ Concluído  
**Branch:** `develop`  
**Responsável:** Backend Lead  
**Estimativa:** 1 dia  

**Checklist:**
- [x] Criar projeto Spring Boot 3.2+ com Java 21
- [x] Configurar Maven com todas as dependências
- [x] Configurar profiles (dev, prod)
- [x] Setup Docker Compose (PostgreSQL + Redis)
- [x] Configurar application.yml
- [x] Configurar CORS
- [x] Configurar Swagger

**Arquivos criados:**
- `pom.xml`
- `docker-compose.yml`
- `application.yml`, `application-dev.yml`, `application-prod.yml`
- `CorsConfig.java`
- `SwaggerConfig.java`
- Estrutura de pastas conforme arquitetura

---

### ✅ TASK-BE-002: Configurar Segurança e JWT
**Status:** ✅ Concluído  
**Branch:** `task/be-002-security-jwt`  
**Responsável:** Backend Dev 1  
**Estimativa:** 2 dias  

**Checklist:**
- [x] Implementar SecurityConfig
- [x] Criar JwtTokenProvider (geração e validação)
- [x] Implementar JwtAuthenticationFilter
- [ ] Implementar Rate Limiting (opcional - deixado para futuro)
- [x] Criar CustomUserDetailsService
- [x] Criar SecurityUtils

**Arquivos criados:**
- `JwtTokenProvider.java` - Geração e validação de tokens JWT
- `SecurityConfig.java` - Configuração do Spring Security
- `JwtAuthenticationFilter.java` - Filtro para validar tokens
- `CustomUserDetailsService.java` - Serviço para carregar usuários (mock temporário)
- `SecurityUtils.java` - Utilitários de segurança

---

### ✅ TASK-BE-003: Modelagem do Banco de Dados
**Status:** ✅ Concluído  
**Branch:** `task/be-003-database-modeling`  
**Responsável:** Backend Dev 2  
**Estimativa:** 2 dias  

**Checklist:**
- [x] Criar enums (UserType, Category, ServiceType, AppointmentStatus)
- [x] Criar entidades JPA:
  - [x] User
  - [x] Professional
  - [x] ServiceOffering
  - [x] Appointment
  - [x] Review
  - [x] Address
- [x] Definir relacionamentos
- [x] Criar migrations Flyway (V1 a V5)
- [x] Adicionar índices para performance

**Arquivos criados:**
- Enums: `UserType.java`, `Category.java`, `ServiceType.java`, `AppointmentStatus.java`
- Entidades: `User.java`, `Professional.java`, `ServiceOffering.java`, `Appointment.java`, `Review.java`, `Address.java`
- Migrations: `V1__create_users_and_addresses_tables.sql`, `V2__create_professionals_table.sql`, `V3__create_services_table.sql`, `V4__create_appointments_table.sql`, `V5__create_reviews_table.sql`

---

### ✅ TASK-BE-004: Repositories
**Status:** ✅ Concluído  
**Branch:** `task/be-004-repositories`  
**Responsável:** Backend Dev 2  
**Estimativa:** 1 dia  

**Checklist:**
- [x] Criar UserRepository com queries customizadas
- [x] Criar ProfessionalRepository com busca geográfica
- [x] Criar ServiceOfferingRepository
- [x] Criar AppointmentRepository com filtros
- [x] Criar ReviewRepository
- [x] Implementar especificações para queries dinâmicas

**Arquivos criados:**
- Ports: `UserRepositoryPort.java`, `ProfessionalRepositoryPort.java`, `ServiceOfferingRepositoryPort.java`, `AppointmentRepositoryPort.java`, `ReviewRepositoryPort.java`
- JPA Repositories: `UserJpaRepository.java`, `ProfessionalJpaRepository.java`, `ServiceOfferingJpaRepository.java`, `AppointmentJpaRepository.java`, `ReviewJpaRepository.java`
- Adapters: `UserRepositoryAdapter.java`, `ProfessionalRepositoryAdapter.java`, `ServiceOfferingRepositoryAdapter.java`, `AppointmentRepositoryAdapter.java`, `ReviewRepositoryAdapter.java`

---

## 📝 SPRINT 2 - AUTENTICAÇÃO

### ✅ TASK-BE-005: Implementar Autenticação
**Status:** ✅ Concluído  
**Branch:** `task/be-005-authentication`  
**Responsável:** Backend Dev 1  
**Estimativa:** 3 dias  

**Checklist:**
- [x] Criar AuthController (register, login, refresh, logout)
- [x] Implementar AuthService:
  - [x] Cadastro de cliente
  - [x] Cadastro de profissional
  - [x] Login com JWT
  - [x] Refresh token
  - [ ] Logout (invalidar token) - Deixado para implementação futura
  - [ ] Forgot password - Deixado para implementação futura
  - [ ] Reset password - Deixado para implementação futura
- [x] Validar dados de entrada com Bean Validation
- [x] Criptografar senha com BCrypt
- [x] Criar DTOs (Request/Response)
- [ ] Escrever testes unitários - Deixado para implementação futura

**Arquivos criados:**
- `AuthController.java` - Endpoints REST de autenticação
- `AuthService.java` - Lógica de negócio de autenticação
- DTOs: `LoginRequest.java`, `RegisterClientRequest.java`, `RegisterProfessionalRequest.java`, `RefreshTokenRequest.java`, `AddressRequest.java`, `ServiceRequest.java`, `AuthResponse.java`, `UserResponse.java`, `ApiResponse.java`
- Exceptions: `BusinessException.java`, `ResourceNotFoundException.java`, `GlobalExceptionHandler.java`

---

### ✅ TASK-BE-006: Implementar Geocoding
**Status:** ✅ Concluído  
**Branch:** `task/be-006-geocoding`  
**Responsável:** Backend Dev 1  
**Estimativa:** 2 dias  

**Checklist:**
- [x] Criar GeocodingPort (interface)
- [x] Implementar GeocodingAdapter:
  - [x] Integração com ViaCEP (busca por CEP)
  - [x] Integração com Google Maps API (geocoding com coordenadas)
  - [x] Validação de URL (prevenção CVE-2024-22259)
- [x] Implementar GeocodingService com cache Redis
- [x] Criar GeocodingController (endpoints REST)
- [x] Configurar RestClient em vez de RestTemplate
- [x] Configurar CacheConfig para Redis
- [x] Criar DTOs (Request/Response)
- [x] Atualizar SecurityConstants para endpoints públicos

**Arquivos criados:**
- Ports: `GeocodingPort.java`
- Adapters: `GeocodingAdapter.java` - Implementação com ViaCEP e Google Maps
- Services: `GeocodingService.java` - Lógica de negócio com cache Redis
- Controllers: `GeocodingController.java` - Endpoints REST
- Config: `RestClientConfig.java` - Configuração do RestClient
- Config: `CacheConfig.java` - Configuração do cache Redis
- DTOs: `AddressToCoordsRequest.java`, `CepRequest.java`, `CoordinatesResponse.java`, `AddressResponse.java`
- DTOs (APIs externas): `ViaCepResponse.java`, `GoogleGeocodeResponse.java`

**Endpoints implementados:**
- `POST /api/v1/geocoding/address-to-coords` - Converte endereço para coordenadas
- `POST /api/v1/geocoding/cep` - Busca endereço por CEP

**Correções implementadas:**
- ✅ CVE-2024-22259 corrigido (validação de URL para prevenir Open Redirect)
- ✅ Construtor deprecado `URL(String)` substituído por `URI.create()` (Java 20+)
- ✅ Refatoração de `RestTemplate` para `RestClient` (Spring 6.1+)

---

### ✅ TASK-BE-007: Implementar UserController
**Status:** ✅ Concluído  
**Branch:** `task/be-007-user-controller`  
**Responsável:** Backend Dev 1  
**Estimativa:** 1 dia  

**Checklist:**
- [x] Criar UserController com endpoints:
  - [x] `GET /me` - Obter perfil do usuário autenticado
  - [x] `PUT /me` - Atualizar perfil do usuário autenticado
  - [x] `PUT /me/password` - Alterar senha
  - [x] `DELETE /me` - Deletar conta (soft delete)
- [x] Implementar UserService com validações de segurança
- [x] Criar DTOs (UpdateUserRequest, ChangePasswordRequest)
- [x] Implementar validação de acesso (usuário só pode acessar seus próprios recursos)
- [x] Usar MapStruct para conversões (conforme item 8 da arquitetura)

**Arquivos criados:**
- `UserController.java` - Endpoints REST para gerenciamento de perfil
- `UserService.java` - Lógica de negócio para gerenciamento de usuário
- DTOs: `UpdateUserRequest.java`, `ChangePasswordRequest.java`

**Melhorias implementadas:**
- ✅ Implementação de mappers MapStruct (UserDtoMapper)
- ✅ Validação de segurança para garantir que usuário só acessa seus próprios recursos

---

### ✅ TASK-BE-008: Implementar Busca de Serviços
**Status:** ✅ Concluído  
**Branch:** `task/be-008-search-services`  
**Responsável:** Backend Dev 1  
**Estimativa:** 1 dia  

**Checklist:**
- [x] Criar SearchController com endpoint `GET /services`
- [x] Implementar SearchService.searchServices():
  - [x] Buscar por categoria e tipo
  - [x] Agrupar serviços por nome/categoria/tipo
  - [x] Calcular preço mínimo e máximo
  - [x] Contar profissionais únicos
- [x] Implementar cache Redis para resultados de busca
- [x] Criar DTOs (ServiceSearchResponse)
- [x] Adicionar query customizada em ServiceOfferingJpaRepository

**Arquivos criados:**
- `SearchController.java` - Endpoint de busca de serviços
- `SearchService.java` - Lógica de busca com agregação
- DTOs: `ServiceSearchResponse.java`
- Query customizada: `findActiveServicesByCategoryAndType` em `ServiceOfferingJpaRepository.java`

**Configurações:**
- ✅ Cache `serviceSearch` configurado no `CacheConfig.java` (TTL: 1 hora)

---

### ✅ TASK-BE-009: Implementar Busca de Profissionais
**Status:** ✅ Concluído  
**Branch:** `task/be-009-search-professionals`  
**Responsável:** Backend Dev 1  
**Estimativa:** 2 dias  

**Checklist:**
- [x] Criar endpoint `GET /professionals` no SearchController
- [x] Implementar SearchService.searchProfessionals():
  - [x] Buscar por serviceId ou categoria/tipo
  - [x] Calcular distância usando fórmula de Haversine (se coordenadas fornecidas)
  - [x] Suportar ordenação por distância ou avaliação
  - [x] Implementar paginação manual
  - [x] Filtrar profissionais e usuários ativos
- [x] Criar DTOs (ProfessionalSearchResponse)
- [x] Tornar método calculateDistance público para reutilização

**Arquivos criados:**
- Endpoint adicionado em `SearchController.java`
- Método `searchProfessionals` em `SearchService.java`
- DTOs: `ProfessionalSearchResponse.java`

**Funcionalidades:**
- ✅ Cálculo de distância geográfica usando fórmula de Haversine
- ✅ Ordenação por distância ou avaliação média
- ✅ Paginação manual com suporte a page e size
- ✅ Filtro por distância máxima (maxDistanceKm)

---

### ✅ TASK-BE-010: Implementar ProfessionalController
**Status:** ✅ Concluído  
**Branch:** `task/be-010-professional-controller`  
**Responsável:** Backend Dev 1  
**Estimativa:** 2 dias  

**Checklist:**
- [x] Criar ProfessionalController com endpoints:
  - [x] `GET /{id}` - Detalhes públicos do profissional (com serviços, avaliações, distância)
  - [x] `GET /me/services` - Listar serviços do profissional autenticado
  - [x] `POST /me/services` - Criar novo serviço
  - [x] `PUT /me/services/{serviceId}` - Atualizar serviço
  - [x] `DELETE /me/services/{serviceId}` - Deletar serviço (com validação de agendamentos ativos)
- [x] Implementar ProfessionalService:
  - [x] getProfessionalById (com cache)
  - [x] getMyServices
  - [x] createService, updateService, deleteService
- [x] Criar DTOs (ProfessionalDetailResponse, ServiceResponse, ReviewResponse, CreateServiceRequest, UpdateServiceRequest)
- [x] Implementar validações de segurança
- [x] Usar MapStruct para conversões (ServiceDtoMapper, ReviewDtoMapper, ProfessionalDtoMapper, AddressDtoMapper)
- [x] Configurar cache para detalhes do profissional

**Arquivos criados:**
- `ProfessionalController.java` - Endpoints REST para profissionais
- `ProfessionalService.java` - Lógica de negócio para profissionais
- DTOs: `ProfessionalDetailResponse.java`, `ServiceResponse.java`, `ReviewResponse.java`, `CreateServiceRequest.java`, `UpdateServiceRequest.java`
- Mappers: `ServiceDtoMapper.java`, `ReviewDtoMapper.java`, `ProfessionalDtoMapper.java`, `AddressDtoMapper.java`

**Configurações:**
- ✅ Cache `professionalDetail` configurado no `CacheConfig.java` (TTL: 30 minutos)
- ✅ Query otimizada em `ReviewJpaRepository.java` com `LEFT JOIN FETCH` para evitar LazyInitializationException

**Melhorias implementadas:**
- ✅ Implementação completa de mappers MapStruct conforme item 8 da arquitetura
- ✅ Validação para impedir exclusão de serviços com agendamentos ativos
- ✅ Eager loading de relacionamentos em queries de reviews

---

## 📝 SPRINT 3 - MAPPER E REFATORAÇÃO

### ✅ Implementação de Mappers MapStruct (Item 8 da Arquitetura)
**Status:** ✅ Concluído  
**Data:** 2026-01-21  
**Responsável:** Backend Dev 1  

**Checklist:**
- [x] Criar UserDtoMapper (User → UserResponse)
- [x] Criar AddressDtoMapper (Address → AddressResponse)
- [x] Criar AddressRequestMapper (AddressRequest → Address)
- [x] Criar ServiceDtoMapper (ServiceOffering → ServiceResponse)
- [x] Criar ReviewDtoMapper (Review → ReviewResponse)
- [x] Criar ProfessionalDtoMapper (Professional → ProfessionalDetailResponse/ProfessionalSearchResponse)
- [x] Refatorar AuthService para usar mappers
- [x] Refatorar UserService para usar mappers
- [x] Refatorar ProfessionalService para usar mappers
- [x] Refatorar SearchService para usar mappers
- [x] Remover métodos manuais de conversão (toUserResponse, toServiceResponse, etc.)

**Arquivos criados:**
- `shared/mapper/UserDtoMapper.java`
- `shared/mapper/AddressDtoMapper.java`
- `shared/mapper/AddressRequestMapper.java`
- `shared/mapper/ServiceDtoMapper.java`
- `shared/mapper/ReviewDtoMapper.java`
- `shared/mapper/ProfessionalDtoMapper.java`

**Melhorias implementadas:**
- ✅ Código mais limpo e manutenível
- ✅ Type-safe com validação em tempo de compilação
- ✅ Performance otimizada (geração de código pelo MapStruct)
- ✅ Conformidade total com item 8 da arquitetura
- ✅ Correção de bug crítico: autenticação criada antes de gerar tokens em registerClient e registerProfessional

---

## 🔄 Legenda de Status

- ✅ **Concluído**: Task finalizada e testada
- 🔄 **Em andamento**: Task sendo trabalhada
- ⏳ **Aguardando**: Task pendente (dependências ou planejamento)
- 🐛 **Bloqueado**: Task com impedimento/bloqueio
- ❌ **Cancelado**: Task cancelada

---

## 📌 Observações

- Todas as tasks devem ser desenvolvidas em branches separadas
- Nome do branch: `task/{numero-task}-{descricao-curta}`
- Exemplo: `task/be-002-security-jwt`
- Após concluir, fazer merge para `develop`
- Antes de merge, criar Pull Request para revisão

---

**Última atualização:** 2026-01-21

---

## 📊 Resumo de Progresso

**Tasks Concluídas:** 10/19 (52.6%)  
**Sprints Completas:** Sprint 1 ✅ | Sprint 2 ✅ | Sprint 3 (Mapper) ✅  
**Próxima Task:** TASK-BE-011 - Implementar AppointmentController - Cliente
