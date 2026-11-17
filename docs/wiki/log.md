# Net Zero Belfast wiki log

Append-only. Add new entries at the end, using `## [YYYY-MM-DD] operation | title`.

## [2026-11-17] repository | LLMWiki and agent harness established

Built the harness from FeedWatchBuild's, adapted for a Java desktop game ([ADR-001](../decisions/001-repo-structure-and-harness.md), proposed). Oscar's answers: this repo is a **solo version only** and never goes to the team's GitLab repo, and the first pass is the harness plus a full ingest, with no code. Created the root, `docs/` and `src/` `AGENTS.md` files with `CLAUDE.md` imports, the wiki schema, ADR and plan templates, seven skills, `.claude/settings.json`, `scripts/WikiLint.java` (ported from FeedWatchBuild's PHP lint without the public/private checks), `.gitignore`, `.gitattributes` and a CI workflow that runs the lint. Moved the team handover from `NetZeroBelfast_handover/handover/` to `docs/raw/net-zero-belfast-handover/`, unchanged.

## [2026-11-17] ingest | Team handover (all 22 files)

Read every file in the handover: `HANDOVER.md`, rules v4 (Markdown and Word, which match), the SUB1a report draft, the module handbook (all 18 pages), the three data files, all diagrams (PNGs plus Graphviz, Mermaid and draw.io sources) and the balance simulation. Created seven source receipts, the overview, project context, learning outcomes, ten rules pages, the requirements catalogue with FR-01 to FR-17 and NFR-01 to NFR-11 (SUB1a wording, with drafted acceptance criteria) and stretch items S-01 and S-02, four architecture pages, the screens page and three operations pages.

Recorded on ingest: the board drawing numbers squares from 1 but `board.json` from 0; no data file has the disaster explanations FR-17 needs; unaffordable Choice options aren't covered; the quiz reward is stored twice; "after 2 rounds" for bankruptcy is ambiguous; carbon tax and meter treatment of closed or flooded squares isn't stated; Legacy Score rounding isn't stated; Flood or Storm with no owned squares isn't covered. These are Q-11 to Q-17 on [open questions](rules/open-questions.md), alongside the handover's Q-01 to Q-10. Also noted: the sequence diagrams have model objects talking to the player, which [game flow](architecture/game-flow.md) proposes to handle by returning pending decisions.

## [2026-11-17] decide | ADR-002 to ADR-004 proposed

Drafted ADRs for the UI toolkit (recommends Swing, for NFR-09), the build tool and Java version (recommends the Maven Wrapper, targeting Java 17 until the lab PCs' version is known), and reading the JSON data files (recommends Gson with validation on load). Decisions are left for Oscar.

## [2026-11-17] decide | ADR-002 accepted: Swing

Oscar chose Swing for the UI, the recommended option. Recorded his decision in [ADR-002](../decisions/002-ui-toolkit.md) as he gave it, with the recommendation's reasons (NFR-09, no dependency, no per-platform packaging). Updated the consequences, `src/AGENTS.md`, the [system overview](architecture/system-overview.md), [screens and layout](ux/screens-and-layout.md), the [overview](overview.md), the index, [build progress](operations/build-progress.md) and the README.

## [2026-11-17] decide | ADR-003 accepted: Maven Wrapper, Java 21

Oscar chose Maven with the Maven Wrapper, targeting Java 21. The draft recommended Java 17 because the team's requirements target EEECS lab PCs; Oscar noted this solo version runs on his own laptop (JDK 21), so the recommendation was revised to 21 before he decided. Recorded in [ADR-003](../decisions/003-build-tool-and-java-version.md) with its consequences. Updated the root and `src/` `AGENTS.md` (stack and validation commands), the [system overview](architecture/system-overview.md), [local development](operations/local-development.md), [NFR-04](requirements/nfr-04-responsiveness.md) (reference PC: Oscar's laptop), [NFR-09](requirements/nfr-09-portability.md) (Java 21 or later), [project context](project-context.md), the [overview](overview.md), the index, [build progress](operations/build-progress.md) and the README.

## [2026-11-17] decide | ADR-004 accepted: Gson

Oscar chose Gson, the recommended option, to read the three JSON data files. Recorded in [ADR-004](../decisions/004-reading-the-json-data-files.md) as he gave it, with the recommendation's reasons. Consequences added: Gson is the only runtime library, used only in the `config` package, which hands the model plain records; validation after loading is required because Gson fills missing fields silently; keys starting with `_` are ignored. Updated `src/AGENTS.md`, the [system overview](architecture/system-overview.md), [data files](architecture/data-files.md), [domain model](architecture/domain-model.md), the [overview](overview.md), the index, [build progress](operations/build-progress.md) and the README. All three stack decisions are now made.

## [2026-11-17] decide | Open rules questions Q-01 to Q-17 answered

Oscar accepted the recommended answer to every open rules question ("all recommended"). Recorded on [open questions](rules/open-questions.md) and carried into the rules pages ([setup](rules/setup-and-characters.md), [board](rules/board-and-squares.md), [turn](rules/turn-and-actions.md), [end of round](rules/end-of-round.md), [Climate Meter](rules/climate-meter-and-disasters.md), [bankruptcy](rules/bankruptcy.md), [cards](rules/climate-cards.md), [scoring](rules/scoring-and-end-of-game.md)) and the acceptance criteria of FR-02, FR-03, FR-05, FR-08 to FR-15 and FR-17. Q-04 was checked on the web: the 77% 2040 target comes from the Climate Change (2040 Emissions Target) Regulations (NI) 2024, not the 2022 Act itself, so the value 11 stands. For Q-11 the agent drafted four disaster explanation lines; the Flood and Heatwave lines are sourced to the Met Office, while Storm and Drought need a source check before Oscar approves them (the Met Office says the UK isn't measurably getting stormier). NFR-06 and NFR-07 marked agreed, as proposed and accepted.

## [2026-11-17] decide | ADR-001 accepted

Oscar confirmed the repo structure ("I confirm the repo structure"). [ADR-001](../decisions/001-repo-structure-and-harness.md) is accepted, Option A. All four ADRs are now accepted.

## [2026-11-17] plan | Plan 001 walking skeleton drafted

Drafted [plan 001](../plans/001-walking-skeleton.md): a Maven project with the wrapper at the repo root, working copies of the data files (with the Q-05 two-way note), Gson records and `GameConfig` validation with clear errors, a seeded `Dice`, the JaCoCo 80% rule, a shaded runnable jar with a `--check` mode, an empty Swing window, and a CI build step. Covers NFR-07 AC-1 and AC-3, NFR-06 AC-1, and NFR-09 AC-2 as a packaging proof. Disaster explanations are left to plan 005. Waiting for Oscar's approval and confirmation of the package name (`netzerobelfast`) and jar name.

## [2026-11-17] build | Plan 001 walking skeleton built

Oscar approved [plan 001](../plans/001-walking-skeleton.md) (jar `net-zero-belfast.jar`, package `netzerobelfast`) and it is now `done`. Added the Maven project and wrapper (Maven 3.9.16, checksum-pinned), working data files in `src/main/resources/data/` (only the Q-05 direction changed), the `netzerobelfast.config` loader and validator, a seeded `Dice`, the launcher with `--check` and an empty Swing window, and a CI build step. 83 tests pass; line coverage is 99.1% for `config` and 100% for `model`, and forcing the threshold to 100% fails the build as it should. Verified `--check`, a broken price stopping start-up with a named key, and the jar running alone from an empty folder. Differences from the plan are in its Outcome: counts are tested rather than validated (NFR-07), Gson reads a strict tree mapped by hand, broken data is made in tests, and Gson's `error_prone_annotations` dependency is left out of the jar (correction note added to [ADR-004](../decisions/004-reading-the-json-data-files.md)). Updated [data files](architecture/data-files.md) (now `built`), [domain model](architecture/domain-model.md), [system overview](architecture/system-overview.md), [local development](operations/local-development.md), [build progress](operations/build-progress.md), both `AGENTS.md` files, the [overview](overview.md), the index and the README.

## [2026-11-17] spec | FR-01, FR-02 and FR-03 agreed

Oscar answered the remaining questions: display names up to 20 characters after trimming, unique names kept (FR-01 AC-4), and no second route question at the far junction after the City Centre cut-through (FR-03 AC-7). All three requirements are now `agreed`. While planning, found [Q-18](rules/open-questions.md): which way a player on Glider City Hall (30) moves after arriving by Glider, now the cut-through is two-way. It blocks plan 003, not plan 002.

## [2026-11-17] plan | Plan 002 players, characters and movement drafted

Drafted [plan 002](../plans/002-players-characters-and-movement.md): `Character` and six subclasses from the data, `Player`, a `Board` built from `board.json` that moves one step at a time, and `Game` as a small state machine (awaiting roll, awaiting route, move done) that pauses for junction choices instead of calling the UI. Covers FR-01 AC-1 to AC-4, FR-02 AC-1 to AC-6, and FR-03 except AC-6 (bankruptcy, plan 005). Adds `game.maxNameLength` to the working `config.json`. Records the change from `Board.move(from, steps, route)` to step-by-step movement. Waiting for Oscar's approval.

## [2026-11-17] build | Plan 002 players, characters and movement built

Oscar approved [plan 002](../plans/002-players-characters-and-movement.md) and it is now `done`. Added `Character` and six subclasses, `Player`, a `Board` built from `board.json` that moves one step at a time, `Game` with setup checks and a turn phase machine (roll, route choice, direction choice, end turn), and `IllegalActionException`. Added `game.maxNameLength` (20) to the working `config.json`. 129 tests pass with no warnings; `model` coverage is 100%. Two deliberate bugs in the junction logic were each caught by the tests. One reading for Oscar to confirm: a move ending exactly on the far junction after the cut-through means the next move starts with the route question. FR-02 is `built`; FR-01 waits for its screen (plan 007) and FR-03 for AC-6 (plan 005). Updated the [domain model](architecture/domain-model.md), [game flow](architecture/game-flow.md), [data files](architecture/data-files.md), the rules pages' build status, the requirement pages, [build progress](operations/build-progress.md), `src/AGENTS.md`, the [overview](overview.md), the index and the README.

## [2026-11-17] decide | Far-junction reading confirmed; Q-18 answered

Oscar confirmed that ending a move exactly on the far junction after the cut-through means the next move starts with the route question (FR-03). He answered [Q-18](rules/open-questions.md): after a Glider ride to Glider City Hall (30), the player decides their direction at the start of their next move, which `Game`'s `AWAITING_DIRECTION` phase already supports. Recorded on the open questions, [board and squares](rules/board-and-squares.md), [FR-03](requirements/fr-03-dice-movement-and-junctions.md), [FR-09](requirements/fr-09-glider.md), plan 002's Outcome and build progress. No rules questions are open.
