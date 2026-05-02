# /review-pr — Pull Request Review

Reviews the current PR (or the one specified as argument) exhaustively.

## Usage
- `/review-pr` — reviews the PR for the current branch
- `/review-pr 42` — reviews PR number 42

## Step 1: Identify the PR

If a number was passed as argument, use that PR.

If not, run:
```
git branch --show-current
gh pr list --head [current-branch] --json number,title,url,baseRefName
```

If no PR exists for the current branch, notify the user and stop.

## Step 2: Get PR information

Run:
```
gh pr view [number] --json title,body,baseRefName,headRefName,additions,deletions,changedFiles,commits
gh pr diff [number]
```

## Step 3: Review the code (exhaustive analysis)

Analyze the full diff evaluating **all** of the following categories:

---

### 🔴 CRITICAL ERRORS (block merge)

- Unhandled exceptions that could cause 500s
- Potential NullPointerExceptions (no prior validation)
- Incorrect or broken business logic
- Incompatible or destructive DB migrations
- Broken compilation
- Forgotten `TODO`, `FIXME`, `HACK`, or `System.out.println` in the diff
- Empty PR description or missing required sections

---

### 🔐 SECURITY (OWASP Top 10)

- [ ] **SQL Injection:** parameterized queries used? No string concatenation in queries?
- [ ] **XSS:** user input sanitized before returning in responses?
- [ ] **Broken Authentication:** tokens validated correctly? Expiries correct?
- [ ] **Sensitive Data Exposure:** passwords, tokens, or sensitive data in logs or responses?
- [ ] **Broken Access Control:** new endpoints have correct security annotations?
- [ ] **Security Misconfiguration:** unnecessary actuator or info endpoints exposed?
- [ ] **Hardcoded credentials:** any secrets or passwords in the code?
- [ ] **JWT handling:** signature verified? Expiry validated? `ApiResponse` standard used?

---

### 📚 DOCUMENTATION

- [ ] New public methods have Javadoc?
- [ ] New REST endpoints have annotations explaining their purpose?
- [ ] New entity/DTO fields have descriptions where not self-evident?
- [ ] New exceptions have clear messages?
- [ ] `CLAUDE.md` updated if new endpoints or patterns were added?

---

### 🏗️ ARCHITECTURE & BAD PRACTICES

- Layering respected? (Controller does not call Repository directly)
- DTOs contain no business logic?
- `ApiResponse<T>` used on all endpoints? (`ApiResponse.success(data)`, `ApiResponse.emptySuccess()`, `ApiResponse.error(ErrorType.X)`)
- New endpoints use `@RequestBody @Valid` to activate DTO validation?
- Business exceptions handled in `GlobalExceptionHandler`?
- `@Transactional` used where appropriate?
- Duplicated code that could be extracted to utilities?
- Lombok used correctly (`@Data`, `@Builder`, `@RequiredArgsConstructor`)?
- Unused imports or unnecessary commented-out code?
- Variable and method names are descriptive and in English?
- Endpoints follow REST conventions (correct HTTP verbs, plural for collections)?

---

### ⚡ PERFORMANCE

- N+1 queries? (fetch without JOIN when relations are needed)
- Indexes on frequently searched fields?
- Expensive operations that should be async (`@Async`, already configured in `AsyncConfig`)?
- Lists returned with proper pagination?

---

### ✅ TESTS

- New code has unit or integration tests in `src/test/`?
- Error cases covered in addition to the happy path?
- **Note:** it is not possible to verify test results from the diff alone. If new tests exist, indicate they must be run manually before merge.

---

## Step 4: Generate report

```
## PR Review #[number]: [title]

**Base:** [base-branch] ← [head-branch]
**Changes:** +[additions] / -[deletions] in [N] files

---

### 🔴 Critical Errors
[list or "None found"]

### 🔐 Security Issues
[list or "None found"]

### 📚 Missing Documentation
[list or "Complete"]

### 🏗️ Bad Practices
[list or "None found"]

### ⚡ Performance Issues
[list or "None found"]

### ✅ Test Coverage
[observations]

---

### Verdict
[APPROVED / APPROVED WITH SUGGESTIONS / CHANGES REQUIRED]

### Suggested next steps:
1. [concrete action]
2. [concrete action]
```

If there are critical errors or security issues, explain the risk and propose a concrete fix with code.

---

## Step 5: Update documentation if needed

After generating the report, read `CLAUDE.md` and check if the PR introduces changes that make it outdated. Evaluate:

**Active Endpoints table** — did the PR add, remove, or modify any endpoint? Update the table if so.

**Environment Variables** — did the PR add new required variables? Add them to the relevant section.

**Patterns & Conventions** — did the PR introduce a new pattern worth documenting? Add it with an example.

If nothing needs updating, state: `"CLAUDE.md is up to date, no changes needed."`

If there are changes, apply them directly and show a summary:
```
### Documentation updated
- [section]: [what changed]
```
