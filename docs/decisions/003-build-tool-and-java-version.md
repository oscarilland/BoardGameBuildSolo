---
title: ADR-003 Build tool and Java version
type: decision
id: ADR-003
status: accepted
sources: [../wiki/sources/handover.md, ../wiki/sources/module-handbook.md]
updated: 2026-11-17
---

# ADR-003 - Build tool and Java version

**Status:** accepted 17 November 2026 (proposed the same day). **Maven with the Maven Wrapper, targeting Java 21.**

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

**Maven with the wrapper.** Maven's coverage rule turns NFR-06 into a check that fails the build, which is the strongest evidence for it.

**Java version, revised during the discussion:** the first draft recommended Java 17, because the team's requirements target EEECS lab PCs whose Java version is unknown. Oscar pointed out that this solo version runs on his own laptop, which has JDK 21. With the lab PC constraint gone, the recommendation became **Java 21**: it matches the laptop, it's the current long-term-support release, and any new Java install is 21 or later.

## Decision

"Maven + Java 21." - Oscar, 17 November 2026, after noting: "I'm not on a lab PC, I'm on my own personal laptop."

So the target machine for this solo version is Oscar's laptop, not an EEECS lab PC. Oscar may reword this section at any time.

## Consequences

- Plan 001 adds `pom.xml` and the Maven Wrapper (`mvnw`, `mvnw.cmd`, `.mvn/`), with `maven.compiler.release` set to 21, JUnit 5 through Surefire, JaCoCo with an 80% line rule on the game logic, and a plugin that builds one runnable jar.
- The root `AGENTS.md` validation table gets the real commands (`mvnw verify` and so on), and CI gains a build and test step on Java 21.
- `src/` takes Maven's layout: `src/main/java`, `src/main/resources` (the working data files) and `src/test/java`. Plan 001 settles whether `pom.xml` sits at the repo root (the usual choice, recommended) or in `src/`.
- The game needs Java 21 or later to run. NFR-09 ("only a Java runtime") still holds, with "Java 21 or later" as the runtime.
- NFR-04's timing targets are measured on Oscar's laptop instead of a lab PC.
- The wrapper downloads Maven the first time it runs, so that run needs a network connection.

## How we'll know it worked

`mvnw verify` on a clean clone compiles, runs every test, enforces 80% coverage on the model and produces a jar that starts with `java -jar`.

## Questions a marker might ask

**"How do you know your tests cover the game logic?"** JaCoCo measures line coverage on every build, and the build fails if the model package drops below 80%, so the number in our report comes straight from the tool.
