# TÁ MARCADO! - PLANO DE IMPLEMENTAÇÃO

## 📋 STACK TECNOLÓGICO

### Backend
- **Linguagem**: Java 21 (LTS)
- **Framework**: Spring Boot 3.2+
- **Banco de Dados**: PostgreSQL 16
- **Cache**: Redis 7+
- **Autenticação**: Spring Security + JWT
- **Documentação API**: Swagger/OpenAPI 3.0
- **Build**: Maven 3.9+
- **Cloud**: AWS / Google Cloud

### Frontend
- **Framework**: React Native 0.81.0
- **Navegação**: React Navigation 6.x
- **Estado Global**: Redux Toolkit / Zustand
- **HTTP Client**: Axios
- **Mapas/Geolocalização**: react-native-maps, @react-native-community/geolocation
- **Notificações**: Firebase Cloud Messaging
- **UI Components**: React Native Paper / NativeBase

### DevOps
- **CI/CD**: GitHub Actions / GitLab CI
- **Containerização**: Docker + Docker Compose
- **Monitoramento**: Prometheus + Grafana
- **Logs**: ELK Stack (Elasticsearch, Logstash, Kibana)

---

## 🔌 ENDPOINTS DA API REST

### Base URL: `https://api.tamarcado.com.br/v1`

---

## 1. AUTENTICAÇÃO E USUÁRIOS

### 1.1 Autenticação
```
POST   /auth/register/client
POST   /auth/register/professional
POST   /auth/login
POST   /auth/refresh-token
POST   /auth/logout
POST   /auth/forgot-password
POST   /auth/reset-password
```

**Payloads:**

**POST /auth/register/client**
```json
{
  "name": "João Silva",
  "email": "joao@email.com",
  "password": "senha123",
  "phone": "(11) 98765-4321",
  "address": {
    "cep": "01310-100",
    "street": "Av. Paulista",
    "number": "1000",
    "complement": "Apto 101",
    "neighborhood": "Bela Vista",
    "city": "São Paulo",
    "state": "SP"
  }
}
```

**POST /auth/register/professional**
```json
{
  "name": "Maria Costa",
  "email": "maria@email.com",
  "password": "senha123",
  "phone": "(11) 97654-3210",
  "address": {
    "cep": "01311-000",
    "street": "Rua Augusta",
    "number": "500",
    "complement": "",
    "neighborhood": "Consolação",
    "city": "São Paulo",
    "state": "SP"
  },
  "category": "BELEZA",
  "serviceType": "BARBEIRO",
  "services": [
    {
      "name": "Corte Simples",
      "price": 30.00
    },
    {
      "name": "Corte + Barba",
      "price": 45.00
    }
  ]
}
```

**POST /auth/login**
```json
{
  "email": "joao@email.com",
  "password": "senha123"
}
```

**Response:**
```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiIs...",
  "refreshToken": "dGhpc2lzYXJlZnJlc2h0b2tlbg==",
  "tokenType": "Bearer",
  "expiresIn": 3600,
  "user": {
    "id": "uuid",
    "name": "João Silva",
    "email": "joao@email.com",
    "userType": "CLIENT",
    "photo": "https://..."
  }
}
```

### 1.2 Perfil do Usuário
```
GET    /users/me
PUT    /users/me
PUT    /users/me/password
PUT    /users/me/photo
DELETE /users/me
```

---

## 2. CATEGORIAS E SERVIÇOS

### 2.1 Categorias
```
GET    /categories
GET    /categories/{category}/types
```

**Response GET /categories:**
```json
[
  {
    "code": "BELEZA",
    "name": "Beleza",
    "icon": "💇",
    "types": [
      {
        "code": "BARBEIRO",
        "name": "Barbeiro"
      },
      {
        "code": "MANICURE",
        "name": "Manicure"
      }
    ]
  }
]
```

### 2.2 Serviços (Profissional)
```
GET    /professionals/me/services
POST   /professionals/me/services
PUT    /professionals/me/services/{serviceId}
DELETE /professionals/me/services/{serviceId}
```

**POST /professionals/me/services**
```json
{
  "name": "Barba Completa",
  "price": 25.00
}
```

---

## 3. BUSCA DE PROFISSIONAIS (Cliente)

### 3.1 Busca e Filtros
```
GET    /search/services?category={category}&type={type}
GET    /search/professionals?serviceId={serviceId}&lat={lat}&lng={lng}&sortBy={distance|rating}
GET    /professionals/{professionalId}
```

**GET /search/services?category=BELEZA&type=BARBEIRO**
```json
{
  "services": [
    {
      "id": "uuid",
      "name": "Corte Simples",
      "minPrice": 30.00,
      "maxPrice": 50.00,
      "professionalCount": 15
    },
    {
      "id": "uuid",
      "name": "Corte + Barba",
      "minPrice": 40.00,
      "maxPrice": 70.00,
      "professionalCount": 12
    }
  ]
}
```

**GET /search/professionals?serviceId=uuid&lat=-23.550520&lng=-46.633308&sortBy=distance**
```json
{
  "professionals": [
    {
      "id": "uuid",
      "name": "João Silva",
      "photo": "https://...",
      "rating": 4.8,
      "totalRatings": 127,
      "distance": 2.5,
      "city": "São Paulo",
      "state": "SP",
      "servicePrice": 30.00
    }
  ],
  "pagination": {
    "page": 1,
    "pageSize": 20,
    "totalPages": 3,
    "totalItems": 45
  }
}
```

**GET /professionals/{professionalId}**
```json
{
  "id": "uuid",
  "name": "João Silva",
  "photo": "https://...",
  "phone": "(11) 98765-4321",
  "rating": 4.8,
  "totalRatings": 127,
  "distance": 2.5,
  "city": "São Paulo",
  "state": "SP",
  "category": "BELEZA",
  "serviceType": "BARBEIRO",
  "services": [
    {
      "id": "uuid",
      "name": "Corte Simples",
      "price": 30.00
    }
  ],
  "reviews": [
    {
      "id": "uuid",
      "clientName": "Maria S.",
      "rating": 5,
      "comment": "Excelente profissional!",
      "createdAt": "2026-01-15T14:30:00Z"
    }
  ]
}
```

---

## 4. AGENDAMENTOS

### 4.1 CRUD Agendamentos (Cliente)
```
POST   /appointments
GET    /appointments/client
GET    /appointments/{appointmentId}
DELETE /appointments/{appointmentId}
```

**POST /appointments**
```json
{
  "professionalId": "uuid",
  "serviceId": "uuid",
  "date": "2026-01-25",
  "time": "14:00",
  "notes": "Prefiro corte mais curto"
}
```

**Response:**
```json
{
  "id": "uuid",
  "professionalId": "uuid",
  "professionalName": "João Silva",
  "professionalPhone": "(11) 98765-4321",
  "clientId": "uuid",
  "clientName": "Maria Costa",
  "service": {
    "id": "uuid",
    "name": "Corte Simples",
    "price": 30.00
  },
  "date": "2026-01-25",
  "time": "14:00",
  "notes": "Prefiro corte mais curto",
  "status": "PENDING",
  "createdAt": "2026-01-20T15:30:00Z"
}
```

**GET /appointments/client?status={PENDING|ACCEPTED|COMPLETED|REJECTED}**
```json
{
  "appointments": [
    {
      "id": "uuid",
      "professionalName": "João Silva",
      "professionalPhoto": "https://...",
      "service": {
        "name": "Corte Simples",
        "price": 30.00
      },
      "date": "2026-01-25",
      "time": "14:00",
      "status": "PENDING",
      "createdAt": "2026-01-20T15:30:00Z"
    }
  ]
}
```

### 4.2 Gerenciar Agendamentos (Profissional)
```
GET    /appointments/professional
PUT    /appointments/{appointmentId}/accept
PUT    /appointments/{appointmentId}/reject
PUT    /appointments/{appointmentId}/complete
```

**GET /appointments/professional?status={PENDING|ACCEPTED|COMPLETED|REJECTED}**
```json
{
  "appointments": [
    {
      "id": "uuid",
      "clientName": "Maria Costa",
      "clientPhone": "(11) 97654-3210",
      "distance": 3.2,
      "service": {
        "name": "Corte Simples",
        "price": 30.00
      },
      "date": "2026-01-25",
      "time": "14:00",
      "notes": "Prefiro corte mais curto",
      "status": "PENDING",
      "createdAt": "2026-01-20T15:30:00Z"
    }
  ]
}
```

**PUT /appointments/{appointmentId}/accept**
```json
{
  "message": "Agendamento aceito com sucesso"
}
```

**PUT /appointments/{appointmentId}/reject**
```json
{
  "reason": "Horário não disponível"
}
```

---

## 5. AVALIAÇÕES

### 5.1 Criar e Listar Avaliações
```
POST   /reviews
GET    /professionals/{professionalId}/reviews
GET    /reviews/client/me
```

**POST /reviews**
```json
{
  "appointmentId": "uuid",
  "rating": 5,
  "comment": "Excelente profissional! Muito atencioso."
}
```

**GET /professionals/{professionalId}/reviews?page=1&pageSize=10**
```json
{
  "averageRating": 4.8,
  "totalReviews": 127,
  "reviews": [
    {
      "id": "uuid",
      "clientName": "Maria S.",
      "rating": 5,
      "comment": "Excelente profissional!",
      "createdAt": "2026-01-15T14:30:00Z"
    }
  ],
  "pagination": {
    "page": 1,
    "pageSize": 10,
    "totalPages": 13,
    "totalItems": 127
  }
}
```

---

## 6. NOTIFICAÇÕES

### 6.1 Push Notifications
```
POST   /notifications/register-device
GET    /notifications
PUT    /notifications/{notificationId}/read
DELETE /notifications/device/{deviceToken}
```

**POST /notifications/register-device**
```json
{
  "deviceToken": "firebase-token-here",
  "platform": "ANDROID"
}
```

**GET /notifications?unreadOnly=true**
```json
{
  "notifications": [
    {
      "id": "uuid",
      "type": "APPOINTMENT_ACCEPTED",
      "title": "Agendamento Confirmado!",
      "message": "João Silva aceitou seu agendamento para 25/01/2026 às 14:00",
      "data": {
        "appointmentId": "uuid"
      },
      "isRead": false,
      "createdAt": "2026-01-20T15:30:00Z"
    }
  ]
}
```

---

## 7. GEOLOCALIZAÇÃO

### 7.1 Geocoding
```
POST   /geocoding/address-to-coords
POST   /geocoding/cep
```

**POST /geocoding/address-to-coords**
```json
{
  "street": "Av. Paulista",
  "number": "1000",
  "city": "São Paulo",
  "state": "SP",
  "cep": "01310-100"
}
```

**Response:**
```json
{
  "latitude": -23.561414,
  "longitude": -46.655881
}
```

**POST /geocoding/cep**
```json
{
  "cep": "01310-100"
}
```

**Response:**
```json
{
  "cep": "01310-100",
  "street": "Avenida Paulista",
  "neighborhood": "Bela Vista",
  "city": "São Paulo",
  "state": "SP"
}
```

---

## 8. DASHBOARD

### 8.1 Estatísticas (Profissional)
```
GET    /dashboard/professional/stats
```

**Response:**
```json
{
  "todayAppointments": 3,
  "pendingAppointments": 5,
  "averageRating": 4.8,
  "totalRatings": 127,
  "monthRevenue": 2450.00,
  "completedThisMonth": 35
}
```

### 8.2 Estatísticas (Cliente)
```
GET    /dashboard/client/stats
```

**Response:**
```json
{
  "upcomingAppointments": 2,
  "completedAppointments": 15,
  "favoriteCategory": "BELEZA"
}
```

---

## 9. ADMIN (Futuro)
```
GET    /admin/users
PUT    /admin/users/{userId}/block
GET    /admin/reports
GET    /admin/analytics
```

---

## 📊 MODELO DE DADOS (Principais Entidades)

### User
```java
@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    private String name;
    
    @Column(unique = true)
    private String email;
    
    private String password; // BCrypt
    
    private String phone;
    
    private String photo; // URL S3
    
    @Enumerated(EnumType.STRING)
    private UserType userType; // CLIENT, PROFESSIONAL
    
    @OneToOne(cascade = CascadeType.ALL)
    private Address address;
    
    private LocalDateTime createdAt;
    
    private LocalDateTime updatedAt;
    
    private boolean active;
}
```

### Professional (extends User)
```java
@Entity
@Table(name = "professionals")
public class Professional {
    @Id
    private UUID id; // Same as User.id
    
    @Enumerated(EnumType.STRING)
    private Category category;
    
    @Enumerated(EnumType.STRING)
    private ServiceType serviceType;
    
    @OneToMany(mappedBy = "professional")
    private List<ServiceOffering> serviceOfferings;
    
    private Double averageRating;
    
    private Integer totalRatings;
}
```

### ServiceOffering
```java
@Entity
@Table(name = "service_offerings")
public class ServiceOffering {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    @ManyToOne
    @JoinColumn(name = "professional_id")
    private Professional professional;
    
    private String name;
    
    private BigDecimal price;
    
    private boolean active;
    
    private LocalDateTime createdAt;
}
```

### Appointment
```java
@Entity
@Table(name = "appointments")
public class Appointment {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    @ManyToOne
    @JoinColumn(name = "client_id")
    private User client;
    
    @ManyToOne
    @JoinColumn(name = "professional_id")
    private Professional professional;
    
    @ManyToOne
    @JoinColumn(name = "service_id")
    private ServiceOffering serviceOffering;
    
    private LocalDate date;
    
    private LocalTime time;
    
    private String notes;
    
    @Enumerated(EnumType.STRING)
    private AppointmentStatus status; // PENDING, ACCEPTED, REJECTED, COMPLETED, CANCELLED
    
    private LocalDateTime createdAt;
    
    private LocalDateTime updatedAt;
}
```

### Review
```java
@Entity
@Table(name = "reviews")
public class Review {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    @OneToOne
    @JoinColumn(name = "appointment_id")
    private Appointment appointment;
    
    @ManyToOne
    @JoinColumn(name = "professional_id")
    private Professional professional;
    
    private Integer rating; // 1-5
    
    private String comment;
    
    private LocalDateTime createdAt;
}
```

### Address
```java
@Entity
@Table(name = "addresses")
public class Address {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    private String cep;
    private String street;
    private String number;
    private String complement;
    private String neighborhood;
    private String city;
    private String state;
    
    private Double latitude;
    private Double longitude;
}
```

---

## 🔐 SEGURANÇA

### Headers Obrigatórios
```
Authorization: Bearer {token}
Content-Type: application/json
X-API-Version: v1
X-Platform: android|ios
X-App-Version: 1.0.0
```

### Rate Limiting
- Autenticação: 5 tentativas/minuto por IP
- Busca: 60 requisições/minuto por usuário
- Criação: 10 requisições/minuto por usuário
- Geral: 100 requisições/minuto por usuário

---

## 📱 ESTRUTURA DO PROJETO REACT NATIVE

```
ta-marcado-app/
├── src/
│   ├── api/
│   │   ├── client.js
│   │   ├── auth.js
│   │   ├── appointments.js
│   │   ├── professionals.js
│   │   └── reviews.js
│   ├── components/
│   │   ├── common/
│   │   │   ├── Button.jsx
│   │   │   ├── Input.jsx
│   │   │   ├── Card.jsx
│   │   │   └── StarRating.jsx
│   │   ├── navigation/
│   │   │   └── BottomTabBar.jsx
│   │   └── features/
│   │       ├── ServiceCard.jsx
│   │       ├── ProfessionalCard.jsx
│   │       └── AppointmentCard.jsx
│   ├── screens/
│   │   ├── auth/
│   │   │   ├── LoginScreen.jsx
│   │   │   ├── RegisterClientScreen.jsx
│   │   │   └── RegisterProfessionalScreen.jsx
│   │   ├── client/
│   │   │   ├── HomeScreen.jsx
│   │   │   ├── SearchScreen.jsx
│   │   │   ├── ProfessionalDetailScreen.jsx
│   │   │   ├── BookingScreen.jsx
│   │   │   ├── AppointmentsScreen.jsx
│   │   │   └── ProfileScreen.jsx
│   │   └── professional/
│   │       ├── DashboardScreen.jsx
│   │       ├── AppointmentsScreen.jsx
│   │       ├── ServiceManagementScreen.jsx
│   │       └── ProfileScreen.jsx
│   ├── navigation/
│   │   ├── AppNavigator.jsx
│   │   ├── AuthNavigator.jsx
│   │   ├── ClientNavigator.jsx
│   │   └── ProfessionalNavigator.jsx
│   ├── store/
│   │   ├── slices/
│   │   │   ├── authSlice.js
│   │   │   ├── appointmentsSlice.js
│   │   │   └── professionalsSlice.js
│   │   └── store.js
│   ├── services/
│   │   ├── geolocation.js
│   │   ├── notifications.js
│   │   └── storage.js
│   ├── utils/
│   │   ├── masks.js
│   │   ├── validators.js
│   │   ├── formatters.js
│   │   └── constants.js
│   ├── hooks/
│   │   ├── useAuth.js
│   │   ├── useGeolocation.js
│   │   └── useNotifications.js
│   └── theme/
│       ├── colors.js
│       ├── typography.js
│       └── spacing.js
├── android/
├── ios/
├── package.json
└── app.json
```

---

## 🏗️ ESTRUTURA DO PROJETO BACKEND (Java/Spring Boot)

```
ta-marcado-backend/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/tamarcado/
│   │   │       ├── TaMarcadoApplication.java
│   │   │       ├── config/
│   │   │       │   ├── SecurityConfig.java
│   │   │       │   ├── SwaggerConfig.java
│   │   │       │   ├── RedisConfig.java
│   │   │       │   └── CorsConfig.java
│   │   │       ├── controller/
│   │   │       │   ├── AuthController.java
│   │   │       │   ├── UserController.java
│   │   │       │   ├── ProfessionalController.java
│   │   │       │   ├── AppointmentController.java
│   │   │       │   ├── ReviewController.java
│   │   │       │   ├── SearchController.java
│   │   │       │   └── NotificationController.java
│   │   │       ├── service/
│   │   │       │   ├── AuthService.java
│   │   │       │   ├── UserService.java
│   │   │       │   ├── ProfessionalService.java
│   │   │       │   ├── AppointmentService.java
│   │   │       │   ├── ReviewService.java
│   │   │       │   ├── GeocodingService.java
│   │   │       │   ├── NotificationService.java
│   │   │       │   └── EmailService.java
│   │   │       ├── repository/
│   │   │       │   ├── UserRepository.java
│   │   │       │   ├── ProfessionalRepository.java
│   │   │       │   ├── ServiceRepository.java
│   │   │       │   ├── AppointmentRepository.java
│   │   │       │   └── ReviewRepository.java
│   │   │       ├── model/
│   │   │       │   ├── entity/
│   │   │       │   │   ├── User.java
│   │   │       │   │   ├── Professional.java
│   │   │       │   │   ├── ServiceOffering.java
│   │   │       │   │   ├── Appointment.java
│   │   │       │   │   ├── Review.java
│   │   │       │   │   └── Address.java
│   │   │       │   ├── dto/
│   │   │       │   │   ├── request/
│   │   │       │   │   │   ├── LoginRequest.java
│   │   │       │   │   │   ├── RegisterClientRequest.java
│   │   │       │   │   │   ├── RegisterProfessionalRequest.java
│   │   │       │   │   │   └── CreateAppointmentRequest.java
│   │   │       │   │   └── response/
│   │   │       │   │       ├── AuthResponse.java
│   │   │       │   │       ├── UserResponse.java
│   │   │       │   │       └── AppointmentResponse.java
│   │   │       │   └── enums/
│   │   │       │       ├── UserType.java
│   │   │       │       ├── Category.java
│   │   │       │       ├── ServiceType.java
│   │   │       │       └── AppointmentStatus.java
│   │   │       ├── security/
│   │   │       │   ├── JwtTokenProvider.java
│   │   │       │   ├── JwtAuthenticationFilter.java
│   │   │       │   └── CustomUserDetailsService.java
│   │   │       ├── exception/
│   │   │       │   ├── GlobalExceptionHandler.java
│   │   │       │   ├── ResourceNotFoundException.java
│   │   │       │   ├── UnauthorizedException.java
│   │   │       │   └── BusinessException.java
│   │   │       └── util/
│   │   │           ├── DistanceCalculator.java
│   │   │           ├── MaskUtils.java
│   │   │           └── ValidationUtils.java
│   │   └── resources/
│   │       ├── application.yml
│   │       ├── application-dev.yml
│   │       ├── application-prod.yml
│   │       └── db/
│   │           └── migration/
│   │               ├── V1__create_users_table.sql
│   │               ├── V2__create_professionals_table.sql
│   │               ├── V3__create_services_table.sql
│   │               ├── V4__create_appointments_table.sql
│   │               └── V5__create_reviews_table.sql
│   └── test/
│       └── java/
│           └── com/tamarcado/
│               ├── controller/
│               ├── service/
│               └── repository/
├── docker/
│   ├── Dockerfile
│   └── docker-compose.yml
├── pom.xml
└── README.md
```

---

## 📝 TASKS PARA DESENVOLVIMENTO

## **SPRINT 1 - FUNDAÇÃO (2 semanas)**

### Backend - Infraestrutura Base

**TASK-BE-001: Setup do Projeto Backend**
- [ ] Criar projeto Spring Boot 3.2+ com Java 21
- [ ] Configurar Maven com dependências:
  - Spring Web
  - Spring Data JPA
  - Spring Security
  - PostgreSQL Driver
  - Redis
  - Lombok
  - MapStruct
  - JWT (jjwt)
  - Swagger/OpenAPI
  - Flyway
- [ ] Configurar profiles (dev, prod)
- [ ] Setup Docker Compose (PostgreSQL + Redis)
- [ ] Configurar application.yml
- **Estimativa:** 1 dia
- **Responsável:** Backend Lead

**TASK-BE-002: Configurar Segurança e JWT**
- [ ] Implementar SecurityConfig
- [ ] Criar JwtTokenProvider (geração e validação)
- [ ] Implementar JwtAuthenticationFilter
- [ ] Configurar CORS
- [ ] Implementar Rate Limiting
- [ ] Criar CustomUserDetailsService
- **Estimativa:** 2 dias
- **Responsável:** Backend Dev 1

**TASK-BE-003: Modelagem do Banco de Dados**
- [ ] Criar entidades JPA:
  - User
  - Professional
  - Service
  - Appointment
  - Review
  - Address
- [ ] Definir relacionamentos
- [ ] Criar enums (UserType, Category, ServiceType, AppointmentStatus)
- [ ] Criar migrations Flyway (V1 a V5)
- [ ] Adicionar índices para performance
- **Estimativa:** 2 dias
- **Responsável:** Backend Dev 2

**TASK-BE-004: Repositories**
- [ ] Criar UserRepository com queries customizadas
- [ ] Criar ProfessionalRepository com busca geográfica
- [ ] Criar ServiceRepository
- [ ] Criar AppointmentRepository com filtros
- [ ] Criar ReviewRepository
- [ ] Implementar especificações para queries dinâmicas
- **Estimativa:** 1 dia
- **Responsável:** Backend Dev 2

### Frontend - Setup Inicial

**TASK-FE-001: Setup do Projeto React Native**
- [ ] Criar projeto React Native 0.81.0
- [ ] Configurar TypeScript (opcional mas recomendado)
- [ ] Instalar dependências:
  - @react-navigation/native
  - @react-navigation/bottom-tabs
  - @react-navigation/stack
  - @reduxjs/toolkit
  - react-redux
  - axios
  - react-native-maps
  - @react-native-community/geolocation
  - react-native-firebase
  - react-native-paper (ou NativeBase)
  - react-native-vector-icons
- [ ] Configurar estrutura de pastas
- [ ] Setup ESLint e Prettier
- **Estimativa:** 1 dia
- **Responsável:** Frontend Lead

**TASK-FE-002: Configurar Navegação**
- [ ] Criar AppNavigator (root)
- [ ] Criar AuthNavigator (login, registro)
- [ ] Criar ClientNavigator (tabs + stacks)
- [ ] Criar ProfessionalNavigator (tabs + stacks)
- [ ] Implementar Bottom Tab Bar customizado com ícones SVG
- [ ] Configurar deep linking
- **Estimativa:** 2 dias
- **Responsável:** Frontend Dev 1

**TASK-FE-003: Theme e Design System**
- [ ] Criar theme (colors, typography, spacing)
- [ ] Implementar componentes base:
  - Button
  - Input (com máscaras)
  - Card
  - StarRating
  - Avatar
  - Badge
  - Loading
- [ ] Configurar ícones SVG customizados
- [ ] Criar constantes de cores (#1E3A8A, #4A5568, etc)
- **Estimativa:** 2 dias
- **Responsável:** Frontend Dev 2

**TASK-FE-004: Setup Redux e API Client**
- [ ] Configurar Redux Toolkit store
- [ ] Criar slices:
  - authSlice
  - appointmentsSlice
  - professionalsSlice
- [ ] Configurar Axios com interceptors
- [ ] Implementar refresh token automático
- [ ] Criar serviços de API (auth, appointments, etc)
- [ ] Implementar tratamento de erros global
- **Estimativa:** 2 dias
- **Responsável:** Frontend Dev 1

---

## **SPRINT 2 - AUTENTICAÇÃO (2 semanas)**

### Backend

**TASK-BE-005: Implementar Autenticação**
- [ ] Criar AuthController (register, login, refresh, logout)
- [ ] Implementar AuthService:
  - Cadastro de cliente
  - Cadastro de profissional
  - Login com JWT
  - Refresh token
  - Logout (invalidar token)
  - Forgot password
  - Reset password
- [ ] Validar dados de entrada com Bean Validation
- [ ] Criptografar senha com BCrypt
- [ ] Implementar envio de email (recuperação senha)
- [ ] Criar DTOs (Request/Response)
- [ ] Escrever testes unitários
- **Estimativa:** 3 dias
- **Responsável:** Backend Dev 1

**TASK-BE-006: Implementar Geocoding**
- [ ] Integrar com API de geocoding (Google Maps / ViaCEP)
- [ ] Criar GeocodingService
- [ ] Implementar conversão endereço → coordenadas
- [ ] Implementar busca de endereço por CEP
- [ ] Criar cache de geocoding no Redis
- [ ] Criar endpoints /geocoding/*
- **Estimativa:** 2 dias
- **Responsável:** Backend Dev 2

**TASK-BE-007: Implementar UserController**
- [ ] Criar UserController
- [ ] Implementar endpoints:
  - GET /users/me
  - PUT /users/me
  - PUT /users/me/password
  - PUT /users/me/photo (upload S3)
  - DELETE /users/me
- [ ] Implementar UserService
- [ ] Validar permissões (user só edita próprio perfil)
- [ ] Testes unitários e integração
- **Estimativa:** 2 días
- **Responsável:** Backend Dev 1

### Frontend

**TASK-FE-005: Implementar Telas de Login**
- [ ] Criar LoginScreen
- [ ] Implementar formulário com validação
- [ ] Conectar com Redux (authSlice)
- [ ] Implementar chamada de API
- [ ] Armazenar token no AsyncStorage
- [ ] Navegar para tela correta após login
- [ ] Implementar "Esqueceu senha?"
- [ ] Loading states
- **Estimativa:** 2 dias
- **Responsável:** Frontend Dev 1

**TASK-FE-006: Implementar Cadastro de Cliente**
- [ ] Criar RegisterClientScreen
- [ ] Implementar formulário multi-step (dados + endereço)
- [ ] Adicionar máscaras (telefone, CEP)
- [ ] Validações em tempo real
- [ ] Integrar busca de CEP
- [ ] Conectar com API de registro
- [ ] Navegar para login após sucesso
- **Estimativa:** 2 dias
- **Responsável:** Frontend Dev 2

**TASK-FE-007: Implementar Cadastro de Profissional**
- [ ] Criar RegisterProfessionalScreen
- [ ] Implementar formulário multi-step:
  - Dados pessoais
  - Endereço
  - Categoria e tipo
  - Serviços
- [ ] Dropdown dinâmico de categorias/tipos
- [ ] Adicionar/remover serviços dinamicamente
- [ ] Validações completas
- [ ] Conectar com API
- **Estimativa:** 3 dias
- **Responsável:** Frontend Dev 2

**TASK-FE-008: Implementar Perfil do Usuário**
- [ ] Criar ProfileScreen (cliente e profissional)
- [ ] Exibir dados do usuário
- [ ] Implementar edição de perfil
- [ ] Upload de foto (câmera ou galeria)
- [ ] Mostrar endereço com aviso "privado"
- [ ] Implementar logout
- [ ] Para profissional: mostrar gerenciamento de serviços
- **Estimativa:** 2 dias
- **Responsável:** Frontend Dev 1

---

## **SPRINT 3 - BUSCA E PROFISSIONAIS (2 semanas)**

### Backend

**TASK-BE-008: Implementar Busca de Serviços**
- [ ] Criar SearchController
- [ ] Implementar GET /search/services
- [ ] Buscar serviços por categoria e tipo
- [ ] Agrupar serviços únicos
- [ ] Calcular minPrice e maxPrice
- [ ] Contar profissionais por serviço
- [ ] Implementar cache Redis
- [ ] Testes
- **Estimativa:** 2 dias
- **Responsável:** Backend Dev 2

**TASK-BE-009: Implementar Busca de Profissionais**
- [ ] Implementar GET /search/professionals
- [ ] Buscar profissionais por serviço
- [ ] Calcular distância geográfica (Haversine)
- [ ] Ordenar por distância ou rating
- [ ] Implementar paginação
- [ ] Filtrar apenas profissionais ativos
- [ ] Otimizar query com índices espaciais
- [ ] Testes de performance
- **Estimativa:** 3 dias
- **Responsável:** Backend Dev 2

**TASK-BE-010: Implementar ProfessionalController**
- [ ] Criar ProfessionalController
- [ ] Implementar GET /professionals/{id}
- [ ] Incluir serviços do profissional
- [ ] Incluir avaliações (últimas 10)
- [ ] Calcular distância do cliente
- [ ] Implementar cache
- [ ] GET /professionals/me/services
- [ ] POST /professionals/me/services
- [ ] PUT /professionals/me/services/{id}
- [ ] DELETE /professionals/me/services/{id}
- [ ] Validar: não excluir serviço com agendamento ativo
- [ ] Testes
- **Estimativa:** 3 dias
- **Responsável:** Backend Dev 1

### Frontend

**TASK-FE-009: Implementar Home do Cliente - Busca 3 Etapas**
- [ ] Criar HomeScreen (cliente)
- [ ] Implementar Etapa 1: Dropdown de categorias + lista de tipos
- [ ] Adicionar ícones para cada tipo de serviço
- [ ] Implementar Etapa 2: Lista de serviços específicos
- [ ] Implementar Etapa 3: Lista de profissionais
- [ ] Adicionar filtros (distância vs estrelas)
- [ ] Implementar ProfessionalCard component
- [ ] Loading e estados vazios
- **Estimativa:** 3 dias
- **Responsável:** Frontend Dev 1

**TASK-FE-010: Implementar Detalhes do Profissional**
- [ ] Criar ProfessionalDetailScreen
- [ ] Mostrar foto, nome, rating
- [ ] Mostrar distância e cidade/estado (não endereço completo)
- [ ] Listar todos os serviços com preços
- [ ] Mostrar avaliações
- [ ] Botão "Agendar Serviço"
- [ ] Pull to refresh
- **Estimativa:** 2 dias
- **Responsável:** Frontend Dev 2

**TASK-FE-011: Implementar Geolocalização**
- [ ] Criar useGeolocation hook
- [ ] Solicitar permissões de localização
- [ ] Obter localização atual do usuário
- [ ] Passar coordenadas para busca de profissionais
- [ ] Implementar fallback se localização negada
- [ ] Tratamento de erros
- **Estimativa:** 1 dia
- **Responsável:** Frontend Dev 1

---

## **SPRINT 4 - AGENDAMENTOS (2 semanas)**

### Backend

**TASK-BE-011: Implementar AppointmentController - Cliente**
- [ ] Criar AppointmentController
- [ ] POST /appointments (criar agendamento)
- [ ] Validar:
  - Data não pode ser passada
  - Profissional e serviço existem
  - Serviço pertence ao profissional
- [ ] Criar agendamento com status PENDING
- [ ] GET /appointments/client (listar por status)
- [ ] GET /appointments/{id}
- [ ] DELETE /appointments/{id} (cancelar se PENDING)
- [ ] Implementar AppointmentService
- [ ] Testes
- **Estimativa:** 3 dias
- **Responsável:** Backend Dev 1

**TASK-BE-012: Implementar Gerenciamento de Agendamentos - Profissional**
- [ ] GET /appointments/professional (listar por status)
- [ ] PUT /appointments/{id}/accept
- [ ] PUT /appointments/{id}/reject
- [ ] PUT /appointments/{id}/complete
- [ ] Validar permissões (profissional só gerencia seus agendamentos)
- [ ] Calcular distância até cliente
- [ ] Atualizar status corretamente
- [ ] Enviar notificações ao cliente
- [ ] Testes
- **Estimativa:** 2 dias
- **Responsável:** Backend Dev 1

**TASK-BE-013: Implementar Sistema de Notificações**
- [ ] Criar NotificationController
- [ ] POST /notifications/register-device
- [ ] Integrar Firebase Cloud Messaging
- [ ] Criar NotificationService
- [ ] Implementar envio de notificações:
  - Novo agendamento (profissional)
  - Agendamento aceito (cliente)
  - Agendamento recusado (cliente)
  - Lembrete de agendamento
- [ ] GET /notifications (listar)
- [ ] PUT /notifications/{id}/read
- [ ] Armazenar notificações no BD
- [ ] Testes
- **Estimativa:** 3 dias
- **Responsável:** Backend Dev 2

### Frontend

**TASK-FE-012: Implementar Tela de Agendamento**
- [ ] Criar BookingScreen
- [ ] Mostrar profissional selecionado
- [ ] Dropdown de serviços (pré-selecionado da busca)
- [ ] Date picker para data
- [ ] Time picker para horário
- [ ] Campo de observações
- [ ] Mostrar resumo (preço total)
- [ ] Validar data/horário
- [ ] Conectar com API
- [ ] Mostrar confirmação
- [ ] Navegar para Agendamentos
- **Estimativa:** 2 dias
- **Responsável:** Frontend Dev 2

**TASK-FE-013: Implementar Lista de Agendamentos - Cliente**
- [ ] Criar AppointmentsScreen (cliente)
- [ ] Implementar tabs: Pendentes, Confirmados, Concluídos, Cancelados
- [ ] Criar AppointmentCard component
- [ ] Mostrar status com cores
- [ ] Implementar cancelamento (se pendente)
- [ ] Botão "Avaliar" se concluído
- [ ] Pull to refresh
- [ ] Loading states
- **Estimativa:** 2 dias
- **Responsável:** Frontend Dev 1

**TASK-FE-014: Implementar Agendamentos - Profissional**
- [ ] Criar AppointmentsScreen (profissional)
- [ ] Implementar tabs por status
- [ ] Mostrar dados do cliente (nome, telefone, distância)
- [ ] Não mostrar endereço completo do cliente
- [ ] Botões Aceitar/Recusar (se pendente)
- [ ] Botão "Concluir" (se aceito)
- [ ] Modal de confirmação para ações
- [ ] Atualizar lista após ação
- **Estimativa:** 2 dias
- **Responsável:** Frontend Dev 2

**TASK-FE-015: Implementar Notificações Push**
- [ ] Configurar Firebase Cloud Messaging
- [ ] Solicitar permissões de notificação
- [ ] Registrar device token no backend
- [ ] Implementar useNotifications hook
- [ ] Lidar com notificações em foreground
- [ ] Lidar com notificações em background
- [ ] Navegar para tela correta ao clicar
- [ ] Mostrar badge de notificações não lidas
- **Estimativa:** 2 dias
- **Responsável:** Frontend Dev 1

---

## **SPRINT 5 - AVALIAÇÕES E DASHBOARD (2 semanas)**

### Backend

**TASK-BE-014: Implementar Sistema de Avaliações**
- [ ] Criar ReviewController
- [ ] POST /reviews (criar avaliação)
- [ ] Validar:
  - Agendamento está COMPLETED
  - Cliente só avalia próprio agendamento
  - Ainda não avaliado
  - Rating entre 1 e 5
- [ ] Atualizar média do profissional automaticamente
- [ ] GET /professionals/{id}/reviews (com paginação)
- [ ] GET /reviews/client/me (avaliações feitas)
- [ ] Implementar ReviewService
- [ ] Testes
- **Estimativa:** 2 dias
- **Responsável:** Backend Dev 2

**TASK-BE-015: Implementar Dashboard - Profissional**
- [ ] Criar endpoint GET /dashboard/professional/stats
- [ ] Calcular estatísticas:
  - Agendamentos hoje
  - Agendamentos pendentes
  - Média de avaliação
  - Total de avaliações
  - Receita do mês (opcional)
  - Serviços concluídos no mês
- [ ] Implementar cache Redis (TTL 5 minutos)
- [ ] Otimizar queries
- [ ] Testes
- **Estimativa:** 2 dias
- **Responsável:** Backend Dev 1

**TASK-BE-016: Implementar Dashboard - Cliente**
- [ ] Criar endpoint GET /dashboard/client/stats
- [ ] Calcular estatísticas:
  - Próximos agendamentos
  - Agendamentos concluídos
  - Categoria favorita
- [ ] Cache Redis
- [ ] Testes
- **Estimativa:** 1 dia
- **Responsável:** Backend Dev 1

### Frontend

**TASK-FE-016: Implementar Avaliação de Serviço**
- [ ] Criar ReviewScreen
- [ ] Componente StarRating interativo
- [ ] Campo de comentário
- [ ] Validar rating obrigatório
- [ ] Conectar com API
- [ ] Mostrar confirmação
- [ ] Atualizar lista de agendamentos
- **Estimativa:** 1 dia
- **Responsável:** Frontend Dev 2

**TASK-FE-017: Implementar Dashboard - Profissional**
- [ ] Criar DashboardScreen (profissional)
- [ ] Mostrar cards de estatísticas
- [ ] Lista de serviços cadastrados
- [ ] Botão para adicionar serviço
- [ ] Últimas avaliações recebidas
- [ ] Pull to refresh
- [ ] Loading skeleton
- **Estimativa:** 2 dias
- **Responsável:** Frontend Dev 2

**TASK-FE-018: Implementar Gerenciamento de Serviços**
- [ ] No ProfileScreen do profissional
- [ ] Lista de serviços com botões editar/excluir
- [ ] Modal para adicionar serviço
- [ ] Modal para editar serviço
- [ ] Confirmação antes de excluir
- [ ] Validar: não excluir se houver agendamentos ativos
- [ ] Atualizar lista após alterações
- **Estimativa:** 2 dias
- **Responsável:** Frontend Dev 1

**TASK-FE-019: Polimento de UI/UX**
- [ ] Revisar todas as telas
- [ ] Adicionar animações de transição
- [ ] Melhorar feedback visual
- [ ] Implementar loading skeletons
- [ ] Ajustar espaçamentos e cores
- [ ] Testar responsividade (diferentes tamanhos)
- [ ] Melhorar tratamento de erros
- [ ] Adicionar mensagens de sucesso/erro com toasts
- **Estimativa:** 3 dias
- **Responsável:** Frontend Dev 1 e 2

---

## **SPRINT 6 - TESTES E OTIMIZAÇÕES (2 semanas)**

### Backend

**TASK-BE-017: Testes de Integração**
- [ ] Escrever testes de integração para todos os endpoints
- [ ] Usar TestContainers para PostgreSQL
- [ ] Testar fluxos completos:
  - Cadastro → Login → Busca → Agendamento
  - Profissional gerencia agendamento
  - Cliente avalia
- [ ] Cobertura mínima de 80%
- **Estimativa:** 3 dias
- **Responsável:** Backend Dev 1 e 2

**TASK-BE-018: Performance e Otimizações**
- [ ] Adicionar índices faltantes no BD
- [ ] Implementar paginação em todos os endpoints
- [ ] Otimizar queries N+1
- [ ] Configurar cache Redis estratégico
- [ ] Implementar compressão de respostas
- [ ] Configurar connection pool
- [ ] Realizar testes de carga (JMeter/Gatling)
- **Estimativa:** 2 dias
- **Responsável:** Backend Lead

**TASK-BE-019: Documentação e Deploy**
- [ ] Completar documentação Swagger
- [ ] Criar README completo
- [ ] Documentar variáveis de ambiente
- [ ] Criar guia de instalação local
- [ ] Configurar CI/CD (GitHub Actions)
- [ ] Configurar deploy (AWS/GCP)
- [ ] Configurar monitoramento (Prometheus/Grafana)
- [ ] Setup logs centralizados
- **Estimativa:** 3 dias
- **Responsável:** DevOps / Backend Lead

### Frontend

**TASK-FE-020: Testes E2E**
- [ ] Configurar Detox para testes E2E
- [ ] Escrever testes para fluxos principais:
  - Cadastro e login
  - Busca de profissional
  - Criar agendamento
  - Profissional aceita agendamento
  - Cliente avalia
- [ ] Testes em Android e iOS
- **Estimativa:** 3 dias
- **Responsável:** Frontend Dev 1

**TASK-FE-021: Otimizações de Performance**
- [ ] Implementar React.memo onde necessário
- [ ] Otimizar re-renders
- [ ] Implementar lazy loading de imagens
- [ ] Reduzir tamanho do bundle
- [ ] Implementar cache de imagens
- [ ] Otimizar FlatLists (virtualization)
- [ ] Profiling e correção de memory leaks
- **Estimativa:** 2 dias
- **Responsável:** Frontend Lead

**TASK-FE-022: Build e Deploy**
- [ ] Configurar build de produção (Android)
- [ ] Configurar build de produção (iOS)
- [ ] Gerar ícones e splash screens
- [ ] Configurar CodePush (atualizações OTA)
- [ ] Preparar assets para stores
- [ ] Criar screenshots para stores
- [ ] Escrever descrições das stores
- [ ] Deploy beta (TestFlight / Play Console)
- **Estimativa:** 2 dias
- **Responsável:** Frontend Lead

---

## **SPRINT 7 - LANÇAMENTO (1 semana)**

**TASK-FINAL-001: Testes de QA**
- [ ] Testes manuais completos
- [ ] Testes em dispositivos reais
- [ ] Testes de usabilidade
- [ ] Correção de bugs críticos
- [ ] Validação de segurança
- **Estimativa:** 3 dias
- **Responsável:** QA Team

**TASK-FINAL-002: Preparação para Lançamento**
- [ ] Deploy produção backend
- [ ] Configurar domínio e SSL
- [ ] Deploy app nas stores
- [ ] Configurar analytics (Firebase/Amplitude)
- [ ] Configurar crash reporting (Sentry)
- [ ] Preparar documentação de suporte
- [ ] Criar FAQs
- **Estimativa:** 2 dias
- **Responsável:** DevOps + Leads

---

## 📊 RESUMO DO CRONOGRAMA

| Sprint | Duração | Foco | Entregas |
|--------|---------|------|----------|
| Sprint 1 | 2 semanas | Fundação | Setup, DB, Navegação, Design System |
| Sprint 2 | 2 semanas | Autenticação | Login, Cadastros, Perfil, Geocoding |
| Sprint 3 | 2 semanas | Busca | Search 3 etapas, Profissionais, Geolocalização |
| Sprint 4 | 2 semanas | Agendamentos | Criar, Gerenciar, Notificações |
| Sprint 5 | 2 semanas | Avaliações | Reviews, Dashboard, UI Polish |
| Sprint 6 | 2 semanas | Qualidade | Testes, Performance, Deploy |
| Sprint 7 | 1 semana | Lançamento | QA Final, Prod Deploy |

**TOTAL: 13 semanas (~3 meses)**

---

## 👥 EQUIPE RECOMENDADA

- **1 Backend Lead** (Java/Spring)
- **2 Backend Developers** (Java/Spring)
- **1 Frontend Lead** (React Native)
- **2 Frontend Developers** (React Native)
- **1 DevOps Engineer**
- **1 QA Engineer**
- **1 Product Owner**
- **1 UI/UX Designer** (part-time)

---

## 🔧 FERRAMENTAS E SERVIÇOS

### Desenvolvimento
- **IDE**: IntelliJ IDEA (backend), VS Code (frontend)
- **Versionamento**: Git + GitHub/GitLab
- **Comunicação**: Slack/Discord
- **Gestão**: Jira/Linear/Trello

### Infraestrutura
- **Hospedagem Backend**: AWS EC2 / Google Cloud Run
- **Banco de Dados**: AWS RDS PostgreSQL / Google Cloud SQL
- **Cache**: AWS ElastiCache Redis / Google Memorystore
- **Storage**: AWS S3 / Google Cloud Storage
- **CDN**: CloudFlare
- **Notificações**: Firebase Cloud Messaging

### Monitoramento
- **APM**: New Relic / Datadog
- **Logs**: ELK Stack / AWS CloudWatch
- **Crash Reporting**: Sentry
- **Analytics**: Google Analytics / Amplitude

---

## 💰 ESTIMATIVA DE CUSTOS (Mensal)

### Infraestrutura
- **Servidor Backend**: ~$50-100/mês
- **Banco de Dados**: ~$30-80/mês
- **Redis Cache**: ~$20-50/mês
- **Storage (S3)**: ~$10-30/mês
- **CDN**: ~$10-20/mês
- **Firebase**: Gratuito até 1M mensagens/mês
- **Total**: ~$120-280/mês (início)

### Serviços
- **Google Maps API**: ~$200/mês (após tier gratuito)
- **SendGrid (emails)**: Gratuito até 100/dia
- **Monitoring**: ~$50-100/mês
- **Total Serviços**: ~$250-300/mês

**TOTAL MENSAL**: ~$370-580/mês

---

## 📈 MÉTRICAS DE SUCESSO

### Performance
- Tempo de resposta API: < 200ms (p95)
- Tempo de carregamento app: < 3s
- Taxa de erro: < 0.1%
- Uptime: > 99.9%

### Negócio
- Taxa de conversão cadastro: > 60%
- Taxa de conclusão de agendamento: > 70%
- Avaliação média: > 4.5 estrelas
- Retenção 30 dias: > 40%

---

**Documento criado em:** 2026-01-20
**Versão:** 1.0
**Projeto:** Tá Marcado! - Implementation Plan
