# /check-structure — Verify Project Structure Compliance

Audits the current branch diff (or a specific file) to verify it follows all architectural conventions defined in CLAUDE.md. Reports violations and proposes concrete fixes.

## Usage
- `/check-structure` — audits all Java files changed since `development`
- `/check-structure AuthController.java` — audits a specific file
- `/check-structure --full` — audits all production Java files in `src/main/`

---

## Step 1: Determine scope

**If a file is passed:** read that file only.

**If `--full`:** list all files in `src/main/java/` and read every one.

**Default (no argument):**
```bash
git diff development...HEAD --name-only -- "*.java"
```
Read each changed Java file in `src/main/java/` (skip test files).

If nothing changed since `development`, inform the user and stop.

---

## Step 2: Run all structural checks

For each file in scope, evaluate the following rules. Mark each as ✅ or ❌.

---

### 🏗️ LAYER ARCHITECTURE

- [ ] **No cross-layer bypass:** controllers do not call repositories directly; services do not call controllers
- [ ] **Interface injection:** controllers, filters, and services inject interfaces, not concrete implementations
  - ❌ `UserEntityServiceImpl userService` in a controller field
  - ✅ `UserService userService`
- [ ] **No entity exposure:** `@Entity` classes are never used as controller method return types or request body types — always map through a DTO record

---

### 📦 PACKAGE STRUCTURE

Files must be in their correct package:

| Class type | Package |
|------------|---------|
| HTTP endpoints | `controller/` |
| Business logic | `services/` |
| JPA data access | `repositories/` |
| Request/response DTOs | `dtos/request/` or `dtos/response/` |
| JPA entities | `models/entities/` |
| Enums | `models/enums/` |
| Custom exceptions | `models/exceptions/` |
| Custom annotations | `models/annotations/` |
| Stateless helpers | `utils/` |
| Spring configuration | `config/` |

- [ ] Every class is in the correct package for its responsibility

---

### 🔁 API RESPONSE

- [ ] **All controller methods return `ResponseEntity<ApiResponse<T>>`** — no `ResponseEntity<T>`, `@ResponseBody T`, or raw objects
- [ ] **Three allowed factory methods only:**
  - `ApiResponse.success(data)` — 2xx with data
  - `ApiResponse.emptySuccess()` — 2xx with no body
  - `ApiResponse.error(ErrorType.CODE)` — error with numeric code
- [ ] **No custom error JSON:** all error responses go through `GlobalExceptionHandler` or `ApiResponse.error()`

---

### ❌ EXCEPTION HANDLING

- [ ] **Custom exceptions extend `RuntimeException`** (not `Exception`)
- [ ] **Every custom exception has a handler in `GlobalExceptionHandler`**
  - Read `models/exceptions/` and compare against the `@ExceptionHandler` methods in `GlobalExceptionHandler`
  - List any unhandled exceptions as ❌
- [ ] **Exception messages match ErrorType names** — `@ExceptionHandler` returns `ApiResponse.error(ErrorType.X)`, not a hardcoded string
- [ ] **No try/catch in controllers** — exceptions bubble up to `GlobalExceptionHandler`

---

### ✅ BEAN VALIDATION

- [ ] **Request DTOs use validation annotations** from `jakarta.validation.constraints` or project custom annotations
- [ ] **`message` attribute matches an `ErrorType` enum name** so `GlobalExceptionHandler.resolveErrorType()` maps correctly:
  - ✅ `@NotBlank(message = "FIELD_BLANK")`
  - ✅ `@Email(message = "INVALID_EMAIL")`
  - ❌ `@NotBlank` with default message "must not be blank"
- [ ] **Controllers use `@Valid` on `@RequestBody`** to activate DTO validation

---

### 🔒 SECURITY & JWT

- [ ] **New public endpoints are listed in `SecurityConfig.requestMatchers(...).permitAll()`**
- [ ] **New protected endpoints have `security = @SecurityRequirement(name = "bearerAuth")` in `@Operation`**
- [ ] **Tokens are never stored or logged in plain text** — refresh tokens as SHA-256 Base64, verification/reset tokens as SHA-256 hex
- [ ] **SHA-256 hashing always uses `TokenUtils`** — no direct `MessageDigest` usage elsewhere
- [ ] **No hardcoded secrets or credentials** in any file

---

### 📊 SWAGGER / OPENAPI

For every controller method:
- [ ] Has `@Operation(summary = "...", description = "...")`
- [ ] Has `@ApiResponse` for each possible HTTP status the method returns
- [ ] Has `security = @SecurityRequirement(name = "bearerAuth")` if protected
- [ ] No `@Operation` with an empty or missing `summary`

---

### 🗄️ DATABASE MIGRATIONS

- [ ] **No `ddl-auto` set to `update` or `create`** in non-test properties
- [ ] **Every schema change has a Flyway migration** in `src/main/resources/db/migration/`
- [ ] **Migration file naming:** `V{n}__{description_in_snake_case}.sql`
- [ ] **No modification of existing migration files** — always create a new one

---

### 🔧 LOMBOK & RECORDS

- [ ] **No manually written getters/setters** — use `@Getter`, `@Setter`, `@Data`, or `@Builder`
- [ ] **DTOs are `record` types**, not classes with fields
- [ ] **Entity classes use `@Entity`, `@Table`, `@Id`, `@GeneratedValue`** — no custom table creation logic

---

### ⚡ RATE LIMITING

For every new **public** endpoint:
- [ ] Has an entry in `application.properties`:
  ```properties
  rate-limiting.limits[/v1/path/endpoint]=N
  ```
- [ ] A reasonable limit (auth endpoints: 5–20/min, read endpoints: 60–100/min)

---

## Step 3: Generate the report

```
## Structure Audit — [date]
Branch: [current branch] vs development
Files analyzed: [N files]

---

### 🔴 Violations (must fix before merge)
[rule] — [file:line] — [description]
[proposed fix with code snippet]

### 🟡 Warnings (strong recommendation)
[rule] — [file:line] — [description]

### ✅ Compliant checks
[list of passing rules, grouped by category]

---

**Overall:** [PASS / FAIL]
**Summary:** [N violations, N warnings across N files]
```

If there are no violations, state clearly:
> "All structural rules pass. The diff is consistent with CLAUDE.md conventions."

---

## Step 4: Apply fixes (if authorized)

If the user says "fix it" or "apply the fixes":
- Apply only the clearly mechanical fixes (wrong message attribute on annotations, missing `@Valid`, wrong return type)
- For architectural violations (wrong layer, entity in response), explain why and ask how to proceed
- Never modify migration files
- Never modify `SecurityConfig` without confirming which endpoints are affected
