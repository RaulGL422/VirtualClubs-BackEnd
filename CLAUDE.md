# CLAUDE.md — Virtual Clubs Backend

## Project Overview

REST API built with Spring Boot for managing **virtual sports clubs**. Currently implements the full authentication system.

- **App version:** 0.2.0 | **Spring Boot:** 4.0.5 | **Java:** 25
- **Database:** PostgreSQL
- **Production URL:** https://api-vc.rgal.dev
- **Swagger UI:** https://api-vc.rgal.dev/swagger-ui.html

---

## Local Setup

**Prerequisites:** Java 25, Maven 3.9+, PostgreSQL running

1. Clone the repo and copy the environment file:
   ```bash
   cp .env.example .env
   ```
2. Fill in the required variables in `.env` (see [Environment Variables](#environment-variables) below).
3. Run the app with the `dev` profile:
   ```bash
   ./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
   ```
4. Run tests (uses H2 in-memory, no PostgreSQL needed):
   ```bash
   ./mvnw test
   ```

### Sandbox profile — BD limpia sin configuración

Arranca la app con una BD H2 en memoria, sin variables de entorno y con un usuario de prueba ya listo:

```bash
./mvnw spring-boot:run -Dspring-boot.run.profiles=sandbox
```

| Detalle | Valor |
|---------|-------|
| Email de prueba | `test@sandbox.local` |
| Password | `VCtest123!` |
| Email verificado | Sí (listo para login directo) |
| Swagger UI | http://localhost:8080/swagger-ui.html |
| Consola H2 | http://localhost:8080/h2-console (JDBC: `jdbc:h2:mem:virtualclubs`, user: `sa`) |

**Comportamiento del email en sandbox:** los correos no se envían pero su contenido completo (incluida la URL del token) aparece en la consola al nivel `DEBUG`. Útil para probar los flujos de verificación y reset de contraseña sin necesitar una cuenta de Resend.

---

## Architecture

The project follows a standard layered architecture:

| Layer | Package | Responsibility |
|-------|---------|----------------|
| Config | `config/` | Security filter chain, CORS, async thread pool, OpenAPI setup |
| Controller | `controller/` | HTTP endpoints, request/response mapping, global exception handler |
| Service | `services/` | Business logic, token management, email notifications |
| Repository | `repositories/` | JPA data access (Spring Data) |
| DTOs | `dtos/` | Request/response records — immutable, no business logic |
| Models | `models/` | Entities, enums, custom exceptions, validation annotations |
| Utils | `utils/` | Stateless helpers: JWT, token hashing, deep links, device info |

---

## Active Endpoints

| Method | Path | Auth | Description |
|--------|------|------|-------------|
| POST | `/v1/auth/login` | No | Login — handled by `JwtAuthLoginFilter`, not a controller method |
| POST | `/v1/auth/register` | No | Register new user; sends verification email automatically |
| POST | `/v1/auth/refresh` | No | Exchange a refresh token for a new token pair |
| DELETE | `/v1/auth/logout` | Yes | Revoke the current device's session |
| GET | `/v1/auth/verify` | No | Verify email with a one-time token; redirects to deep link |
| POST | `/v1/auth/requestVerify` | Yes | Resend verification email to the authenticated user |
| POST | `/v1/auth/requestPasswordReset` | No | Request password reset (always returns 200 — anti-enumeration) |
| GET | `/v1/auth/resetPasswordRedirect` | No | Redirect to the mobile deep link with the reset token |
| POST | `/v1/auth/resetPassword` | No | Set a new password using a reset token |
| POST | `/v1/auth/google` | No | Login or register with Google OAuth2 (ID Token from Android SDK) |

---

## API Versioning

- **Mechanism:** path-based versioning (`/v1/`, `/v2/`).
- **Current version:** `v1`.

### Support policy
- At least one previous version stays active after a new one is published.
- Version lifecycle: `Active → Deprecated (min. 3 months) → Removed`.
- No endpoint is removed before its replacement is published in the new version.

### Deprecation headers
Deprecated endpoints must include:
```
Deprecation: true
Sunset: <ISO-8601 removal date, e.g. 2026-12-01>
```

### When to create `/v2/`
Only for breaking changes:
- Request/response body structure change the mobile client cannot absorb
- Renaming or removing a required field
- Changing the semantic meaning of an existing field

Additive changes (new optional fields, new endpoints) do **not** require a new version.

---

## Patterns & Conventions

### Standard API Response
```java
ApiResponse.success(data)           // successful response with data
ApiResponse.emptySuccess()          // successful response with no data
ApiResponse.error(ErrorType.CODE)   // error response with numeric code
```
`ApiResponse<T>` is the only allowed response wrapper — always use it.

### Error Codes
Errors always include a numeric code (1–13) so the mobile client can handle them programmatically. See `models/enums/ErrorType.java` for the full list.

### Swagger / OpenAPI
Every new endpoint **must** include Swagger annotations — they are part of the API contract.

```java
// Public endpoint
@Operation(summary = "Short title", description = "What it does and edge cases.")
@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Success")
@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid input (code 7-10)")
@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "429", description = "Rate limit exceeded (code 13)")
@PostMapping("/path")

// Protected endpoint — add security to @Operation
@Operation(summary = "...", security = @SecurityRequirement(name = "bearerAuth"))
@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Invalid token (code 4)")
```

Global config lives in `config/OpenApiConfig.java`. Swagger UI is public at `/swagger-ui.html`.

### JWT Security
- **Access token:** 10 hours
- **Refresh token:** 7 days
- Refresh tokens are stored as **SHA-256 Base64 hashes** (`TokenUtils.sha256Base64`)
- Verification/reset tokens are stored as **SHA-256 hex hashes** (`TokenUtils.sha256Hex`)
- Each device has its own refresh token (multi-device support)
- An invalid/expired `Bearer` token on **any** endpoint — including `permitAll()` ones — returns 401 + `ApiResponse.error(ErrorType.INVALID_TOKEN)`

### Password Rules (`@StrongPassword`)
Minimum 6 characters, at least 1 letter (any case) and 1 digit.

### Rate Limiting
- Configured in `application.properties` under the `rate-limiting.limits` prefix
- Format: `rate-limiting.limits[/path/endpoint]=N` (max requests per minute per IP)
- Adding a new endpoint only requires one line in `application.properties` — no code changes needed
- Exceeding the limit returns HTTP 429 + `ApiResponse.error(ErrorType.RATE_LIMIT_EXCEEDED)` (code 13)

```properties
# Example
rate-limiting.limits[/v1/clubs/join]=10
```

### Database Migrations (Flyway)
- **Location:** `src/main/resources/db/migration/`
- **Naming:** `V{n}__{description_in_snake_case}.sql` — e.g. `V5__add_index_refresh_tokens.sql`
- **Critical rule:** never modify a script already executed in production — always create a new one
- Hibernate is set to `ddl-auto=validate` — Flyway owns the schema
- Tests use H2 with `spring.flyway.enabled=false` and `create-drop`
- To reset the dev database: `./mvnw flyway:clean` (⚠️ only with `_DEBUG` variables in `.env`)

### Pagination
- `UserEntityRepository` inherits `findAll(Pageable)` from `JpaRepository`
- `RefreshTokenRepository` exposes `findByUser(UserEntity, Pageable)`
- Use `PageRequest.of(page, size, Sort.by("id").descending())` when calling paginated methods

### Testing
- **Profile:** `@ActiveProfiles({"dev", "test"})` — the `test` profile overrides the DB with H2 (`application-test.properties`)
- **Required mocks:** `GoogleAuthService` and `EmailService` must use `@MockitoBean` (not `@MockBean`, deprecated in Spring Boot 3.4+)
- **Integration tests:** `@SpringBootTest(webEnvironment = MOCK)` + `@AutoConfigureMockMvc`
- **Unit tests:** `@ExtendWith(MockitoExtension.class)` — no Spring context
- **Cleanup:** integration test classes use `@Transactional` — Spring auto-rolls back after each test

---

## Environment Variables

**`dev` profile:**
```
SPRING_PROFILE_ACTIVE=dev
PORT=
URL_DATABASE_POSTGRES_DEBUG=      # PostgreSQL host
PORT_DATABASE_POSTGRES_DEBUG=     # PostgreSQL port
NAME_DATABASE_POSTGRES_DEBUG=     # database name
USERNAME_DATABASE_POSTGRES_DEBUG=
PASSWORD_DATABASE_POSTGRES_DEBUG=
JWT_SECRET_DEBUG=                  # HMAC-SHA256 key (min. 32 chars)
RESEND_API_KEY=                    # Resend API key for email delivery
GOOGLE_CLIENT_ID_DEBUG=           # Google Cloud Console OAuth2 Client ID
API_BASE_URL_DEBUG=               # optional — defaults to http://localhost:8080
```

**`prod` profile:**
```
SPRING_PROFILE_ACTIVE=prod
PORT=
URL_DATABASE_POSTGRES_PROD=
PORT_DATABASE_POSTGRES_PROD=
NAME_DATABASE_POSTGRES_PROD=
USERNAME_DATABASE_POSTGRES_PROD=
PASSWORD_DATABASE_POSTGRES_PROD=
JWT_SECRET_PROD=
RESEND_API_KEY=
GOOGLE_CLIENT_ID_PROD=
API_BASE_URL_PROD=
```

---

## Profile Configuration

| Profile | Log level | Actuator endpoints | Notes |
|---------|-----------|-------------------|-------|
| `dev` | DEBUG (console) | health, info, metrics, logfile | Uses `_DEBUG` variables |
| `prod` | INFO (console + file) | health, info, metrics | Health probes enabled |

---

## CI/CD

- **Trigger:** push to `development` branch
- **Action:** multi-arch Docker build (amd64/arm64) → push to GHCR

---

## Contributing

Branch naming, commit format, and PR checklist are documented in [CONTRIBUTING.md](CONTRIBUTING.md) — that's the single source of truth for both human contributors and Claude. Highlights that matter most for Claude-driven changes are already covered in [Rules for Claude](#rules-for-claude) below (never commit to `main`/`development`, branch off `development`, etc.).

---

## Rules for Claude

- Prioritize **secure code** (OWASP Top 10) — this project handles authentication
- Before changing any security logic, explain the impact
- Never commit or push directly to `main` or `development`
- Do not delete commented-out code related to pending features
- When adding a new endpoint, update the endpoints table in this file and `SecurityConfig` if public
- The project uses **Lombok** — do not generate getters/setters manually
- Use **records** for immutable DTOs
- `ApiResponse<T>` is the only response wrapper — always use it
- Inject **interfaces** in controllers, filters, and services — never concrete implementations (`UserService`, `RefreshTokenService`, `UserTokenService`)
- For SHA-256 hashing always use `TokenUtils` — do not instantiate `MessageDigest` directly elsewhere

## graphify

This project has a knowledge graph at graphify-out/ with god nodes, community structure, and cross-file relationships.

Rules:
- For codebase questions, first run `graphify query "<question>"` when graphify-out/graph.json exists. Use `graphify path "<A>" "<B>"` for relationships and `graphify explain "<concept>"` for focused concepts. These return a scoped subgraph, usually much smaller than GRAPH_REPORT.md or raw grep output.
- If graphify-out/wiki/index.md exists, use it for broad navigation instead of raw source browsing.
- Read graphify-out/GRAPH_REPORT.md only for broad architecture review or when query/path/explain do not surface enough context.
- After modifying code, run `graphify update .` to keep the graph current (AST-only, no API cost).
- If the bare `graphify` command is not on PATH (common on Windows Python installs), use `python -m graphify` (or the interpreter saved at `graphify-out/.graphify_python`) instead of failing silently.
