# /release-debug — Publicar Release a Debug

Agrupa las features con `📦 Pendiente debug` en Notion, genera la entrada de CHANGELOG, propone el bump de versión y crea la rama de release. Al confirmar, actualiza los archivos, crea el PR y mueve las tarjetas a `⌛🔎 Pendiente de Testeo`.

## Uso
- `/release-debug` — modo interactivo completo, Claude propone el bump de versión
- `/release-debug --patch` — fuerza bump de patch (0.0.X)
- `/release-debug --minor` — fuerza bump de minor (0.X.0)

---

## Paso 1: Verificar estado del repositorio

Ejecuta `git branch --show-current` y `git status` en paralelo.

Si hay cambios sin commitear, **detente**:
> "Hay cambios sin commitear. Haz commit primero con `/commit`."

Si la rama actual no es `development`, avisa pero permite continuar:
> "Estás en `[rama]`, no en `development`. El release se creará como una rama desde `development`. ¿Continuar? (sí/no)"

---

## Paso 2: Obtener features pendientes de Notion

La búsqueda semántica de Notion no filtra por valor de propiedad — para encontrar **todas** las tarjetas con `📦 Pendiente debug` hay que lanzar varias búsquedas con términos distintos y luego verificar el estado de cada resultado individualmente.

### 2.1 — Búsquedas en paralelo

Lanza estas 4 búsquedas en paralelo usando `notion-search`, todas con `data_source_url: "collection://276a7f5d-0a0f-802c-8d6f-000b821853c1"` y `page_size: 25`:

| # | `query` | Objetivo |
|---|---------|----------|
| A | `"implementar funcionalidad backend"` | Captura tareas de feature/API |
| B | `"bug error corregir fix"` | Captura tarjetas de errores |
| C | `"mejora refactor tests arquitectura"` | Captura tareas internas |
| D | `"VirtualClubs tarea"` | Red de seguridad amplia |

Combina los resultados de las 4 búsquedas eliminando duplicados por `id`.

### 2.2 — Verificar estado de cada resultado

Para cada página encontrada (en paralelo, máx. 6 a la vez), usa `notion-fetch` con el `id` de la página y lee la propiedad `Estado` del JSON de propiedades.

**Incluye en el release únicamente las que tengan `"Estado": "📦 Pendiente debug"`.**

Para cada tarjeta incluida, extrae:
- `userDefined:ID` → número VC-N
- `Nombre de la tarea`
- `Tipo de tarea` (multi_select, para clasificar en CHANGELOG)
- `id` de la página (para actualizar estado en el Paso 10)

### 2.3 — Si no hay tarjetas confirmadas en `📦 Pendiente debug`

> "No encontré tarjetas en estado 'Pendiente debug' en Notion. ¿Continuar igualmente documentando los commits recientes? (sí/no)"
>
> Si sí → usa solo git log (Paso 3). Si no → detente.

---

## Paso 3: Revisar commits recientes para contexto

Ejecuta:
- `git tag --sort=-version:refname | head -1` para obtener el último tag de versión
- Si existe tag `vX.Y.Z`: `git log vX.Y.Z..HEAD --oneline`
- Si no hay tags: `git log --oneline -30`

Esta información complementa lo que ya traen las tarjetas Notion.

---

## Paso 4: Leer versión actual

Lee `pom.xml` y extrae el valor de `<version>` del proyecto (segunda ocurrencia — la primera es la versión del parent Spring Boot). Elimina el sufijo `-SNAPSHOT` si lo tiene → versión base.

Lee `CHANGELOG.md` y extrae la última versión publicada (el encabezado `## [X.Y.Z]` más reciente, ignorando `[Sin publicar]`).

Usa como versión base la mayor de las dos.

---

## Paso 5: Proponer bump de versión

Si se pasó `--patch` o `--minor`, úsalo directamente. Si no, analiza:

**Reglas de bump:**

| Señal en las tarjetas o commits | Bump propuesto |
|---------------------------------|---------------|
| Tarjeta con tipo `🛠️ Funcionalidad`, `⛓️ API`, `🔒 Autenticación`, `📊 Base de datos` o commit `feat(*)` | **MINOR** — (0.X.0) |
| Solo tipos `🐞 Error`, `🔎 Testing`, `✏️ Diseño`, `💻 BackEnd` o commits `fix(*)`, `chore(*)`, `docs(*)` | **PATCH** — (0.0.X) |
| Sin tarjetas y sin commits de features | **PATCH** por defecto |

Muestra al usuario antes de continuar:
```
Versión actual:   X.Y.Z
Versión propuesta: X.Y.(Z+1)  ← PATCH
  Razón: [explicación en 1 línea]

¿Confirmar? (sí / --patch / --minor / cancelar)
```

---

## Paso 6: Generar entrada de CHANGELOG

Basándote en las tarjetas Notion y commits, genera la sección para la nueva versión.

**Formato de la entrada:**
```markdown
## [X.Y.Z] — AAAA-MM-DD

### Añadido
- [nuevos endpoints, módulos, funcionalidades visibles desde el cliente] — VC-N

### Cambiado
- [modificaciones en comportamiento existente, actualizaciones de dependencias] — VC-N

### Corregido
- [bugs corregidos] — VC-N

### Interno
- [tests, refactoring, documentación, mejoras sin impacto en API] — VC-N
```

**Clasificación por tipo de tarjeta:**

| Tipo de tarea | Sección CHANGELOG |
|---------------|-------------------|
| 🛠️ Funcionalidad / ⛓️ API | Añadido (si es nuevo) o Cambiado (si modifica existente) |
| 🔒 Autenticación / 📊 Base de datos | Añadido o Cambiado según corresponda |
| 🐞 Error | Corregido |
| 🔎 Testing / ✏️ Diseño / 💻 BackEnd (sin cambio de API) | Interno |

Omite las secciones vacías. Incluye el sufijo `— VC-N` al final de cada ítem que tenga tarjeta.

---

## Paso 7: Mostrar resumen completo y pedir confirmación

```
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
  Release Debug  v[X.Y.Z]
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

Tarjetas incluidas ([N]):
  VC-N — [Nombre de la tarea]  ([Tipo])
  ...

[entrada de CHANGELOG generada completa]

Archivos a modificar:
  CHANGELOG.md   nueva entrada v[X.Y.Z]
  pom.xml        [versión actual] → [X.Y.Z]
  CLAUDE.md      Versión app: [X.Y.Z]

Rama a crear:  chore/release-v[X.Y.Z]  (desde development)
Git tag:       v[X.Y.Z]

Al confirmar:
  • Se aplican los cambios en los tres archivos
  • Se crea la rama, el commit y el PR a development
  • Se crea el tag git v[X.Y.Z] localmente
  • [N] tarjetas pasan a ⌛🔎 Pendiente de Testeo en Notion

¿Proceder? (sí/cancelar)
```

---

## Paso 8: Aplicar cambios en archivos

**8.1 — `CHANGELOG.md`:**
Inserta la nueva entrada justo después de la línea `## [Sin publicar]` y antes de la última versión publicada. Separa con `---`.

**8.2 — `pom.xml`:**
Cambia `<version>[actual]</version>` del proyecto (la segunda `<version>` del archivo, no la del parent) a `<version>[X.Y.Z]</version>`.

**8.3 — `CLAUDE.md`:**
Actualiza `**Versión app:** [actual]` a `**Versión app:** [X.Y.Z]`.

---

## Paso 9: Crear rama, commit, push y PR

```bash
git fetch origin development
git checkout development
git pull origin development
git checkout -b chore/release-v[X.Y.Z]
```

Agrega solo los tres archivos modificados:
```bash
git add CHANGELOG.md pom.xml CLAUDE.md
```

Commit con formato:
```
chore(release): bump versión a v[X.Y.Z]

Incluye: [lista de VC-N separados por coma, o "sin tarjetas Notion"]

Co-Authored-By: Claude Sonnet 4.6 <noreply@anthropic.com>
```

```bash
git push --set-upstream origin chore/release-v[X.Y.Z]
```

Crea el tag localmente (no hacer push del tag todavía — el usuario decide cuándo):
```bash
git tag v[X.Y.Z]
```

Crea el PR:
```bash
gh pr create \
  --title "chore(release): versión v[X.Y.Z]" \
  --body "[sección completa del CHANGELOG + lista de tarjetas incluidas]" \
  --base development
```

---

## Paso 10: Actualizar Notion

Para **cada** tarjeta confirmada en el Paso 2 con estado `📦 Pendiente debug`, actualiza en paralelo (máx. 6 a la vez):
- `notion-update-page` con `command: "update_properties"`
- `page_id`: el ID de la página
- `properties`: `{"Estado": "⌛🔎 Pendiente de testeo"}`

> **Nota:** el valor exacto del estado es `"⌛🔎 Pendiente de testeo"` (t minúscula).

---

## Paso 11: Confirmar resultado

```
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
  ✅ Release v[X.Y.Z] preparado
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

  PR:     [URL del PR]
  Notion: [N] tarjetas → ⌛🔎 Pendiente de Testeo
  Tag:    v[X.Y.Z] creado localmente

Próximos pasos:
  1. Fusiona el PR a development
  2. El CI construirá la imagen automáticamente (push a GHCR)
  3. Actualiza el servidor debug con la nueva imagen
  4. Ejecuta pruebas manuales
  5. Si todo está OK → las tarjetas pasan a 👁️ Pendiente de Publicar (manual)
  6. Cuando publiques a producción → `git push origin v[X.Y.Z]` para subir el tag
```
