# Virtual Clubs — Backend API

![Build](https://github.com/RaulGL422/VirtualClubs-BackEnd/actions/workflows/build.yml/badge.svg)
![Java](https://img.shields.io/badge/Java-25-blue)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-4.0.5-6DB33F)
![License](https://img.shields.io/github/license/RaulGL422/VirtualClubs-BackEnd)

> [Read in English](README.md)

REST API para la gestión de **clubes deportivos virtuales**, construida con Spring Boot 4 y Java 25. Actualmente implementa el sistema de autenticación completo con soporte multi-dispositivo, verificación de email, reset de contraseña y login con Google.

> **API en producción:** `https://api-vc.rgal.dev`
> **Documentación interactiva:** `https://api-vc.rgal.dev/swagger-ui/index.html`

---

## Tecnologías

| Capa | Tecnología |
|------|-----------|
| Framework | Spring Boot 4.0.5 |
| Lenguaje | Java 25 |
| Seguridad | Spring Security + JJWT (HMAC-SHA256) |
| Base de datos | PostgreSQL + Spring Data JPA |
| Migraciones | Flyway |
| Email | Resend (vía SMTP) + Thymeleaf |
| Rate limiting | Bucket4j + Caffeine |
| OAuth2 | Google Identity (ID Token verification) |
| CI/CD | GitHub Actions → Docker multi-arch → GHCR |
| Despliegue | Docker (linux/amd64, linux/arm64) |

---

## Arquitectura

```
src/main/java/galindo/raul/virtualclubs/
├── config/
│   ├── AsyncConfig.java              ThreadPoolTaskExecutor para emails async
│   ├── CorsProperties.java           Orígenes CORS desde properties
│   └── security/
│       ├── SecurityConfig.java       Cadena de filtros + endpoints públicos
│       ├── EncoderConfig.java        BCryptPasswordEncoder bean
│       ├── filters/
│       │   ├── JwtAuthFilter.java          Valida Bearer token en cada request
│       │   ├── JwtAuthLoginFilter.java     Intercepta POST /v1/auth/login
│       │   └── RateLimitingFilter.java     Rate limiting por IP con Bucket4j
│       └── utils/JwtUtils.java       Genera y valida JWT
├── controller/
│   ├── AuthController.java           Endpoints de autenticación
│   └── GlobalExceptionHandler.java   @RestControllerAdvice centralizado
├── dtos/
│   ├── request/                      DTOs de entrada (records inmutables)
│   └── response/                     ApiResponse<T> genérico + respuestas específicas
├── models/
│   ├── entities/                     UserEntity, AuthProviderEntity, RefreshTokenEntity...
│   ├── enums/                        Role, TokenType, ErrorType (13 códigos)
│   ├── exceptions/                   7 excepciones de negocio custom
│   └── annotations/                  @StrongPassword (validación custom)
├── repositories/                     Interfaces JPA con paginación
├── services/                         Lógica de negocio (siempre inyectada por interfaz)
└── utils/
    ├── TokenUtils.java               SHA-256 hex y Base64 — único punto de hashing
    ├── AuthUrlUtils.java             URLs hacia endpoints de la API (para emails)
    └── DeepLinkUtils.java            Deeplinks virtualclubs://...
```

---

## Endpoints

### Autenticación — `/v1/auth`

| Método | Ruta | Auth | Descripción |
|--------|------|:----:|-------------|
| `POST` | `/v1/auth/login` | — | Login con email y contraseña |
| `POST` | `/v1/auth/register` | — | Registro; envía email de verificación automáticamente |
| `POST` | `/v1/auth/refresh` | — | Renueva tokens con el refresh token |
| `DELETE` | `/v1/auth/logout` | ✓ | Cierra sesión en el dispositivo actual |
| `POST` | `/v1/auth/google` | — | Login / registro con Google OAuth2 (ID Token de Android) |
| `GET` | `/v1/auth/verify` | — | Verifica email con token de un solo uso |
| `POST` | `/v1/auth/requestVerify` | ✓ | Reenvía email de verificación |
| `POST` | `/v1/auth/requestPasswordReset` | — | Solicita reset de contraseña |
| `GET` | `/v1/auth/resetPasswordRedirect` | — | Redirige al deeplink de reset |
| `POST` | `/v1/auth/resetPassword` | — | Establece nueva contraseña con token de reset |

### Formato de respuesta

Todos los endpoints usan `ApiResponse<T>` como envolvente:

```json
// Éxito con datos
{ "data": { ... } }

// Éxito sin datos
{ "data": null }

// Error — el cliente mobile maneja el código numérico
{ "message": 4 }
```

### Códigos de error

| Código | Nombre | Descripción |
|--------|--------|-------------|
| 1 | `INTERNAL_ERROR` | Error interno del servidor |
| 2 | `INVALID_CREDENTIALS` | Email o contraseña incorrectos |
| 3 | `INVALID_REFRESH_TOKEN` | Refresh token inválido o revocado |
| 4 | `INVALID_TOKEN` | Bearer token inválido o expirado |
| 5 | `USER_NOT_FOUND` | Usuario no encontrado |
| 6 | `EMAIL_ALREADY_EXISTS` | El email ya está registrado |
| 7 | `FIELD_BLANK` | Campo obligatorio vacío |
| 8 | `INVALID_EMAIL` | Formato de email incorrecto |
| 9 | `PASSWORD_TOO_SHORT` | Contraseña demasiado corta |
| 10 | `PASSWORD_TOO_WEAK` | Contraseña sin suficiente complejidad |
| 11 | `EMAIL_NOT_VERIFIED` | Email pendiente de verificación |
| 12 | `NO_LOCAL_PROVIDER` | Cuenta vinculada solo a proveedor externo |
| 13 | `RATE_LIMIT_EXCEEDED` | Demasiadas peticiones — vuelve en un momento |

---

## Decisiones de seguridad

### Tokens JWT multi-dispositivo
Cada dispositivo obtiene su propio par de tokens (access + refresh). Un logout en un dispositivo no afecta al resto de sesiones activas. Los refresh tokens **nunca se almacenan en claro** — solo su hash SHA-256 en Base64.

```
Access token:   10 horas
Refresh token:  7 días  (almacenado como SHA-256 Base64)
```

### Tokens de un solo uso
Los tokens de verificación de email y reset de contraseña son de un solo uso y expiran:

```
Verificación de email:  24 horas  (almacenado como SHA-256 hex)
Reset de contraseña:    30 minutos
```

### Validación de contraseña
Anotación `@StrongPassword` implementada como `ConstraintValidator` custom:
- Mínimo 6 caracteres
- Al menos 2 mayúsculas, 2 minúsculas y 1 dígito

### Rate limiting
Configurado por endpoint en `application.properties` con Bucket4j + caché Caffeine. La IP real se extrae de `X-Forwarded-For` para funcionar correctamente detrás de proxy.

```properties
rate-limiting.limits[/v1/auth/login]=5           # 5 req/min
rate-limiting.limits[/v1/auth/register]=3
rate-limiting.limits[/v1/auth/requestPasswordReset]=3
```

### Enumeración de usuarios
`POST /v1/auth/requestPasswordReset` siempre devuelve `200 OK` independientemente de si el email existe o no, para evitar que un atacante enumere usuarios registrados.

---

## Base de datos

Esquema gestionado completamente con **Flyway** (9 migraciones). `Hibernate` se limita a validar el esquema (`ddl-auto=validate`).

```
users                   Datos básicos + estado de verificación de email
auth_providers          Proveedor de autenticación (local / google)
refresh_tokens          Tokens de sesión por dispositivo (hash SHA-256)
device                  Dispositivos registrados por usuario
user_tokens             Tokens de un solo uso (verificación / reset)
roles / permissions     RBAC — roles ADMIN y USER
```

---

## Ejecutar en local

### Requisitos previos
- Java 25
- PostgreSQL (o Docker)
- Una API key de [Resend](https://resend.com) para los emails
- Un Client ID de Google Cloud Console para el login con Google

### 1. Clonar y configurar variables de entorno

```bash
git clone https://github.com/RaulGL422/VirtualClubs-BackEnd.git
cd VirtualClubs-BackEnd
cp .env.example .env
# Edita .env con tus valores
```

Contenido mínimo del `.env` para el perfil `dev`:

```env
SPRING_PROFILE_ACTIVE=dev
PORT=4584
URL_DATABASE_POSTGRES_DEBUG=localhost
NAME_DATABASE_POSTGRES_DEBUG=virtualclubs
USERNAME_DATABASE_POSTGRES_DEBUG=postgres
PASSWORD_DATABASE_POSTGRES_DEBUG=tu_password
JWT_SECRET_DEBUG=clave-hmac-sha256-de-minimo-32-caracteres
RESEND_API_KEY=re_xxxx
GOOGLE_CLIENT_ID_DEBUG=xxxx.apps.googleusercontent.com
```

### 2. Ejecutar con Maven

```bash
./mvnw spring-boot:run
```

Flyway aplicará las migraciones automáticamente al arrancar.

### 3. Ejecutar con Docker

```bash
docker build -t virtualclubs-backend .
docker run -p 4584:4584 --env-file .env virtualclubs-backend
```

O usar la imagen publicada en GHCR:

```bash
docker pull ghcr.io/raulgl422/backend:latest
docker run -p 4584:4584 --env-file .env ghcr.io/raulgl422/backend:latest
```

---

## Tests

```bash
./mvnw test
```

Los tests de integración usan **H2 en memoria** (perfil `test`) — no requieren PostgreSQL. Flyway se desactiva en tests; Hibernate crea el esquema directamente con `create-drop`.

```
src/test/
├── AuthControllerIntegrationTest   Tests de endpoints con MockMvc
└── AuthControllerUnitTest          Tests de capa web con @WebMvcTest
```

---

## CI/CD

Cada push a la rama `development` lanza el pipeline de GitHub Actions:

1. Build del JAR con Maven
2. Construcción de imagen Docker multi-arquitectura (`linux/amd64`, `linux/arm64`)
3. Push a GitHub Container Registry: `ghcr.io/raulgl422/backend:latest`

---

## Versionado de API

El versionado se hace por path (`/v1/`, `/v2/`). La versión activa es `v1`.

- Los cambios aditivos (nuevos campos opcionales, nuevos endpoints) no requieren nueva versión.
- Los breaking changes publican el nuevo endpoint en `/v2/` manteniendo `/v1/` activo con headers `Deprecation: true` y `Sunset: <fecha>` hasta la migración del cliente Android.

---

## Licencia

[Apache 2.0](LICENSE)
