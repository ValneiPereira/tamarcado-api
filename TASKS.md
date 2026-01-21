# 📋 TASKS - TÁ MARCADO! API

## 🎯 Progresso Geral

- [x] **TASK-BE-001**: Setup do Projeto Backend ✅
- [ ] **TASK-BE-002**: Configurar Segurança e JWT
- [ ] **TASK-BE-003**: Modelagem do Banco de Dados
- [ ] **TASK-BE-004**: Repositories
- [ ] **TASK-BE-005**: Implementar Autenticação
- [ ] **TASK-BE-006**: Implementar Geocoding
- [ ] **TASK-BE-007**: Implementar UserController
- [ ] **TASK-BE-008**: Implementar Busca de Serviços
- [ ] **TASK-BE-009**: Implementar Busca de Profissionais
- [ ] **TASK-BE-010**: Implementar ProfessionalController
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

### 🔄 TASK-BE-002: Configurar Segurança e JWT
**Status:** 🔄 Em andamento  
**Branch:** `task/be-002-security-jwt`  
**Responsável:** Backend Dev 1  
**Estimativa:** 2 dias  

**Checklist:**
- [ ] Implementar SecurityConfig
- [ ] Criar JwtTokenProvider (geração e validação)
- [ ] Implementar JwtAuthenticationFilter
- [ ] Implementar Rate Limiting
- [ ] Criar CustomUserDetailsService

---

### 📋 TASK-BE-003: Modelagem do Banco de Dados
**Status:** ⏳ Aguardando  
**Branch:** `task/be-003-database-modeling`  
**Responsável:** Backend Dev 2  
**Estimativa:** 2 dias  

**Checklist:**
- [ ] Criar enums (UserType, Category, ServiceType, AppointmentStatus)
- [ ] Criar entidades JPA:
  - [ ] User
  - [ ] Professional
  - [ ] Service
  - [ ] Appointment
  - [ ] Review
  - [ ] Address
- [ ] Definir relacionamentos
- [ ] Criar migrations Flyway (V1 a V5)
- [ ] Adicionar índices para performance

---

### 📋 TASK-BE-004: Repositories
**Status:** ⏳ Aguardando  
**Branch:** `task/be-004-repositories`  
**Responsável:** Backend Dev 2  
**Estimativa:** 1 dia  

**Checklist:**
- [ ] Criar UserRepository com queries customizadas
- [ ] Criar ProfessionalRepository com busca geográfica
- [ ] Criar ServiceRepository
- [ ] Criar AppointmentRepository com filtros
- [ ] Criar ReviewRepository
- [ ] Implementar especificações para queries dinâmicas

---

## 📝 SPRINT 2 - AUTENTICAÇÃO

### 📋 TASK-BE-005: Implementar Autenticação
**Status:** ⏳ Aguardando  
**Branch:** `task/be-005-authentication`  
**Responsável:** Backend Dev 1  
**Estimativa:** 3 dias  

**Checklist:**
- [ ] Criar AuthController (register, login, refresh, logout)
- [ ] Implementar AuthService:
  - [ ] Cadastro de cliente
  - [ ] Cadastro de profissional
  - [ ] Login com JWT
  - [ ] Refresh token
  - [ ] Logout (invalidar token)
  - [ ] Forgot password
  - [ ] Reset password
- [ ] Validar dados de entrada com Bean Validation
- [ ] Criptografar senha com BCrypt
- [ ] Criar DTOs (Request/Response)
- [ ] Escrever testes unitários

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