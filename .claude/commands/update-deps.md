# /update-deps — Revisar y Actualizar Dependencias

Revisa el `pom.xml` en busca de dependencias desactualizadas y propone actualizaciones seguras.

## Paso 1: Leer el pom.xml

Lee `pom.xml` completo y extrae todas las dependencias con sus versiones actuales.

## Paso 2: Ejecutar el plugin de versiones de Maven

```bash
./mvnw versions:display-dependency-updates -q 2>&1 | grep -E "\->" | head -40
```

Esto muestra directamente qué tiene actualizaciones disponibles.

Si el comando falla, analiza las versiones manualmente contra el conocimiento de las últimas versiones estables de cada dependencia.

## Paso 3: Clasificar las actualizaciones

Para cada dependencia con actualización disponible, clasifica:

**SEGURA (patch / minor sin breaking changes conocidos):**
- Cambio de versión patch (x.y.Z → x.y.Z+1)
- Minor en librerías maduras con buen historial de compatibilidad

**REQUIERE REVISIÓN (minor con posibles cambios):**
- jjwt — cambios de API entre versiones menores
- Spring Security — puede requerir ajustes en SecurityConfig
- google-api-client — puede tener cambios en firmas

**MAYOR / NO RECOMENDADA ahora:**
- Spring Boot major versions
- Java version changes
- Cambios que requieren migración

## Paso 4: Presentar reporte

```
## Actualizaciones de Dependencias — [fecha]

### Actualizaciones Seguras (aplico automáticamente si confirmas)
| Dependencia | Versión actual | Versión nueva | Tipo |
|-------------|---------------|---------------|------|
| commons-codec | 1.15 | 1.17.1 | patch |
| ... | | | |

### Requieren Revisión (te explico qué cambia antes de actualizar)
| Dependencia | Versión actual | Versión nueva | Riesgo |
|-------------|---------------|---------------|--------|
| jjwt | 0.11.5 | 0.12.x | API cambia en 0.12 |
| ... | | | |

### No Recomendadas Ahora
[dependencias con major bump o razones para no actualizar]

---
¿Quieres que aplique las actualizaciones seguras? (sí/no)
¿Quieres que te explique los cambios de alguna dependencia específica? (nombre)
```

## Paso 5: Aplicar actualizaciones confirmadas

Si el usuario confirma, edita el `pom.xml` actualizando solo las versiones aprobadas.

Luego ejecuta para verificar que compila:
```bash
./mvnw compile -q
```

Si hay error de compilación después de actualizar, revierte el cambio específico y reporta qué dependencia causó el problema.

## Paso 6: Recordatorio

Al terminar, sugiere hacer `/commit` con tipo `chore(deps): actualiza dependencias`.
