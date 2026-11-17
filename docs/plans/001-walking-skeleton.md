---
title: Plan 001 Walking skeleton
type: plan
id: PLAN-001
status: done
requirements: [NFR-06, NFR-07, NFR-09]
sources: [../decisions/002-ui-toolkit.md, ../decisions/003-build-tool-and-java-version.md, ../decisions/004-reading-the-json-data-files.md, ../wiki/architecture/data-files.md]
updated: 2026-11-17
---

# Plan 001 - Walking skeleton

**Approved by Oscar, 17 November 2026:** "it is net-zero-belfast.jar, i approve plan 001". The jar name is confirmed; the package keeps the default, `netzerobelfast`.

## Goal

Prove the whole tool chain end to end before any rules are written. At the end, `mvnw verify` builds the project, loads and validates the three data files, runs JUnit with the 80% coverage rule, and produces one runnable jar. That jar opens an empty Swing window saying what it loaded. CI does the same on every push.

## Scope

- **Requirements covered:**
  - [NFR-07](../wiki/requirements/nfr-07-data-driven-balance.md) (agreed): AC-1 and AC-3 in full; AC-2 for loading only (game behaviour comes in later plans).
  - [NFR-06](../wiki/requirements/nfr-06-reliability-and-test-coverage.md) (agreed): AC-1, the coverage rule.
  - [NFR-09](../wiki/requirements/nfr-09-portability.md) (draft): AC-2, data inside the jar, and a first check of AC-1 on Windows only. This proves packaging; it doesn't mark NFR-09 built.
  - [NFR-08](../wiki/requirements/nfr-08-privacy-and-security.md): AC-5, data validated on load.
- **Decisions relied on:**
  - [ADR-002](../decisions/002-ui-toolkit.md): Swing.
  - [ADR-003](../decisions/003-build-tool-and-java-version.md): the Maven Wrapper, targeting Java 21.
  - [ADR-004](../decisions/004-reading-the-json-data-files.md): Gson, with validation.
- **Open rules questions:** [Q-05](../wiki/rules/open-questions.md) is noted in the working `board.json`. [Q-13](../wiki/rules/open-questions.md) affects validation, because a quiz card's reward is now optional.
- **Out of scope:**
  - Any game rules: players, movement, actions, end of round. That starts in plan 002.
  - NFR-06's 20-game headless run (plan 006).
  - The disaster explanation text, which isn't approved yet (plan 005).
  - Real screens.

## Prerequisites

1. **Network access** the first time: the wrapper downloads Maven, and Maven downloads Gson, JUnit, JaCoCo and its plugins. FeedWatchBuild's issue log noted that Avast's HTTPS scanning broke certificate checks on this laptop. If it's back on, Java's downloads may fail the same way; see Risks.
2. **Oscar confirms two names** (defaults shown, used unless he says otherwise):
   - the base Java package: `netzerobelfast`, with `netzerobelfast.model`, `.config`, `.ui` and `.sim`;
   - the jar: `target/net-zero-belfast.jar`.

## Tasks

1. **Maven project at the repo root.** Add `pom.xml` with:
   - `maven.compiler.release` 21, UTF-8 source encoding;
   - dependencies, each pinned to the latest stable release at build time (record the versions in Outcome): Gson (compile, at least 2.10 for record support), and JUnit Jupiter (test scope);
   - plugins, also pinned:
     - Surefire, to run JUnit 5;
     - JaCoCo, with a `check` rule: line coverage of at least 0.80 on `netzerobelfast.model` and `netzerobelfast.config`, failing `verify` if it's lower; `ui` and the `App` launcher are excluded;
     - Shade, building `target/net-zero-belfast.jar` with Gson inside and `netzerobelfast.App` as the main class.
2. **Maven Wrapper.** Add `mvnw`, `mvnw.cmd` and `.mvn/wrapper/maven-wrapper.properties` from the official Apache Maven Wrapper (the script-only type, which downloads Maven on first run), pinned to a Maven 3.9.x release. Add `mvnw text eol=lf` to `.gitattributes` and commit `mvnw` as executable, so it runs in CI and on macOS.
3. **Working data files.** Copy the three raw JSON files byte for byte to `src/main/resources/data/`. In the working `board.json`, change `cityCentreRoute.direction` to record the decision: two-way, per Q-05. The raw files stay untouched.
4. **Config records** (`netzerobelfast.config`): one Java record per data shape, for example `GameRules`, `CharacterDef`, `SquareDef`, `BoardDef`, `CardDef`, `CardOption`, `Effect`, `Source`, `DangerBand`, `InterimTarget`. Keys starting with `_` are ignored. Text-only keys such as `scoring.netZero` aren't mapped.
5. **`GameConfig`** (`netzerobelfast.config`):
   - `GameConfig.load()` reads the three files from the classpath; an overload reads from a folder, for tests.
   - It parses them with Gson into the records, then calls `validate()`, and exposes typed getters only. The model never sees Gson.
   - Any problem throws `ConfigException` with a message naming the file, the key and what was expected, for example `board.json: squares[7].price = 3, expected 8-12 for GREEN_PROJECT`.
6. **Validation rules**, from [data files](../wiki/architecture/data-files.md):
   - **Game and characters:** `minPlayers` ≤ `maxPlayers`; `rounds` × `yearsPerRound` = `endYear` − `startYear`; 6 characters with unique ids, at least `maxPlayers` of them.
   - **Board:**
     - exactly 36 squares with ids 0-35, each once;
     - type counts 10 / 12 / 4 / 3 / 7, and 4 nature projects;
     - prices inside `priceRange` for their type; no price on squares that can't be owned;
     - every district referenced exists;
     - the loop is ids 0-29 and the City Centre route is 30-35;
     - the junctions and Glider stops exist and have the right types.
   - **Climate Meter:** danger bands rise in order, and only the last has `maxMeter: null`; the interim target rounds fall within 1 to `rounds`.
   - **Resilience:** `levelCosts` and `disasterCostCut` both have `maxLevel` entries, and each cut is between 0 and 1.
   - **Cards:**
     - 30 cards with unique ids;
     - quiz cards have 4 answers and `correctIndex` 0-3, and `reward` is optional (Q-13);
     - choice cards have 2 options, at least one with `money` ≥ 0 ([FR-10](../wiki/requirements/fr-10-climate-cards.md) AC-8);
     - event cards have an `effect`;
     - every card has non-empty `text` and `fact`, and at least one source with a title and URL ([NFR-10](../wiki/requirements/nfr-10-fact-accuracy.md) AC-1).
7. **`Dice`** (`netzerobelfast.model`): takes a `java.util.Random` in its constructor, and has `rollOne()` (1-6) and `rollTwo()` (2-12, the sum of two `rollOne()` calls). It's the only randomness in the model.
8. **`App`** (`netzerobelfast.App`):
   - It loads `GameConfig`.
   - On success it opens a `JFrame` titled "Net Zero Belfast" on the event dispatch thread, showing "Loaded 36 squares, 30 cards, 6 characters", with the rest of the screen left for plan 007.
   - On a `ConfigException` it shows the message in an error dialog and exits with code 1.
   - `java -jar … --check` loads and validates with no window, prints the summary, and exits 0 or 1, for CI and quick checks.
9. **CI:** add a step that runs `./mvnw -B verify` on Java 21, followed by `java -jar target/net-zero-belfast.jar --check`. Keep the wiki lint step.
10. **Docs:** see below.

## Tests

| Test | Checks | Criterion |
|---|---|---|
| `GameConfigTest.loadsRealData` | The real files load; 36 squares, 30 cards, 6 characters; Industrialist £40M and 8 GP; meter starts at 50; 2040 target 11 | NFR-07 AC-1 |
| `GameConfigTest.readsValuesFromData` | A test copy of `config.json` with polluting `incomePerRound` changed from 7 to 6 loads as 6 | NFR-07 AC-2 (loading part) |
| `GameConfigValidationTest` (one test per rule) | Broken copies in `src/test/resources/broken/` fail with a `ConfigException` naming the file and key: malformed JSON; missing file; 35 squares; duplicate id; green price 3; nature count 3; danger bands out of order; quiz `correctIndex` 4; a choice card with both options costing money; a card with no source; `minPlayers` above `maxPlayers` | NFR-07 AC-3, NFR-08 AC-5, FR-10 AC-8, NFR-10 AC-1 |
| `GameConfigTest.quizRewardDefaults` | A quiz card without `reward` reports the config default of 2 | Q-13 |
| `GameConfigTest.ignoresCommentKeys` | `_note` keys don't cause errors | Data files page |
| `DiceTest.sameSeedSameRolls` | Two `Dice` with seed 42 give identical sequences of 1,000 rolls | FR-03 AC-1 (groundwork) |
| `DiceTest.ranges` | Over 10,000 rolls, `rollOne` is always 1-6 and `rollTwo` always 2-12, and every value appears | FR-03 AC-1 |
| `DiceTest.fairish` | Over 60,000 seeded `rollOne` calls, each face occurs 9,000-11,000 times | Guards against an off-by-one |
| Coverage | `mvnw verify` fails if `model` and `config` line coverage is below 80% | NFR-06 AC-1 |

The `App` window isn't unit-tested: Swing needs a display, and CI is headless. It's covered by the manual check below, and `--check` is run in CI.

## Docs to update

- **Root [AGENTS.md](../../AGENTS.md):** the validation table gets the real Java command.
- **[src/AGENTS.md](../../src/AGENTS.md):** current state, the base package, and where the working data files live.
- **[Local development](../wiki/operations/local-development.md):** the exact commands, the jar name, and how to run `--check`.
- **[System overview](../wiki/architecture/system-overview.md):** the pinned versions of Gson, JUnit, JaCoCo and Maven in the dependency table.
- **[Data files](../wiki/architecture/data-files.md):** the working copies' location, the validation rules as built, and the Q-05 note; status `built` for loading and validation.
- **[Domain model](../wiki/architecture/domain-model.md):** `Dice` built; "Changes from the analysis model" records `GameConfig` as typed records instead of `get(key): int` (gap 7).
- **Requirements:** NFR-07 stays `agreed` until the game uses the values (plan 002 onwards); the [catalogue](../wiki/requirements/catalogue.md) notes partial progress.
- **[Build progress](../wiki/operations/build-progress.md):** tick plan 001's tasks; issues log for anything that goes wrong.
- **`index.md` and `log.md`:** a `build` entry.

## Verification

1. `.\mvnw.cmd verify` from the repo root on Oscar's laptop: it compiles, every test passes, and the coverage check passes. Open `target/site/jacoco/index.html` and look at the numbers.
2. `java -jar target\net-zero-belfast.jar --check` prints "Loaded 36 squares, 30 cards, 6 characters" and exits 0.
3. `java -jar target\net-zero-belfast.jar` opens the window. Close it.
4. Temporarily change a price in `src/main/resources/data/board.json` to 99, rebuild and run: the error dialog names `board.json` and the square. Undo the change.
5. Copy only the jar to an empty folder and run it there: it still starts, because the data is inside it (NFR-09 AC-2).
6. Push, and check CI is green (Oscar pushes).

## What Oscar should be able to explain

- What the Maven Wrapper is, and why the repo carries `mvnw` instead of asking everyone to install Maven.
- What `mvnw verify` runs, in order, and what makes it fail.
- How Gson turns `board.json` into Java records, and why `validate()` is still needed (Gson fills missing fields with defaults silently).
- Why the game reads working copies in `src/main/resources/data/` rather than the files in `docs/raw/`.
- Why `Dice` takes a `Random` in its constructor, and how a fixed seed makes tests repeatable.
- What the Shade plugin does to make a jar that runs with only Java installed.
- Why the window is opened on the event dispatch thread.

## Risks

| Risk | What we'll do |
|---|---|
| Downloads fail because of HTTPS interception (Avast, as in FeedWatchBuild's issue 1) | Check whether Avast HTTPS scanning is on. If it is, exclude Java from scanning rather than trusting Avast's certificate. Log it in the issues log |
| Coverage below 80% with so little code | The only model and config code is `Dice` and `GameConfig`, and both are tested thoroughly, so it should pass. Don't lower the threshold; add the missing tests |
| Gson and records: an older Gson can't build records | Use Gson 2.10 or later (the pin will be newer) |
| A copying mistake in the working data files | `GameConfigTest.loadsRealData` checks known values; `git diff --no-index` between the raw and working copies shows only the Q-05 change |
| Line endings break `mvnw` on CI or macOS | `.gitattributes` forces LF for `mvnw`, and it's committed as executable |

## Outcome

**Built 17 November 2026.** `mvnw verify` passes on a clean build with no warnings: 83 tests (15 in `GameConfigTest`, 63 in `GameConfigValidationTest`, 5 in `DiceTest`). Line coverage is 99.1% for `config` (421 of 425 lines) and 100% for `model`.

### What was built

| Path | What it is |
|---|---|
| `pom.xml`, `mvnw`, `mvnw.cmd`, `.mvn/wrapper/` | Maven project and wrapper at the repo root |
| `src/main/resources/data/` | Working copies of the three data files; only `board.json`'s `cityCentreRoute.direction` differs from the raw copy (Q-05) |
| `netzerobelfast.config` | `GameConfig` (load, quiz reward, summary), `Rules`, `BoardDef`, `CardDef` records, `ConfigParser`, `ConfigValidator`, `JsonNode`, `ConfigException` |
| `netzerobelfast.model.Dice` | Seeded dice, the model's only randomness |
| `netzerobelfast.App`, `netzerobelfast.ui.MainWindow` | Launcher with `--check`, and the empty window |
| `.github/workflows/ci.yml` | `./mvnw -B verify`, then `--check` on the jar, then the wiki lint |

### Pinned versions

| Tool | Version |
|---|---|
| Maven (through Maven Wrapper 3.3.4, script-only, SHA-256 pinned) | 3.9.16 |
| Gson | 2.15.0 |
| JUnit (BOM) | 6.1.3 |
| JaCoCo | 0.8.16 |
| maven-compiler / surefire / jar / resources / shade plugins | 3.16.0 / 3.6.1 / 3.5.1 / 3.5.0 / 3.6.2 |

Each was the newest stable release on Maven Central on the day. Maven 3.10.1 also exists; 3.9.16 was used because the plan said 3.9.x.

### Verification results

1. `mvnw verify`: passed. Forcing the coverage minimum to 100% (`-Dcoverage.minimum=1.00`) makes it fail, which proves the rule is live.
2. `java -jar target\net-zero-belfast.jar --check` printed "Loaded 36 squares, 30 cards, 6 characters" and exited 0.
3. The window opened with the title "Net Zero Belfast" and stayed up with nothing on standard error. **Oscar still to look at it himself.**
4. With the Waste Incinerator's price set to 99, the jar refused to start: `board.json: squares[1].price is 99, expected 4-7 for POLLUTING`, exit 1. Restored afterwards.
5. The jar alone in an empty folder passed `--check` (NFR-09 AC-2).
6. CI: waiting for Oscar to push.

### What differed from the plan

1. **Validation doesn't hard-code the board's counts.** The plan listed "exactly 36 squares", "type counts 10/12/4/3/7", "30 cards" and "4 answers" as validation rules. Putting those in code would make the board's size a code constant, which goes against NFR-07. The validator checks structure instead: squares in id order from 0, every square on exactly one route, prices in range, Glider stops and junctions consistent, quiz cards with at least 2 answers and a valid `correctIndex`, Choice cards with exactly 2 options. The counts are asserted in `GameConfigTest` against the real files, so a change to them is still noticed.
2. **Gson reads a strict JSON tree, then `JsonNode` maps it to records by hand**, instead of Gson filling the records directly. This gives an exact key path in every error message, and avoids Gson silently putting 0 or null into a missing field. Gson's default lenient mode is switched off, so unquoted keys and trailing text are errors.
3. **Broken data is made in the tests**, by copying the real files into a temporary folder and changing one value (`TestData`), instead of keeping fixture files in `src/test/resources/broken/`. Each test shows its one change, and fixtures can't drift from the real data.
4. **Gson 2.15 has one dependency of its own**, `error_prone_annotations`. ADR-004 said it had none. Those annotations are only for compilers, so the jar excludes them; the jar is 374 KB.
5. `Dice` also exposes `random()`, so card shuffles and disaster choices can share the same seeded source later.
6. `isNature` on a square that isn't a Green Project is rejected while parsing, not in the validator.
