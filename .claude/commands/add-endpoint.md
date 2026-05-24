# /add-endpoint — Add New REST Endpoint

Guides the complete process of adding an endpoint following the project's patterns.

## Usage
- `/add-endpoint POST /v1/users/profile update user profile`

---

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
- [ ] Rate limiting: application.properties entry if the endpoint is public
- [ ] Test: integration test for the new endpoint

Proceed with this implementation? (yes/no/modify)
```

---

## Step 2: Create the files

Follow this order: Exceptions → DTOs → Service → Controller → Security → Handler

### Required conventions:

**Request DTOs — always records:**
```java
public record NameRequest(
    @NotBlank(message = "FIELD_BLANK") String field,
    @Email(message = "INVALID_EMAIL") String email,
    @StrongPassword @Size(min = 6, message = "PASSWORD_TOO_SHORT") String password
) {}
```

The `message` in validation annotations must match an `ErrorType` enum name exactly so `GlobalExceptionHandler.resolveErrorType()` maps them correctly.

**Response DTOs — always records:**
```java
public record NameResponse(
    // only necessary fields, never JPA entities directly
) {}
```

**Endpoint response — ApiResponse<T> is the only wrapper:**
```java
return ResponseEntity.ok(ApiResponse.success(data));
return ResponseEntity.ok(ApiResponse.emptySuccess());
return ResponseEntity.badRequest().body(ApiResponse.error(ErrorType.CODE));
```

**Controller method with full Swagger annotations:**
```java
@Operation(summary = "Short title",
    description = "Detailed description of what it does and edge cases.")
@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Success")
@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid input (code 7-10)")
@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "429", description = "Rate limit exceeded (code 13)")
// If auth required — add security to @Operation AND include the 401 response:
@Operation(summary = "...", security = @SecurityRequirement(name = "bearerAuth"))
@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Invalid token (code 4)")
@PostMapping("/path")
public ResponseEntity<ApiResponse<NameResponse>> method(
        @RequestBody @Valid NameRequest request,
        HttpServletRequest httpRequest) {
    ...
}
```

**Inject interfaces, never concrete implementations:**
```java
// ✅ correct
private final UserService userService;
private final RefreshTokenService refreshTokenService;

// ❌ wrong
private final UserEntityServiceImpl userService;
```

**Security — if the endpoint is public, add it to SecurityConfig:**
```java
.requestMatchers(
    "/v1/auth/login",
    "/v1/auth/newpublicpath"   // ← add here
).permitAll()
```

**Rate limiting — one line in application.properties for every public endpoint:**
```properties
rate-limiting.limits[/v1/auth/newpublicpath]=10
```

---

## Step 3: Handle new exceptions

If the endpoint introduces new error cases:

1. Add a new `ErrorType` constant with the next available code
2. Create a new exception class in `models/exceptions/` following the pattern:
```java
@Getter
public class NewException extends RuntimeException {
    private final String email; // or relevant field
    public NewException(String email) { this.email = email; }
}
```
3. Add a handler in `GlobalExceptionHandler`:
```java
@ExceptionHandler(NewException.class)
public ResponseEntity<ApiResponse<Void>> handleNew(NewException e) {
    log.warn("Descriptive message: {}", e.getEmail());
    return ResponseEntity.status(HttpStatus.XYZ)
        .body(ApiResponse.error(ErrorType.NEW_CODE));
}
```

---

## Step 4: Update CLAUDE.md

Add the new endpoint to the Active Endpoints table in CLAUDE.md.

---

## Step 5: Generate an integration test for the new endpoint

Create a test class in `src/test/java/` (or add to an existing one) that covers at minimum:

1. **Happy path** — valid request returns the expected status and response body
2. **Validation errors** — blank/invalid fields return 400 with the correct ErrorType code
3. **Auth check** — if protected: request without Bearer returns 403; invalid Bearer returns 401
4. **Business error** — at least one error case (e.g., duplicate, not found) returns the correct status

Use the project's integration test pattern:
```java
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
@ActiveProfiles({"dev", "test"})
@Transactional
class [Feature]IntegrationTest {

    @Autowired private MockMvc mockMvc;
    @MockitoBean private GoogleAuthService googleAuthService;
    @MockitoBean private EmailService emailService;
    ...
}
```

Run `./mvnw test -Dtest=ClassName --no-transfer-progress` and confirm all tests pass before reporting done.

---

## Step 6: Done

Report:
> "Endpoint created. `./mvnw test` passes. Use `/commit` to save the changes."
