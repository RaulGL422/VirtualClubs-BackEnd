# /review-pr — Revisión de Pull Request

Revisa el PR actual (o el especificado como argumento) de forma exhaustiva.

## Uso
- `/review-pr` — revisa el PR de la rama actual
- `/review-pr 42` — revisa el PR número 42

## Paso 1: Identificar el PR

Si se pasó un número como argumento, usa ese PR.

Si no, ejecuta:
```
git branch --show-current
gh pr list --head [rama-actual] --json number,title,url,baseRefName
```

Si no existe PR para la rama actual, avisa al usuario y detente.

## Paso 2: Obtener información del PR

Ejecuta:
```
gh pr view [numero] --json title,body,baseRefName,headRefName,additions,deletions,changedFiles,commits
gh pr diff [numero]
```

## Paso 3: Revisar el código (análisis exhaustivo)

Analiza el diff completo evaluando **todas** las categorías siguientes:

---

### 🔴 ERRORES CRÍTICOS (bloquean el merge)

- Excepciones sin manejar que podrían causar 500
- NullPointerException potenciales (sin validación previa)
- Lógica de negocio incorrecta o broken
- Migraciones de BD incompatibles o destructivas
- Compilación rota
- `TODO`, `FIXME`, `HACK` o `System.out.println` olvidados en el diff
- Descripción del PR vacía o sin secciones requeridas

---

### 🔐 SEGURIDAD (basado en OWASP Top 10)

- [ ] **SQL Injection:** ¿Se usan queries parametrizadas? ¿No hay concatenación de strings en queries?
- [ ] **XSS:** ¿Se sanitiza input del usuario antes de devolver en respuestas?
- [ ] **Broken Authentication:** ¿Tokens validados correctamente? ¿Expiraciones correctas?
- [ ] **Sensitive Data Exposure:** ¿Passwords, tokens o datos sensibles en logs o respuestas?
- [ ] **Broken Access Control:** ¿Endpoints nuevos tienen la anotación de seguridad correcta?
- [ ] **Security Misconfiguration:** ¿Se exponen endpoints de actuator o info innecesarios?
- [ ] **Credentials hardcodeadas:** ¿Hay secrets o contraseñas en el código?
- [ ] **JWT handling:** ¿Se verifica la firma? ¿Se valida expiración? ¿Se usa `ApiResponse` estándar?

---

### 📚 DOCUMENTACIÓN

- [ ] ¿Los métodos públicos nuevos tienen Javadoc?
- [ ] ¿Los endpoints REST tienen comentarios que expliquen el propósito?
- [ ] ¿Los campos en entidades y DTOs tienen descripción cuando no son evidentes?
- [ ] ¿Las excepciones nuevas tienen un mensaje claro?
- [ ] ¿Se actualizó `CLAUDE.md` si se agregaron endpoints o patrones nuevos?

---

### 🏗️ ARQUITECTURA Y MALAS PRÁCTICAS

- ¿Se respeta la separación de capas? (Controller no llama a Repository directamente)
- ¿Los DTOs no contienen lógica de negocio?
- ¿Se usa `ApiResponse<T>` en todos los endpoints? (`ApiResponse.success(data)`, `ApiResponse.emptySuccess()`, `ApiResponse.error(ErrorType.X)`)
- ¿Los endpoints nuevos usan `@RequestBody @Valid` para activar las validaciones del DTO?
- ¿Las excepciones de negocio están en `GlobalExceptionHandler`?
- ¿Se usa `@Transactional` donde corresponde?
- ¿Hay código duplicado que podría extraerse a utilidades?
- ¿Se usó Lombok correctamente (`@Data`, `@Builder`, `@RequiredArgsConstructor`)?
- ¿Hay imports sin usar o código comentado innecesario?
- ¿Los nombres de variables y métodos son descriptivos y en inglés?
- ¿Los endpoints siguen la convención REST (verbos HTTP correctos, plural para colecciones)?

---

### ⚡ RENDIMIENTO

- ¿Hay queries N+1 (fetch sin JOIN cuando se necesitan relaciones)?
- ¿Se usan índices en campos que se buscan frecuentemente?
- ¿Operaciones costosas que deberían ser asíncronas (`@Async`, ya configurado en `AsyncConfig`)?
- ¿Se pagina correctamente cuando se devuelven listas?

---

### ✅ TESTS

- ¿El nuevo código tiene tests unitarios o de integración en `src/test/`?
- ¿Se cubren los casos de error además del happy path?
- **Nota:** No es posible verificar si los tests pasan solo con el diff. Si hay tests nuevos, indicar que deben ejecutarse manualmente antes del merge.

---

## Paso 4: Generar reporte

Presenta el resultado con este formato:

```
## Revisión PR #[numero]: [título]

**Base:** [rama-base] ← [rama-head]
**Cambios:** +[adiciones] / -[eliminaciones] en [N] archivos

---

### 🔴 Errores Críticos
[lista o "Ninguno encontrado"]

### 🔐 Problemas de Seguridad
[lista o "Ninguno encontrado"]

### 📚 Documentación Faltante
[lista o "Completa"]

### 🏗️ Malas Prácticas
[lista o "Ninguna encontrada"]

### ⚡ Problemas de Rendimiento
[lista o "Ninguno encontrado"]

### ✅ Estado de Tests
[observaciones]

---

### Veredicto
[APROBADO / APROBADO CON SUGERENCIAS / CAMBIOS REQUERIDOS]

### Próximos pasos sugeridos:
1. [acción concreta]
2. [acción concreta]
```

Si hay errores críticos o problemas de seguridad, explica el riesgo y propón el fix concreto con código.

---

## Paso 5: Actualizar documentación si corresponde

Después de generar el reporte, lee `CLAUDE.md` y determina si el PR introduce cambios que lo dejan desactualizado. Evalúa estas secciones específicas:

**Tabla de Endpoints Activos**
¿El PR agrega, elimina o modifica algún endpoint? Si es así, actualiza la tabla.

**Endpoints Comentados**
¿El PR reactiva alguna funcionalidad que estaba comentada (Google OAuth2, email, password reset)? Si es así, muévela a "Activos" y elimínala de "Comentados".

**Deuda Técnica Conocida**
¿El PR resuelve algún ítem de la tabla de deuda técnica (agrega tests, implementa rate limiting, etc.)? Si es así, elimina esa fila.
¿El PR introduce deuda nueva (código comentado, TODO, funcionalidad a medias)? Agrégala.

**Variables de Entorno**
¿El PR añade nuevas variables de entorno requeridas? Agrégalas a la sección correspondiente.

**Patrones y Convenciones**
¿El PR introduce un patrón nuevo que no está documentado y que se usará en el futuro? Agrégalo con un ejemplo.

Si no hay nada que actualizar en `CLAUDE.md`, indica: `"CLAUDE.md está al día, no requiere cambios."`

Si hay cambios, aplícalos directamente y al final muestra un resumen:
```
### Documentación actualizada
- [sección]: [qué cambió]
- [sección]: [qué cambió]
```
