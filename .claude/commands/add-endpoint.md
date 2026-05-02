# /add-endpoint — Add New REST Endpoint

Guides the complete process of adding an endpoint following the project's patterns.

## Usage
- `/add-endpoint POST /v1/users/profile update user profile`

## Step 1: Analyze the requested endpoint

From the argument extract:
- **HTTP method:** GET / POST / PUT / PATCH / DELETE
- **Path:** /v1/...
- **Description:** what it should do

Show a plan to the user before writing any code:
```
Endpoint: [METHOD] [path]
Description: [what it does]

Files to create/modify:
- [ ] DTO Request: dtos/request/[Name]Request.java (if applicable)
- [ ] DTO Response: dtos/response/[Name]Response.java (if applicable)
- [ ] Service: services/[Name]Service.java or method in existing service
- [ ] Controller: method in the appropriate controller with Swagger annotations
- [ ] Exceptions: if there are new error cases
- [ ] ErrorType: if there are new error codes
- [ ] SecurityConfig: if the endpoint is public or requires a specific role
- [ ] GlobalExceptionHandler: if there are new exceptions

Proceed with this implementation? (yes/no/modify)
```

## Step 2: Create the files

Follow this order: Exceptions → DTOs → Service → Controller → Security → Handler

### Required conventions:

**Request DTOs:**
```java
public record NameRequest(
    @NotBlank String field,
    @Valid @StrongPassword String password // if applicable
) {}
```

**Response DTOs:**
```java
public record NameResponse(
    // only necessary fields, never JPA entities directly
) {}
```

**Endpoint response:**
```java
// Always ResponseEntity<ApiResponse<T>>
return ResponseEntity.ok(ApiResponse.success(data));                              // with data
return ResponseEntity.ok(ApiResponse.emptySuccess());                             // no data
return ResponseEntity.badRequest().body(ApiResponse.error(ErrorType.CODE));       // error
```

**Controller method:**
```java
@Operation(summary = "Short title",
    description = "Detailed description of what it does and edge cases.")
@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Success")
@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid input (code 7-10)")
// If auth required:
@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Invalid token (code 4)")
// Always include @RequestBody @Valid on POST/PUT to activate DTO validation
@PostMapping("/path")
public ResponseEntity<ApiResponse<NameResponse>> method(
        @RequestBody @Valid NameRequest request,
        HttpServletRequest httpRequest) {  // only if device info is needed
    ...
}
```

**Protected endpoints with JWT** — add `security` to `@Operation`:
```java
@Operation(summary = "...", security = @SecurityRequirement(name = "bearerAuth"))
```

**Endpoint security:**
Add to `SecurityConfig` in the correct section:
- `.requestMatchers(HttpMethod.POST, "/v1/path").permitAll()` — if public
- Nothing if it only requires authentication (already covered by the filter)
- `.requestMatchers("/v1/path").hasRole("ADMIN")` — if a specific role is required

## Step 3: Update CLAUDE.md

Add the new endpoint to the endpoints table in CLAUDE.md.

## Step 4: Reminder

When done, indicate:
> "Endpoint created. Consider adding tests, then use `/commit` to save the changes."
