# Graph Report - VirtualClubs-BackEnd  (2026-07-16)

## Corpus Check
- 130 files · ~35,876 words
- Verdict: corpus is large enough that graph structure adds value.

## Summary
- 1067 nodes · 2438 edges · 60 communities (53 shown, 7 thin omitted)
- Extraction: 90% EXTRACTED · 10% INFERRED · 0% AMBIGUOUS · INFERRED: 245 edges (avg confidence: 0.8)
- Token cost: 0 input · 0 output

## Graph Freshness
- Built from commit: `ddcdc8ae`
- Run `git rev-parse HEAD` and compare to check if the graph is stale.
- Run `graphify update .` after code changes (no API cost).

## Community Hubs (Navigation)
- Login Filter & Auth Controller
- Refresh & User Token Utilities
- Device & Refresh Token Persistence
- Global Exception Handling
- Google Auth & Security Integration Tests
- Email Notification Service
- Exception Handler Unit Tests
- Google Auth Integration Tests
- Auth Controller Integration Test Utils
- Auth Controller Unit Tests
- Security Config & CORS
- Project Documentation (READMEs/CLAUDE.md)
- Password Validation Annotation
- Role Entity & Seeding
- Password Reset Integration Tests
- Token Refresh Service
- Email Verification Integration Tests
- User Entity Repository & Details Service
- JWT Auth Filter Tests
- Sandbox Data Initializer
- User Entity & Exceptions
- JWT Utils & Properties
- User Entity Service Tests
- ErrorType
- JWT Auth Filter & ErrorType
- JWT Token Validation Tests
- Auth URL Utils
- SecurityConfigIntegrationTest
- Async Config
- User Entity Service Impl
- Maven Wrapper Script
- Permission Entity
- OpenAPI Config
- Password Encoder Config
- Root Package Marker
- JwtAuthLoginFilter.java
- CLAUDE.md — Virtual Clubs Backend
- Virtual Clubs — Backend API
- Virtual Clubs — Backend API
- RateLimitingFilter.java
- Step 2: Run all structural checks
- Test
- /check-structure command
- AuthProviderEntity
- DeepLinkUtilsTest
- Step 2: Read and analyze the files
- /review-pr — Pull Request Review
- .sha256Hex
- PermissionEntity
- .handleNoLocalProvider
- Endpoints
- GoogleAuthRequest
- RequestPasswordResetRequest
- Running locally
- Ejecutar en local
- ghcr.io/raulgl422/backend Docker Image
- Component
- Startup Banner (banner.txt)
- TokensService
- ExtendWith

## God Nodes (most connected - your core abstractions)
1. `UserEntity` - 84 edges
2. `JwtUtils` - 32 edges
3. `ApiResponse` - 32 edges
4. `GoogleAuthService` - 29 edges
5. `AuthControllerIntegrationTest` - 29 edges
6. `EmailService` - 28 edges
7. `PasswordResetIntegrationTest` - 28 edges
8. `UserEntityServiceTest` - 26 edges
9. `AuthController` - 25 edges
10. `AuthControllerUnitTest` - 25 edges

## Surprising Connections (you probably didn't know these)
- `/check-security command` --references--> `TokenUtils`  [AMBIGUOUS]
  .claude/commands/check-security.md → src/main/java/galindo/raul/virtualclubs/utils/TokenUtils.java
- `/add-endpoint command` --references--> `SecurityConfig`  [EXTRACTED]
  .claude/commands/add-endpoint.md → src/main/java/galindo/raul/virtualclubs/config/security/SecurityConfig.java
- `/check-api-contract command` --references--> `SecurityConfig`  [EXTRACTED]
  .claude/commands/check-api-contract.md → src/main/java/galindo/raul/virtualclubs/config/security/SecurityConfig.java
- `/check-security command` --references--> `SecurityConfig`  [EXTRACTED]
  .claude/commands/check-security.md → src/main/java/galindo/raul/virtualclubs/config/security/SecurityConfig.java
- `/check-structure command` --references--> `SecurityConfig`  [EXTRACTED]
  .claude/commands/check-structure.md → src/main/java/galindo/raul/virtualclubs/config/security/SecurityConfig.java

## Import Cycles
- None detected.

## Hyperedges (group relationships)
- **Test generation command family (add-test delegates to specialized commands)** — claude_commands_add_test_add_test, claude_commands_add_filter_test_add_filter_test, claude_commands_add_exception_handler_test_add_exception_handler_test [EXTRACTED 0.95]
- **Git workflow pipeline: branch -> commit -> PR -> review** — claude_commands_new_feature_new_feature, claude_commands_commit_commit, claude_commands_create_pr_create_pr, claude_commands_review_pr_review_pr [EXTRACTED 0.90]
- **Error handling pattern: ErrorType, GlobalExceptionHandler, ApiResponse, add-error-type command** — src_main_java_galindo_raul_virtualclubs_models_enums_errortype_errortype, src_main_java_galindo_raul_virtualclubs_controller_globalexceptionhandler_globalexceptionhandler, src_main_java_galindo_raul_virtualclubs_dtos_response_apiresponse_apiresponse, claude_commands_add_error_type_add_error_type [EXTRACTED 0.90]
- **Thymeleaf Email Template System (layout fragment + verify/reset content templates)** — src_main_resources_templates_layout_layout_fragment, src_main_resources_templates_password_reset_email_template, src_main_resources_templates_verify_email_template [EXTRACTED 1.00]

## Communities (60 total, 7 thin omitted)

### Community 0 - "Login Filter & Auth Controller"
Cohesion: 0.11
Nodes (17): AuthProviderEntity, AllArgsConstructor, Builder, Entity, Getter, NoArgsConstructor, Setter, Table (+9 more)

### Community 1 - "Refresh & User Token Utilities"
Cohesion: 0.29
Nodes (7): UserEntityRepository, PasswordEncoder, RequiredArgsConstructor, Service, Slf4j, Transactional, UserEntityServiceImpl

### Community 2 - "Device & Refresh Token Persistence"
Cohesion: 0.06
Nodes (39): Page, Pageable, Dispositive, DeviceEntity, AllArgsConstructor, Builder, Entity, Getter (+31 more)

### Community 3 - "Global Exception Handling"
Cohesion: 0.23
Nodes (10): Bucket, Cache, Filter, ServletRequest, ServletResponse, FilterChain, HttpServletRequest, ObjectMapper (+2 more)

### Community 4 - "Google Auth & Security Integration Tests"
Cohesion: 0.16
Nodes (15): PostConstruct, AllArgsConstructor, Builder, Entity, Getter, NoArgsConstructor, Table, RoleEntity (+7 more)

### Community 5 - "Email Notification Service"
Cohesion: 0.25
Nodes (5): BeforeEach, ExtendWith, MessageSource, Test, NotificationServiceTest

### Community 6 - "Exception Handler Unit Tests"
Cohesion: 0.08
Nodes (19): Import, GlobalExceptionHandlerTest, ActiveProfiles, Bean, EnableWebSecurity, GetMapping, HttpSecurity, MockMvc (+11 more)

### Community 7 - "Google Auth Integration Tests"
Cohesion: 0.08
Nodes (19): /project-status command, InvalidTokenException, GoogleAuthService, GoogleIdTokenVerifier, Payload, Service, Slf4j, ActiveProfiles (+11 more)

### Community 8 - "Auth Controller Integration Test Utils"
Cohesion: 0.14
Nodes (11): AuthControllerIntegrationTest, ActiveProfiles, AutoConfigureMockMvc, MockMvc, ObjectMapper, SpringBootTest, Test, Transactional (+3 more)

### Community 9 - "Auth Controller Unit Tests"
Cohesion: 0.22
Nodes (4): Tokens, AuthControllerUnitTest, PasswordEncoder, Test

### Community 10 - "Security Config & CORS"
Cohesion: 0.13
Nodes (21): AuthenticationConfiguration, CorsConfigurationSource, EnableMethodSecurity, SpringBootApplication, CorsProperties, ConfigurationProperties, Validated, ConfigurationProperties (+13 more)

### Community 11 - "Project Documentation (READMEs/CLAUDE.md)"
Cohesion: 0.08
Nodes (26): [0.0.1] — 2025-12-26, [0.0.2] — 2026-03-27, [0.0.3] — 2026-03-28, [0.1.0] — 2026-04-02, [0.1.1] — 2026-05-02, [0.2.0] — 2026-05-24, Added, Added (+18 more)

### Community 12 - "Password Validation Annotation"
Cohesion: 0.12
Nodes (16): Constraint, ConstraintValidator, ConstraintValidatorContext, CsvSource, Documented, NullSource, ParameterizedTest, Retention (+8 more)

### Community 14 - "Password Reset Integration Tests"
Cohesion: 0.23
Nodes (11): ActiveProfiles, Bean, EnableWebSecurity, HttpSecurity, MockMvc, ObjectMapper, SecurityFilterChain, TestConfiguration (+3 more)

### Community 16 - "Email Verification Integration Tests"
Cohesion: 0.20
Nodes (9): EmailVerificationIntegrationTest, ActiveProfiles, AutoConfigureMockMvc, MockMvc, ObjectMapper, SpringBootTest, Test, Transactional (+1 more)

### Community 17 - "User Entity Repository & Details Service"
Cohesion: 0.07
Nodes (47): /add-endpoint command, /add-error-type command, /add-exception-handler-test command, /add-filter-test command, /add-migration command, /add-test command, /check-api-contract command, /check-security command (+39 more)

### Community 18 - "JWT Auth Filter Tests"
Cohesion: 0.20
Nodes (7): AfterEach, BeforeEach, ExtendWith, FilterChain, Test, UserDetailsService, JwtAuthFilterTest

### Community 20 - "User Entity & Exceptions"
Cohesion: 0.16
Nodes (5): SecureRandom, InternalErrorException, TokenUtils, Test, TokenUtilsTest

### Community 21 - "JWT Utils & Properties"
Cohesion: 0.23
Nodes (9): Claims, SecretKey, ConfigurationProperties, Validated, JwtProperties, Component, RequiredArgsConstructor, JwtUtils (+1 more)

### Community 22 - "User Entity Service Tests"
Cohesion: 0.23
Nodes (7): AuthRequest, AuthenticationManager, BeforeEach, ExtendWith, FilterChain, Test, JwtAuthLoginFilterTest

### Community 23 - "ErrorType"
Cohesion: 0.20
Nodes (12): AuthenticationException, Authentication, AuthenticationManager, FilterChain, HttpServletRequest, HttpServletResponse, ObjectMapper, Override (+4 more)

### Community 24 - "JWT Auth Filter & ErrorType"
Cohesion: 0.22
Nodes (12): JsonValue, OncePerRequestFilter, FilterChain, HttpServletRequest, HttpServletResponse, ObjectMapper, Override, RequiredArgsConstructor (+4 more)

### Community 25 - "JWT Token Validation Tests"
Cohesion: 0.29
Nodes (3): EntityGraph, Query, Override

### Community 26 - "Auth URL Utils"
Cohesion: 0.08
Nodes (30): Async, Environment, EmailProperties, Component, ConfigurationProperties, Getter, Setter, EmailRequest (+22 more)

### Community 27 - "SecurityConfigIntegrationTest"
Cohesion: 0.28
Nodes (4): BeforeEach, ExtendWith, Test, UserTokenServiceTest

### Community 28 - "Async Config"
Cohesion: 0.27
Nodes (9): AsyncConfigurer, AsyncUncaughtExceptionHandler, EnableAsync, Logger, AsyncConfig, Bean, Configuration, Override (+1 more)

### Community 29 - "User Entity Service Impl"
Cohesion: 0.27
Nodes (3): AuthUrlUtils, AuthUrlUtilsTest, Test

### Community 30 - "Maven Wrapper Script"
Cohesion: 0.33
Nodes (6): mvnw script, clean(), die(), exec_maven(), set_java_home(), verbose()

### Community 31 - "Permission Entity"
Cohesion: 0.26
Nodes (3): DeepLinkUtils, DeepLinkUtilsTest, Test

### Community 32 - "OpenAPI Config"
Cohesion: 0.53
Nodes (4): OpenAPI, Bean, Configuration, OpenApiConfig

### Community 33 - "Password Encoder Config"
Cohesion: 0.53
Nodes (4): EncoderConfig, Bean, Configuration, PasswordEncoder

### Community 35 - "JwtAuthLoginFilter.java"
Cohesion: 0.19
Nodes (8): DeleteMapping, Operation, HttpServletRequest, PostMapping, ResponseEntity, GoogleAuthRequest, RefreshRequest, RequestPasswordResetRequest

### Community 36 - "CLAUDE.md — Virtual Clubs Backend"
Cohesion: 0.05
Nodes (39): Active Endpoints, API Versioning, Architecture, CI/CD, CLAUDE.md — Virtual Clubs Backend, Contributing, Database Migrations (Flyway), Deprecation headers (+31 more)

### Community 37 - "Virtual Clubs — Backend API"
Cohesion: 0.25
Nodes (8): API versioning, Architecture, CI/CD, Database, License, Tech stack, Tests, Virtual Clubs — Backend API

### Community 38 - "Virtual Clubs — Backend API"
Cohesion: 0.25
Nodes (8): Arquitectura, Base de datos, CI/CD, Licencia, Tecnologías, Tests, Versionado de API, Virtual Clubs — Backend API

### Community 39 - "RateLimitingFilter.java"
Cohesion: 0.21
Nodes (10): AuthController, Authentication, PasswordEncoder, RequestMapping, RequiredArgsConstructor, RestController, Slf4j, GoogleResponse (+2 more)

### Community 40 - "Step 2: Run all structural checks"
Cohesion: 0.21
Nodes (4): GetMapping, HttpServletResponse, Getter, NoLocalProviderException

### Community 41 - "Test"
Cohesion: 0.12
Nodes (19): JpaRepository, AllArgsConstructor, Builder, Entity, Getter, NoArgsConstructor, Setter, Table (+11 more)

### Community 42 - "/check-structure command"
Cohesion: 0.32
Nodes (4): CommonUtils, HttpServletRequest, CommonUtilsTest, Test

### Community 43 - "AuthProviderEntity"
Cohesion: 0.39
Nodes (6): MessageSource, RequiredArgsConstructor, Service, Slf4j, Transactional, NotificationService

### Community 44 - "DeepLinkUtilsTest"
Cohesion: 0.60
Nodes (5): Component, ConfigurationProperties, Getter, Setter, TokenProperties

### Community 46 - "/review-pr — Pull Request Review"
Cohesion: 0.50
Nodes (4): Authentication — `/v1/auth`, Endpoints, Error codes, Response format

### Community 47 - ".sha256Hex"
Cohesion: 0.50
Nodes (4): Autenticación — `/v1/auth`, Códigos de error, Endpoints, Formato de respuesta

### Community 48 - "PermissionEntity"
Cohesion: 0.39
Nodes (7): AllArgsConstructor, Builder, Entity, Getter, NoArgsConstructor, Table, PermissionEntity

### Community 49 - ".handleNoLocalProvider"
Cohesion: 0.33
Nodes (6): Decisiones de seguridad, Enumeración de usuarios, Rate limiting, Tokens de un solo uso, Tokens JWT multi-dispositivo, Validación de contraseña

### Community 53 - "Endpoints"
Cohesion: 0.21
Nodes (9): ActiveProfiles, AutoConfigureMockMvc, MockMvc, ObjectMapper, PasswordEncoder, SpringBootTest, Test, Transactional (+1 more)

### Community 54 - "GoogleAuthRequest"
Cohesion: 0.33
Nodes (6): Multi-device JWT tokens, One-time tokens, Password validation, Rate limiting, Security decisions, User enumeration

### Community 65 - "RequestPasswordResetRequest"
Cohesion: 0.28
Nodes (4): BeforeEach, ExtendWith, Test, TokensServiceTest

### Community 66 - "Running locally"
Cohesion: 0.40
Nodes (5): 1. Clone and configure environment variables, 2. Run with Maven, 3. Run with Docker, Prerequisites, Running locally

### Community 67 - "Ejecutar en local"
Cohesion: 0.40
Nodes (5): 1. Clonar y configurar variables de entorno, 2. Ejecutar con Maven, 3. Ejecutar con Docker, Ejecutar en local, Requisitos previos

### Community 73 - "Component"
Cohesion: 0.20
Nodes (11): ApplicationArguments, ApplicationRunner, PreDestroy, Component, Override, PasswordEncoder, Profile, RequiredArgsConstructor (+3 more)

### Community 84 - "TokensService"
Cohesion: 0.32
Nodes (5): /explain command, RefreshTokenException, RequiredArgsConstructor, Service, TokensService

### Community 87 - "ExtendWith"
Cohesion: 0.22
Nodes (6): EmailNotFoundException, Getter, Getter, UserAlreadyExistException, ExtendWith, PasswordEncoder

## Ambiguous Edges - Review These
- `TokenUtils` → `/check-security command`  [AMBIGUOUS]
  .claude/commands/check-security.md · relation: references

## Knowledge Gaps
- **110 isolated node(s):** `galindo.raul:virtualclubs`, `INTERNAL_ERROR`, `INVALID_CREDENTIALS`, `INVALID_REFRESH_TOKEN`, `INVALID_TOKEN` (+105 more)
  These have ≤1 connection - possible missing edges or undocumented components.
- **7 thin communities (<3 nodes) omitted from report** — run `graphify query` to explore isolated nodes.

## Suggested Questions
_Questions this graph is uniquely positioned to answer:_

- **What is the exact relationship between `TokenUtils` and `/check-security command`?**
  _Edge tagged AMBIGUOUS (relation: references) - confidence is low._
- **Why does `UserEntity` connect `Device & Refresh Token Persistence` to `Login Filter & Auth Controller`, `Refresh & User Token Utilities`, `Google Auth & Security Integration Tests`, `Email Notification Service`, `Auth Controller Unit Tests`, `Role Entity & Seeding`, `Sandbox Data Initializer`, `User Entity Service Tests`, `ErrorType`, `JWT Token Validation Tests`, `SecurityConfigIntegrationTest`, `JwtAuthLoginFilter.java`, `Step 2: Run all structural checks`, `Test`, `AuthProviderEntity`, `Step 2: Read and analyze the files`, `RequestPasswordResetRequest`, `TokensService`, `ExtendWith`?**
  _High betweenness centrality (0.198) - this node is a cross-community bridge._
- **Why does `GoogleAuthService` connect `Google Auth Integration Tests` to `Login Filter & Auth Controller`, `RateLimitingFilter.java`, `Auth Controller Integration Test Utils`, `Auth Controller Unit Tests`, `Password Reset Integration Tests`, `Email Verification Integration Tests`, `User Entity Repository & Details Service`, `Endpoints`, `Auth URL Utils`?**
  _High betweenness centrality (0.126) - this node is a cross-community bridge._
- **Why does `JwtUtils` connect `JWT Utils & Properties` to `RequestPasswordResetRequest`, `RateLimitingFilter.java`, `Auth Controller Unit Tests`, `Security Config & CORS`, `Password Reset Integration Tests`, `Token Refresh Service`, `JWT Auth Filter Tests`, `TokensService`, `JWT Auth Filter & ErrorType`?**
  _High betweenness centrality (0.070) - this node is a cross-community bridge._
- **What connects `galindo.raul:virtualclubs`, `INTERNAL_ERROR`, `INVALID_CREDENTIALS` to the rest of the system?**
  _110 weakly-connected nodes found - possible documentation gaps or missing edges._
- **Should `Login Filter & Auth Controller` be split into smaller, more focused modules?**
  _Cohesion score 0.10588235294117647 - nodes in this community are weakly interconnected._
- **Should `Device & Refresh Token Persistence` be split into smaller, more focused modules?**
  _Cohesion score 0.06418219461697723 - nodes in this community are weakly interconnected._