# Graph Report - Back  (2026-07-16)

## Corpus Check
- 131 files · ~36,021 words
- Verdict: corpus is large enough that graph structure adds value.

## Summary
- 1279 nodes · 2593 edges · 76 communities (59 shown, 17 thin omitted)
- Extraction: 91% EXTRACTED · 8% INFERRED · 0% AMBIGUOUS · INFERRED: 220 edges (avg confidence: 0.8)
- Token cost: 0 input · 0 output

## Graph Freshness
- Built from commit: `8a6c9c54`
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
- .handleNoLocalProvider
- /add-error-type — Añadir Tipo de Error
- /add-test — Generate Tests for a Class
- Step 2: Structure of the explanation
- GoogleAuthRequest
- /add-endpoint — Add New REST Endpoint
- /add-exception-handler-test — Test the GlobalExceptionHandler
- /add-migration — Crear Migración Flyway
- /new-feature — Create Feature Branch
- /add-filter-test — Unit Test for a Spring Security Filter
- /check-api-contract — Verificar Contrato API
- /commit — Semantic Commit + Push
- /create-pr — Create Pull Request
- /update-deps — Review and Update Dependencies
- RequestPasswordResetRequest
- Running locally
- Ejecutar en local
- /project-status — Project Status Overview
- pull_request_template.md
- ghcr.io/raulgl422/backend Docker Image
- Component
- Override
- PasswordEncoder
- Profile
- RequiredArgsConstructor
- Slf4j
- Transactional
- Startup Banner (banner.txt)

## God Nodes (most connected - your core abstractions)
1. `UserEntity` - 82 edges
2. `JwtUtils` - 32 edges
3. `ApiResponse` - 32 edges
4. `AuthControllerIntegrationTest` - 29 edges
5. `PasswordResetIntegrationTest` - 28 edges
6. `GoogleAuthService` - 27 edges
7. `EmailService` - 26 edges
8. `AuthController` - 25 edges
9. `AuthControllerUnitTest` - 25 edges
10. `ErrorType` - 24 edges

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

## Communities (76 total, 17 thin omitted)

### Community 0 - "Login Filter & Auth Controller"
Cohesion: 0.40
Nodes (5): [0.1.0] — 2026-04-02, Added, Changed, Fixed, Internal

### Community 1 - "Refresh & User Token Utilities"
Cohesion: 0.50
Nodes (4): [0.2.0] — 2026-05-24, Added, Changed, Internal

### Community 2 - "Device & Refresh Token Persistence"
Cohesion: 0.07
Nodes (38): JpaRepository, Page, Pageable, DeviceEntity, AllArgsConstructor, Builder, Entity, Getter (+30 more)

### Community 3 - "Global Exception Handling"
Cohesion: 0.25
Nodes (10): Bucket, Cache, Filter, ServletRequest, ServletResponse, FilterChain, HttpServletRequest, ObjectMapper (+2 more)

### Community 4 - "Google Auth & Security Integration Tests"
Cohesion: 0.21
Nodes (6): ActiveProfiles, AutoConfigureMockMvc, MockMvc, SpringBootTest, Test, SecurityConfigIntegrationTest

### Community 5 - "Email Notification Service"
Cohesion: 0.09
Nodes (20): AuthenticationManager, FilterChain, JwtAuthLoginFilter, MessageSource, NotificationService, AuthRequest, BeforeEach, ExtendWith (+12 more)

### Community 6 - "Exception Handler Unit Tests"
Cohesion: 0.08
Nodes (19): Import, GlobalExceptionHandlerTest, ActiveProfiles, Bean, EnableWebSecurity, GetMapping, HttpSecurity, MockMvc (+11 more)

### Community 7 - "Google Auth Integration Tests"
Cohesion: 0.06
Nodes (30): /project-status command, AuthProviderEntity, AllArgsConstructor, Builder, Entity, Getter, NoArgsConstructor, Setter (+22 more)

### Community 8 - "Auth Controller Integration Test Utils"
Cohesion: 0.14
Nodes (11): AuthControllerIntegrationTest, ActiveProfiles, AutoConfigureMockMvc, MockMvc, ObjectMapper, SpringBootTest, Test, Transactional (+3 more)

### Community 9 - "Auth Controller Unit Tests"
Cohesion: 0.14
Nodes (14): Tokens, AuthControllerUnitTest, ActiveProfiles, Bean, EnableWebSecurity, HttpSecurity, MockMvc, ObjectMapper (+6 more)

### Community 10 - "Security Config & CORS"
Cohesion: 0.13
Nodes (21): AuthenticationConfiguration, CorsConfigurationSource, EnableMethodSecurity, SpringBootApplication, CorsProperties, ConfigurationProperties, Validated, ConfigurationProperties (+13 more)

### Community 11 - "Project Documentation (READMEs/CLAUDE.md)"
Cohesion: 0.14
Nodes (14): [0.0.1] — 2025-12-26, [0.0.2] — 2026-03-27, [0.1.1] — 2026-05-02, Added, Added, Changed, Changelog, Fixed (+6 more)

### Community 12 - "Password Validation Annotation"
Cohesion: 0.12
Nodes (16): Constraint, ConstraintValidator, ConstraintValidatorContext, CsvSource, Documented, NullSource, ParameterizedTest, Retention (+8 more)

### Community 13 - "Role Entity & Seeding"
Cohesion: 0.05
Nodes (47): EntityGraph, PostConstruct, Query, AllArgsConstructor, Builder, Entity, Getter, NoArgsConstructor (+39 more)

### Community 14 - "Password Reset Integration Tests"
Cohesion: 0.21
Nodes (9): ActiveProfiles, AutoConfigureMockMvc, MockMvc, ObjectMapper, PasswordEncoder, SpringBootTest, Test, Transactional (+1 more)

### Community 15 - "Token Refresh Service"
Cohesion: 0.20
Nodes (6): Dispositive, RefreshTokenService, BeforeEach, ExtendWith, Test, TokensServiceTest

### Community 16 - "Email Verification Integration Tests"
Cohesion: 0.20
Nodes (9): EmailVerificationIntegrationTest, ActiveProfiles, AutoConfigureMockMvc, MockMvc, ObjectMapper, SpringBootTest, Test, Transactional (+1 more)

### Community 17 - "User Entity Repository & Details Service"
Cohesion: 0.07
Nodes (47): /add-endpoint command, /add-error-type command, /add-exception-handler-test command, /add-filter-test command, /add-migration command, /add-test command, /check-api-contract command, /check-security command (+39 more)

### Community 18 - "JWT Auth Filter Tests"
Cohesion: 0.20
Nodes (7): AfterEach, BeforeEach, ExtendWith, FilterChain, Test, UserDetailsService, JwtAuthFilterTest

### Community 19 - "Sandbox Data Initializer"
Cohesion: 0.17
Nodes (13): ApplicationArguments, ApplicationRunner, Component, Override, PasswordEncoder, PreDestroy, Profile, RequiredArgsConstructor (+5 more)

### Community 20 - "User Entity & Exceptions"
Cohesion: 0.67
Nodes (3): [0.0.3] — 2026-03-28, Fixed, Internal

### Community 21 - "JWT Utils & Properties"
Cohesion: 0.38
Nodes (5): Claims, SecretKey, Component, RequiredArgsConstructor, JwtUtils

### Community 23 - "ErrorType"
Cohesion: 0.27
Nodes (9): ActiveProfiles, EmailProperties, GoogleAuthService, JavaMailSender, SpringBootTest, EmailServiceTest, EmailService, Test (+1 more)

### Community 24 - "JWT Auth Filter & ErrorType"
Cohesion: 0.22
Nodes (12): JsonValue, OncePerRequestFilter, FilterChain, HttpServletRequest, HttpServletResponse, ObjectMapper, Override, RequiredArgsConstructor (+4 more)

### Community 25 - "JWT Token Validation Tests"
Cohesion: 0.21
Nodes (6): ConfigurationProperties, Validated, JwtProperties, BeforeEach, Test, JwtServiceTest

### Community 26 - "Auth URL Utils"
Cohesion: 0.06
Nodes (33): Async, Environment, EmailProperties, Component, ConfigurationProperties, Getter, Setter, EmailRequest (+25 more)

### Community 28 - "Async Config"
Cohesion: 0.27
Nodes (9): AsyncConfigurer, AsyncUncaughtExceptionHandler, EnableAsync, Logger, AsyncConfig, Bean, Configuration, Override (+1 more)

### Community 29 - "User Entity Service Impl"
Cohesion: 0.14
Nodes (13): Branch naming, Code style, Commit format, Conventional Commits, Contributing to VirtualClubs Backend, Getting started, Opening a pull request, Rules (+5 more)

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
Cohesion: 0.05
Nodes (40): AuthenticationException, DeleteMapping, Operation, Authentication, AuthenticationManager, FilterChain, HttpServletRequest, HttpServletResponse (+32 more)

### Community 36 - "CLAUDE.md — Virtual Clubs Backend"
Cohesion: 0.07
Nodes (26): Active Endpoints, API Versioning, Architecture, CI/CD, CLAUDE.md — Virtual Clubs Backend, Contributing, Database Migrations (Flyway), Deprecation headers (+18 more)

### Community 37 - "Virtual Clubs — Backend API"
Cohesion: 0.17
Nodes (12): API versioning, Architecture, Authentication — `/v1/auth`, CI/CD, Database, Endpoints, Error codes, License (+4 more)

### Community 38 - "Virtual Clubs — Backend API"
Cohesion: 0.17
Nodes (12): Arquitectura, Autenticación — `/v1/auth`, Base de datos, CI/CD, Códigos de error, Endpoints, Formato de respuesta, Licencia (+4 more)

### Community 40 - "Step 2: Run all structural checks"
Cohesion: 0.12
Nodes (16): 🔁 API RESPONSE, ✅ BEAN VALIDATION, /check-structure — Verify Project Structure Compliance, 🗄️ DATABASE MIGRATIONS, ❌ EXCEPTION HANDLING, 🏗️ LAYER ARCHITECTURE, 🔧 LOMBOK & RECORDS, 📦 PACKAGE STRUCTURE (+8 more)

### Community 41 - "Test"
Cohesion: 0.06
Nodes (32): SecureRandom, Component, ConfigurationProperties, Getter, Setter, TokenProperties, AllArgsConstructor, Builder (+24 more)

### Community 45 - "Step 2: Read and analyze the files"
Cohesion: 0.14
Nodes (13): /check-security — Security Audit, OWASP A01 — Broken Access Control, OWASP A02 — Cryptographic Failures, OWASP A03 — Injection, OWASP A04 — Insecure Design, OWASP A05 — Security Misconfiguration, OWASP A07 — Authentication Failures, OWASP A09 — Logging Failures (+5 more)

### Community 46 - "/review-pr — Pull Request Review"
Cohesion: 0.14
Nodes (13): 🏗️ ARCHITECTURE & BAD PRACTICES, 🔴 CRITICAL ERRORS (block merge), 📚 DOCUMENTATION, ⚡ PERFORMANCE, /review-pr — Pull Request Review, 🔐 SECURITY (OWASP Top 10), Step 1: Identify the PR, Step 2: Get PR information (+5 more)

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

### Community 64 - "/update-deps — Review and Update Dependencies"
Cohesion: 0.25
Nodes (7): Step 1: Read pom.xml, Step 2: Run the Maven versions plugin, Step 3: Classify the updates, Step 4: Present report, Step 5: Apply confirmed updates, Step 6: Reminder, /update-deps — Review and Update Dependencies

### Community 65 - "RequestPasswordResetRequest"
Cohesion: 0.32
Nodes (5): /explain command, RefreshTokenException, RequiredArgsConstructor, Service, TokensService

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

## Ambiguous Edges - Review These
- `TokenUtils` → `/check-security command`  [AMBIGUOUS]
  .claude/commands/check-security.md · relation: references

## Knowledge Gaps
- **243 isolated node(s):** `Project Overview`, `Sandbox profile — BD limpia sin configuración`, `Architecture`, `Active Endpoints`, `Support policy` (+238 more)
  These have ≤1 connection - possible missing edges or undocumented components.
- **17 thin communities (<3 nodes) omitted from report** — run `graphify query` to explore isolated nodes.

## Suggested Questions
_Questions this graph is uniquely positioned to answer:_

- **What is the exact relationship between `TokenUtils` and `/check-security command`?**
  _Edge tagged AMBIGUOUS (relation: references) - confidence is low._
- **Why does `UserEntity` connect `Device & Refresh Token Persistence` to `RequestPasswordResetRequest`, `JwtAuthLoginFilter.java`, `Google Auth Integration Tests`, `Test`, `Role Entity & Seeding`, `Token Refresh Service`, `Auth URL Utils`?**
  _High betweenness centrality (0.152) - this node is a cross-community bridge._
- **Why does `GoogleAuthService` connect `Google Auth Integration Tests` to `JwtAuthLoginFilter.java`, `Google Auth & Security Integration Tests`, `Auth Controller Integration Test Utils`, `Auth Controller Unit Tests`, `Password Reset Integration Tests`, `Email Verification Integration Tests`, `User Entity Repository & Details Service`, `Auth URL Utils`?**
  _High betweenness centrality (0.074) - this node is a cross-community bridge._
- **Why does `ApiResponse` connect `User Entity Repository & Details Service` to `JwtAuthLoginFilter.java`?**
  _High betweenness centrality (0.048) - this node is a cross-community bridge._
- **What connects `Project Overview`, `Sandbox profile — BD limpia sin configuración`, `Architecture` to the rest of the system?**
  _243 weakly-connected nodes found - possible documentation gaps or missing edges._
- **Should `Device & Refresh Token Persistence` be split into smaller, more focused modules?**
  _Cohesion score 0.06944444444444445 - nodes in this community are weakly interconnected._
- **Should `Email Notification Service` be split into smaller, more focused modules?**
  _Cohesion score 0.08502024291497975 - nodes in this community are weakly interconnected._