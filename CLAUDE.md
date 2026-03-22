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

## Endpoints Comentados (Pendientes de Implementar)

- `POST /v1/auth/google` — Google OAuth2 (GoogleAuthService comentado)
- `POST /v1/auth/requestPasswordReset` — Solicitar reset de contraseña
- `GET /v1/auth/resetPasswordRedirect` — Redirect desde email
- `POST /v1/auth/resetPassword` — Establecer nueva contraseña
- `GET /v1/auth/verify` — Verificar email
- `POST /v1/auth/requestVerify` — Solicitar reverificación de email

---

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

### Convenciones de Testing
- **Perfil:** `@ActiveProfiles({"dev", "test"})` — el perfil `test` sobrescribe la BD con H2 en memoria (`application-test.properties`)
- **Mocks obligatorios:** `GoogleAuthService` y `MailerService` deben anotarse con `@MockitoBean` (no `@MockBean`, deprecado en Spring Boot 3.4+) para evitar conexiones externas al arrancar el contexto
- **Integración:** `@SpringBootTest(webEnvironment = MOCK)` + `@AutoConfigureMockMvc` para tests de endpoints sin levantar servidor real
- **Unitarios:** `@ExtendWith(MockitoExtension.class)` — sin contexto Spring, instanciación directa o `@InjectMocks`
- **Limpieza:** los tests de integración borran la BD en `@AfterEach` en orden FK (refresh tokens → user tokens → users)

---

## Funcionalidades Comentadas (No Activas)

Hay código preparado pero no activo todavía. **No eliminar**, son funcionalidades previstas:
- `MailerService.java` — Envío de emails con SendGrid + Thymeleaf templates
- `GoogleAuthService.java` — Verificación de Google ID tokens
- `UserTokenService.java` + `UserTokenRepository.java` + `UserTokenEntity.java` — Tokens de verificación de email y reset de contraseña

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
| `/commit` | Commit semántico en español + push a rama actual |
| `/create-pr [base]` | Crea PR hacia `development` por defecto, o hacia `[base]` si se especifica |
| `/review-pr [N]` | Revisar PR: errores, documentación, seguridad, malas prácticas |
| `/new-feature [desc]` | Crear rama desde `development` por defecto; usar `--from <rama>` para otra base |
| `/sync-main` | Sincronizar rama actual con main via rebase |
| `/check-security [archivo]` | Auditoría OWASP del código modificado o módulo de seguridad |
| `/add-endpoint MÉTODO /ruta desc` | Guía paso a paso para agregar endpoint siguiendo patrones del proyecto |
| `/add-test [clase]` | Genera tests unitarios e integración para una clase |
| `/explain [archivo o concepto]` | Explica un archivo o concepto del proyecto en términos simples |
| `/update-deps` | Revisa dependencias desactualizadas en pom.xml |
| `/project-status` | Estado general: git, endpoints, PRs abiertos, deuda técnica |

---

## Deuda Técnica Conocida

| Prioridad | Problema | Dónde |
|-----------|----------|-------|
| ALTA | No hay rate limiting en `/login` y `/register` | `SecurityConfig.java` |
| MEDIA | Google OAuth2 preparado pero comentado | `GoogleAuthService.java` |
| MEDIA | Email verification y password reset comentados | `MailerService`, `UserTokenService`, `UserTokenEntity` |
| BAJA | `AsyncConfig` configurado pero sin uso real aún | `AsyncConfig.java` |

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
