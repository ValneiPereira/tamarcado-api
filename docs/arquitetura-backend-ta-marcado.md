# TÁ MARCADO! - ARQUITETURA DO BACKEND

## 🏗️ ARQUITETURA GERAL

### Padrão: **Clean Architecture + Hexagonal Architecture (Ports & Adapters)**

Vamos usar uma **arquitetura em camadas** que separa claramente as responsabilidades e garante:
- ✅ Testabilidade
- ✅ Manutenibilidade
- ✅ Escalabilidade
- ✅ Independência de frameworks
- ✅ Separação de concerns

---

## 📐 CAMADAS DA ARQUITETURA

```
┌─────────────────────────────────────────────────────────────┐
│                     PRESENTATION LAYER                       │
│              (Controllers, DTOs, Mappers)                    │
│  - REST Controllers                                          │
│  - Request/Response DTOs                                     │
│  - Input Validation                                          │
│  - Exception Handlers                                        │
└─────────────────────────────────────────────────────────────┘
                            ↓↑
┌─────────────────────────────────────────────────────────────┐
│                     APPLICATION LAYER                        │
│                  (Use Cases / Services)                      │
│  - Business Logic                                            │
│  - Orchestration                                             │
│  - Transaction Management                                    │
│  - Security                                                  │
└─────────────────────────────────────────────────────────────┘
                            ↓↑
┌─────────────────────────────────────────────────────────────┐
│                       DOMAIN LAYER                           │
│              (Entities, Value Objects, Rules)                │
│  - Domain Entities                                           │
│  - Business Rules                                            │
│  - Domain Services                                           │
│  - Domain Events                                             │
└─────────────────────────────────────────────────────────────┘
                            ↓↑
┌─────────────────────────────────────────────────────────────┐
│                   INFRASTRUCTURE LAYER                       │
│           (Repositories, External Services)                  │
│  - Database Access (JPA)                                     │
│  - External APIs                                             │
│  - File Storage (S3)                                         │
│  - Cache (Redis)                                             │
│  - Messaging                                                 │
└─────────────────────────────────────────────────────────────┘
```

---

## 🎯 ESTRUTURA DE PACOTES

```java
com.tamarcado
├── TaMarcadoApplication.java
│
├── adapter/                              // ADAPTERS (Hexagonal)
│   ├── in/                              // Input Adapters
│   │   └── rest/                        // REST Controllers
│   │       ├── AuthController.java
│   │       ├── UserController.java
│   │       ├── ProfessionalController.java
│   │       ├── AppointmentController.java
│   │       ├── ReviewController.java
│   │       └── SearchController.java
│   │
│   └── out/                             // Output Adapters
│       ├── persistence/                 // Database
│       │   ├── jpa/
│       │   │   ├── UserJpaRepository.java
│       │   │   ├── AppointmentJpaRepository.java
│       │   │   └── ...
│       │   └── impl/
│       │       ├── UserRepositoryImpl.java
│       │       └── ...
│       │
│       ├── cache/                       // Redis
│       │   └── RedisCacheAdapter.java
│       │
│       ├── storage/                     // S3
│       │   └── S3StorageAdapter.java
│       │
│       ├── messaging/                   // Firebase/SQS
│       │   └── FirebaseNotificationAdapter.java
│       │
│       └── external/                    // APIs Externas
│           ├── GeocodingAdapter.java
│           └── EmailAdapter.java
│
├── application/                          // APPLICATION LAYER
│   ├── usecase/                         // Use Cases
│   │   ├── auth/
│   │   │   ├── RegisterClientUseCase.java
│   │   │   ├── RegisterProfessionalUseCase.java
│   │   │   ├── LoginUseCase.java
│   │   │   └── RefreshTokenUseCase.java
│   │   │
│   │   ├── appointment/
│   │   │   ├── CreateAppointmentUseCase.java
│   │   │   ├── AcceptAppointmentUseCase.java
│   │   │   ├── RejectAppointmentUseCase.java
│   │   │   ├── CompleteAppointmentUseCase.java
│   │   │   └── ListAppointmentsUseCase.java
│   │   │
│   │   ├── search/
│   │   │   ├── SearchServicesUseCase.java
│   │   │   └── SearchProfessionalsUseCase.java
│   │   │
│   │   └── review/
│   │       ├── CreateReviewUseCase.java
│   │       └── ListReviewsUseCase.java
│   │
│   ├── service/                         // Application Services
│   │   ├── AuthService.java
│   │   ├── UserService.java
│   │   ├── AppointmentService.java
│   │   ├── NotificationService.java
│   │   └── GeocodingService.java
│   │
│   └── port/                            // Ports (Interfaces)
│       ├── in/                          // Input Ports
│       │   ├── RegisterClientPort.java
│       │   ├── CreateAppointmentPort.java
│       │   └── ...
│       │
│       └── out/                         // Output Ports
│           ├── UserRepositoryPort.java
│           ├── AppointmentRepositoryPort.java
│           ├── NotificationPort.java
│           ├── StoragePort.java
│           └── GeocodingPort.java
│
├── domain/                               // DOMAIN LAYER
│   ├── model/                           // Domain Entities
│   │   ├── user/
│   │   │   ├── User.java
│   │   │   ├── Client.java
│   │   │   ├── Professional.java
│   │   │   └── Address.java
│   │   │
│   │   ├── appointment/
│   │   │   ├── Appointment.java
│   │   │   └── AppointmentStatus.java
│   │   │
│   │   ├── service/
│   │   │   ├── ServiceOffering.java
│   │   │   ├── Category.java
│   │   │   └── ServiceType.java
│   │   │
│   │   └── review/
│   │       └── Review.java
│   │
│   ├── valueobject/                     // Value Objects
│   │   ├── Email.java
│   │   ├── Phone.java
│   │   ├── Rating.java
│   │   ├── Coordinates.java
│   │   └── Money.java
│   │
│   ├── exception/                       // Domain Exceptions
│   │   ├── DomainException.java
│   │   ├── UserNotFoundException.java
│   │   ├── InvalidAppointmentException.java
│   │   └── ...
│   │
│   └── service/                         // Domain Services
│       ├── DistanceCalculator.java
│       ├── AppointmentValidator.java
│       └── RatingCalculator.java
│
├── infrastructure/                       // INFRASTRUCTURE LAYER
│   ├── config/                          // Configurações
│   │   ├── SecurityConfig.java
│   │   ├── SwaggerConfig.java
│   │   ├── RedisConfig.java
│   │   ├── JpaConfig.java
│   │   ├── AsyncConfig.java
│   │   └── CorsConfig.java
│   │
│   ├── security/                        // Segurança
│   │   ├── JwtTokenProvider.java
│   │   ├── JwtAuthenticationFilter.java
│   │   ├── CustomUserDetailsService.java
│   │   └── SecurityUtils.java
│   │
│   ├── persistence/                     // Persistence Entities
│   │   ├── entity/
│   │   │   ├── UserEntity.java
│   │   │   ├── ProfessionalEntity.java
│   │   │   ├── AppointmentEntity.java
│   │   │   ├── ServiceEntity.java
│   │   │   └── ReviewEntity.java
│   │   │
│   │   └── mapper/                      // Entity <-> Domain Mappers
│   │       ├── UserMapper.java
│   │       ├── AppointmentMapper.java
│   │       └── ...
│   │
│   └── messaging/                       // Mensageria
│       ├── event/
│       │   ├── AppointmentCreatedEvent.java
│       │   ├── AppointmentAcceptedEvent.java
│       │   └── ...
│       │
│       └── listener/
│           └── AppointmentEventListener.java
│
├── shared/                              // SHARED (Cross-cutting)
│   ├── dto/                            // DTOs compartilhados
│   │   ├── request/
│   │   │   ├── LoginRequest.java
│   │   │   ├── RegisterClientRequest.java
│   │   │   └── CreateAppointmentRequest.java
│   │   │
│   │   └── response/
│   │       ├── AuthResponse.java
│   │       ├── UserResponse.java
│   │       ├── AppointmentResponse.java
│   │       └── ApiResponse.java
│   │
│   ├── mapper/                         // DTO Mappers
│   │   ├── UserDtoMapper.java
│   │   ├── AppointmentDtoMapper.java
│   │   └── ...
│   │
│   ├── exception/                      // Exception Handling
│   │   ├── GlobalExceptionHandler.java
│   │   ├── ApiException.java
│   │   ├── ResourceNotFoundException.java
│   │   ├── UnauthorizedException.java
│   │   ├── ValidationException.java
│   │   └── BusinessException.java
│   │
│   ├── util/                           // Utilitários
│   │   ├── DateUtils.java
│   │   ├── StringUtils.java
│   │   ├── MaskUtils.java
│   │   └── ValidationUtils.java
│   │
│   └── constant/                       // Constantes
│       ├── ApiConstants.java
│       ├── ErrorMessages.java
│       └── SecurityConstants.java
│
└── resources/
    ├── application.yml
    ├── application-dev.yml
    ├── application-prod.yml
    └── db/
        └── migration/
            ├── V1__create_users_table.sql
            ├── V2__create_professionals_table.sql
            └── ...
```

---

## 🔧 PADRÕES E PRÁTICAS

### 1. **Clean Architecture Principles**

#### Regra de Dependência
```
Presentation → Application → Domain ← Infrastructure
                                ↑
                         (Dependencies Point Inward)
```

- **Domain Layer**: Não depende de nada (core business)
- **Application Layer**: Depende apenas do Domain
- **Infrastructure**: Depende de Domain e Application
- **Presentation**: Depende de Application

### 2. **CQRS (Command Query Responsibility Segregation)**

Separar comandos (write) de queries (read):

```java
// COMMAND - Altera estado
public interface CreateAppointmentPort {
    Appointment execute(CreateAppointmentCommand command);
}

// QUERY - Apenas leitura
public interface ListAppointmentsPort {
    List<Appointment> execute(ListAppointmentsQuery query);
}
```

### 3. **Domain-Driven Design (DDD)**

#### Agregados
```java
// Appointment é um agregado raiz
@Entity
public class Appointment {
    @Id
    private UUID id;
    
    // Aggregate root mantém consistência
    public void accept() {
        if (this.status != AppointmentStatus.PENDING) {
            throw new InvalidAppointmentException("Only pending appointments can be accepted");
        }
        this.status = AppointmentStatus.ACCEPTED;
        this.acceptedAt = LocalDateTime.now();
        
        // Domain Event
        DomainEventPublisher.publish(new AppointmentAcceptedEvent(this.id));
    }
}
```

#### Value Objects
```java
// Email como Value Object
public record Email(String value) {
    public Email {
        if (!isValid(value)) {
            throw new IllegalArgumentException("Invalid email");
        }
    }
    
    private static boolean isValid(String email) {
        return email.matches("^[A-Za-z0-9+_.-]+@(.+)$");
    }
}
```

### 4. **Repository Pattern**

```java
// Port (Interface no domain)
public interface AppointmentRepositoryPort {
    Appointment save(Appointment appointment);
    Optional<Appointment> findById(UUID id);
    List<Appointment> findByClientId(UUID clientId);
    List<Appointment> findByProfessionalId(UUID professionalId);
}

// Adapter (Implementação na infrastructure)
@Component
public class AppointmentRepositoryAdapter implements AppointmentRepositoryPort {
    private final AppointmentJpaRepository jpaRepository;
    private final AppointmentMapper mapper;
    
    @Override
    public Appointment save(Appointment appointment) {
        AppointmentEntity entity = mapper.toEntity(appointment);
        AppointmentEntity saved = jpaRepository.save(entity);
        return mapper.toDomain(saved);
    }
}
```

### 5. **Use Case Pattern**

Cada use case é uma classe com responsabilidade única:

```java
@Service
@RequiredArgsConstructor
public class CreateAppointmentUseCase {
    private final AppointmentRepositoryPort appointmentRepository;
    private final UserRepositoryPort userRepository;
    private final NotificationPort notificationPort;
    private final AppointmentValidator validator;
    
    @Transactional
    public Appointment execute(CreateAppointmentCommand command) {
        // 1. Validar
        validator.validate(command);
        
        // 2. Buscar entidades necessárias
        Client client = userRepository.findClientById(command.clientId())
            .orElseThrow(() -> new ResourceNotFoundException("Client not found"));
            
        Professional professional = userRepository.findProfessionalById(command.professionalId())
            .orElseThrow(() -> new ResourceNotFoundException("Professional not found"));
        
        // 3. Criar agregado
        Appointment appointment = Appointment.create(
            client,
            professional,
            command.serviceId(),
            command.date(),
            command.time(),
            command.notes()
        );
        
        // 4. Persistir
        Appointment saved = appointmentRepository.save(appointment);
        
        // 5. Notificar
        notificationPort.notifyProfessional(professional, saved);
        
        // 6. Retornar
        return saved;
    }
}
```

### 6. **Dependency Injection**

Usar construtor injection com Lombok:

```java
@Service
@RequiredArgsConstructor // Lombok gera construtor
public class UserService {
    private final UserRepositoryPort userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider tokenProvider;
}
```

### 7. **Exception Handling**

Hierarquia de exceções:

```java
// Base
public abstract class TaMarcadoException extends RuntimeException {
    private final ErrorCode errorCode;
}

// Domain Exceptions
public class AppointmentException extends TaMarcadoException {
    public AppointmentException(String message) {
        super(ErrorCode.APPOINTMENT_ERROR, message);
    }
}

// Global Handler
@RestControllerAdvice
public class GlobalExceptionHandler {
    
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiError> handleNotFound(ResourceNotFoundException ex) {
        return ResponseEntity
            .status(HttpStatus.NOT_FOUND)
            .body(new ApiError(ex.getMessage()));
    }
    
    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<ApiError> handleValidation(ValidationException ex) {
        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(new ApiError(ex.getMessage(), ex.getErrors()));
    }
}
```

### 8. **DTOs e Mappers**

Usar MapStruct para conversões:

```java
@Mapper(componentModel = "spring")
public interface UserDtoMapper {
    
    UserResponse toResponse(User user);
    
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    User toEntity(RegisterClientRequest request);
    
    List<UserResponse> toResponseList(List<User> users);
}
```

### 9. **Validação**

Bean Validation nas DTOs:

```java
public record CreateAppointmentRequest(
    @NotNull(message = "Professional ID is required")
    UUID professionalId,
    
    @NotNull(message = "Service ID is required")
    UUID serviceId,
    
    @NotNull(message = "Date is required")
    @Future(message = "Date must be in the future")
    LocalDate date,
    
    @NotNull(message = "Time is required")
    LocalTime time,
    
    @Size(max = 500, message = "Notes cannot exceed 500 characters")
    String notes
) {}
```

### 10. **Transações**

```java
@Service
@RequiredArgsConstructor
public class AppointmentService {
    
    @Transactional // Transação gerenciada automaticamente
    public Appointment acceptAppointment(UUID id, UUID professionalId) {
        Appointment appointment = findById(id);
        appointment.accept(professionalId);
        
        // Se lançar exceção, faz rollback automático
        notificationService.notifyClient(appointment);
        
        return appointmentRepository.save(appointment);
    }
    
    @Transactional(readOnly = true) // Otimização para leitura
    public List<Appointment> findByProfessional(UUID professionalId) {
        return appointmentRepository.findByProfessionalId(professionalId);
    }
}
```

---

## 🔐 SEGURANÇA

### JWT Authentication Flow

```java
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    
    private final JwtTokenProvider tokenProvider;
    private final CustomUserDetailsService userDetailsService;
    
    @Override
    protected void doFilterInternal(
        HttpServletRequest request,
        HttpServletResponse response,
        FilterChain filterChain
    ) throws ServletException, IOException {
        
        String token = extractToken(request);
        
        if (token != null && tokenProvider.validateToken(token)) {
            String userId = tokenProvider.getUserIdFromToken(token);
            UserDetails userDetails = userDetailsService.loadUserById(userId);
            
            UsernamePasswordAuthenticationToken authentication = 
                new UsernamePasswordAuthenticationToken(
                    userDetails, 
                    null, 
                    userDetails.getAuthorities()
                );
                
            SecurityContextHolder.getContext().setAuthentication(authentication);
        }
        
        filterChain.doFilter(request, response);
    }
}
```

### Role-Based Access Control

```java
@RestController
@RequestMapping("/api/v1/appointments")
@RequiredArgsConstructor
public class AppointmentController {
    
    @PostMapping
    @PreAuthorize("hasRole('CLIENT')") // Apenas clientes
    public ResponseEntity<AppointmentResponse> create(
        @Valid @RequestBody CreateAppointmentRequest request,
        @AuthenticationPrincipal UserPrincipal user
    ) {
        // ...
    }
    
    @PutMapping("/{id}/accept")
    @PreAuthorize("hasRole('PROFESSIONAL')") // Apenas profissionais
    public ResponseEntity<AppointmentResponse> accept(
        @PathVariable UUID id,
        @AuthenticationPrincipal UserPrincipal user
    ) {
        // ...
    }
}
```

---

## 🚀 PERFORMANCE

### 1. **Caching Strategy**

```java
@Service
@RequiredArgsConstructor
public class ProfessionalService {
    
    private final RedisTemplate<String, Professional> redisTemplate;
    
    @Cacheable(value = "professionals", key = "#id")
    public Professional findById(UUID id) {
        return professionalRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Professional not found"));
    }
    
    @CacheEvict(value = "professionals", key = "#professional.id")
    public Professional update(Professional professional) {
        return professionalRepository.save(professional);
    }
}
```

### 2. **Paginação**

```java
@GetMapping("/search")
public ResponseEntity<Page<ProfessionalResponse>> search(
    @RequestParam UUID serviceId,
    @RequestParam Double lat,
    @RequestParam Double lng,
    @RequestParam(defaultValue = "distance") String sortBy,
    @PageableDefault(size = 20) Pageable pageable
) {
    Page<Professional> professionals = searchService.search(
        serviceId, lat, lng, sortBy, pageable
    );
    
    return ResponseEntity.ok(
        professionals.map(professionalMapper::toResponse)
    );
}
```

### 3. **Queries Otimizadas**

```java
public interface ProfessionalRepository extends JpaRepository<Professional, UUID> {
    
    // Fetch join para evitar N+1
    @Query("""
        SELECT DISTINCT p FROM Professional p
        LEFT JOIN FETCH p.services s
        LEFT JOIN FETCH p.reviews r
        WHERE p.id = :id
    """)
    Optional<Professional> findByIdWithDetails(@Param("id") UUID id);
    
    // Query nativa para busca geográfica
    @Query(value = """
        SELECT p.*, 
               ST_Distance(
                   ST_MakePoint(p.longitude, p.latitude)::geography,
                   ST_MakePoint(:lng, :lat)::geography
               ) / 1000 as distance
        FROM professionals p
        INNER JOIN services s ON s.professional_id = p.id
        WHERE s.id = :serviceId
        ORDER BY distance ASC
        LIMIT :limit
    """, nativeQuery = true)
    List<Professional> findNearbyProfessionals(
        @Param("serviceId") UUID serviceId,
        @Param("lat") Double lat,
        @Param("lng") Double lng,
        @Param("limit") Integer limit
    );
}
```

### 4. **Async Processing**

```java
@Service
@RequiredArgsConstructor
public class NotificationService {
    
    @Async("taskExecutor") // Executa em thread separada
    public CompletableFuture<Void> sendNotification(User user, String message) {
        try {
            firebaseService.send(user.getDeviceToken(), message);
            return CompletableFuture.completedFuture(null);
        } catch (Exception e) {
            return CompletableFuture.failedFuture(e);
        }
    }
}

@Configuration
@EnableAsync
public class AsyncConfig {
    
    @Bean(name = "taskExecutor")
    public Executor taskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(5);
        executor.setMaxPoolSize(10);
        executor.setQueueCapacity(100);
        executor.setThreadNamePrefix("async-");
        executor.initialize();
        return executor;
    }
}
```

---

## 📊 OBSERVABILIDADE

### 1. **Logging**

```java
@Slf4j // Lombok
@Service
@RequiredArgsConstructor
public class AppointmentService {
    
    public Appointment create(CreateAppointmentCommand command) {
        log.info("Creating appointment for client: {}, professional: {}", 
            command.clientId(), command.professionalId());
        
        try {
            Appointment appointment = // ...
            
            log.info("Appointment created successfully: {}", appointment.getId());
            return appointment;
            
        } catch (Exception e) {
            log.error("Error creating appointment", e);
            throw e;
        }
    }
}
```

### 2. **Metrics**

```java
@Service
@RequiredArgsConstructor
public class AppointmentService {
    
    private final MeterRegistry meterRegistry;
    
    public Appointment create(CreateAppointmentCommand command) {
        Timer.Sample sample = Timer.start(meterRegistry);
        
        try {
            Appointment appointment = // ...
            
            meterRegistry.counter("appointments.created", 
                "status", "success").increment();
                
            return appointment;
            
        } catch (Exception e) {
            meterRegistry.counter("appointments.created", 
                "status", "error").increment();
            throw e;
            
        } finally {
            sample.stop(Timer.builder("appointments.create.time")
                .register(meterRegistry));
        }
    }
}
```

### 3. **Health Checks**

```java
@Component
public class DatabaseHealthIndicator implements HealthIndicator {
    
    private final DataSource dataSource;
    
    @Override
    public Health health() {
        try (Connection connection = dataSource.getConnection()) {
            if (connection.isValid(1000)) {
                return Health.up()
                    .withDetail("database", "PostgreSQL")
                    .build();
            }
        } catch (Exception e) {
            return Health.down()
                .withDetail("error", e.getMessage())
                .build();
        }
        return Health.down().build();
    }
}
```

---

## 🧪 TESTES

### 1. **Testes Unitários**

```java
@ExtendWith(MockitoExtension.class)
class CreateAppointmentUseCaseTest {
    
    @Mock
    private AppointmentRepositoryPort repository;
    
    @Mock
    private UserRepositoryPort userRepository;
    
    @InjectMocks
    private CreateAppointmentUseCase useCase;
    
    @Test
    void shouldCreateAppointment() {
        // Given
        UUID clientId = UUID.randomUUID();
        CreateAppointmentCommand command = new CreateAppointmentCommand(
            clientId, 
            UUID.randomUUID(), 
            UUID.randomUUID(), 
            LocalDate.now().plusDays(1),
            LocalTime.of(14, 0),
            "Notes"
        );
        
        when(userRepository.findClientById(any()))
            .thenReturn(Optional.of(mock(Client.class)));
        when(userRepository.findProfessionalById(any()))
            .thenReturn(Optional.of(mock(Professional.class)));
        
        // When
        Appointment result = useCase.execute(command);
        
        // Then
        assertNotNull(result);
        verify(repository).save(any(Appointment.class));
    }
}
```

### 2. **Testes de Integração**

```java
@SpringBootTest
@AutoConfigureTestDatabase
@Testcontainers
class AppointmentIntegrationTest {
    
    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16")
        .withDatabaseName("tamarcado_test");
    
    @Autowired
    private AppointmentService appointmentService;
    
    @Autowired
    private TestEntityManager entityManager;
    
    @Test
    @Transactional
    void shouldCreateAndRetrieveAppointment() {
        // Given
        Client client = createTestClient();
        Professional professional = createTestProfessional();
        entityManager.persist(client);
        entityManager.persist(professional);
        
        CreateAppointmentCommand command = // ...
        
        // When
        Appointment created = appointmentService.create(command);
        entityManager.flush();
        
        Appointment retrieved = appointmentService.findById(created.getId());
        
        // Then
        assertEquals(created.getId(), retrieved.getId());
        assertEquals(AppointmentStatus.PENDING, retrieved.getStatus());
    }
}
```

---

## 📦 DEPLOYMENT

### Docker Compose (Desenvolvimento)

```yaml
version: '3.8'

services:
  postgres:
    image: postgres:16-alpine
    environment:
      POSTGRES_DB: tamarcado
      POSTGRES_USER: tamarcado
      POSTGRES_PASSWORD: secret
    ports:
      - "5432:5432"
    volumes:
      - postgres_data:/var/lib/postgresql/data

  redis:
    image: redis:7-alpine
    ports:
      - "6379:6379"
    volumes:
      - redis_data:/data

  backend:
    build: ..
    ports:
      - "8080:8080"
    environment:
      SPRING_PROFILES_ACTIVE: dev
      SPRING_DATASOURCE_URL: jdbc:postgresql://postgres:5432/tamarcado
      SPRING_DATA_REDIS_HOST: redis
    depends_on:
      - postgres
      - redis

volumes:
  postgres_data:
  redis_data:
```

---

## 📝 RESUMO DA ARQUITETURA

### ✅ Vantagens

1. **Testabilidade**: Camadas desacopladas facilitam testes
2. **Manutenibilidade**: Separação clara de responsabilidades
3. **Escalabilidade**: Fácil adicionar novas features
4. **Independência**: Domain não depende de frameworks
5. **Flexibilidade**: Fácil trocar implementações (BD, cache, etc)

### 🎯 Padrões Utilizados

- ✅ Clean Architecture / Hexagonal Architecture
- ✅ Domain-Driven Design (DDD)
- ✅ CQRS (Command Query Responsibility Segregation)
- ✅ Repository Pattern
- ✅ Use Case Pattern
- ✅ Dependency Injection
- ✅ DTO Pattern
- ✅ Mapper Pattern
- ✅ Builder Pattern
- ✅ Factory Pattern

### 🛠️ Tecnologias

- Java 21 (LTS)
- Spring Boot 3.2+
- Spring Data JPA
- Spring Security
- PostgreSQL 16
- Redis 7
- MapStruct
- Lombok
- Flyway
- JUnit 5
- Mockito
- Testcontainers

---

**Documento criado em:** 2026-01-20
**Versão:** 1.0
**Projeto:** Tá Marcado! - Backend Architecture
