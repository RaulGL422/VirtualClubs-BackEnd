# /project-status — Project Status Overview

Generates a summary of the current state of the project.

## Step 1: Git and GitHub state

Run in parallel:
```bash
git branch --show-current
git log --oneline -8
git status --short
git stash list
gh pr list --state open --json number,title,headRefName,createdAt
```

## Step 2: Commented-out pending features

Search for commented-out feature code with concrete patterns:
```bash
grep -r "GoogleAuthService\|MailerService\|UserTokenService\|UserTokenEntity\|UserTokenRepository" \
  src/main/java --include="*.java" -l
```

Search for pending TODOs and FIXMEs:
```bash
grep -rn "TODO\|FIXME\|HACK\|System\.out\.print" src/main/java --include="*.java"
```

## Step 3: Generate report

```
## Project Status — Virtual Clubs Backend — [current date]

### Git
- Current branch: [branch]
- Recent commits: [list]
- Uncommitted changes: [N files] / Clean
- Saved stashes: [N] / None
- Open PRs: [list with number and title] / None

### Technical Debt
- TODOs/FIXMEs found: [list with file:line] / None
- Commented-out pending features:
  - [ ] [feature found]
  - [ ] [feature found]

### Suggested next action
[A single concrete action based on the current state]
```
