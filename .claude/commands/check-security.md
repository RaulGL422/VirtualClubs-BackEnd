# /check-security — Auditoría de Seguridad del Código

Realiza una revisión de seguridad enfocada en el código del proyecto.

## Uso
- `/check-security` — audita todo el código modificado desde `main`
- `/check-security src/main/java/galindo/raul/virtualclubs/controller/AuthController.java` — audita archivo específico

## Paso 1: Determinar alcance

Si se pasó un archivo como argumento, audita ese archivo.

Si no, obtén los archivos modificados desde main:
```bash
git diff main...HEAD --name-only -- "*.java"
```

Si no hay archivos modificados, audita los archivos de seguridad críticos del proyecto:
- `config/security/`
- `services/TokensService.java`
- `services/UserEntityServiceImpl.java`
- `controller/AuthController.java`

## Paso 2: Leer y analizar los archivos

Lee cada archivo en el alcance y evalúa:

---

### OWASP A01 — Broken Access Control
- ¿Endpoints protegidos con `@PreAuthorize` o configurados en `SecurityConfig`?
- ¿Hay endpoints que devuelvan datos de otros usuarios sin verificar identidad?
- ¿El usuario autenticado solo puede modificar sus propios recursos?

### OWASP A02 — Cryptographic Failures
- ¿Se usan algoritmos débiles (MD5, SHA-1 para passwords)?
- ¿Las contraseñas se hashean con BCrypt correctamente?
- ¿Los refresh tokens se almacenan como hash (SHA-256), no en texto plano?
- ¿El JWT secret es suficientemente largo y proviene de variable de entorno?

### OWASP A03 — Injection
- ¿Hay queries construidas por concatenación de strings?
- ¿Se usan métodos de Spring Data JPA o `@Query` con parámetros?
- ¿Hay deserialización de objetos sin validar?

### OWASP A04 — Insecure Design
- ¿Rate limiting en endpoints de autenticación? (login, register, reset-password)
- ¿Límite en intentos fallidos de login?
- ¿Los mensajes de error revelan información interna (stack traces, nombres de tablas)?

### OWASP A05 — Security Misconfiguration
- ¿CSRF deshabilitado apropiadamente (API stateless)?
- ¿Actuator expone solo endpoints necesarios?
- ¿CORS configurado restrictivamente (no `*`)?
- ¿Headers de seguridad HTTP presentes?

### OWASP A07 — Authentication Failures
- ¿JWT valida firma, expiración y tipo de token?
- ¿Refresh tokens son single-use o se invalidan al rotar?
- ¿Logout efectivamente invalida el token en BD?
- ¿Los tokens tienen tiempo de expiración razonable?

### OWASP A09 — Logging Failures
- ¿Se loggean eventos de seguridad (login fallido, acceso denegado)?
- ¿Los logs NO contienen passwords, tokens o datos sensibles?
- ¿Hay logging suficiente para detectar ataques?

### Específico del Proyecto
- ¿`@StrongPassword` validación es suficientemente fuerte?
- ¿El `deviceId` generado por SHA-256 es predecible/manipulable?
- ¿El manejo de Google ID tokens verifica correctamente el emisor?

---

## Paso 3: Generar reporte de seguridad

```
## Auditoría de Seguridad — [fecha]

**Archivos analizados:** [lista]

### 🔴 Vulnerabilidades Críticas (parchear urgente)
[lista con descripción, ubicación exacta (archivo:línea) y fix propuesto]

### 🟡 Vulnerabilidades Medias (parchear pronto)
[lista con descripción, ubicación y recomendación]

### 🟢 Mejoras de Hardening (buenas prácticas)
[sugerencias opcionales para fortalecer la seguridad]

### ✅ Controles Correctos
[menciona lo que sí está bien implementado]

---

**Puntuación de riesgo:** [ALTO / MEDIO / BAJO]
**Recomendación:** [acción inmediata si hay críticos]
```

Para cada vulnerabilidad encontrada, muestra el código vulnerable y el fix propuesto con código.
