# /check-api-contract — Verificar Contrato API

Verifica que la tabla **Active Endpoints** de `CLAUDE.md` coincide exactamente con los endpoints reales en los controllers y que los permisos de seguridad son correctos.

## Uso
- `/check-api-contract` — compara docs vs código

---

## Paso 1: Leer la documentación

Lee `CLAUDE.md` y extrae la tabla **Active Endpoints**:
- Method (GET/POST/DELETE/PUT/PATCH)
- Path
- Auth (Yes/No)
- Description

---

## Paso 2: Leer los controllers

Busca todos los controllers con endpoints:
```bash
grep -r "@RestController\|@Controller" src/main/java --include="*.java" -l
```

Para cada archivo encontrado, lee el contenido y extrae:
- `@RequestMapping` a nivel de clase (prefijo de ruta base)
- Todos los `@GetMapping`, `@PostMapping`, `@PutMapping`, `@PatchMapping`, `@DeleteMapping` con sus paths
- Si están anotados con `@PreAuthorize` o si son de acceso libre

---

## Paso 3: Leer SecurityConfig

Lee `src/main/java/.../config/security/SecurityConfig.java` y extrae:
- Rutas en `permitAll()` → Auth: No
- Rutas en `authenticated()` o `hasRole(...)` → Auth: Yes

---

## Paso 4: Cruzar y detectar discrepancias

| Caso | Significado |
|------|-------------|
| En CLAUDE.md pero no en código | Documentado pero eliminado del código |
| En código pero no en CLAUDE.md | Endpoint nuevo sin documentar |
| Path coincide pero Auth difiere | Discrepancia de seguridad — posible bug |
| Path difiere ligeramente | Typo o refactor sin actualizar docs |

---

## Paso 5: Reportar

```
## API Contract Check — [fecha]

### ✅ Correctos ([N] endpoints)
| Method | Path | Auth |
[lista]

### ❌ En docs pero no en código
| Method | Path | Problema |
[lista o "Ninguno"]

### ⚠️ En código pero no en docs
| Method | Path | Auth real |
[lista o "Ninguno"]

### 🔐 Discrepancias de autenticación
| Method | Path | Docs dice | Código dice |
[lista o "Ninguno"]

---
Veredicto: [PASS ✅ / DISCREPANCIAS ENCONTRADAS ⚠️]
```

---

## Paso 6: Actualizar CLAUDE.md (opcional)

Si hay discrepancias, pregunta:
> "¿Actualizo la tabla Active Endpoints en CLAUDE.md para que coincida con el código? (sí/no)"

Si el usuario confirma, actualiza la tabla manteniendo el formato existente. El código es siempre la fuente de verdad.
