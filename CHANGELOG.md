# Changelog

All notable changes to the Virtual Clubs API are documented here.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project follows [Semantic Versioning](https://semver.org/).

> **Version criteria:**
> - `MAJOR.0.0` — breaking API change (modified contracts, renamed or removed endpoints)
> - `0.MINOR.0` — new module or set of endpoints
> - `0.0.PATCH` — fixes, internal improvements, changes with no contract impact

---

## [Unreleased]

---

## [0.1.0] — 2026-04-02

### Added
- Email verification flow: `GET /v1/auth/verify` and `POST /v1/auth/requestVerify` — VC-20
- Password reset flow: `POST /v1/auth/requestPasswordReset`, `GET /v1/auth/resetPasswordRedirect`, `POST /v1/auth/resetPassword` — VC-21
- `POST /v1/auth/google` endpoint for Google OAuth2 authentication (Android SDK ID Token) — VC-22

### Changed
- Spring Boot upgrade 3.5.3 → 4.0.5; adapted Jackson 3 imports and CORS config in `SecurityFilterChain` — VC-48
- `DeviceEntity` extracted from `RefreshTokenEntity` as an independent entity with `@ManyToOne` — VC-39
- API versioning strategy documented in CLAUDE.md — VC-41

### Fixed
- `removeToken()` now removes only the token for the specific device instead of all user tokens — VC-24
- Test password `PAss1` (5 chars) corrected to `PAss12` to satisfy `@Size(min=6)` — VC-27

### Internal
- `EmailService` connected to `AsyncConfig` thread pool via `@Async` — VC-23
- `@ConfigurationProperties` for JWT and CORS properties (`JwtProperties`, `CorsProperties` records) — VC-31
- Web layer tests with `@WebMvcTest` in `AuthControllerUnitTest` — VC-32
- Emoji log messages replaced with plain text — VC-36
- Pagination added in `RefreshTokenRepository` with `findByUser(UserEntity, Pageable)` — VC-40

---

## [0.0.3] — 2026-03-28

### Fixed
- Failed login returns 401 UNAUTHORIZED instead of 403 FORBIDDEN — VC-25
- Malformed JWT on `/refresh` returns 401 instead of 500 INTERNAL_SERVER_ERROR — VC-26
- Memory leak in `RateLimitingFilter.buckets` with unbounded `ConcurrentHashMap`, replaced by Caffeine cache — VC-47

### Internal
- `FetchType.LAZY` on `authProviderEntities` and `roles` relations in `UserEntity` — VC-29
- `@EntityGraph` in `loadUserByUsername` to load user + roles + permissions in a single query — VC-46
- Dependency injection unified with `@RequiredArgsConstructor` across services — VC-30
- DTO validation messages migrated from numeric codes to `ErrorType` enum names — VC-37
- Removed `--enable-preview` Java 21 flag from build configuration — VC-34

---

## [0.0.2] — 2026-03-27

### Added
- Unit and integration test suite for the authentication module
- Flyway integration for versioned database migrations

### Changed
- Dependencies updated: JJWT migrated to 0.12.6

### Fixed
- Compilation failures and logic errors in the test suite

---

## [0.0.1] — 2025-12-26

### Added
- Full multi-device JWT authentication system
- Endpoints: `POST /v1/auth/login`, `POST /v1/auth/register`, `POST /v1/auth/refresh`, `DELETE /v1/auth/logout`
- Google OAuth2 support (ID token verification)
- Hashed refresh tokens (SHA-256) in database — never stored in plain text
- Multi-device support: each device manages its own independent refresh token
- Strong password validation with `@StrongPassword` (2 uppercase, 2 lowercase, 1 digit)
- Standardized API responses with `ApiResponse<T>`
- 13 typed error codes in `ErrorType` for programmatic handling on the mobile client
- `dev` / `prod` profile configuration with differentiated log levels and actuator endpoints
- CI/CD: multi-architecture Docker build (amd64/arm64) pushed to GHCR on every push to `development`
