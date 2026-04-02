# Changelog

Todos los cambios notables de Virtual Clubs API se documentan aquí.

El formato está basado en [Keep a Changelog](https://keepachangelog.com/es/1.1.0/),
y el proyecto sigue [Versionado Semántico](https://semver.org/lang/es/).

> **Criterio de versiones:**
> - `MAJOR.0.0` — cambio breaking en la API (contratos modificados, endpoints renombrados/eliminados)
> - `0.MINOR.0` — nuevo módulo o conjunto de endpoints
> - `0.0.PATCH` — correcciones, mejoras internas, actualizaciones sin cambio de contrato

---

## [Sin publicar]

---

## [0.1.0] — 2026-04-02

### Añadido
- Flujo de verificación de email: `GET /v1/auth/verify` y `POST /v1/auth/requestVerify` — VC-20
- Flujo de reset de contraseña: `POST /v1/auth/requestPasswordReset`, `GET /v1/auth/resetPasswordRedirect`, `POST /v1/auth/resetPassword` — VC-21
- Endpoint `POST /v1/auth/google` para autenticación con Google OAuth2 (ID Token del SDK de Android) — VC-22

### Cambiado
- Actualización Spring Boot 3.5.3 → 4.0.5; adapta imports de Jackson 3 y configuración CORS en `SecurityFilterChain` — VC-48
- `DeviceEntity` extraída de `RefreshTokenEntity` como entidad independiente con `@ManyToOne` — VC-39
- Estrategia de versionado de API documentada en CLAUDE.md — VC-41

### Corregido
- `removeToken()` ahora elimina únicamente el token del dispositivo específico en lugar de todos los tokens del usuario — VC-24
- Password de prueba `PAss1` (5 chars) corregido a `PAss12` para cumplir `@Size(min=6)` — VC-27

### Interno
- `EmailService` conectado al `ThreadPoolTaskExecutor` de `AsyncConfig` mediante `@Async` — VC-23
- `@ConfigurationProperties` para propiedades JWT y CORS (records `JwtProperties`, `CorsProperties`) — VC-31
- Tests de capa web con `@WebMvcTest` en `AuthControllerUnitTest` — VC-32
- Emojis en mensajes de log reemplazados por texto plano — VC-36
- Paginación añadida en `RefreshTokenRepository` con `findByUser(UserEntity, Pageable)` — VC-40

---

## [0.0.3] — 2026-03-28

### Corregido
- Login fallido devuelve 401 UNAUTHORIZED en lugar de 403 FORBIDDEN — VC-25
- JWT malformado en `/refresh` devuelve 401 en lugar de 500 INTERNAL_SERVER_ERROR — VC-26
- Fuga de memoria en `RateLimitingFilter.buckets` con `ConcurrentHashMap` sin TTL, reemplazado por Caffeine cache — VC-47

### Interno
- `FetchType.LAZY` en relaciones `authProviderEntities` y `roles` de `UserEntity` — VC-29
- `@EntityGraph` en `loadUserByUsername` para cargar usuario + roles + permisos en una sola query — VC-46
- Inyección de dependencias unificada con `@RequiredArgsConstructor` en servicios — VC-30
- Mensajes de validación en DTOs migrados de códigos numéricos a nombres de enum `ErrorType` — VC-37
- Eliminado flag `--enable-preview` de Java 21 de la configuración de compilación — VC-34

---

## [0.0.2] — 2026-03-27

### Añadido
- Suite de tests unitarios e integración para el módulo de autenticación
- Sistema de tareas integrado con Notion (slash commands de Claude Code)
- Integración de Flyway para migraciones versionadas de base de datos

### Cambiado
- Dependencias actualizadas: JJWT migrado a 0.12.6

### Corregido
- Fallos de compilación y lógica en la suite de tests

---

## [0.0.1] — 2025-12-26

### Añadido
- Sistema completo de autenticación JWT multi-dispositivo
- Endpoints: `POST /v1/auth/login`, `POST /v1/auth/register`, `POST /v1/auth/refresh`, `DELETE /v1/auth/logout`
- Soporte OAuth2 con Google (verificación de `id_token`)
- Refresh tokens hasheados (SHA-256) en base de datos — nunca en texto plano
- Soporte multi-dispositivo: cada dispositivo gestiona su propio refresh token independiente
- Validación de contraseñas fuertes con `@StrongPassword` (2 mayúsc., 2 minúsc., 1 dígito)
- Respuestas API estandarizadas con `ApiResponse<T>`
- 27 códigos de error tipados en `ErrorType` para manejo en cliente mobile
- Configuración de perfiles `dev` / `prod` con niveles de log y actuator diferenciados
- CI/CD: build Docker multi-arquitectura (amd64/arm64) hacia GHCR en cada push a `development`
