---
name: release
description: Package Net Zero Belfast as a runnable build and verify it. Only when explicitly asked.
disable-model-invocation: true
---

Run the **release** operation from `docs/AGENTS.md`.

1. Check `git status` is clean. Note the commit being released.
2. Run the full validation from the root `AGENTS.md`: wiki lint, the build tool's full check with tests and coverage, and the 20-game headless run.
3. Package the game the way ADR-003 describes. Never upload it anywhere, and never copy it to the team's GitLab repo or Canvas.
4. Verify as described in the root `AGENTS.md` under "Release verification", including starting the packaged game from a clean folder and playing one round through the UI. A package that builds but doesn't start has failed.
5. Update `docs/wiki/operations/` and append a `release` entry to `docs/wiki/log.md`, saying what changed and what didn't. Run `java scripts/WikiLint.java`.
