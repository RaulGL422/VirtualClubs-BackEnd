# /explain — Explicar un Archivo o Concepto

Explica un archivo del proyecto o un concepto técnico en términos claros, pensado para alguien que está aprendiendo.

## Uso
- `/explain JwtAuthFilter`
- `/explain SecurityConfig`
- `/explain TokensService`
- `/explain refresh token`
- `/explain rebase`

## Paso 1: Determinar si es un archivo del proyecto o un concepto general

**Si es un archivo del proyecto:**
Búscalo en `src/main/java/` y léelo completo antes de responder.

**Si es un concepto general** (JWT, rebase, BCrypt, etc.):
Responde desde conocimiento general, con ejemplos concretos del proyecto cuando sea posible.

## Paso 2: Estructura de la explicación

Adapta el nivel al desarrollador junior que usa este proyecto. Sigue este orden:

### ¿Qué es? (1-2 oraciones)
La respuesta más corta posible a "¿para qué sirve esto?".

### ¿Por qué existe en este proyecto?
El problema concreto que resuelve. Si es un archivo, qué pasaría si no existiera.

### ¿Cómo funciona? (el núcleo)
Explica el flujo paso a paso con lenguaje simple.
- Usa analogías del mundo real cuando ayuden
- Para archivos Java: explica los métodos principales uno por uno
- Señala las líneas o secciones más importantes con el número de línea

### ¿Cómo se conecta con el resto del proyecto?
Qué lo llama, qué llama él, de qué depende.

### ¿Qué NO hace? (si aplica)
Límites o confusiones comunes sobre su responsabilidad.

### Ejemplo concreto (si aplica)
Un flujo real del proyecto que use esta pieza (ej: "cuando el usuario hace login, esto es lo que pasa en este archivo...").

## Paso 3: Preguntar si hay dudas

Al final, ofrece profundizar en algún aspecto específico.
