# /update-deps — Review and Update Dependencies

Reviews `pom.xml` for outdated dependencies and proposes safe updates.

## Step 1: Read pom.xml

Read the full `pom.xml` and extract all dependencies with their current versions.

## Step 2: Run the Maven versions plugin

```bash
./mvnw versions:display-dependency-updates -q 2>&1 | grep -E "\->" | head -40
```

This shows directly which dependencies have updates available.

If the command fails, analyze the versions manually against knowledge of the latest stable version of each dependency.

## Step 3: Classify the updates

For each dependency with an available update, classify it as:

**SAFE (patch / minor with no known breaking changes):**
- Patch version change (x.y.Z → x.y.Z+1)
- Minor in mature libraries with a good compatibility track record

**REQUIRES REVIEW (minor with possible changes):**
- jjwt — API changes between minor versions
- Spring Security — may require adjustments in `SecurityConfig`
- google-api-client — may have signature changes

**MAJOR / NOT RECOMMENDED now:**
- Spring Boot major versions
- Java version changes
- Changes that require migration

## Step 4: Present report

```
## Dependency Updates — [date]

### Safe Updates (I apply automatically if you confirm)
| Dependency | Current version | New version | Type |
|------------|----------------|-------------|------|
| commons-codec | 1.15 | 1.17.1 | patch |

### Require Review (I explain what changes before updating)
| Dependency | Current version | New version | Risk |
|------------|----------------|-------------|------|
| jjwt | 0.11.5 | 0.12.x | API changes in 0.12 |

### Not Recommended Now
[dependencies with major bump or reasons not to update]

---
Apply the safe updates? (yes/no)
Explain what changes in a specific dependency? (name)
```

## Step 5: Apply confirmed updates

If the user confirms, edit `pom.xml` updating only the approved versions.

Then run to verify it compiles:
```bash
./mvnw compile -q
```

If there is a compilation error after updating, revert the specific change and report which dependency caused the problem.

## Step 6: Reminder

When done, suggest running `/commit` with type `chore(deps): update dependencies`.
