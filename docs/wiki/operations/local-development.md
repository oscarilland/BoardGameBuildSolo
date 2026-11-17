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
| JDK 21 or later | Temurin JDK 21.0.8 installed | The wiki lint now; the game targets Java 21 ([ADR-003](../../decisions/003-build-tool-and-java-version.md)) |
| Git | Installed | Version control |
| Maven | Not installed | Not needed: the Maven Wrapper (`mvnw`) downloads it on first run, which needs a network connection once |
| Python 3 | Installed | Only to run the reference [balance simulation](../sources/balance-simulation.md) |

## Checks that run today

```text
java scripts/WikiLint.java
```

Run from the repo root. It runs as a single-file Java program, so it needs no build tool. Exit code 0 means clean. CI runs the same check on every push (`.github/workflows/ci.yml`).

## Once plan 001 is built

Plan 001 adds the Maven Wrapper. Expected commands, from the repo root in PowerShell (use `./mvnw` in Git Bash or on macOS):

```text
.\mvnw.cmd verify     # compile, run all tests, enforce 80% coverage
.\mvnw.cmd package    # build the runnable jar
java -jar target\<name>.jar
```

Plan 001 confirms the exact jar name and records it here.

The target machine for this solo version is Oscar's own laptop, not an EEECS lab PC.

## Related

- [Build progress](build-progress.md)
- [Working with Claude and Codex](working-with-agents.md)
