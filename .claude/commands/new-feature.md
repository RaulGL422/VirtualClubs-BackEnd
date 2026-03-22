# /new-feature — Crear Nueva Rama de Feature

Crea una rama de trabajo correctamente nombrada a partir de `development` (por defecto) o de la rama especificada.

## Uso
- `/new-feature agregar endpoint de perfil de usuario` — crea desde `development`
- `/new-feature agregar endpoint de perfil de usuario --from main` — crea desde `main`
- `/new-feature agregar endpoint de perfil de usuario --from feature/auth` — crea desde otra rama

## Paso 1: Verificar estado de trabajo

Ejecuta `git status`. Si hay cambios sin commitear, avisa al usuario:
> "Tienes cambios sin commitear en la rama actual. ¿Quieres que haga stash antes de cambiar de rama? (sí/no)"

## Paso 2: Determinar tipo y nombre

Basándote en la descripción del argumento, determina:

**Tipo de rama:**
- `feature/` — nueva funcionalidad
- `fix/` — corrección de bug
- `refactor/` — refactoring
- `chore/` — tareas de mantenimiento
- `docs/` — solo documentación

**Nombre:** kebab-case, **solo ASCII** — sin tildes ni ñ (máximo 40 caracteres), ej:
- `feature/user-profile-endpoint`
- `fix/refresh-token-expiration`
- `chore/update-dependencies`

Propón el nombre al usuario antes de crear la rama.

## Paso 3: Crear la rama desde la rama base

La rama base es `development` por defecto. Si el usuario pasó `--from <rama>`, usa esa.

```bash
git fetch origin [rama-base]
git checkout [rama-base]
git pull origin [rama-base]
git checkout -b [tipo/nombre-propuesto]
```

## Paso 4: Push inicial al remoto

Empuja la rama inmediatamente para que quede trackeada desde el inicio:
```bash
git push --set-upstream origin [nombre-rama]
```

## Paso 5: Confirmar

Muestra la rama creada, la URL del remoto, y recuerda al usuario usar `/commit` cuando termine sus cambios.
