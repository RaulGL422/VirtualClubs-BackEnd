# /add-test — Generate Tests for a Class

Generates unit and integration tests for a project class. Executes immediately without waiting for confirmation.

## Usage
- `/add-test UserEntityServiceImpl`
- `/add-test TokensService`
- `/add-test AuthController`

---

## Step 1: Locate the class

Find the file in `src/main/java/` and read it completely to understand:
- Its dependencies (constructor / `@Autowired` fields)
- All public methods
- The exceptions it can throw
- The error and success cases for each method

Also check `src/test/java/` for existing tests to avoid duplication.

---

## Step 2: Choose the test type

| Class type | Test strategy |
|------------|---------------|
| Service / Util / Validator | Unit test with Mockito (`@ExtendWith(MockitoExtension.class)`) |
| Controller | `@WebMvcTest` — web layer only, no DB |
| Full flow (auth, tokens, email) | `@SpringBootTest(webEnvironment = MOCK)` with H2 |
| Spring Security filter | Use `/add-filter-test` instead |
| `@RestControllerAdvice` | Use `/add-exception-handler-test` instead |

---

## Step 3: Generate the tests immediately

Write every test case without pausing for confirmation. Cover:
- Happy path for every public method
- Every exception the method can throw
- Edge cases visible in the implementation (null inputs, empty collections, boundary conditions)

### Project-specific patterns — ALWAYS apply these

**Imports (Spring Boot 4.x / Jackson 3.x):**
```java
import tools.jackson.databind.ObjectMapper;                       // NOT com.fasterxml
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean; // NOT @MockBean
```

**Profiles and transaction:**
```java
@ActiveProfiles({"dev", "test"})   // always both for integration tests
@Transactional                      // integration tests auto-rollback after each test
```

**Mocking external services — always @MockitoBean for:**
- `GoogleAuthService` — would call Google APIs
- `EmailService` — would send real emails

**Password valid for tests:** `"PAss12"` (2 upper, 2 lower, 1 digit — passes `@StrongPassword` and `@Size(min=6)`)

---

### Unit test structure (services, utils):

```java
@ExtendWith(MockitoExtension.class)
class UserEntityServiceImplTest {

    @Mock private UserEntityRepository userRepository;
    @Mock private PasswordEncoder passwordEncoder;

    @InjectMocks private UserEntityServiceImpl userService;

    @Test
    void registerUser_emailNuevo_retornaUsuarioGuardado() {
        // Arrange
        when(userRepository.existsByEmail("test@test.com")).thenReturn(false);
        when(passwordEncoder.encode("PAss12")).thenReturn("hashed");
        when(userRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        // Act
        UserEntity result = userService.registerUser("test@test.com", "PAss12");

        // Assert
        assertThat(result.getEmail()).isEqualTo("test@test.com");
        verify(userRepository).save(any(UserEntity.class));
    }

    @Test
    void registerUser_emailDuplicado_lanzaUserAlreadyExistException() {
        when(userRepository.existsByEmail("test@test.com")).thenReturn(true);

        assertThrows(UserAlreadyExistException.class,
                () -> userService.registerUser("test@test.com", "PAss12"));
    }
}
```

---

### @WebMvcTest structure (controller — web layer only):

```java
@WebMvcTest(AuthController.class)
@ActiveProfiles({"dev", "test"})
class AuthControllerUnitTest {

    @Autowired private MockMvc mockMvc;

    @MockitoBean private UserEntityServiceImpl userService;
    @MockitoBean private TokensService tokensService;
    // ... one @MockitoBean per dependency the controller uses

    @TestConfiguration
    @EnableWebSecurity
    static class TestSecurityConfig {
        @Bean
        SecurityFilterChain testSecurityFilterChain(HttpSecurity http,
                JwtUtils jwtUtils, UserEntityServiceImpl userService,
                ObjectMapper objectMapper) throws Exception {
            JwtAuthFilter jwtAuthFilter = new JwtAuthFilter(jwtUtils, userService, objectMapper);
            http
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth
                    .requestMatchers("/v1/auth/register", "/v1/auth/refresh", "/v1/auth/google").permitAll()
                    .anyRequest().authenticated())
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);
            return http.build();
        }
    }
}
```

---

### Integration test structure (full context with H2):

```java
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
@ActiveProfiles({"dev", "test"})
@Transactional
class AuthControllerIntegrationTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    @MockitoBean private GoogleAuthService googleAuthService;
    @MockitoBean private EmailService emailService;

    @Test
    void register_usuarioNuevo_retorna201ConTokens() throws Exception {
        mockMvc.perform(post("/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"test@test.com\",\"password\":\"PAss12\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.accessToken").isNotEmpty());
    }
}
```

---

### Naming convention for test methods:

```
[method]_[condition]_[expected outcome]
```

Examples:
- `registerUser_emailDuplicado_lanzaUserAlreadyExistException`
- `getNewTokens_usuarioValido_retornaTokesConAccessYRefresh`
- `logout_sinAutenticacion_retorna403`

---

## Step 4: Run the tests

After creating the file, run:
```bash
./mvnw test -Dtest=ClassName --no-transfer-progress
```

Report the result: how many tests were created, how many passed, and which cases remain uncovered.
