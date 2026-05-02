# /new-feature — Create Feature Branch

Creates a properly named working branch from `development` (default) or from the specified base.

## Usage
- `/new-feature add user profile endpoint` — free description
- `/new-feature VC-5` — task reference (branch named accordingly)
- `/new-feature VC-5 --from main` — task reference from a different base branch

---

## Step 1: Check working state

Run `git status`. If there are uncommitted changes, ask:
> "You have uncommitted changes on the current branch. Do you want me to stash them before switching? (yes/no)"

---

## Step 2: Determine branch name

Detect if the argument matches the pattern `VC-N`, `vc-N`, or a plain integer (e.g. `5`, `VC-5`, `vc-5`).

### If VC-N format:

Ask the user for a short description to complete the branch name:
> "Task reference VC-[N] detected. Provide a short description for the branch (kebab-case, ASCII only):"

Then propose:
```
Branch: [prefix]/vc-[N]-[description]
Base:   [base-branch]

Confirm? (yes/no)
```

**Branch prefix based on context:**
- If the user mentions a bug or fix → `fix/`
- If the user mentions tests → `test/`
- If the user mentions config or chore → `chore/`
- Default for features → `feature/`

### If free description:

Infer type and generate branch name:

| Change type | Prefix |
|-------------|--------|
| New feature | `feature/` |
| Bug fix | `fix/` |
| Tests | `test/` |
| Config / maintenance | `chore/` |
| Documentation | `docs/` |

**Name:** kebab-case, ASCII only (no accented characters), max 40 characters.

Propose the name to the user before creating the branch.

---

## Step 3: Create the branch

Default base is `development`. If `--from <branch>` was passed, use that.

```bash
git fetch origin [base-branch]
git checkout [base-branch]
git pull origin [base-branch]
git checkout -b [branch-name]
```

## Step 4: Push to remote

```bash
git push --set-upstream origin [branch-name]
```

## Step 5: Confirm

Show the created branch name and remind the user to use `/commit` for changes and `/create-pr` when done.
