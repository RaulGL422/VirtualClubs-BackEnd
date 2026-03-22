# /create-pr — Crear Pull Request

Crea un PR desde la rama actual hacia `development` (por defecto) o la base especificada.

## Uso
- `/create-pr` — crea PR hacia `development`
- `/create-pr main` — crea PR hacia `main`

## Paso 1: Verificar precondiciones

Ejecuta `git branch --show-current`.

Si la rama es `main` o `development`, detente:
> "No puedes crear un PR desde `main` o `development`."

La rama base es `development` por defecto. Si se pasó un argumento (ej. `main`), úsalo como base.

**Detectar tarjeta Notion:** Si el nombre de la rama contiene el patrón `vc-[N]`, extrae el número N para usarlo más adelante.

Verifica que hay commits por encima de la rama base:
```bash
git log [rama-base]...HEAD --oneline
```

Si no hay commits, detente:
> "No hay cambios para crear un PR. Haz commits primero con `/commit`."

## Paso 2: Revisar los cambios

Ejecuta en paralelo (usando [rama-base] determinada en el paso anterior):
- `git log [rama-base]...HEAD --oneline` — ver todos los commits del PR
- `git diff [rama-base]...HEAD --stat` — resumen de archivos cambiados
- `git diff [rama-base]...HEAD` — diff completo para entender el contexto

## Paso 3: Verificar autenticación de gh

```bash
gh auth status
```

Si no está autenticado, detente y avisa:
> "El CLI de GitHub (`gh`) no está autenticado. Ejecuta `gh auth login` en tu terminal y vuelve a intentarlo."

## Paso 4: Generar título y descripción del PR

Basándote en los commits y el diff, genera:

**Título** (máx 70 caracteres): Claro y descriptivo, en español.

**Descripción** con las secciones:
- **¿Qué hace este PR?** — Resumen en 2-3 puntos
- **Cambios principales** — Lista de archivos y qué cambió
- **Cómo probar** — Pasos para verificar que funciona
- **Checklist** — Checkboxes de: tests, documentación, CLAUDE.md actualizado, seguridad revisada

## Paso 5: Confirmar y crear el PR

Muestra al usuario el título y la descripción generados y pregunta:
> "¿Creo el PR con este título y descripción? (sí/editar/cancelar)"

Espera confirmación antes de continuar.

Si confirma, primero empuja la rama y luego crea el PR:
```bash
git push --set-upstream origin [rama-actual]
gh pr create --title "[título]" --body "[descripción]" --base [rama-base]
```

## Paso 6: Actualizar estado en Notion (si aplica)

Si se detectó una tarjeta Notion (VC-N en el nombre de rama), actualiza su estado a `📬 PR Abierto`:
- Busca la página con `notion-search` en `data_source_url: "collection://276a7f5d-0a0f-802c-8d6f-000b821853c1"` usando el número N
- Usa `notion-update-page` con `command: "update_properties"` y `properties: {"Estado": "📬 PR Abierto"}`

## Paso 7: Lanzar revisión automática

Después de crear el PR, ejecuta automáticamente `/review-pr [numero]` para obtener la revisión inmediata del PR recién creado.

Muestra la URL del PR al final.
