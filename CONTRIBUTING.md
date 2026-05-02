# Contributing to VirtualClubs Backend

Thanks for your interest in contributing! This guide covers everything you need to get started.

---

## Getting started

1. Fork the repository and clone your fork.
2. Copy the environment file and fill in the required variables:
   ```bash
   cp .env.example .env
   ```
3. Run the app locally:
   ```bash
   ./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
   ```
4. Run the test suite (uses H2 in-memory, no PostgreSQL needed):
   ```bash
   ./mvnw test
   ```

---

## Branch naming

Branch off from `development`, never from `main`.

```
<prefix>/<short-description-in-kebab-case>
```

| Change type | Prefix |
|-------------|--------|
| New feature | `feature/` |
| Bug fix | `fix/` |
| Tests | `test/` |
| Chore / config | `chore/` |
| Documentation | `docs/` |

Examples: `feature/club-membership`, `fix/refresh-token-expiry`

- ASCII only — no accented characters or special symbols
- Maximum 55 characters

---

## Commit format

Follows [Conventional Commits](https://www.conventionalcommits.org/) in **English**:

```
<type>(<optional scope>): <short description>
```

| Type | When to use |
|------|-------------|
| `feat` | New feature or endpoint |
| `fix` | Bug fix |
| `refactor` | Code restructuring without behavior change |
| `test` | Adding or fixing tests |
| `chore` | Dependencies, config, tooling |
| `docs` | Documentation only |
| `perf` | Performance improvement |

Examples:
```
feat(auth): add email verification resend endpoint
fix(jwt): handle expired token on public endpoints
chore(deps): bump spring-boot to 4.0.5
```

---

## Opening a pull request

1. Push your branch and open a PR toward `development`.
2. Fill in the PR template completely.
3. Make sure all tests pass: `./mvnw test`
4. Request a review — a maintainer will respond within a few days.

### Rules
- Never commit directly to `main` or `development`
- One logical change per PR — keep diffs focused
- New endpoints must include Swagger annotations and be added to the endpoints table in `CLAUDE.md`
- Security-sensitive changes (auth, tokens, password handling) require extra explanation in the PR description

---

## Code style

- **Java 25**, **Spring Boot**, **Lombok** — do not write getters/setters manually
- Use **records** for immutable DTOs
- `ApiResponse<T>` is the only allowed response wrapper
- Inject interfaces, never concrete implementations
- All endpoints follow REST conventions (correct HTTP verbs, plural for collections)
- Variable and method names in **English**

---

## Security

If you find a security vulnerability, **do not open a public issue**. See [SECURITY.md](SECURITY.md) for responsible disclosure instructions.
