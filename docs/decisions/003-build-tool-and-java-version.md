---
title: ADR-003 Build tool and Java version
type: decision
id: ADR-003
status: proposed
sources: [../wiki/sources/handover.md, ../wiki/sources/module-handbook.md]
updated: 2026-11-17
---

# ADR-003 - Build tool and Java version

## Context

The game needs to compile, run JUnit tests, measure line coverage ([NFR-06](../wiki/requirements/nfr-06-reliability-and-test-coverage.md): at least 80% for game logic), and package into something that runs with only a Java runtime ([NFR-09](../wiki/requirements/nfr-09-portability.md)). The handbook strongly recommends Java and asks for an exported project archive and executables at SUB2.

Facts on Oscar's machine (17 November 2026): Temurin JDK 21.0.8 is installed. Maven and Gradle are not. The Java version on EEECS lab PCs is **not known**.

## Options

### Option A - Maven, with the Maven Wrapper (recommended)

`pom.xml` plus `mvnw` scripts, so nothing extra needs installing. JUnit 5 through Surefire, JaCoCo for coverage with an 80% rule on the `model` package, and the Shade or Assembly plugin for a single runnable jar.

- **For:** declarative and widely taught. The coverage threshold can fail the build. IntelliJ, Eclipse and VS Code all open it directly.
- **Against:** XML is verbose. The wrapper downloads Maven on first run (needs network once).

### Option B - Gradle, with the Gradle Wrapper

- **For:** shorter build file, fast incremental builds.
- **Against:** a Groovy or Kotlin build script is harder to explain line by line, and its versions churn faster.

### Option C - No build tool (`javac` and a script)

- **For:** nothing to learn.
- **Against:** JUnit and JaCoCo jars must be managed by hand. Weak for NFR-06.

### Java version

| Choice | For | Against |
|---|---|---|
| Target Java 17 (compile on JDK 21 with `--release 17`) | Runs on any lab PC with Java 17 or later. Still has records, switch expressions and text blocks | No Java 21 features (pattern matching for switch on records, sequenced collections) |
| Target Java 21 | Newest LTS, matches the installed JDK | Fails on a lab PC that only has Java 17 |

## Recommendation

**Maven with the wrapper, targeting Java 17**, until the lab PCs' Java version is confirmed. Then move to 21 if they have it. Maven's coverage rule turns NFR-06 into a check that fails the build, which is the strongest evidence for it.

## Decision

*Oscar writes this section in his own words: what he chose and why. Until he does, the status stays `proposed`.*

## Consequences

- The root `AGENTS.md` validation table gets the real commands (for example `./mvnw verify`), and CI gains a build and test step.
- `src/` takes Maven's layout: `src/main/java`, `src/main/resources` (the working data files) and `src/test/java`. Plan 001 settles whether the Maven project sits at the repo root or in `src/`.

## How we'll know it worked

`./mvnw verify` on a clean clone compiles, runs every test, enforces 80% coverage on the model and produces a jar that starts with `java -jar`.

## Questions a marker might ask

**"How do you know your tests cover the game logic?"** JaCoCo measures line coverage on every build, and the build fails if the model package drops below 80%, so the number in our report comes straight from the tool.
