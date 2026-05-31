# /add-error-type — Añadir Tipo de Error

Añade un nuevo `ErrorType` con su excepción y su handler en `GlobalExceptionHandler`. Ningún paso se puede saltar sin romper la cadena de errores.

## Uso
- `/add-error-type CLUB_NOT_FOUND 14 "El club solicitado no existe"`
- `/add-error-type MEMBER_ALREADY_EXISTS 15 "El usuario ya es miembro de este club"`

Formato: `/add-error-type [NOMBRE_MAYUSCULAS] [código_numérico] "[descripción]"`

---

## Paso 1: Leer el estado actual

Lee en paralelo:
- `src/main/java/.../models/enums/ErrorType.java` — valores actuales y códigos
- `src/main/java/.../controller/GlobalExceptionHandler.java` — handlers existentes como referencia
- `src/main/java/.../dtos/response/ApiResponse.java` — cómo se construye `ApiResponse.error(...)`

---

## Paso 2: Validar

Comprueba que el nombre y el código no existan ya en `ErrorType.java`. Si alguno existe, para y avisa:
> "⛔ `[NOMBRE]` o código `[N]` ya existe. ¿Quisiste decir otro?"

---

## Paso 3: Proponer los cambios

```
Cambios que se aplicarán:

1. ErrorType.java
   [NOMBRE]([código], "[descripción]")

2. models/exceptions/[NombreEnPascalCase]Exception.java  ← nueva clase
   public class [NombreEnPascalCase]Exception extends RuntimeException

3. GlobalExceptionHandler.java  ← nuevo handler
   @ExceptionHandler([NombreEnPascalCase]Exception.class)
   → HTTP [status_propuesto] + ApiResponse.error(ErrorType.[NOMBRE])

HTTP status propuesto: [400/403/404/409]
Razón: [justificación basada en la semántica del error]

¿Aplicar? (sí/editar/cancelar)
```

---

## Paso 4: Aplicar los cambios

### 4.1 — `ErrorType.java`

Añade el nuevo valor al enum manteniendo el orden numérico:
```java
[NOMBRE]([código], "[descripción]");
```

### 4.2 — Crear la excepción

Crea `src/main/java/.../models/exceptions/[Nombre]Exception.java`:

```java
public class [Nombre]Exception extends RuntimeException {
    public [Nombre]Exception() {
        super("[descripción]");
    }
}
```

### 4.3 — Handler en `GlobalExceptionHandler.java`

Añade junto a handlers de tipo similar:
```java
@ExceptionHandler([Nombre]Exception.class)
public ResponseEntity<ApiResponse<Void>> handle[Nombre](
        [Nombre]Exception ex, HttpServletRequest request) {
    return ResponseEntity
            .status(HttpStatus.[STATUS])
            .body(ApiResponse.error(ErrorType.[NOMBRE]));
}
```

---

## Paso 5: Verificar compilación

```bash
./mvnw compile -q
```

---

## Paso 6: Confirmar

```
✅ Tipo de error añadido:
  ErrorType.[NOMBRE] (código [N])
  Exception:  [Nombre]Exception.java
  Handler:    HTTP [status] en GlobalExceptionHandler

Uso: throw new [Nombre]Exception()  en el servicio correspondiente.
```
