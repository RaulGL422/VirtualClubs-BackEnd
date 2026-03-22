# TODO — Virtual Clubs Backend

Documento de seguimiento de mejoras pendientes, bugs encontrados y funcionalidades por implementar.
Última actualización: 2026-03-22

---

## Índice

1. [Bugs / Errores de lógica](#1-bugs--errores-de-lógica)
2. [Mejoras de Spring Boot y buenas prácticas](#2-mejoras-de-spring-boot-y-buenas-prácticas)
3. [Seguridad](#3-seguridad)
4. [Código y patrones](#4-código-y-patrones)
5. [Arquitectura y escalabilidad](#5-arquitectura-y-escalabilidad)
6. [Tests](#6-tests)
7. [Funcionalidades pendientes de implementar](#7-funcionalidades-pendientes-de-implementar)
8. [Deuda técnica conocida](#8-deuda-técnica-conocida)

---

## 1. Bugs / Errores de lógica

### BUG-01 — `removeToken()` elimina TODOS los tokens del usuario al hacer refresh
**Archivo:** `services/RefreshTokenServiceImpl.java`
**Severidad:** Alta

Cuando se valida un refresh token correctamente (en `TokensService.refreshTokens()`), el método `removeToken()` borra **todos** los refresh tokens del usuario de **todos sus dispositivos**. Esto significa que si un usuario hace `/refresh` desde el móvil, pierde la sesión en todos sus otros dispositivos simultáneamente.

**Comportamiento esperado:** solo debería invalidar el token que se acaba de usar, no el resto.

**Solución propuesta:** cambiar `refreshTokenRepository.deleteAll()` por una búsqueda y borrado del token específico por hash, manteniendo el resto intacto.

---

### BUG-02 — HTTP 403 incorrecto en login fallido
**Archivo:** `config/security/filters/JwtAuthLoginFilter.java` → `unsuccessfulAuthentication()`
**Severidad:** Media

Cuando las credenciales son incorrectas, el filtro responde con `HttpStatus.FORBIDDEN` (403). El estándar HTTP establece:
- **401 UNAUTHORIZED** → "No sé quién eres" (credenciales incorrectas o ausentes)
- **403 FORBIDDEN** → "Sé quién eres, pero no tienes permiso"

Un login fallido debe devolver 401, no 403. Este error puede confundir a clientes que implementen lógica basada en status codes.

**Solución propuesta:** cambiar a `HttpServletResponse.SC_UNAUTHORIZED` (401).

---

### BUG-03 — JWT malformado en `/refresh` devuelve 500 en lugar de 401
**Archivo:** `controller/AuthController.java` → endpoint `POST /v1/auth/refresh`
**Severidad:** Media

Si el cliente envía un `refreshToken` con formato inválido (no es un JWT, está truncado, etc.), `JwtUtils.getUsernameFromToken()` lanza una excepción interna de la librería JJWT. El `GlobalExceptionHandler` la captura con el handler genérico `Exception` y devuelve `500 INTERNAL_ERROR`.

**Comportamiento esperado:** un token malformado es un error del cliente → debería devolver `401 UNAUTHORIZED` con `ErrorType.INVALID_REFRESH_TOKEN`.

**Solución propuesta:** añadir un bloque try-catch específico para `JwtException` (de JJWT) en el controller o en `GlobalExceptionHandler`, mapeándolo a 401.

---

### BUG-04 — Password de test `"PAss1"` podría no cumplir `@Size(min=6)`
**Archivo:** `test/controller/AuthControllerIntegrationTest.java`
**Severidad:** Baja (posible)

El password usado en los tests es `"PAss1"`, que tiene **5 caracteres**. La anotación `@Size(min=6, max=64)` en `RegisterRequest` debería rechazarlo con un 400. Si los tests de registro pasan con este valor, significa que la validación `@Size` no se está aplicando correctamente o hay un conflicto entre validadores.

**Acción recomendada:** verificar si el test `register_usuarioNuevo_retorna200()` efectivamente usa este password y si la validación `@Size` está activa. Cambiar el password de test a `"PAss12"` (6 chars) para que sea inequívoco.

---

## 2. Mejoras de Spring Boot y buenas prácticas

### MEJORA-01 — Reemplazar `ddl-auto=update` con migraciones versionadas (Flyway)
**Archivo:** `src/main/resources/application.properties`
**Prioridad:** Alta

`spring.jpa.hibernate.ddl-auto=update` le pide a Hibernate que compare el esquema de la BD con las entidades y aplique cambios automáticamente. En producción esto es peligroso porque:
- Puede alterar columnas de forma inesperada
- No guarda historial de cambios
- Imposible hacer rollback si algo sale mal

**Solución:** integrar **Flyway** (o Liquibase). Con Flyway, los cambios de base de datos se escriben en archivos SQL versionados (`V1__init.sql`, `V2__add_roles.sql`, etc.) que se ejecutan en orden y se registran. Es el estándar en aplicaciones Spring Boot en producción.

```
src/main/resources/
└── db/
    └── migration/
        ├── V1__create_users_table.sql
        ├── V2__create_roles_table.sql
        └── V3__create_refresh_tokens_table.sql
```

---

### MEJORA-02 — Cambiar `FetchType.EAGER` a `LAZY` en relaciones JPA
**Archivo:** `models/entities/UserEntity.java`
**Prioridad:** Media

Las relaciones `authProviderEntities` y `roles` en `UserEntity` usan `fetch = FetchType.EAGER`. Esto significa que **cada vez** que se carga un usuario desde la BD (incluso para una simple validación de token), Hibernate lanza queries adicionales para traer sus roles y permisos aunque no se necesiten.

Con `LAZY`, los datos se cargan solo cuando se acceden explícitamente. Es más eficiente y es el comportamiento recomendado por defecto para `@OneToMany` y `@ManyToMany`.

**Nota:** cambiar a LAZY requiere asegurarse de que el acceso a las colecciones ocurra dentro de una transacción activa (lo cual ya es el caso en los servicios con `@Transactional`).

---

### MEJORA-03 — Unificar inyección de dependencias con constructor injection
**Archivos:** varios (especialmente `services/`)
**Prioridad:** Media

Hay inconsistencia entre clases que usan `@Autowired` sobre campos y otras que usan `@RequiredArgsConstructor` de Lombok. La **constructor injection** (vía `@RequiredArgsConstructor`) es la práctica recomendada por Spring porque:
- Las dependencias son explícitas e inmutables (`final`)
- Facilita los tests unitarios (puedes instanciar la clase directamente sin contexto Spring)
- Detecta dependencias circulares en tiempo de arranque, no en runtime

**Acción:** eliminar `@Autowired` sobre campos y marcar las dependencias como `private final`, dejando que `@RequiredArgsConstructor` genere el constructor.

---

### MEJORA-04 — Usar `@ConfigurationProperties` para grupos de propiedades
**Archivos:** `config/security/utils/JwtUtils.java`, `config/CorsConfig.java`
**Prioridad:** Baja-Media

Actualmente se inyectan propiedades individuales con `@Value("${jwt.secret}")`, `@Value("${jwt.expiration}")`, etc. Para grupos de propiedades relacionadas, Spring Boot ofrece `@ConfigurationProperties`:

```java
@ConfigurationProperties(prefix = "jwt")
public record JwtProperties(String secret, long expiration, long refreshExpiration) {}
```

Ventajas:
- Todas las propiedades de un módulo en un solo lugar
- Spring valida las propiedades al arrancar (con `@Validated`)
- Autocompletado en IDEs para los valores en `.properties`

---

### MEJORA-05 — Usar `@WebMvcTest` para tests de controller en lugar de `@SpringBootTest`
**Archivo:** `test/controller/AuthControllerIntegrationTest.java`
**Prioridad:** Baja

`@SpringBootTest` levanta el contexto Spring completo (todas las beans, JPA, seguridad, etc.). Para tests de la capa web existe `@WebMvcTest(AuthController.class)` que solo carga los beans relacionados con MVC, siendo considerablemente más rápido.

La diferencia práctica: los tests de integración actuales mezclan test de controller con test de base de datos. Separar ambos en:
- `@WebMvcTest` → test el controller aislado (con mocks del service)
- `@SpringBootTest` → test el flujo completo de extremo a extremo

---

### MEJORA-06 — Devolver `201 Created` en el endpoint de registro
**Archivo:** `controller/AuthController.java` → `POST /v1/auth/register`
**Prioridad:** Baja

El endpoint de registro de usuario devuelve `200 OK`. Por convención REST, la creación de un recurso debe devolver `201 CREATED`. Esto se hace retornando `ResponseEntity`:

```java
return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(response));
```

Es un cambio pequeño pero alinea la API con el estándar HTTP y comunica mejor la semántica de la operación.

---

### MEJORA-07 — Desactivar `--enable-preview` de Java 21 en producción
**Archivo:** `pom.xml`
**Prioridad:** Media

El proyecto compila con `--enable-preview` de Java 21. Las _preview features_ son funcionalidades que pueden cambiar o eliminarse entre versiones menores de Java. A menos que se esté usando una feature específica que lo requiera, es más seguro compilar sin esta flag para evitar problemas al actualizar Java.

**Acción:** revisar si hay alguna preview feature en uso (como pattern matching extendido, string templates, etc.). Si no hay ninguna, eliminar el flag del `pom.xml`.

---

## 3. Seguridad

### SEG-01 — Sin rate limiting en `/login` y `/register`
**Archivos:** `config/security/SecurityConfig.java`
**Prioridad:** Alta

Los endpoints de autenticación no tienen límite de intentos. Esto los deja vulnerables a:
- **Brute force** en `/login` (adivinar contraseñas)
- **Spam de registros** en `/register` (crear cuentas masivamente)

**Solución propuesta:** añadir **Bucket4j** (librería de rate limiting para Spring) o implementar un filtro que limite las peticiones por IP. Spring Cloud Gateway también ofrece rate limiting si se añade un API Gateway.

Ejemplo básico de lo que se necesitaría:
```
- /login: máximo 5 intentos fallidos por IP cada 15 minutos
- /register: máximo 3 registros por IP por hora
```

---

### SEG-02 — Logs con emojis pueden romper sistemas de monitorización
**Archivos:** varios servicios y filtros
**Prioridad:** Baja

Los logs usan emojis (📝, ❌, ⚠️) que son caracteres Unicode fuera del rango ASCII. En sistemas de logging estructurado como ELK Stack (Elasticsearch + Logstash + Kibana) o Datadog, estos caracteres pueden:
- Romper el parsing de logs
- Generar entradas malformadas en índices
- Dificultar búsquedas y alertas

**Solución:** usar prefijos de texto como `[INFO]`, `[ERROR]`, `[WARN]` o simplemente confiar en el nivel de log de SLF4J.

---

## 4. Código y patrones

### COD-01 — Mensajes de validación como números en anotaciones
**Archivos:** `dtos/request/AuthRequest.java`, `RegisterRequest.java`, `RefreshRequest.java`
**Prioridad:** Baja

Las anotaciones de validación usan `message = "5"` para codificar el número de error:

```java
@NotBlank(message = "5")   // EMAIL_REQUIRED
@Email(message = "9")      // INVALID_EMAIL_FORMAT
```

Esto funciona, pero es una solución ad-hoc que requiere conocer de memoria qué número corresponde a qué error. El estándar de Bean Validation permite usar claves de `messages.properties`:

```java
@NotBlank(message = "{error.email.required}")
```

Con un archivo `src/main/resources/ValidationMessages.properties`:
```
error.email.required=5
```

O mejor aún: usar el propio nombre del enum como mensaje y mapear en el `GlobalExceptionHandler`.

---

### COD-02 — `AsyncConfig` configura threads sin uso real activo
**Archivo:** `config/AsyncConfig.java`
**Prioridad:** Baja

Hay un `ThreadPoolTaskExecutor` configurado con 5-20 hilos y una cola de 50, pero ningún método con `@Async` está activo actualmente (los de `MailerService` están comentados). El bean se registra y ocupa recursos en memoria sin propósito.

**Opciones:**
1. Mantenerlo pero añadir un comentario explicando que es para cuando se active `MailerService`
2. Moverlo junto al código que lo va a usar (dentro del módulo de email cuando se implemente)

---

## 5. Arquitectura y escalabilidad

### ARQ-01 — Información de dispositivo embebida en `RefreshTokenEntity`
**Archivo:** `models/entities/RefreshTokenEntity.java`
**Prioridad:** Baja

`RefreshTokenEntity` almacena `deviceId`, `deviceName`, `deviceType`, `ipAddress` directamente como columnas. Si un usuario tiene 5 dispositivos, esa información se repite en cada token de cada dispositivo. Un modelo más limpio sería una entidad `DeviceEntity` separada con relación `@ManyToOne` desde `RefreshTokenEntity`.

**Nota:** este cambio requeriría una migración de base de datos. Considerar cuando se implemente Flyway (MEJORA-01).

---

### ARQ-02 — Sin paginación en consultas de repositorios
**Archivo:** `repositories/RefreshTokenRepository.java`
**Prioridad:** Baja

Los métodos del repositorio devuelven `List<RefreshTokenEntity>`. Con pocos usuarios esto es indiferente, pero cuando haya miles de usuarios con múltiples dispositivos, cargar toda la lista en memoria será un problema. Spring Data JPA soporta `Pageable` y `Page<T>` para paginar resultados.

Esta mejora es más relevante cuando se empiecen a construir endpoints de administración (listar usuarios, listar sesiones activas, etc.).

---

### ARQ-03 — Sin estrategia de versionado de API más allá del path
**Prioridad:** Baja (planificación futura)

La URL `/v1/auth/...` versiona la API en el path, que es correcto. Pero no hay ninguna estrategia documentada para:
- Cuándo y cómo deprecar endpoints de v1
- Cómo introducir v2 cuando sea necesario
- Si se van a soportar múltiples versiones simultáneamente

Definir esto ahora (aunque sea en documentación) evita decisiones apresuradas cuando llegue el momento.

---

## 6. Tests

### TEST-01 — `@AfterEach` sin orden FK puede volverse frágil
**Archivo:** `test/controller/AuthControllerIntegrationTest.java`
**Prioridad:** Baja

La limpieza de BD entre tests borra `refreshTokenRepository.deleteAll()` primero y luego `userRepository.deleteAll()`. Funciona por el orden de FK, pero si se añaden más entidades relacionadas (tokens de verificación, sesiones, etc.), este orden manual puede quedar desactualizado silenciosamente y causar fallos difíciles de diagnosticar.

**Alternativa más robusta:** anotar los tests de integración con `@Transactional` para que cada test haga rollback automático al terminar, sin necesidad de borrado manual. O usar `@Sql(scripts = "/cleanup.sql")`.

---

## 7. Funcionalidades pendientes de implementar

Todo el código base para estas funcionalidades ya existe pero está comentado. No eliminar.

### FEAT-01 — Google OAuth2
**Archivos involucrados:**
- `services/GoogleAuthService.java` (comentado)
- `controller/AuthController.java` → `POST /v1/auth/google` (comentado)
- `dtos/request/GoogleAuthRequest.java`
- `models/exceptions/GoogleIdException.java`, `NoLocalProviderException.java`
- `controller/GlobalExceptionHandler.java` → handlers comentados

**Descripción:** permite al usuario autenticarse usando un Google ID Token desde la app móvil. El flujo completo ya está diseñado en `UserEntityServiceImpl.registerOrLoadUserWithGoogle()`.

**Dependencia externa:** `google-api-client` (ya en `pom.xml`)

---

### FEAT-02 — Verificación de email
**Archivos involucrados:**
- `models/entities/UserTokenEntity.java` (completamente comentado)
- `repositories/UserTokenRepository.java` (completamente comentado)
- `services/MailerService.java` → `generateVerifyEmail()` (comentado)
- `controller/AuthController.java` → `GET /v1/auth/verify` y `POST /v1/auth/requestVerify` (comentados)
- `models/enums/TokenType.java` → `EMAIL_VERIFICATION`
- `utils/DeepLinkUtils.java` → `verifyEmail()` (implementado, listo para usar)
- `models/exceptions/EmailNotVerifiedException.java`

**Descripción:** al registrarse, el usuario recibe un email con un link/deeplink para verificar su cuenta. El campo `emailVerified` y `emailVerifiedAt` ya existen en `UserEntity`.

**Dependencia externa:** SendGrid + Thymeleaf (ya en `pom.xml`). Requiere configurar la API key de SendGrid.

---

### FEAT-03 — Reset de contraseña
**Archivos involucrados:**
- `models/entities/UserTokenEntity.java` (completamente comentado)
- `repositories/UserTokenRepository.java` (completamente comentado)
- `services/MailerService.java` → `generatePasswordResetEmail()` (comentado)
- `controller/AuthController.java` → `POST /v1/auth/requestPasswordReset`, `GET /v1/auth/resetPasswordRedirect`, `POST /v1/auth/resetPassword` (comentados)
- `models/enums/TokenType.java` → `PASSWORD_RESET`
- `utils/DeepLinkUtils.java` → `resetPassword()` (implementado, listo para usar)
- `dtos/request/ResetPasswordRequest.java`, `RequestPasswordResetRequest.java`

**Descripción:** el usuario solicita un reset de contraseña por email. Se genera un token de un solo uso (almacenado como hash SHA-256 en `UserTokenEntity`) con expiración de 1 hora. El link redirige a la app mediante deeplink `virtualclubs://pass/reset-password?token=...`.

**Propiedades ya configuradas:** `token.email-verification-expiration=172800000` (48h), `token.password-reset-expiration=3600000` (1h) en `application-dev.properties`.

---

## 8. Deuda técnica conocida

Extraída del `CLAUDE.md` del proyecto:

| Prioridad | Problema | Dónde | Ver sección |
|-----------|----------|-------|-------------|
| ALTA | Sin rate limiting en `/login` y `/register` | `SecurityConfig.java` | SEG-01 |
| ALTA | `ddl-auto=update` en producción | `application.properties` | MEJORA-01 |
| ALTA | HTTP 403 incorrecto en login fallido | `JwtAuthLoginFilter.java` | BUG-02 |
| MEDIA | Google OAuth2 preparado pero comentado | `GoogleAuthService.java` | FEAT-01 |
| MEDIA | Email verification y password reset comentados | `MailerService`, `UserTokenService` | FEAT-02, FEAT-03 |
| MEDIA | JWT malformado devuelve 500 en vez de 401 | `AuthController.java` | BUG-03 |
| MEDIA | `FetchType.EAGER` en relaciones JPA | `UserEntity.java` | MEJORA-02 |
| BAJA | `AsyncConfig` configurado pero sin uso real | `AsyncConfig.java` | COD-02 |
| BAJA | Inyección de dependencias mixta | varios servicios | MEJORA-03 |
| BAJA | `removeToken()` elimina todos los tokens | `RefreshTokenServiceImpl.java` | BUG-01 |

---

*Para implementar cualquiera de estos puntos, crear una rama `feature/<nombre>` o `fix/<nombre>` desde `development`.*
