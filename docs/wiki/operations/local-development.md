---
title: Local development
type: operation
status: draft
sources: [../../decisions/003-build-tool-and-java-version.md]
updated: 2026-11-17
---

# Local development

## What's needed

| Tool | Status on Oscar's machine (17 November 2026) | Needed for |
|---|---|---|
| JDK 17 or later | Temurin JDK 21.0.8 installed | The wiki lint now; the game later |
| Git | Installed | Version control |
| Maven or Gradle | Not installed | Not needed: [ADR-003](../../decisions/003-build-tool-and-java-version.md) recommends a wrapper script that downloads it |
| Python 3 | Installed | Only to run the reference [balance simulation](../sources/balance-simulation.md) |

## Checks that run today

```text
java scripts/WikiLint.java
```

Run from the repo root. It runs as a single-file Java program, so it needs no build tool. Exit code 0 means clean. CI runs the same check on every push (`.github/workflows/ci.yml`).

## Once plan 001 is built

The build, test, coverage and run commands depend on ADR-003. Plan 001 will add them here and to the validation table in the root `AGENTS.md`.

## Related

- [Build progress](build-progress.md)
- [Working with Claude and Codex](working-with-agents.md)
