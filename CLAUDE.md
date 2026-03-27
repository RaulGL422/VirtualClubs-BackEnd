# CLAUDE.md — Virtual Clubs Backend

## Contexto del Proyecto

API REST Spring Boot para gestión de **clubes deportivos virtuales**. Actualmente implementa el sistema de autenticación completo. Es un proyecto en desarrollo activo por un desarrollador junior.

- **Versión app:** 0.0.2 | **Spring Boot:** 3.5.3 | **Java:** 21
- **Base de datos:** PostgreSQL
- **URL producción:** https://api-vc.rgal.dev
- **Rama de trabajo habitual:** ramas `feature/*` o `fix/*` (nunca main/development directamente)
- **Nombres de rama:** inglés o español pero **solo ASCII** — sin tildes, sin ñ (ej: `feature/email-verification`, no `feature/verificación-email`)

---

## Arquitectura del Proyecto

```
src/main/java/galindo/raul/virtualclubs/
├── config/
│   ├── AsyncConfig.java            ThreadPoolTaskExecutor (5 core, 20 max, 50 queue)
│   ├── CorsConfig.java             Orígenes CORS desde properties
│   └── security/
│       ├── SecurityConfig.java     Cadena de filtros, endpoints públicos
│       ├── EncoderConfig.java      BCryptPasswordEncoder bean
│       ├── filters/
│       │   ├── JwtAuthFilter.java          Valida Bearer token en cada request
│       │   └── JwtAuthLoginFilter.java     Intercepta POST /v1/auth/login
│       └── utils/JwtUtils.java     Genera/valida JWT (HMAC-SHA256)
├── controller/
│   ├── AuthController.java         Endpoints: register, refresh, logout
│   └── GlobalExceptionHandler.java @RestControllerAdvice centralizado
├── dtos/
│   ├── request/                    AuthRequest, RegisterRequest, RefreshRequest, GoogleAuthRequest, ResetPasswordRequest, RequestPasswordResetRequest
│   └── response/                   ApiResponse<T> (genérico), RegisterResponse, RefreshResponse
├── models/
│   ├── entities/                   UserEntity, AuthProviderEntity, RefreshTokenEntity, RoleEntity, PermissionEntity
│   ├── enums/                      Role (ADMIN/USER), TokenType, ErrorType (27 códigos)
│   ├── exceptions/                 11 excepciones de negocio custom
│   ├── annotations/                @StrongPassword (2 mayúsc, 2 minúsc, 1 dígito)
│   ├── Dispositive.java            Record: deviceId, deviceName, deviceType, ipAddress
│   └── Tokens.java                 Record: accessToken, refreshToken
├── repositories/
│   ├── UserEntityRepository.java
│   └── RefreshTokenRepository.java
├── services/
│   ├── UserEntityServiceImpl.java  Registro, carga de usuario, Google OAuth2
│   ├── TokensService.java          Genera, refresca y hashea tokens JWT
│   └── RefreshTokenServiceImpl.java CRUD de RefreshTokenEntity por dispositivo
└── utils/
    ├── CommonUtils.java            Extrae info de dispositivo del request
    ├── DeepLinkUtils.java          Genera deeplinks virtualclubs://...
    └── TokenUtils.java             Genera tokens aleatorios y SHA-256
```

---

## Endpoints Activos

| Método | Ruta | Auth | Descripción |
|--------|------|------|-------------|
| POST | `/v1/auth/login` | No | Login (manejado por JwtAuthLoginFilter, no controller) |
| POST | `/v1/auth/register` | No | Registro nuevo usuario |
| POST | `/v1/auth/refresh` | No | Renovar tokens con refreshToken |
| DELETE | `/v1/auth/logout` | Sí | Cerrar sesión en dispositivo actual |

## Patrones y Convenciones

### Respuesta API Estándar
```java
// Siempre usar ApiResponse<T>
ApiResponse.success(data)           // respuesta exitosa con datos
ApiResponse.emptySuccess()          // respuesta exitosa sin datos
ApiResponse.error(ErrorType.CODIGO) // respuesta de error con código
```

### Códigos de Error (ErrorType enum)
Los errores siempre se devuelven con un código numérico (1-27) para que el cliente mobile los maneje. Ver `models/enums/ErrorType.java` para la lista completa.

### Seguridad JWT
- **Access token:** 10 horas (`jwt.expiration=36000000`)
- **Refresh token:** 7 días (`jwt.refreshExpiration=604800000`)
- Los refresh tokens se almacenan como **hash SHA-256** en BD (nunca en texto plano)
- Cada dispositivo tiene su propio refresh token (multi-device support)

### Variables de Entorno Requeridas
```
SPRING_PROFILE_ACTIVE=dev|prod
PORT=
URL_DATABASE_POSTGRES_DEBUG=
USERNAME_DATABASE_POSTGRES_DEBUG=
PASSWORD_DATABASE_POSTGRES_DEBUG=
JWT_SECRET_DEBUG=
```

### Reglas de Contraseña (@StrongPassword)
Regex: mínimo 2 mayúsculas, 2 minúsculas, 1 dígito.

### Rate Limiting

- **Configuración:** `application.properties`, prefijo `rate-limiting.limits`
- **Formato:** `rate-limiting.limits[/ruta/endpoint]=N` donde N = máx. peticiones por minuto por IP
- **Para añadir un endpoint nuevo:** solo añadir una línea en `application.properties`; el filtro lo recoge automáticamente sin tocar código
- **Implementación:** `RateLimitingFilter` + `RateLimitingProperties` en `config/security/`
- **Respuesta al superar el límite:** HTTP 429 + `ApiResponse.error(ErrorType.RATE_LIMIT_EXCEEDED)` (código 28)
- **IPs detrás de proxy:** se extrae la IP real de `X-Forwarded-For` (primer valor)
- **Activación:** registrado en `SecurityConfig` antes de `JwtAuthFilter`

```properties
# Ejemplo — añadir un endpoint nuevo
rate-limiting.limits[/v1/clubs/join]=10
```

### Migraciones de Base de Datos (Flyway)
- **Ubicación:** `src/main/resources/db/migration/`
- **Formato de nombre:** `V{n}__{descripcion_en_snake_case}.sql` — ej: `V5__add_index_refresh_tokens.sql`
- **Regla crítica:** nunca modificar un script ya ejecutado en producción — siempre crear uno nuevo
- `ddl-auto=validate` — Hibernate solo valida el esquema, Flyway lo gestiona
- En tests (H2): `spring.flyway.enabled=false` — H2 usa `create-drop` directamente
- Para recrear la BD en dev: `./mvnw flyway:clean` (⚠️ solo con variables `_DEBUG` en `.env`)

### Convenciones de Testing
- **Perfil:** `@ActiveProfiles({"dev", "test"})` — el perfil `test` sobrescribe la BD con H2 en memoria (`application-test.properties`)
- **Mocks obligatorios:** `GoogleAuthService` y `MailerService` deben anotarse con `@MockitoBean` (no `@MockBean`, deprecado en Spring Boot 3.4+) para evitar conexiones externas al arrancar el contexto
- **Integración:** `@SpringBootTest(webEnvironment = MOCK)` + `@AutoConfigureMockMvc` para tests de endpoints sin levantar servidor real
- **Unitarios:** `@ExtendWith(MockitoExtension.class)` — sin contexto Spring, instanciación directa o `@InjectMocks`
- **Limpieza:** los tests de integración borran la BD en `@AfterEach` en orden FK (refresh tokens → user tokens → users)

---

## Configuración de Perfiles

| Perfil | Logs | DB Actuator | Notas |
|--------|------|-------------|-------|
| `dev` | DEBUG en consola | health, info, metrics, logfile | Variables DEBUG |
| `prod` | INFO en consola + archivo | health, info, metrics | Health probes habilitados |

---

## CI/CD

- **Trigger:** Push a rama `development`
- **Acción:** Build Docker multi-arquitectura (amd64/arm64) → push a GHCR
- **Tag imagen:** `ghcr.io/raulgl422/backend:latest`

---

## Comandos Disponibles (Claude Slash Commands)

| Comando | Descripción |
|---------|-------------|
| `/commit` | Commit semántico en español + push; detecta automáticamente referencia Notion del nombre de rama |
| `/create-pr [base]` | Crea PR hacia `development` por defecto, o hacia `[base]` si se especifica |
| `/review-pr [N]` | Revisar PR: errores, documentación, seguridad, malas prácticas |
| `/new-feature [desc\|VC-N]` | Crear rama desde `development`; si se pasa `VC-N`, obtiene detalles de Notion y actualiza estado |
| `/sync-main` | Sincronizar rama actual con main via rebase |
| `/check-security [archivo]` | Auditoría OWASP del código modificado o módulo de seguridad |
| `/add-endpoint MÉTODO /ruta desc` | Guía paso a paso para agregar endpoint siguiendo patrones del proyecto |
| `/add-test [clase]` | Genera tests unitarios e integración para una clase |
| `/explain [archivo o concepto]` | Explica un archivo o concepto del proyecto en términos simples |
| `/update-deps` | Revisa dependencias desactualizadas en pom.xml |
| `/project-status` | Estado general: git, endpoints, PRs abiertos, deuda técnica |
| `/add-task [desc]` | Crea una tarjeta nueva en Notion con tipo, prioridad y esfuerzo detectados automáticamente |
| `/release-debug [--patch\|--minor]` | Agrupa features `📦 Pendiente debug`, genera CHANGELOG, bumps versión y crea PR de release |

---

## Integración con Notion

Gestor de tareas del proyecto: base de datos **"Registro de tareas VirtualClubs"**.

- **Database ID:** `276a7f5d-0a0f-80ab-9cd3-d4f3c733a260`
- **Collection (Data Source) ID:** `276a7f5d-0a0f-802c-8d6f-000b821853c1`
- **URL para búsquedas:** `collection://276a7f5d-0a0f-802c-8d6f-000b821853c1`

### Campos relevantes
| Campo | Tipo | Valores |
|-------|------|---------|
| `Nombre de la tarea` | title | — |
| `userDefined:ID` | auto_increment | número de tarjeta (VC-N) |
| `Tipo de tarea` | multi_select | 🐞 Error, 🛠️ Funcionalidad, 🔎 Testing, ✏️ Diseño, 💻 BackEnd, 📱 Android, ⛓️ API, 📊 Base de datos, 🔒 Autenticación |
| `Estado` | status | Sin Empezar, 💻 En curso, 🔎 Testeando, ✏️ Refactorizando, ⌛🔎 Pendiente de testeo, ⌛✏️ Pendiente de Refactorizado, ❌ Testeo fallido, ⏸️ Pausado, 📬 PR Abierto, 📦 Pendiente debug, 👁️ Pendiente de Publicar, ✅ Publicado |
| `Prioridad` | select | Alta, Medio, Baja |
| `Descripción` | text | — |
| `Nivel de esfuerzo` | select | Pequeño, Medio, Grande |

### Mapeo Tipo de tarea → Prefijo de rama
| Tipo | Prefijo |
|------|---------|
| 🐞 Error | `fix/` |
| 🔎 Testing | `test/` |
| 🛠️ Funcionalidad / 💻 BackEnd / ⛓️ API / 🔒 Autenticación / 📊 Base de datos | `feature/` |
| ✏️ Diseño | `chore/` |

### Formato de rama con Notion
`[prefijo]/vc-[N]-[nombre-en-kebab-ascii]` — ej: `fix/vc-3-refresh-token-expiracion`

### Flujo de estados

| Paso | Estado | Responsable |
|------|--------|-------------|
| Tarea creada | `Sin Empezar` | `/add-task` |
| Inicia desarrollo | `💻 En curso` | `/new-feature VC-N` |
| PR abierto | `📬 PR Abierto` | `/create-pr` |
| PR aprobado por revisión | `📦 Pendiente debug` | `/review-pr` (automático si APROBADO) |
| Release publicado a debug | `⌛🔎 Pendiente de Testeo` | `/release-debug` |
| Pruebas manuales OK | `👁️ Pendiente de Publicar` | manual |
| Publicado a producción | `✅ Publicado` | manual |

**Regla importante:** Ningún slash command actualiza el estado a `👁️ Pendiente de Publicar` ni `✅ Publicado` — esas transiciones son siempre manuales.

---

## Notas para Claude

- El desarrollador es **junior**, explica el razonamiento detrás de las decisiones
- Prioriza **código seguro** (OWASP top 10), este proyecto maneja autenticación
- Antes de cualquier cambio en lógica de seguridad, explica el impacto
- Los commits van **en español** con formato semántico
- **Nunca hacer commit/push directo a `main` o `development`**
- Si hay código comentado relacionado con features pendientes, no eliminarlo
- Cuando agregues un endpoint nuevo, actualizar este CLAUDE.md
- El proyecto usa **Lombok** — no generar getters/setters manualmente
- Usar **records** para DTOs inmutables cuando sea apropiado
- La clase `ApiResponse<T>` es el estándar de respuesta — siempre usarla
