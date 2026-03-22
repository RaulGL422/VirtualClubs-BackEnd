# /commit — Commit Semántico + Push

Ejecuta el siguiente flujo de commit completo:

## Paso 1: Verificar rama actual

Ejecuta `git branch --show-current` y verifica que la rama actual **NO sea** `main` ni `development`.

Si la rama es `main` o `development`, **detente inmediatamente** y avisa al usuario:
> "⛔ Estás en la rama `[nombre]`. No está permitido hacer commits directamente en `main` o `development`. Crea una rama nueva con `/new-feature` o cambia de rama manualmente."

## Paso 2: Ver estado del repositorio

Ejecuta en paralelo:
- `git status`
- `git diff --staged`

## Paso 3: Evaluar si hay cambios staged

**Si NO hay nada en staged (`git diff --staged` está vacío):**

Ejecuta `git status` para mostrar los archivos modificados/nuevos y pregunta al usuario:
> "No hay cambios en staging. Encontré los siguientes archivos modificados:
> [lista de archivos]
>
> ¿Quieres que haga `git add -A` para agregar todos? (sí/no) O indícame qué archivos específicos agregar."

Espera la confirmación antes de continuar.

**Si hay archivos staged:**
Continúa directamente al Paso 4.

## Paso 4: Analizar los cambios

Lee el diff completo con `git diff --staged` y determina:

1. **Tipo de commit** según los cambios:
   - `feat` — nueva funcionalidad
   - `fix` — corrección de bug
   - `refactor` — refactoring sin cambio de comportamiento
   - `chore` — tareas de mantenimiento, configuración, dependencias
   - `docs` — solo documentación
   - `test` — solo tests
   - `style` — formato, espacios (sin cambio de lógica)
   - `perf` — mejora de rendimiento
   - `ci` — cambios en CI/CD

2. **Scope opcional** (en qué módulo): `auth`, `security`, `tokens`, `user`, `config`, `deps`, etc.

3. **Descripción** en español, concisa, en imperativo (ej: "agrega endpoint de logout")

## Paso 5: Proponer mensaje de commit

Muestra al usuario el resumen de archivos staged (`git diff --staged --stat`) y el mensaje propuesto:

```
Archivos que entran en este commit:
  [output de git diff --staged --stat]

Mensaje propuesto:
  [tipo](scope): descripción corta en español

  Descripción más detallada si es necesario (opcional).
```

Ejemplos de mensajes:
- `feat(auth): agrega endpoint de verificación de email`
- `fix(tokens): corrige expiración incorrecta del refresh token`
- `refactor(security): extrae lógica de hash a TokenUtils`
- `chore(deps): actualiza jjwt a versión 0.12.0`

**Espera confirmación explícita del usuario antes de continuar.** Opciones:
- `sí` / `ok` / `confirmar` → procede al Paso 6
- `editar [nuevo mensaje]` → usa el nuevo mensaje y confirma de nuevo
- `cancelar` → detente sin hacer commit

## Paso 6: Confirmar y hacer commit

Ejecuta el commit solo después de recibir confirmación explícita del usuario.

Luego ejecuta `git push origin [rama-actual]`.

Si el push falla porque la rama no tiene upstream, usa:
`git push --set-upstream origin [rama-actual]`

## Paso 7: Confirmar resultado

Muestra el resultado del push y el hash del commit creado.
