# /new-feature — Crear Nueva Rama de Feature

Crea una rama de trabajo correctamente nombrada desde `development` (por defecto) o desde la rama especificada. Soporta integración con Notion: si se pasa un ID de tarjeta, obtiene los detalles directamente de la base de datos.

## Uso
- `/new-feature VC-5` — crea rama a partir de la tarjeta Notion #5
- `/new-feature 5` — equivalente a VC-5
- `/new-feature agregar endpoint de perfil` — modo libre, sin Notion
- `/new-feature VC-5 --from main` — tarjeta Notion desde otra rama base

---

## Paso 1: Verificar estado de trabajo

Ejecuta `git status`. Si hay cambios sin commitear, avisa al usuario:
> "Tienes cambios sin commitear en la rama actual. ¿Quieres que haga stash antes de cambiar de rama? (sí/no)"

---

## Paso 2: Determinar origen de la tarea

Detecta si el argumento tiene formato `VC-N`, `vc-N` o es un número entero (ej: `5`, `VC-5`, `vc-5`).

- **Si es ID de Notion** → sigue el **Paso 2A**
- **Si es descripción libre** → sigue el **Paso 2B**

---

### Paso 2A: Obtener tarea desde Notion

Busca la tarjeta usando `notion-search` con:
- `data_source_url`: `"collection://276a7f5d-0a0f-802c-8d6f-000b821853c1"`
- `query`: el nombre o número de la tarea (prueba con el número, luego con `VC-N` si no aparece)

Si la búsqueda devuelve varios resultados, busca el que tenga `userDefined:ID` igual al número proporcionado. Si es necesario, haz `notion-fetch` a la URL del resultado para obtener todos los detalles.

Extrae del resultado:
- `Nombre de la tarea`
- `Tipo de tarea` (array multi_select)
- `Descripción` (primeros 150 caracteres para mostrar al usuario)
- ID/URL de la página (para actualizar el estado después)

**Determinar prefijo de rama según `Tipo de tarea`:**

| Tipo de tarea presente | Prefijo |
|---|---|
| 🐞 Error | `fix/` |
| 🔎 Testing | `test/` |
| 🛠️ Funcionalidad / 💻 BackEnd / ⛓️ API / 🔒 Autenticación / 📊 Base de datos | `feature/` |
| ✏️ Diseño | `chore/` |
| (ninguno coincide o campo vacío) | `chore/` |

Si hay varios tipos, aplica la prioridad: Error > Testing > Funcionalidad/BackEnd/API/Auth/BD > Diseño.

**Generar nombre de rama:**
- Convierte `Nombre de la tarea` a kebab-case ASCII: minúsculas, sin tildes (á→a, é→e, í→i, ó→o, ú→u), sin ñ (ñ→n), sin caracteres especiales, espacios → guiones
- Formato: `[prefijo]/vc-[N]-[nombre-kebab]`
- Máximo 55 caracteres en total; trunca el nombre si hace falta
- Ejemplo: `fix/vc-3-refresh-token-expiracion`, `feature/vc-5-verificar-email`

Muestra al usuario antes de continuar:
```
Tarjeta Notion encontrada:
  #[N] — [Nombre de la tarea]
  Tipo: [Tipo de tarea]
  Descripción: [primeros 150 chars]

Rama propuesta: [nombre-de-rama]
Base: [rama-base]

¿Confirmar? (sí/no)
```

---

### Paso 2B: Modo libre (sin Notion)

Basándote en la descripción, determina tipo y nombre:

**Tipo de rama:**
- `feature/` — nueva funcionalidad
- `fix/` — corrección de bug
- `refactor/` — refactoring
- `test/` — tests
- `chore/` — mantenimiento, dependencias
- `docs/` — solo documentación

**Nombre:** kebab-case, solo ASCII (sin tildes ni ñ), máximo 40 caracteres.

Propón el nombre al usuario antes de crear la rama.

---

## Paso 3: Crear la rama

La rama base es `development` por defecto. Si el usuario pasó `--from <rama>`, usa esa.

```bash
git fetch origin [rama-base]
git checkout [rama-base]
git pull origin [rama-base]
git checkout -b [nombre-rama]
```

## Paso 4: Push inicial al remoto

```bash
git push --set-upstream origin [nombre-rama]
```

## Paso 5: Actualizar Notion (solo si vino del Paso 2A)

Actualiza el estado de la tarjeta a `💻 En curso`:
- Usa `notion-update-page` con `command: "update_properties"`
- `properties`: `{"Estado": "💻 En curso"}`
- `page_id`: el ID de la página obtenida en el Paso 2A

## Paso 6: Confirmar

Muestra la rama creada y, si aplica, confirma que el estado en Notion fue actualizado a "💻 En curso". Recuerda al usuario usar `/commit` para los cambios y `/create-pr` cuando termine.
