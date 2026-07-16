# Virtual Clubs — Backend API

![Build](https://github.com/RaulGL422/VirtualClubs-BackEnd/actions/workflows/deploy.yml/badge.svg)
![Java](https://img.shields.io/badge/Java-25-blue)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-4.0.5-6DB33F)
![License](https://img.shields.io/github/license/RaulGL422/VirtualClubs-BackEnd)

> [Leer en español](README.es.md)

REST API for managing **virtual sports clubs**, built with Spring Boot 4 and Java 25. Currently implements a full authentication system with multi-device support, email verification, password reset, and Google login.

> **Production API:** `https://api-vc.rgal.dev`
> **Interactive docs:** `https://api-vc.rgal.dev/swagger-ui.html`

---

## Tech stack

| Layer | Technology |
|-------|-----------|
| Framework | Spring Boot 4.0.5 |
| Language | Java 25 |
| Security | Spring Security + JJWT (HMAC-SHA256) |
| Database | PostgreSQL + Spring Data JPA |
| Migrations | Flyway |
| Email | Resend (via SMTP) + Thymeleaf |
| Rate limiting | Bucket4j + Caffeine |
| OAuth2 | Google Identity (ID Token verification) |
| CI/CD | GitHub Actions → Docker multi-arch → GHCR |
| Deployment | Docker (linux/amd64, linux/arm64) |

---

## Architecture

```
src/main/java/galindo/raul/virtualclubs/
├── config/
│   ├── AsyncConfig.java              ThreadPoolTaskExecutor for async emails
│   ├── CorsProperties.java           CORS origins from properties
│   └── security/
│       ├── SecurityConfig.java       Filter chain + public endpoints
│       ├── EncoderConfig.java        BCryptPasswordEncoder bean
│       ├── filters/
│       │   ├── JwtAuthFilter.java          Validates Bearer token on every request
│       │   ├── JwtAuthLoginFilter.java     Intercepts POST /v1/auth/login
│       │   └── RateLimitingFilter.java     Per-IP rate limiting with Bucket4j
│       └── utils/JwtUtils.java       Generates and validates JWT
├── controller/
│   ├── AuthController.java           Authentication endpoints
│   └── GlobalExceptionHandler.java   Centralized @RestControllerAdvice
├── dtos/
│   ├── request/                      Input DTOs (immutable records)
│   └── response/                     Generic ApiResponse<T> + specific responses
├── models/
│   ├── entities/                     UserEntity, AuthProviderEntity, RefreshTokenEntity...
│   ├── enums/                        Role, TokenType, ErrorType (13 codes)
│   ├── exceptions/                   7 custom business exceptions
│   └── annotations/                  @StrongPassword (custom validator)
├── repositories/                     JPA interfaces with pagination
├── services/                         Business logic (always injected by interface)
└── utils/
    ├── TokenUtils.java               SHA-256 hex and Base64 — single hashing point
    ├── AuthUrlUtils.java             API endpoint URLs (for emails)
    └── DeepLinkUtils.java            virtualclubs:// deep links
```

---

## Endpoints

### Authentication — `/v1/auth`

| Method | Path | Auth | Description |
|--------|------|:----:|-------------|
| `POST` | `/v1/auth/login` | — | Login with email and password |
| `POST` | `/v1/auth/register` | — | Register; sends verification email automatically |
| `POST` | `/v1/auth/refresh` | — | Renew tokens with the refresh token |
| `DELETE` | `/v1/auth/logout` | ✓ | Log out on the current device |
| `POST` | `/v1/auth/google` | — | Login / register with Google OAuth2 (Android ID Token) |
| `GET` | `/v1/auth/verify` | — | Verify email with a one-time token |
| `POST` | `/v1/auth/requestVerify` | ✓ | Resend verification email |
| `POST` | `/v1/auth/requestPasswordReset` | — | Request a password reset |
| `GET` | `/v1/auth/resetPasswordRedirect` | — | Redirect to the reset deep link |
| `POST` | `/v1/auth/resetPassword` | — | Set a new password with the reset token |

### Response format

All endpoints use `ApiResponse<T>` as a wrapper:

```json
// Success with data
{ "data": { ... } }

// Success without data
{ "data": null }

// Error — the mobile client handles the numeric code
{ "message": 4 }
```

### Error codes

| Code | Name | Description |
|------|------|-------------|
| 1 | `INTERNAL_ERROR` | Internal server error |
| 2 | `INVALID_CREDENTIALS` | Wrong email or password |
| 3 | `INVALID_REFRESH_TOKEN` | Refresh token invalid or revoked |
| 4 | `INVALID_TOKEN` | Bearer token invalid or expired |
| 5 | `USER_NOT_FOUND` | User not found |
| 6 | `EMAIL_ALREADY_EXISTS` | Email already registered |
| 7 | `FIELD_BLANK` | Required field is empty |
| 8 | `INVALID_EMAIL` | Invalid email format |
| 9 | `PASSWORD_TOO_SHORT` | Password too short |
| 10 | `PASSWORD_TOO_WEAK` | Password not complex enough |
| 11 | `EMAIL_NOT_VERIFIED` | Email pending verification |
| 12 | `NO_LOCAL_PROVIDER` | Account linked only to an external provider |
| 13 | `RATE_LIMIT_EXCEEDED` | Too many requests — try again later |

---

## Security decisions

### Multi-device JWT tokens
Each device gets its own access + refresh token pair. Logging out on one device does not affect other active sessions. Refresh tokens are **never stored in plain text** — only their SHA-256 Base64 hash.

```
Access token:   10 hours
Refresh token:  7 days  (stored as SHA-256 Base64)
```

### One-time tokens
Email verification and password reset tokens are single-use and expire:

```
Email verification:  24 hours  (stored as SHA-256 hex)
Password reset:      30 minutes
```

### Password validation
Custom `@StrongPassword` annotation implemented as a `ConstraintValidator`:
- Minimum 6 characters
- At least 2 uppercase, 2 lowercase, and 1 digit

### Rate limiting
Configured per endpoint in `application.properties` using Bucket4j + Caffeine cache. The real IP is extracted from `X-Forwarded-For` to work correctly behind a proxy.

```properties
rate-limiting.limits[/v1/auth/login]=5           # 5 req/min
rate-limiting.limits[/v1/auth/register]=3
rate-limiting.limits[/v1/auth/requestPasswordReset]=3
```

### User enumeration
`POST /v1/auth/requestPasswordReset` always returns `200 OK` regardless of whether the email exists, to prevent attackers from enumerating registered users.

---

## Database

Schema fully managed by **Flyway** (9 migrations). Hibernate only validates the schema (`ddl-auto=validate`).

```
users                   Basic data + email verification status
auth_providers          Authentication provider (local / google)
refresh_tokens          Session tokens per device (SHA-256 hash)
device                  Devices registered per user
user_tokens             One-time tokens (verification / reset)
roles / permissions     RBAC — ADMIN and USER roles
```

---

## Running locally

### Prerequisites
- Java 25
- PostgreSQL (or Docker)
- A [Resend](https://resend.com) API key for emails
- A Google Cloud Console Client ID for Google login

### 1. Clone and configure environment variables

```bash
git clone https://github.com/RaulGL422/VirtualClubs-BackEnd.git
cd VirtualClubs-BackEnd
cp .env.example .env
# Fill in .env with your values
```

Minimum `.env` content for the `dev` profile:

```env
SPRING_PROFILE_ACTIVE=dev
PORT=4584
URL_DATABASE_POSTGRES_DEBUG=localhost
NAME_DATABASE_POSTGRES_DEBUG=virtualclubs
USERNAME_DATABASE_POSTGRES_DEBUG=postgres
PASSWORD_DATABASE_POSTGRES_DEBUG=your_password
JWT_SECRET_DEBUG=hmac-sha256-key-minimum-32-characters
RESEND_API_KEY=re_xxxx
GOOGLE_CLIENT_ID_DEBUG=xxxx.apps.googleusercontent.com
```

### 2. Run with Maven

```bash
./mvnw spring-boot:run
```

Flyway will apply migrations automatically on startup.

### 3. Run with Docker

```bash
docker build -t virtualclubs-backend .
docker run -p 4584:4584 --env-file .env virtualclubs-backend
```

Or use the published GHCR image:

```bash
docker pull ghcr.io/raulgl422/backend:latest
docker run -p 4584:4584 --env-file .env ghcr.io/raulgl422/backend:latest
```

---

## Tests

```bash
./mvnw test
```

Integration tests use **H2 in-memory** (profile `test`) — no PostgreSQL required. Flyway is disabled in tests; Hibernate creates the schema directly with `create-drop`.

```
src/test/
├── AuthControllerIntegrationTest   Endpoint tests with MockMvc
└── AuthControllerUnitTest          Web layer tests with @WebMvcTest
```

---

## CI/CD

Every push to the `development` branch triggers the GitHub Actions pipeline:

1. Build the JAR with Maven
2. Build a multi-architecture Docker image (`linux/amd64`, `linux/arm64`)
3. Push to GitHub Container Registry: `ghcr.io/raulgl422/backend:latest`

---

## API versioning

Versioning is done by path (`/v1/`, `/v2/`). The active version is `v1`.

- Additive changes (new optional fields, new endpoints) do not require a new version.
- Breaking changes publish the new endpoint under `/v2/` while keeping `/v1/` active with `Deprecation: true` and `Sunset: <date>` headers until the Android client migrates.

---

## License

[Apache 2.0](LICENSE)
