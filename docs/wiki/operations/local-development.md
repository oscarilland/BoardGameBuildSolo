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
| Maven | Not installed | Not needed: the Maven Wrapper (`mvnw`) downloads Maven 3.9.16 on first run (checksum-verified) into `~/.m2/wrapper` |
| Python 3 | Installed | Only to run the reference [balance simulation](../sources/balance-simulation.md) |

## Checks that run today

```text
java scripts/WikiLint.java
```

Run from the repo root. It runs as a single-file Java program, so it needs no build tool. Exit code 0 means clean. CI runs the same check on every push (`.github/workflows/ci.yml`).

## Building and running the game

From the repo root in PowerShell (use `./mvnw` in Git Bash or on macOS). The first run downloads Maven 3.9.16 and the libraries, so it needs a network connection.

```text
.\mvnw.cmd verify                              # compile, run all tests, enforce 80% coverage
.\mvnw.cmd clean verify                        # the same, from scratch
.\mvnw.cmd package -DskipTests                 # just rebuild the jar
java -jar target\net-zero-belfast.jar          # open the game
java -jar target\net-zero-belfast.jar --check  # load and validate the data only; exit 0 or 1
```

The coverage report is at `target\site\jacoco\index.html`.

To try a data change, edit the file in `src\main\resources\data\`, rebuild the jar and run `--check`. A bad value stops start-up with a message such as `board.json: squares[1].price is 99, expected 4-7 for POLLUTING`.

The target machine for this solo version is Oscar's own laptop, not an EEECS lab PC.

## Related

- [Build progress](build-progress.md)
- [Working with Claude and Codex](working-with-agents.md)
