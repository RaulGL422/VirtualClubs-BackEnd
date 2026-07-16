# Graph Report - Back  (2026-07-16)

## Corpus Check
- 130 files · ~35,973 words
- Verdict: corpus is large enough that graph structure adds value.

## Summary
- 1242 nodes · 2595 edges · 78 communities (69 shown, 9 thin omitted)
- Extraction: 91% EXTRACTED · 9% INFERRED · 0% AMBIGUOUS · INFERRED: 245 edges (avg confidence: 0.8)
- Token cost: 0 input · 0 output

## Graph Freshness
- Built from commit: `95ec4c93`
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
- /add-error-type — Añadir Tipo de Error
- /add-test — Generate Tests for a Class
- Step 2: Structure of the explanation
- Endpoints
- GoogleAuthRequest
- /add-endpoint — Add New REST Endpoint
- /add-exception-handler-test — Test the GlobalExceptionHandler
- /add-migration — Crear Migración Flyway
- /new-feature — Create Feature Branch
- /add-filter-test — Unit Test for a Spring Security Filter
- /check-api-contract — Verificar Contrato API
- /commit — Semantic Commit + Push
- /create-pr — Create Pull Request
- Endpoints
- /update-deps — Review and Update Dependencies
- RequestPasswordResetRequest
- Running locally
- Ejecutar en local
- /project-status — Project Status Overview
- pull_request_template.md
- ghcr.io/raulgl422/backend Docker Image
- BeforeEach
- ExtendWith
- Component
- Override
- Contributing to VirtualClubs Backend
- Startup Banner (banner.txt)
- TokensService

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
- `/explain command` --references--> `SecurityConfig`  [EXTRACTED]
  .claude/commands/explain.md → src/main/java/galindo/raul/virtualclubs/config/security/SecurityConfig.java
- `/add-filter-test command` --references--> `JwtAuthFilter`  [EXTRACTED]
  .claude/commands/add-filter-test.md → src/main/java/galindo/raul/virtualclubs/config/security/filters/JwtAuthFilter.java
- `/add-test command` --references--> `JwtAuthFilter`  [EXTRACTED]
  .claude/commands/add-test.md → src/main/java/galindo/raul/virtualclubs/config/security/filters/JwtAuthFilter.java
- `/explain command` --references--> `JwtAuthFilter`  [EXTRACTED]
  .claude/commands/explain.md → src/main/java/galindo/raul/virtualclubs/config/security/filters/JwtAuthFilter.java

## Import Cycles
- None detected.

## Hyperedges (group relationships)
- **Test generation command family (add-test delegates to specialized commands)** — claude_commands_add_test_add_test, claude_commands_add_filter_test_add_filter_test, claude_commands_add_exception_handler_test_add_exception_handler_test [EXTRACTED 0.95]
- **Git workflow pipeline: branch -> commit -> PR -> review** — claude_commands_new_feature_new_feature, claude_commands_commit_commit, claude_commands_create_pr_create_pr, claude_commands_review_pr_review_pr [EXTRACTED 0.90]
- **Error handling pattern: ErrorType, GlobalExceptionHandler, ApiResponse, add-error-type command** — src_main_java_galindo_raul_virtualclubs_models_enums_errortype_errortype, src_main_java_galindo_raul_virtualclubs_controller_globalexceptionhandler_globalexceptionhandler, src_main_java_galindo_raul_virtualclubs_dtos_response_apiresponse_apiresponse, claude_commands_add_error_type_add_error_type [EXTRACTED 0.90]
- **Thymeleaf Email Template System (layout fragment + verify/reset content templates)** — src_main_resources_templates_layout_layout_fragment, src_main_resources_templates_password_reset_email_template, src_main_resources_templates_verify_email_template [EXTRACTED 1.00]

## Communities (78 total, 9 thin omitted)

### Community 0 - "Login Filter & Auth Controller"
Cohesion: 0.17
Nodes (9): GoogleAuthIntegrationTest, ActiveProfiles, AutoConfigureMockMvc, BeforeEach, MockMvc, Payload, SpringBootTest, Test (+1 more)

### Community 1 - "Refresh & User Token Utilities"
Cohesion: 0.33
Nodes (7): UserEntityRepository, PasswordEncoder, RequiredArgsConstructor, Service, Slf4j, Transactional, UserEntityServiceImpl

### Community 2 - "Device & Refresh Token Persistence"
Cohesion: 0.08
Nodes (29): Page, Pageable, DeviceEntity, AllArgsConstructor, Builder, Entity, Getter, NoArgsConstructor (+21 more)

### Community 3 - "Global Exception Handling"
Cohesion: 0.08
Nodes (30): Bucket, Cache, /add-exception-handler-test command, /add-filter-test command, /add-test command, MockitoBean / WebMvcTest / @ActiveProfiles(dev,test) testing convention, Filter, JsonValue (+22 more)

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
Cohesion: 0.06
Nodes (42): Async, /project-status command, Environment, EmailProperties, Component, ConfigurationProperties, Getter, Setter (+34 more)

### Community 8 - "Auth Controller Integration Test Utils"
Cohesion: 0.13
Nodes (11): AuthControllerIntegrationTest, ActiveProfiles, AutoConfigureMockMvc, MockMvc, ObjectMapper, SpringBootTest, Test, Transactional (+3 more)

### Community 9 - "Auth Controller Unit Tests"
Cohesion: 0.13
Nodes (14): Tokens, AuthControllerUnitTest, ActiveProfiles, Bean, EnableWebSecurity, HttpSecurity, MockMvc, ObjectMapper (+6 more)

### Community 10 - "Security Config & CORS"
Cohesion: 0.08
Nodes (37): AuthenticationConfiguration, /add-endpoint command, /add-migration command, /check-api-contract command, /check-security command, /check-structure command, /commit command, /create-pr command (+29 more)

### Community 11 - "Project Documentation (READMEs/CLAUDE.md)"
Cohesion: 0.08
Nodes (26): [0.0.1] — 2025-12-26, [0.0.2] — 2026-03-27, [0.0.3] — 2026-03-28, [0.1.0] — 2026-04-02, [0.1.1] — 2026-05-02, [0.2.0] — 2026-05-24, Added, Added (+18 more)

### Community 12 - "Password Validation Annotation"
Cohesion: 0.12
Nodes (16): Constraint, ConstraintValidator, ConstraintValidatorContext, CsvSource, Documented, NullSource, ParameterizedTest, Retention (+8 more)

### Community 13 - "Role Entity & Seeding"
Cohesion: 0.23
Nodes (4): BeforeEach, ExtendWith, PasswordEncoder, UserEntityServiceTest

### Community 14 - "Password Reset Integration Tests"
Cohesion: 0.21
Nodes (9): DeleteMapping, Operation, HttpServletRequest, PostMapping, ResponseEntity, GoogleAuthRequest, RefreshRequest, RequestPasswordResetRequest (+1 more)

### Community 16 - "Email Verification Integration Tests"
Cohesion: 0.20
Nodes (9): EmailVerificationIntegrationTest, ActiveProfiles, AutoConfigureMockMvc, MockMvc, ObjectMapper, SpringBootTest, Test, Transactional (+1 more)

### Community 17 - "User Entity Repository & Details Service"
Cohesion: 0.11
Nodes (15): /add-error-type command, ExceptionHandler, HttpMessageNotReadableException, JwtException, MethodArgumentNotValidException, RestControllerAdvice, GlobalExceptionHandler, ResponseEntity (+7 more)

### Community 18 - "JWT Auth Filter Tests"
Cohesion: 0.18
Nodes (8): AfterEach, BeforeEach, ExtendWith, FilterChain, Test, UserDetailsService, JwtAuthFilterTest, UserDetails

### Community 19 - "Sandbox Data Initializer"
Cohesion: 0.20
Nodes (11): ApplicationArguments, ApplicationRunner, PreDestroy, Component, Override, PasswordEncoder, Profile, RequiredArgsConstructor (+3 more)

### Community 20 - "User Entity & Exceptions"
Cohesion: 0.22
Nodes (8): AuthProviderEntity, AllArgsConstructor, Builder, Entity, Getter, NoArgsConstructor, Setter, Table

### Community 21 - "JWT Utils & Properties"
Cohesion: 0.23
Nodes (9): Claims, SecretKey, ConfigurationProperties, Validated, JwtProperties, Component, RequiredArgsConstructor, JwtUtils (+1 more)

### Community 22 - "User Entity Service Tests"
Cohesion: 0.16
Nodes (12): AuthController, Authentication, GetMapping, HttpServletResponse, PasswordEncoder, RequestMapping, RequiredArgsConstructor, RestController (+4 more)

### Community 24 - "JWT Auth Filter & ErrorType"
Cohesion: 0.29
Nodes (10): OncePerRequestFilter, FilterChain, HttpServletRequest, HttpServletResponse, ObjectMapper, Override, RequiredArgsConstructor, Slf4j (+2 more)

### Community 25 - "JWT Token Validation Tests"
Cohesion: 0.23
Nodes (4): EntityGraph, Query, Override, Test

### Community 26 - "Auth URL Utils"
Cohesion: 0.27
Nodes (3): AuthUrlUtils, AuthUrlUtilsTest, Test

### Community 28 - "Async Config"
Cohesion: 0.27
Nodes (9): AsyncConfigurer, AsyncUncaughtExceptionHandler, EnableAsync, Logger, AsyncConfig, Bean, Configuration, Override (+1 more)

### Community 29 - "User Entity Service Impl"
Cohesion: 0.21
Nodes (8): AllArgsConstructor, Builder, Entity, Getter, NoArgsConstructor, Setter, Table, UserEntity

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
Cohesion: 0.11
Nodes (21): AuthenticationException, Authentication, AuthenticationManager, FilterChain, HttpServletRequest, HttpServletResponse, ObjectMapper, Override (+13 more)

### Community 36 - "CLAUDE.md — Virtual Clubs Backend"
Cohesion: 0.07
Nodes (26): Active Endpoints, API Versioning, Architecture, CI/CD, CLAUDE.md — Virtual Clubs Backend, Contributing, Database Migrations (Flyway), Deprecation headers (+18 more)

### Community 37 - "Virtual Clubs — Backend API"
Cohesion: 0.25
Nodes (8): API versioning, Architecture, CI/CD, Database, License, Tech stack, Tests, Virtual Clubs — Backend API

### Community 38 - "Virtual Clubs — Backend API"
Cohesion: 0.25
Nodes (8): Arquitectura, Base de datos, CI/CD, Licencia, Tecnologías, Tests, Versionado de API, Virtual Clubs — Backend API

### Community 39 - "RateLimitingFilter.java"
Cohesion: 0.28
Nodes (4): CommonUtils, HttpServletRequest, CommonUtilsTest, Test

### Community 40 - "Step 2: Run all structural checks"
Cohesion: 0.12
Nodes (16): 🔁 API RESPONSE, ✅ BEAN VALIDATION, /check-structure — Verify Project Structure Compliance, 🗄️ DATABASE MIGRATIONS, ❌ EXCEPTION HANDLING, 🏗️ LAYER ARCHITECTURE, 🔧 LOMBOK & RECORDS, 📦 PACKAGE STRUCTURE (+8 more)

### Community 41 - "Test"
Cohesion: 0.12
Nodes (19): JpaRepository, AllArgsConstructor, Builder, Entity, Getter, NoArgsConstructor, Setter, Table (+11 more)

### Community 42 - "/check-structure command"
Cohesion: 0.39
Nodes (6): MessageSource, RequiredArgsConstructor, Service, Slf4j, Transactional, NotificationService

### Community 43 - "AuthProviderEntity"
Cohesion: 0.29
Nodes (4): SecureRandom, TokenUtils, BeforeEach, ExtendWith

### Community 44 - "DeepLinkUtilsTest"
Cohesion: 0.60
Nodes (5): Component, ConfigurationProperties, Getter, Setter, TokenProperties

### Community 45 - "Step 2: Read and analyze the files"
Cohesion: 0.14
Nodes (13): /check-security — Security Audit, OWASP A01 — Broken Access Control, OWASP A02 — Cryptographic Failures, OWASP A03 — Injection, OWASP A04 — Insecure Design, OWASP A05 — Security Misconfiguration, OWASP A07 — Authentication Failures, OWASP A09 — Logging Failures (+5 more)

### Community 46 - "/review-pr — Pull Request Review"
Cohesion: 0.14
Nodes (13): 🏗️ ARCHITECTURE & BAD PRACTICES, 🔴 CRITICAL ERRORS (block merge), 📚 DOCUMENTATION, ⚡ PERFORMANCE, /review-pr — Pull Request Review, 🔐 SECURITY (OWASP Top 10), Step 1: Identify the PR, Step 2: Get PR information (+5 more)

### Community 48 - "PermissionEntity"
Cohesion: 0.39
Nodes (7): AllArgsConstructor, Builder, Entity, Getter, NoArgsConstructor, Table, PermissionEntity

### Community 49 - ".handleNoLocalProvider"
Cohesion: 0.33
Nodes (6): Decisiones de seguridad, Enumeración de usuarios, Rate limiting, Tokens de un solo uso, Tokens JWT multi-dispositivo, Validación de contraseña

### Community 50 - "/add-error-type — Añadir Tipo de Error"
Cohesion: 0.17
Nodes (11): 4.1 — `ErrorType.java`, 4.2 — Crear la excepción, 4.3 — Handler en `GlobalExceptionHandler.java`, /add-error-type — Añadir Tipo de Error, Paso 1: Leer el estado actual, Paso 2: Validar, Paso 3: Proponer los cambios, Paso 4: Aplicar los cambios (+3 more)

### Community 51 - "/add-test — Generate Tests for a Class"
Cohesion: 0.17
Nodes (11): /add-test — Generate Tests for a Class, Integration test structure (full context with H2):, Naming convention for test methods:, Project-specific patterns — ALWAYS apply these, Step 1: Locate the class, Step 2: Choose the test type, Step 3: Generate the tests immediately, Step 4: Run the tests (+3 more)

### Community 52 - "Step 2: Structure of the explanation"
Cohesion: 0.17
Nodes (11): Concrete example (if applicable), /explain — Explain a File or Concept, How does it connect with the rest of the project?, How does it work? (the core), Step 1: Determine whether it is a project file or a general concept, Step 2: Structure of the explanation, Step 3: Offer to go deeper, Usage (+3 more)

### Community 53 - "Endpoints"
Cohesion: 0.21
Nodes (9): ActiveProfiles, AutoConfigureMockMvc, MockMvc, ObjectMapper, PasswordEncoder, SpringBootTest, Test, Transactional (+1 more)

### Community 54 - "GoogleAuthRequest"
Cohesion: 0.33
Nodes (6): Multi-device JWT tokens, One-time tokens, Password validation, Rate limiting, Security decisions, User enumeration

### Community 55 - "/add-endpoint — Add New REST Endpoint"
Cohesion: 0.20
Nodes (9): /add-endpoint — Add New REST Endpoint, Required conventions:, Step 1: Analyze the requested endpoint, Step 2: Create the files, Step 3: Handle new exceptions, Step 4: Update CLAUDE.md, Step 5: Generate an integration test for the new endpoint, Step 6: Done (+1 more)

### Community 56 - "/add-exception-handler-test — Test the GlobalExceptionHandler"
Cohesion: 0.20
Nodes (9): /add-exception-handler-test — Test the GlobalExceptionHandler, Critical setup — two rules that MUST be followed:, Full test class template:, Mapping pattern for test names and expected values:, Step 1: Read GlobalExceptionHandler, Step 2: Generate the test immediately, Step 3: Check that GlobalExceptionHandler is complete, Step 4: Run the tests (+1 more)

### Community 57 - "/add-migration — Crear Migración Flyway"
Cohesion: 0.20
Nodes (9): /add-migration — Crear Migración Flyway, Paso 1: Determinar el siguiente número de versión, Paso 2: Proponer nombre de archivo, Paso 3: Generar el SQL, Paso 4: Mostrar y pedir confirmación, Paso 5: Crear el archivo, Paso 6: Verificar compilación, Paso 7: Confirmar (+1 more)

### Community 58 - "/new-feature — Create Feature Branch"
Cohesion: 0.20
Nodes (9): If free description:, If VC-N format:, /new-feature — Create Feature Branch, Step 1: Check working state, Step 2: Determine branch name, Step 3: Create the branch, Step 4: Push to remote, Step 5: Confirm (+1 more)

### Community 59 - "/add-filter-test — Unit Test for a Spring Security Filter"
Cohesion: 0.22
Nodes (8): /add-filter-test — Unit Test for a Spring Security Filter, Key rules:, Required structure:, Step 1: Read the filter, Step 2: Generate the test immediately, Step 3: Run the tests, Test cases to generate for every filter:, Usage

### Community 60 - "/check-api-contract — Verificar Contrato API"
Cohesion: 0.22
Nodes (8): /check-api-contract — Verificar Contrato API, Paso 1: Leer la documentación, Paso 2: Leer los controllers, Paso 3: Leer SecurityConfig, Paso 4: Cruzar y detectar discrepancias, Paso 5: Reportar, Paso 6: Actualizar CLAUDE.md (opcional), Uso

### Community 61 - "/commit — Semantic Commit + Push"
Cohesion: 0.22
Nodes (8): /commit — Semantic Commit + Push, Step 1: Verify current branch, Step 2: Check repository state, Step 3: Evaluate staged changes, Step 4: Analyze the changes, Step 5: Propose commit message, Step 6: Commit and push, Step 7: Confirm result

### Community 62 - "/create-pr — Create Pull Request"
Cohesion: 0.22
Nodes (8): /create-pr — Create Pull Request, Step 1: Verify preconditions, Step 2: Review the changes, Step 3: Verify gh authentication, Step 4: Generate PR title and description, Step 5: Confirm and create the PR, Step 6: Launch automatic review, Usage

### Community 63 - "Endpoints"
Cohesion: 0.21
Nodes (6): ActiveProfiles, AutoConfigureMockMvc, MockMvc, SpringBootTest, Test, SecurityConfigIntegrationTest

### Community 64 - "/update-deps — Review and Update Dependencies"
Cohesion: 0.25
Nodes (7): Step 1: Read pom.xml, Step 2: Run the Maven versions plugin, Step 3: Classify the updates, Step 4: Present report, Step 5: Apply confirmed updates, Step 6: Reminder, /update-deps — Review and Update Dependencies

### Community 65 - "RequestPasswordResetRequest"
Cohesion: 0.19
Nodes (6): Dispositive, RefreshTokenService, BeforeEach, ExtendWith, Test, TokensServiceTest

### Community 66 - "Running locally"
Cohesion: 0.40
Nodes (5): 1. Clone and configure environment variables, 2. Run with Maven, 3. Run with Docker, Prerequisites, Running locally

### Community 67 - "Ejecutar en local"
Cohesion: 0.40
Nodes (5): 1. Clonar y configurar variables de entorno, 2. Ejecutar con Maven, 3. Ejecutar con Docker, Ejecutar en local, Requisitos previos

### Community 68 - "/project-status — Project Status Overview"
Cohesion: 0.40
Nodes (4): /project-status — Project Status Overview, Step 1: Git and GitHub state, Step 2: Commented-out pending features, Step 3: Generate report

### Community 69 - "pull_request_template.md"
Cohesion: 0.40
Nodes (4): Checklist, How to test, Type of change, What does this PR do?

### Community 72 - "ExtendWith"
Cohesion: 0.50
Nodes (4): Authentication — `/v1/auth`, Endpoints, Error codes, Response format

### Community 73 - "Component"
Cohesion: 0.50
Nodes (4): Autenticación — `/v1/auth`, Códigos de error, Endpoints, Formato de respuesta

### Community 75 - "Contributing to VirtualClubs Backend"
Cohesion: 0.14
Nodes (13): Branch naming, Code style, Commit format, Conventional Commits, Contributing to VirtualClubs Backend, Getting started, Opening a pull request, Rules (+5 more)

### Community 84 - "TokensService"
Cohesion: 0.60
Nodes (4): /explain command, RequiredArgsConstructor, Service, TokensService

## Ambiguous Edges - Review These
- `TokenUtils` → `/check-security command`  [AMBIGUOUS]
  .claude/commands/check-security.md · relation: references

## Knowledge Gaps
- **242 isolated node(s):** `Project Overview`, `Sandbox profile — BD limpia sin configuración`, `Architecture`, `Active Endpoints`, `Support policy` (+237 more)
  These have ≤1 connection - possible missing edges or undocumented components.
- **9 thin communities (<3 nodes) omitted from report** — run `graphify query` to explore isolated nodes.

## Suggested Questions
_Questions this graph is uniquely positioned to answer:_

- **What is the exact relationship between `TokenUtils` and `/check-security command`?**
  _Edge tagged AMBIGUOUS (relation: references) - confidence is low._
- **Why does `UserEntity` connect `User Entity Service Impl` to `Login Filter & Auth Controller`, `Refresh & User Token Utilities`, `Device & Refresh Token Persistence`, `JwtAuthLoginFilter.java`, `Google Auth & Security Integration Tests`, `Email Notification Service`, `RequestPasswordResetRequest`, `Test`, `/check-structure command`, `AuthProviderEntity`, `Role Entity & Seeding`, `Password Reset Integration Tests`, `User Entity & Exceptions`, `TokensService`, `User Entity Service Tests`, `JWT Token Validation Tests`, `SecurityConfigIntegrationTest`?**
  _High betweenness centrality (0.162) - this node is a cross-community bridge._
- **Why does `GoogleAuthService` connect `Google Auth Integration Tests` to `Login Filter & Auth Controller`, `Global Exception Handling`, `Auth Controller Integration Test Utils`, `Auth Controller Unit Tests`, `Email Verification Integration Tests`, `Endpoints`, `User Entity Service Tests`, `Endpoints`?**
  _High betweenness centrality (0.093) - this node is a cross-community bridge._
- **Why does `AuthController` connect `User Entity Service Tests` to `RequestPasswordResetRequest`, `Global Exception Handling`, `JwtAuthLoginFilter.java`, `Google Auth Integration Tests`, `Test`, `Security Config & CORS`, `/check-structure command`, `Password Reset Integration Tests`, `TokensService`, `JWT Utils & Properties`?**
  _High betweenness centrality (0.061) - this node is a cross-community bridge._
- **What connects `Project Overview`, `Sandbox profile — BD limpia sin configuración`, `Architecture` to the rest of the system?**
  _242 weakly-connected nodes found - possible documentation gaps or missing edges._
- **Should `Device & Refresh Token Persistence` be split into smaller, more focused modules?**
  _Cohesion score 0.07547169811320754 - nodes in this community are weakly interconnected._
- **Should `Global Exception Handling` be split into smaller, more focused modules?**
  _Cohesion score 0.07807807807807808 - nodes in this community are weakly interconnected._