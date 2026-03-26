# /add-task — Crear Tarea en Notion

Crea una nueva tarjeta en la base de datos "Registro de tareas VirtualClubs" de Notion a partir de una descripción.

## Uso
- `/add-task corregir bug en refresh token cuando el dispositivo cambia de IP`
- `/add-task implementar endpoint de perfil de usuario --prioridad alta`
- `/add-task añadir índice en tabla users para el campo email --esfuerzo pequeño`

---

## Paso 1: Analizar la descripción

Lee el argumento y extrae:

**Tipo de tarea** (multi_select, puede ser varios) — detecta según la naturaleza de la tarea:
- Si menciona "bug", "error", "fallo", "corregir", "arreglar" → `🐞 Error`
- Si menciona "test", "prueba", "cobertura" → `🔎 Testing`
- Si menciona "endpoint", "api", "ruta", "controller" → `⛓️ API`
- Si menciona "base de datos", "tabla", "índice", "migración", "entidad" → `📊 Base de datos`
- Si menciona "login", "auth", "token", "jwt", "contraseña", "sesión" → `🔒 Autenticación`
- Para features generales de backend → `🛠️ Funcionalidad` + `💻 BackEnd`
- Si lleva `--prioridad alta/medio/baja`, extrae ese valor

**Nombre limpio:** título conciso y claro en español para la tarjeta (máximo 60 caracteres).

**Prioridad por defecto:** `Medio` salvo que el argumento incluya `--prioridad alta|medio|baja`.

**Esfuerzo por defecto:** no asignado salvo que el argumento incluya `--esfuerzo pequeño|medio|grande`.

---

## Paso 2: Proponer la tarjeta al usuario

Muestra un resumen antes de crear:

```
Nueva tarea en Notion:
  Título:    [nombre propuesto]
  Tipo:      [tipos detectados]
  Prioridad: [Alta / Medio / Baja]
  Esfuerzo:  [Pequeño / Medio / Grande / —]
  Estado:    Sin Empezar

¿Crear con estos datos? (sí/editar/cancelar)
```

Espera confirmación. Si el usuario dice `editar [campo]: [valor]`, ajusta ese campo y muestra de nuevo.

---

## Paso 3: Crear la tarjeta en Notion

Usa `notion-create-pages` con:
- `parent`: `{"type": "data_source_id", "data_source_id": "276a7f5d-0a0f-802c-8d6f-000b821853c1"}`
- Propiedades: `Nombre de la tarea`, `Estado: "Sin Empezar"`, `Prioridad`, `Tipo de tarea`, `Nivel de esfuerzo` (si aplica)
- `Descripción`: resumen de 1-2 líneas generado a partir del argumento original

---

## Paso 4: Confirmar

Muestra el ID asignado por Notion (VC-N) y la URL de la tarjeta:

```
✅ Tarea creada: VC-[N] — [título]
   [URL de Notion]

Para empezar a trabajar en ella: /new-feature VC-[N]
```
