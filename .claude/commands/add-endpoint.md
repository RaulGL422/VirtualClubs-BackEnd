# /add-endpoint — Agregar Nuevo Endpoint REST

Guía el proceso completo para agregar un endpoint correctamente siguiendo los patrones del proyecto.

## Uso
- `/add-endpoint POST /v1/users/profile actualizar perfil del usuario`

## Paso 1: Analizar el endpoint solicitado

Del argumento extrae:
- **Método HTTP:** GET / POST / PUT / PATCH / DELETE
- **Ruta:** /v1/...
- **Descripción:** qué debe hacer

Muestra un plan al usuario antes de escribir código:
```
Endpoint: [MÉTODO] [ruta]
Descripción: [qué hace]

Archivos a crear/modificar:
- [ ] DTO Request: dtos/request/[Nombre]Request.java (si aplica)
- [ ] DTO Response: dtos/response/[Nombre]Response.java (si aplica)
- [ ] Servicio: services/[Nombre]Service.java o método en servicio existente
- [ ] Controller: método en controller apropiado
- [ ] Excepciones: si hay nuevos casos de error
- [ ] ErrorType: si hay nuevos códigos de error
- [ ] SecurityConfig: si el endpoint es público o requiere rol específico
- [ ] GlobalExceptionHandler: si hay nuevas excepciones

¿Continúo con esta implementación? (sí/no/modificar)
```

## Paso 2: Crear los archivos

Sigue el orden: Excepciones → DTOs → Servicio → Controller → Security → Handler

### Convenciones obligatorias:

**DTOs Request:**
```java
public record NombreRequest(
    @NotBlank String campo,
    @Valid @StrongPassword String password // si aplica
) {}
```

**DTOs Response:**
```java
public record NombreResponse(
    // solo campos necesarios, nunca entidades JPA directamente
) {}
```

**Respuesta del endpoint:**
```java
// Siempre ResponseEntity<ApiResponse<T>>
return ResponseEntity.ok(ApiResponse.success(data));         // con datos
return ResponseEntity.ok(ApiResponse.emptySuccess());        // sin datos
return ResponseEntity.badRequest().body(ApiResponse.error(ErrorType.CODIGO)); // error

// Firma real de ApiResponse:
// ApiResponse.success(T data)
// ApiResponse.emptySuccess()
// ApiResponse.error(ErrorType message)
```

**Método del controller:**
```java
// Siempre incluir @RequestBody @Valid en POST/PUT para activar las validaciones del DTO
@PostMapping("/ruta")
public ResponseEntity<ApiResponse<NombreResponse>> metodo(
        @RequestBody @Valid NombreRequest request,
        HttpServletRequest httpRequest) {  // solo si necesitas info del dispositivo
    ...
}
```

**Seguridad del endpoint:**
Añadir a SecurityConfig en la sección correcta:
- `.requestMatchers(HttpMethod.POST, "/v1/ruta").permitAll()` — si es público
- Nada si requiere solo autenticación (ya está cubierto)
- `.requestMatchers("/v1/ruta").hasRole("ADMIN")` — si requiere rol específico

## Paso 3: Actualizar CLAUDE.md

Añade el nuevo endpoint a la tabla de endpoints en CLAUDE.md.

## Paso 4: Recordatorio

Al terminar, indica:
> "Endpoint creado. Considera agregar tests y luego usa `/commit` para guardar los cambios."
