# /do-task — Resolver una Tarea Completa de Notion

Ejecuta el ciclo completo de desarrollo para una tarjeta Notion: crea la rama, implementa todos los checkboxes, hace commit, crea el PR y lo revisa. Modo autónomo — solo pide confirmación una vez al inicio.

## Uso
- `/do-task VC-5` — resuelve la tarea VC-5 de Notion
- `/do-task 5` — equivalente a VC-5

---

## Paso 1: Validaciones previas

Ejecuta `git status`. Si hay cambios sin commitear, detente:
> "⛔ Tienes cambios sin commitear. Haz `/commit` o stash antes de continuar."

Verifica que la rama actual no sea `main` ni `development` con cambios activos.

---

## Paso 2: Obtener la tarea de Notion

Busca la tarjeta con `notion-search`:
- `data_source_url`: `"collection://276a7f5d-0a0f-802c-8d6f-000b821853c1"`
- `query`: el número N o `VC-N`

Si hay varios resultados, filtra por `userDefined:ID` igual al número proporcionado. Usa `notion-fetch` para obtener el contenido completo de la página (propiedades + cuerpo con checkboxes).

Extrae:
- `Nombre de la tarea`, `Tipo de tarea`, `Descripción`, `page_id`
- Lista de checkboxes `- [ ]` del cuerpo de la página — son los pasos de implementación

Si la tarjeta no existe, detente y avisa al usuario.

---

## Paso 3: Mostrar resumen y pedir confirmación ÚNICA

Muestra al usuario todo lo que se va a hacer y espera **una sola confirmación** antes de ejecutar el flujo completo:

```
Tarea encontrada:
  VC-[N] — [Nombre de la tarea]
  Tipo: [Tipo de tarea]
  Descripción: [primeros 150 chars]

Pasos de implementación:
  [ ] [checkbox 1]
  [ ] [checkbox 2]
  [ ] [checkbox 3]
  ...

Flujo que se ejecutará:
  1. Crear rama [nombre-rama] desde development
  2. Implementar cada paso de la lista
  3. Compilar y pasar tests
  4. Commit semántico
  5. Crear PR hacia development
  6. Revisión automática del PR

¿Ejecutar todo el flujo? (sí/no)
```

Si el usuario dice no, detente sin hacer nada.

---

## Paso 4: Crear la rama

**Determinar prefijo** según `Tipo de tarea`:

| Tipo | Prefijo |
|---|---|
| 🐞 Error | `fix/` |
| 🔎 Testing | `test/` |
| 🛠️ Funcionalidad / 💻 BackEnd / ⛓️ API / 🔒 Autenticación / 📊 Base de datos | `feature/` |
| ✏️ Diseño | `chore/` |
| (vacío o no coincide) | `chore/` |

**Nombre de rama:** kebab-case ASCII, sin tildes (á→a, é→e, í→i, ó→o, ú→u, ñ→n), sin caracteres especiales. Formato: `[prefijo]/vc-[N]-[nombre-kebab]`. Máximo 55 caracteres.

```bash
git fetch origin development
git checkout development
git pull origin development
git checkout -b [nombre-rama]
git push --set-upstream origin [nombre-rama]
```

Actualiza el estado en Notion a `💻 En curso` con `notion-update-page`.

---

## Paso 5: Implementar los checkboxes

Lee **todos** los `- [ ]` del cuerpo de la página Notion. Para cada uno:

1. **Analiza** qué cambio de código o acción requiere el paso
2. **Implementa** el cambio en los archivos correspondientes
3. **Verifica** que el cambio es correcto (lee el archivo modificado para confirmar)
4. **Marca** el checkbox en Notion con `notion-update-page` → `command: "update_content"`, cambiando `- [ ] [texto]` por `- [x] [texto]`

**Tipos de pasos y cómo ejecutarlos:**
- Pasos que buscan algo en el código (`buscar`, `verificar si existe`) → usa `Grep` o `Read`
- Pasos que modifican código → usa `Edit` o `Write`
- Pasos que crean archivos de migración SQL → usa `Write` en `src/main/resources/db/migration/`
- Pasos de compilación (`compilar`, `mvn compile`) → ejecuta `./mvnw compile -q`
- Pasos de tests (`ejecutar tests`, `mvn test`) → ejecuta `./mvnw test`
- Pasos de verificación manual → márcalos como hechos si ya están verificados por pasos anteriores

Si un paso **falla** (compilación, test, o el cambio no es posible), detente y avisa al usuario con el error concreto antes de continuar.

---

## Paso 6: Verificación final

Si los tests no se ejecutaron como parte de los checkboxes, ejecuta:
```bash
./mvnw test
```

Si hay fallos, detente y avisa al usuario. No hagas commit con tests en rojo.

---

## Paso 7: Commit semántico

Determina el tipo de commit según los cambios realizados:
- `feat` / `fix` / `refactor` / `chore` / `perf` / `test` / `docs`

Construye el mensaje con referencia a la tarjeta:
```
[tipo]([scope]): descripción concisa en español

Ref: VC-[N]

Co-Authored-By: Claude Sonnet 4.6 <noreply@anthropic.com>
```

Muestra el mensaje al usuario y espera confirmación antes de hacer el commit. Opciones:
- `sí` / `ok` → procede
- `editar [nuevo mensaje]` → usa el nuevo mensaje
- `cancelar` → detente

```bash
git add [archivos modificados — NO usar git add -A sin confirmar]
git commit -m "[mensaje]"
git push origin [rama-actual]
```

> **Nota sobre `git add`:** lista los archivos que vas a añadir al staging y pregunta si están todos correctos antes de hacer el commit. Nunca uses `git add -A` sin mostrarlo primero.

---

## Paso 8: Crear el Pull Request

Genera título (máx 70 chars) y descripción con secciones:
- **¿Qué hace este PR?** — 2-3 puntos basados en los checkboxes completados
- **Cambios principales** — archivos modificados
- **Cómo probar** — pasos concretos derivados de la tarea
- **Checklist** — con los ítems ya marcados como `[x]`

```bash
gh pr create --title "[título]" --body "[descripción]" --base development
```

Actualiza el estado en Notion a `📬 PR Abierto`.

---

## Paso 9: Revisión automática del PR

Analiza el diff del PR recién creado evaluando:

### 🔴 Errores Críticos
Excepciones sin manejar, NPE potenciales, lógica rota, compilación fallida, `TODO`/`FIXME`/`System.out.println` olvidados.

### 🔐 Seguridad (OWASP Top 10)
SQL injection, XSS, tokens validados, datos sensibles en logs, endpoints sin protección, credentials hardcodeadas.

### 📚 Documentación
Javadoc en métodos públicos nuevos, CLAUDE.md actualizado si hay endpoints o patrones nuevos.

### 🏗️ Arquitectura
Separación de capas, uso de `ApiResponse<T>`, `@Transactional` donde corresponde, código duplicado.

### ⚡ Rendimiento
Queries N+1, operaciones costosas sin `@Async`.

### ✅ Tests
Cobertura de casos de error además del happy path.

**Veredicto y actualización de Notion:**

| Veredicto | Estado Notion |
|-----------|---------------|
| APROBADO | `📦 Pendiente debug` |
| APROBADO CON SUGERENCIAS | `📦 Pendiente debug` |
| CAMBIOS REQUERIDOS | detente y explica qué corregir |

---

## Paso 10: Confirmar resultado final

Muestra un resumen de todo lo ejecutado:

```
✅ Tarea VC-[N] completada:
  Rama:    [nombre-rama]
  Commits: [N] commit(s)
  PR:      #[numero] — [URL]
  Notion:  📦 Pendiente debug
  Tests:   [N] tests, 0 fallos
```