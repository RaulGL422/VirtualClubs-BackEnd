# /project-status — Estado General del Proyecto

Genera un resumen del estado actual del proyecto.

## Paso 1: Estado de Git y GitHub

Ejecuta en paralelo:
```bash
git branch --show-current
git log --oneline -8
git status --short
git stash list
gh pr list --state open --json number,title,headRefName,createdAt
```

## Paso 2: Funcionalidades comentadas

Busca código comentado de features pendientes con patrones concretos:
```bash
grep -r "GoogleAuthService\|MailerService\|UserTokenService\|UserTokenEntity\|UserTokenRepository" \
  src/main/java --include="*.java" -l
```

Busca TODOs y FIXMEs pendientes:
```bash
grep -rn "TODO\|FIXME\|HACK\|System\.out\.print" src/main/java --include="*.java"
```

## Paso 3: Generar reporte

```
## Estado del Proyecto — Virtual Clubs Backend — [fecha actual]

### Git
- Rama actual: [rama]
- Últimos commits: [lista]
- Cambios sin commitear: [N archivos] / Limpio
- Stashes guardados: [N] / Ninguno
- PRs abiertos: [lista con número y título] / Ninguno

### Deuda Técnica
- TODOs/FIXMEs encontrados: [lista con archivo:línea] / Ninguno
- Funcionalidades comentadas pendientes:
  - [ ] Google OAuth2
  - [ ] Verificación de email + Password reset
  - [ ] [otras encontradas]

### Próxima acción sugerida
[Una sola acción concreta basada en el estado actual]
```
