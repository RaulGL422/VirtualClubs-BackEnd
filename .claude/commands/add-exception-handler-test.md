# /add-exception-handler-test — Test the GlobalExceptionHandler

Generates a `@WebMvcTest` that verifies every `@ExceptionHandler` in `GlobalExceptionHandler` returns the correct HTTP status and `ErrorType` numeric code.

## Usage
- `/add-exception-handler-test` — generates or updates `GlobalExceptionHandlerTest`

---

## Step 1: Read GlobalExceptionHandler

Read `src/main/java/galindo/raul/virtualclubs/controller/GlobalExceptionHandler.java` completely.

For each `@ExceptionHandler` method note:
- Which exception class it handles
- HTTP status it returns
- `ErrorType` it uses (and its numeric code from `ErrorType.java`)
- Whether the exception has constructor parameters (field `email`, `message`, etc.)

---

## Step 2: Generate the test immediately

File path: `src/test/java/galindo/raul/virtualclubs/controller/GlobalExceptionHandlerTest.java`

### Critical setup — two rules that MUST be followed:

**Rule 1: `@Import(GlobalExceptionHandler.class)` is mandatory.**
`@WebMvcTest` scans the main application package for `@ControllerAdvice` but may not detect it when a stub controller is used. Explicit import guarantees the handler is in the context.

**Rule 2: The stub controller MUST be registered as `@Bean` in `@TestConfiguration`.**
The stub is a static inner class of the test class — it lives outside the application's component scan package. Without explicit registration, `RequestMappingHandlerMapping` won't find it and requests fall through to the static resource handler (returning 500 instead of the expected code).

```java
@TestConfiguration
@EnableWebSecurity
static class TestSecurityConfig {
    @Bean
    StubController stubController() { return new StubController(); }  // ← REQUIRED

    @Bean
    SecurityFilterChain testSecurityFilterChain(HttpSecurity http) throws Exception {
        http.csrf(AbstractHttpConfigurer::disable)
            .authorizeHttpRequests(auth -> auth.anyRequest().permitAll());
        return http.build();
    }
}
```

---

### Full test class template:

```java
@WebMvcTest(controllers = GlobalExceptionHandlerTest.StubController.class)
@Import(GlobalExceptionHandler.class)   // ← REQUIRED
@ActiveProfiles("test")
class GlobalExceptionHandlerTest {

    private static final String BASE = "/stub";

    @Autowired private MockMvc mockMvc;

    // ─── One test per @ExceptionHandler ────────────────────────────────────

    @Test
    void campoBlanco_retorna400ConCodigo7() throws Exception {
        mockMvc.perform(post(BASE + "/valid")
                        .contentType("application/json")
                        .content("{\"value\": \"\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(7));   // ErrorType.FIELD_BLANK
    }

    @Test
    void jsonMalformado_retorna400ConCodigo7() throws Exception {
        mockMvc.perform(post(BASE + "/valid")
                        .contentType("application/json")
                        .content("{malformed json"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(7));
    }

    @Test
    void userAlreadyExistException_retorna409ConCodigo6() throws Exception {
        mockMvc.perform(get(BASE + "/user-already-exists"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value(6));   // ErrorType.EMAIL_ALREADY_EXISTS
    }

    // ... one test per handler, same pattern

    // ─── Stub DTO for @Valid test ───────────────────────────────────────────
    record StubBody(@NotBlank(message = "FIELD_BLANK") String value) {}

    // ─── Stub controller — one endpoint per exception ──────────────────────
    @RestController
    @RequestMapping("/stub")
    static class StubController {

        @PostMapping("/valid")
        ResponseEntity<Void> bodyValido(@RequestBody @Valid StubBody body) {
            return ResponseEntity.ok().build();
        }

        @GetMapping("/user-already-exists")
        void userAlreadyExists() { throw new UserAlreadyExistException("e@test.com"); }

        // ... one method per exception, matching the handler
    }

    // ─── Security config — permits all to stub endpoints ───────────────────
    @TestConfiguration
    @EnableWebSecurity
    static class TestSecurityConfig {
        @Bean
        StubController stubController() { return new StubController(); }

        @Bean
        SecurityFilterChain testSecurityFilterChain(HttpSecurity http) throws Exception {
            http.csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth.anyRequest().permitAll());
            return http.build();
        }
    }
}
```

---

### Mapping pattern for test names and expected values:

For each `@ExceptionHandler` in `GlobalExceptionHandler`:

```
Exception class            → HTTP status  → ErrorType code
─────────────────────────────────────────────────────────
MethodArgumentNotValidEx   → 400          → 7 (FIELD_BLANK, or specific code from message)
HttpMessageNotReadableEx   → 400          → 7 (FIELD_BLANK)
UsernameNotFoundException  → 404          → 5 (USER_NOT_FOUND)
EmailNotFoundException     → 404          → 5 (USER_NOT_FOUND)
UserAlreadyExistException  → 409          → 6 (EMAIL_ALREADY_EXISTS)
RefreshTokenException      → 401          → 3 (INVALID_REFRESH_TOKEN)
NoLocalProviderException   → 400          → 12 (NO_LOCAL_PROVIDER)
EmailNotVerifiedException  → 403          → 11 (EMAIL_NOT_VERIFIED)
InvalidTokenException      → 403          → 4 (INVALID_TOKEN)
JwtException               → 401          → 3 (INVALID_REFRESH_TOKEN)
InternalErrorException     → 500          → 1 (INTERNAL_ERROR)
Exception (catch-all)      → 500          → 1 (INTERNAL_ERROR)
```

Add a new entry to this table whenever a new handler is added to `GlobalExceptionHandler`.

---

## Step 3: Check that GlobalExceptionHandler is complete

While reading it, verify:
- Every custom exception in `models/exceptions/` has a corresponding `@ExceptionHandler`
- If any exception is missing, add the handler before generating the test

---

## Step 4: Run the tests

```bash
./mvnw test -Dtest=GlobalExceptionHandlerTest --no-transfer-progress
```

Report how many handlers are covered and confirm all pass.
