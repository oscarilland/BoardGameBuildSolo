---
title: Build progress
type: operation
status: draft
sources: [../requirements/catalogue.md, ../rules/open-questions.md]
updated: 2026-11-18
---

# Build progress

Every task needed to finish the solo build, as one checklist. Agents tick tasks off as they finish them, and record anything that goes wrong in the [issues log](#issues-log). **Gates** are steps only Oscar can complete: a decision, an approval, or an answer.

Plans below are a **proposed order**, not approved plans. Each becomes a numbered plan in `docs/plans/` through the plan operation, and needs Oscar's approval before any code is written.

## Phase 0 - Set-up

- [x] Harness created: `AGENTS.md` files, wiki schema, templates, skills, settings, `scripts/WikiLint.java`, CI (17 November 2026)
- [x] Team handover filed in `docs/raw/` and ingested (17 November 2026)
- [x] Wiki lint passes on Oscar's machine
- [x] Gate: Oscar confirms [ADR-001](../../decisions/001-repo-structure-and-harness.md) (repo structure and harness), 17 November 2026
- [ ] Gate: Oscar answers the [project context](../project-context.md) questions (purpose, target date)

## Phase 1 - Design gate

Needed before plan 001 can be approved.

- [x] Gate: decide [ADR-002](../../decisions/002-ui-toolkit.md) UI toolkit: Swing (17 November 2026)
- [x] Gate: decide [ADR-003](../../decisions/003-build-tool-and-java-version.md) build tool and Java version: Maven Wrapper, Java 21, target machine Oscar's laptop (17 November 2026)
- [x] Gate: decide [ADR-004](../../decisions/004-reading-the-json-data-files.md) JSON parsing: Gson (17 November 2026)
- [x] NFR-06 and NFR-07 agreed, the two plan 001 rests on (17 November 2026)
- [ ] Gate: review the other requirements with `/spec` before the plan that uses them

Rules questions ([open questions](../rules/open-questions.md)):

- [x] Q-01 to Q-17 answered: all recommended options (17 November 2026)
- [x] Q-04 checked: the 77% 2040 target is in law (2024 regulations), so 11 stands
- [ ] Gate: approve the four disaster explanation lines (Storm and Drought need a source check first), before plan 005

## Phase 2 - Build (proposed plans)

### Plan 001 - Walking skeleton

- [x] Plan drafted ([plan 001](../../plans/001-walking-skeleton.md)), 17 November 2026
- [x] Gate: plan 001 approved (jar `net-zero-belfast.jar`, package `netzerobelfast`), 17 November 2026
- [x] Maven project and wrapper (ADR-003): Maven 3.9.16, checksum-pinned
- [x] Working copies of the three data files in resources, recording the two-way cut-through in `board.json` (Q-05)
- [x] `GameConfig` loads and validates them ([NFR-07](../requirements/nfr-07-data-driven-balance.md)): 78 config tests, 99.1% line coverage
- [x] Seeded `Dice`; coverage rule in the build, proven to fail at 100% ([NFR-06](../requirements/nfr-06-reliability-and-test-coverage.md))
- [x] Runnable jar with `--check`, works alone in an empty folder; the window opens
- [x] CI workflow builds, tests and runs `--check`
- [ ] Gate: Oscar opens the window himself and pushes; CI green on GitHub
- [x] Plan 001 marked `done` (17 November 2026)

Disaster explanations (Q-11) move to plan 005, once Oscar has approved the lines.

### Plan 002 - Players, characters and movement (FR-01, FR-02, FR-03)

- [x] FR-01, FR-02, FR-03 agreed (20-character names, unique names, no second question at the far junction), 17 November 2026
- [x] Plan drafted ([plan 002](../../plans/002-players-characters-and-movement.md)), 17 November 2026
- [x] Gate: plan 002 approved, 17 November 2026
- [x] `maxNameLength` in the working `config.json`
- [x] `Character` and six subclasses, `Player`, `Board` from the data
- [x] `Game.start` with FR-01 checks and FR-02 starting values
- [x] Turn phases: roll, junction choice, direction choice, end turn; illegal actions rejected
- [x] Movement tests for every junction rule, and a headless run (30 games × 10 rounds); 129 tests, `model` coverage 100%
- [x] Tests checked against two deliberate bugs in the junction logic; both caught
- [x] Plan 002 marked `done` (17 November 2026)
- [x] Gate: Oscar confirms the far-junction reading (ending exactly on it means the route question comes next turn), 17 November 2026

[Q-18](../rules/open-questions.md) answered 17 November 2026: after a Glider ride to City Hall the player chooses their direction.

### Plan 003 - Square actions (FR-04 to FR-09)

- [x] FR-04 to FR-09 agreed (no-op actions not offered; buy and convert in one turn allowed; converted squares aren't nature), 17 November 2026
- [x] Plan drafted ([plan 003](../../plans/003-square-actions.md)), 17 November 2026
- [x] Gate: plan 003 approved, 17 November 2026 (Oscar wrote "approve plan 004" while starting plan 003's build; read as plan 003)
- [x] Square classes with `landOn`; `Action`; `availableActions()` and `take()`
- [x] Buy, co-fund, lobby, convert, Resilience, Glider, with the character abilities
- [x] Tests per action, the extended headless run, and the deliberate-bug check: 182 tests, `model` coverage 99.5%; all three bugs caught
- [x] Plan 003 marked `done` (17 November 2026)

### Plan 004 - Climate cards (FR-10, NFR-10)

- [x] FR-10 and NFR-10 agreed (sources shown with title and web address; plan 004 starts bankruptcy; facts re-checked before any demo), 17 November 2026
- [x] Plan drafted ([plan 004](../../plans/004-climate-cards.md)), 17 November 2026
- [x] Gate: plan 004 approved, 17 November 2026
- [x] Card classes, `CardDeck` with seeded shuffle and reshuffle, `ClimateMeter` (start and adjust)
- [x] Quiz, Choice and Event phases; `lastCard()` with fact and sources
- [x] Compulsory payments and the bankruptcy trigger
- [x] Tests, extended headless run, deliberate-bug check: 210 tests, `model` coverage 99.3%; all three bugs caught
- [x] Plan 004 marked `done` (17 November 2026)

### Plan 005 - End of round, meter, disasters, bankruptcy (FR-11, FR-12, FR-13, FR-17)

- [x] Disaster explanation lines sourced (Met Office) and approved by Oscar, 17 November 2026
- [x] FR-11, FR-12, FR-13, FR-17 agreed (a returning player takes a normal roll in the same turn), 17 November 2026
- [x] Plan drafted ([plan 005](../../plans/005-end-of-round-disasters-and-bankruptcy.md)), 17 November 2026
- [x] Gate: plan 005 approved, 18 November 2026
- [x] Explanations and sources in the working `config.json`, validated
- [x] The six end-of-round steps in order, with `RoundReport`; `GAME_OVER` after round 12
- [x] `ClimateMeter` complete; `DisasterManager`; lasting effects with durations from the data
- [x] Bankruptcy complete: return roll, automatic return, loan, no moves or actions
- [x] Tests, 30 complete headless games, deliberate-bug check: 268 tests, `model` coverage 99.4%; all five bugs caught
- [x] Plan 005 marked `done` (18 November 2026)

### Plan 006 - Scoring, end of game and the 20-game headless run (FR-14, NFR-06)

- [x] FR-14 and FR-16 agreed (shares counted across the whole game; computer players also measure balance), 18 November 2026
- [x] Plan drafted ([plan 006](../../plans/006-scoring-and-balance-simulation.md)), 18 November 2026
- [x] Gate: plan 006 approved, 18 November 2026
- [x] Scoring with exact scores and tie-breaks; `GameResult` on `GAME_OVER`
- [x] End-of-game report data: meter history, target lines, polluting shares
- [x] `sim` package: three strategies, `Simulator`, `BalanceReport`; `--simulate`
- [x] 20-game test (NFR-06 AC-2, AC-3); balance results recorded; issue 4 answered: 295 tests; all four bugs caught
- [x] Plan 006 marked `done` (18 November 2026). **The rules engine is complete.**
- [x] Gate: Oscar reads the [balance results](../rules/balance-and-history.md#java-simulation-plan-006) and decides: wait for playtests, no values changed (Q-10), 18 November 2026

### Plan 007 - Game UI: setup, board, panel, actions, pop-ups (FR-01, FR-15, NFR-02, NFR-04, NFR-05, NFR-11)

- [x] FR-15, NFR-02, NFR-04, NFR-05, NFR-11 agreed (every player's Resilience and deal shown; a 4-5 screen tutorial; keyboard-only play kept), 18 November 2026
- [x] Plan drafted ([plan 007](../../plans/007-game-screens.md)), 18 November 2026
- [x] Gate: plan 007 approved, minimum window 1280×720, 18 November 2026
- [x] Palette and contrast test; title, How to play and setup screens
- [x] Game screen: top bar and meter, board, player panel, action bar; controller
- [x] Route choices, card, round-report and return-roll pop-ups; final-scores screen
- [x] Keyboard-only play; `--seed`
- [x] Tests, screens rendered and checked as images, deliberate-bug check: 351 tests; all four bugs caught; window opens in 1.1 s
- [ ] Gate: Oscar plays a full round by hand at the minimum size and maximised (one play-through after plan 008 covers both)
- [ ] Plan 007 marked `done`

### Plan 008 - Visual polish: dark game theme, bundled font, light animation (NFR-02, NFR-04, NFR-05, NFR-11, FR-15)

- [x] Oscar asked for a sleeker, game-like look after trying plan 007, and chose a dark theme, light animation, hand-painted Swing and a bundled font, 18 November 2026
- [x] Plan drafted ([plan 008](../../plans/008-visual-polish.md)), 18 November 2026
- [x] Gate: plan 008 approved; one play-through covers plans 007 and 008; Inter downloaded once from its official release, 18 November 2026
- [x] Inter font and licence; dark palette with contrast test
- [x] Painted buttons, cards, icons; every screen restyled
- [x] `Animator` with dice, counter-slide and meter animations; pop-ups wait for them
- [x] Tests, screens rendered at both sizes and fixed, deliberate-bug check: 365 tests; all five bugs caught; window opens in 1.2 s
- [ ] Gate: Oscar plays; plans 007 and 008 marked `done`

### Plan 009 - End-of-game report (FR-16)

### Plan 010 - Packaging and release (NFR-09), readability check (NFR-03)

The report and packaging plans moved from 008 and 009 when the visual polish was added (18 November 2026); neither had been drafted.

## Phase 3 - Playtesting

- [ ] Gate: decide whether the solo build gets its own playtests (NFR-01, NFR-02)
- [ ] Gate: before any demo, re-check every card's fact and source, and update the working `climate_cards.json` where a figure has changed ([NFR-10](../requirements/nfr-10-fact-accuracy.md) AC-4)
- [ ] Re-tune `config.json` from results ([Q-10](../rules/open-questions.md)): watch how often mixed tables reach net zero (19% in simulation) and the Farmer's win rate (22%)

## Issues log

Newest last. Each entry says what happened, its effect and what was done.

| # | Date | Issue | Status |
|---|---|---|---|
| 1 | 2026-11-17 | The handbook PDF couldn't be read with the PDF page renderer (`pdftoppm` isn't installed). Text was extracted with Python's PyMuPDF instead; all 18 pages came through | Worked around. Install Poppler if page images are ever needed |
| 2 | 2026-11-17 | **Gson 2.15.0 has a dependency, `error_prone_annotations`**, which ADR-004 said it didn't. The shaded jar warned about overlapping files | Excluded from the jar (compile-time annotations only); ADR-004 has a correction note. Clean build now has no warnings |
| 3 | 2026-11-17 | Plan 001 asked the validator to enforce 36 squares, 10/12/4/3/7 types and 30 cards. That would hard-code the board's size, against NFR-07 | The validator checks structure only; the counts are asserted by `GameConfigTest`. Recorded in plan 001's Outcome |
| 4 | 2026-11-18 | **Bankruptcy almost never happens.** In 60 complete headless games (random players, and players who took every action they could afford), nobody went bankrupt: each end of round pays income before the disaster step, so money is topped up before a disaster can take it. The rules work (tested directly in `BankruptcyTest`); this is a balance question | Measured by plan 006's `--simulate`: bankruptcy happens in about 3% of games (0.03 per game, 5,000 games). Rare but real. Open for Oscar to decide whether that's right ([Q-10](../rules/open-questions.md)) |
| 5 | 2026-11-18 | **The game screen crashed on Start** (found while rendering screens to images during plan 007, before anyone ran it): a custom-painted `JComponent` has no accessibility context, so setting its name threw. Unit tests didn't build Swing panels, so they missed it | Fixed: the painted panels extend `JPanel`, and `ScreensTest` now builds and paints every screen headless through a whole game |

## Related

- [Requirements catalogue](../requirements/catalogue.md)
- [Local development](local-development.md)
