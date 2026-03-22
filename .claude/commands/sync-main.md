# /sync-main — Sincronizar Rama con Main

Actualiza la rama de trabajo actual con los últimos cambios de `main`.

## Paso 1: Verificar contexto

Ejecuta `git branch --show-current`.

Si estás en `main`, avisa:
> "Ya estás en main. ¿Quieres hacer pull directamente? (sí/no)"

## Paso 2: Verificar cambios sin commitear

Ejecuta `git status`. Si hay cambios sin commitear:
> "Tienes cambios sin commitear. Primero haz `/commit` o los cambios se perderán al hacer rebase."
Detente y espera instrucciones.

## Paso 3: Advertir sobre el rebase

Antes de ejecutar, explica al usuario:
> "Voy a hacer **rebase** sobre main. Esto reescribe el historial de tus commits para que aparezcan encima de los últimos cambios de main.
> **Importante:** si ya hiciste push de esta rama al remoto, el próximo push va a requerir `--force-with-lease` (nunca `--force` a secas).
> ¿Continúo? (sí/no)"

Espera confirmación.

## Paso 4: Actualizar main y hacer rebase

```bash
git fetch origin main
git rebase origin/main
```

## Paso 5: Manejar conflictos si los hay

Si hay conflictos, muestra la lista de archivos en conflicto:
```bash
git status --short
```

Avisa al usuario:
> "Hay conflictos en los siguientes archivos: [lista]
> Opciones:
> - `resolver` → te ayudo a resolver cada conflicto
> - `abortar` → cancelo el rebase y quedas como estabas (`git rebase --abort`)
>
> ¿Qué hacemos?"

Si dice resolver: lee cada archivo con conflicto y propón la resolución explicando qué se está manteniendo y por qué. Luego: `git rebase --continue`.

Si dice abortar: ejecuta `git rebase --abort` y confirma que la rama quedó en su estado original.

## Paso 6: Push actualizado si la rama ya estaba en remoto

Después de un rebase exitoso, verifica si la rama tiene upstream:
```bash
git status -sb
```

Si la rama ya estaba trackeada en remoto, avisa:
> "El rebase reescribió el historial. Para actualizar el remoto necesitas:
> `git push --force-with-lease origin [rama]`
> ¿Lo ejecuto? (sí/no)"

Espera confirmación antes de hacer el force push.

## Paso 7: Confirmar resultado

Muestra el log reciente (`git log --oneline -5`) para confirmar que el rebase fue exitoso.
