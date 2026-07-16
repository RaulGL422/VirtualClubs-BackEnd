# /check-security — Security Audit

Performs a security review focused on the project's code.

## Usage
- `/check-security` — audits all code modified since `main`
- `/check-security src/main/java/galindo/raul/virtualclubs/controller/AuthController.java` — audits a specific file

## Step 1: Determine scope

If a file was passed as argument, audit that file.

If not, get the files modified since development:
```bash
git diff development...HEAD --name-only -- "*.java"
```

If no files are modified, audit the project's critical security files:
- `config/security/`
- `services/TokensService.java`
- `services/UserEntityServiceImpl.java`
- `controller/AuthController.java`

## Step 2: Read and analyze the files

Read each file in scope and evaluate:

---

### OWASP A01 — Broken Access Control
- Are endpoints protected with `@PreAuthorize` or configured in `SecurityConfig`?
- Are there endpoints that return other users' data without verifying identity?
- Can authenticated users only modify their own resources?

### OWASP A02 — Cryptographic Failures
- Are weak algorithms used (MD5, SHA-1 for passwords)?
- Are passwords hashed with BCrypt correctly?
- Are refresh tokens stored as hashes (SHA-256), not in plain text?
- Is the JWT secret long enough and sourced from an environment variable?

### OWASP A03 — Injection
- Are there queries built by string concatenation?
- Are Spring Data JPA methods or `@Query` with parameters used?
- Is there unsanitized object deserialization?

### OWASP A04 — Insecure Design
- Is rate limiting in place on auth endpoints (login, register, reset-password)?
- Are failed login attempts limited?
- Do error messages reveal internal information (stack traces, table names)?

### OWASP A05 — Security Misconfiguration
- Is CSRF disabled appropriately (stateless API)?
- Does Actuator expose only the necessary endpoints?
- Is CORS configured restrictively (not `*`)?
- Are HTTP security headers present?

### OWASP A07 — Authentication Failures
- Does the JWT validate signature, expiry, and token type?
- Are refresh tokens single-use or invalidated on rotation?
- Does logout effectively revoke the token in the database?
- Do tokens have a reasonable expiry time?

### OWASP A09 — Logging Failures
- Are security events logged (failed login, access denied)?
- Do logs NOT contain passwords, tokens, or sensitive data?
- Is there sufficient logging to detect attacks?

### Project-specific
- Is the `@StrongPassword` validation strong enough?
- Is the SHA-256-generated `deviceId` predictable or manipulable?
- Does Google ID token handling correctly verify the issuer?

---

## Step 3: Generate security report

```
## Security Audit — [date]

**Files analyzed:** [list]

### 🔴 Critical Vulnerabilities (patch urgently)
[list with description, exact location (file:line) and proposed fix]

### 🟡 Medium Vulnerabilities (patch soon)
[list with description, location and recommendation]

### 🟢 Hardening Improvements (best practices)
[optional suggestions to strengthen security]

### ✅ Correct Controls
[highlights what is implemented correctly]

---

**Risk score:** [HIGH / MEDIUM / LOW]
**Recommendation:** [immediate action if there are critical issues]
```

For each vulnerability found, show the vulnerable code and the proposed fix with code.
