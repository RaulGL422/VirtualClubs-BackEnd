# /add-filter-test — Unit Test for a Spring Security Filter

Generates a focused unit test for a Spring Security filter (`OncePerRequestFilter` subclass).

## Usage
- `/add-filter-test JwtAuthFilter`
- `/add-filter-test RateLimitingFilter`

---

## Step 1: Read the filter

Read the filter class in `src/main/java/` completely to identify:
- Constructor parameters (dependencies to mock)
- The three possible execution paths in `doFilterInternal`:
  1. **Pass-through** — condition not met, calls `filterChain.doFilter()`
  2. **Authenticated** — validation succeeds, sets `SecurityContext` and calls chain
  3. **Short-circuit** — validation fails, writes response and returns WITHOUT calling chain
- The response body format when short-circuiting (HTTP status, content-type, JSON body)

Check `src/test/java/` for an existing test to avoid duplication.

---

## Step 2: Generate the test immediately

Place the test in the same package as the filter under `src/test/java/`.

### Required structure:

```java
package galindo.raul.virtualclubs.config.security.filters;

import tools.jackson.databind.ObjectMapper;    // Jackson 3.x — NOT com.fasterxml
import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FilterNameTest {

    // Mock every constructor dependency
    @Mock private DependencyA depA;
    @Mock private DependencyB depB;
    @Mock private FilterChain chain;   // ← always mock the chain

    private FilterName filter;

    @BeforeEach
    void setUp() {
        filter = new FilterName(depA, depB, new ObjectMapper());
        SecurityContextHolder.clearContext();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();   // ← always clean after each test
    }
```

---

### Test cases to generate for every filter:

**1. Condition not triggered — passes through without acting:**
```java
@Test
void sinCondicionActivadora_continúaAlSiguienteFiltro() throws Exception {
    MockHttpServletRequest req = new MockHttpServletRequest();
    MockHttpServletResponse res = new MockHttpServletResponse();

    filter.doFilter(req, res, chain);   // ← doFilter (public), not doFilterInternal

    verify(chain).doFilter(req, res);
    assertThat(res.getStatus()).isEqualTo(200);
    verifyNoInteractions(depA);
}
```

**2. Valid input — authenticates and continues chain:**
```java
@Test
void inputValido_populaContextoYContinúaCadena() throws Exception {
    // configure mocks for valid scenario
    when(depA.validate("valid-input")).thenReturn(true);

    MockHttpServletRequest req = new MockHttpServletRequest();
    req.addHeader("Authorization", "Bearer valid-input");
    MockHttpServletResponse res = new MockHttpServletResponse();

    filter.doFilter(req, res, chain);

    verify(chain).doFilter(req, res);
    assertThat(SecurityContextHolder.getContext().getAuthentication()).isNotNull();
}
```

**3. Invalid input — short-circuits with error response:**
```java
@Test
void inputInvalido_retornaErrorYNoContinúaCadena() throws Exception {
    when(depA.validate("bad-input")).thenReturn(false);

    MockHttpServletRequest req = new MockHttpServletRequest();
    req.addHeader("Authorization", "Bearer bad-input");
    MockHttpServletResponse res = new MockHttpServletResponse();

    filter.doFilter(req, res, chain);

    assertThat(res.getStatus()).isEqualTo(401);                              // adjust code
    assertThat(res.getContentType()).contains("application/json");
    assertThat(res.getContentAsString()).contains("\"success\":false");
    assertThat(res.getContentAsString()).contains(String.valueOf(ErrorType.INVALID_TOKEN.getCode()));
    verifyNoInteractions(chain);                                              // ← chain must NOT be called
    assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
}
```

**4. Invalid input — SecurityContext stays empty:**
```java
@Test
void inputInvalido_noPopulaSecurityContext() throws Exception {
    when(depA.validate(any())).thenReturn(false);
    // ...
    assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
}
```

---

### Key rules:

- Always call `filter.doFilter(req, res, chain)` — the **public** `Filter.doFilter` method, not `doFilterInternal` (which is protected). `OncePerRequestFilter` internally calls `doFilterInternal` from `doFilter`.
- `verifyNoInteractions(chain)` confirms the filter short-circuited correctly. `verify(chain).doFilter(req, res)` confirms it passed through.
- Never test `doFilterInternal` directly — access is `protected` and would couple the test to internal implementation.
- Use `@Mock FilterChain chain` (Mockito mock) instead of `MockFilterChain` for cleaner `verify()` assertions.

---

## Step 3: Run the tests

```bash
./mvnw test -Dtest=FilterNameTest --no-transfer-progress
```

Report how many paths are covered and which remain.
