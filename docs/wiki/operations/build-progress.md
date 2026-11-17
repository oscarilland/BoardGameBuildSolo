---
title: Build progress
type: operation
status: draft
sources: [../requirements/catalogue.md, ../rules/open-questions.md]
updated: 2026-11-17
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

### Plan 004 - Climate cards (FR-10, NFR-10)

### Plan 005 - End of round, meter, disasters, bankruptcy (FR-11, FR-12, FR-13, FR-17)

### Plan 006 - Scoring, end of game and the 20-game headless run (FR-14, NFR-06)

### Plan 007 - Game UI: setup, board, panel, actions, pop-ups (FR-01, FR-15, NFR-02, NFR-04, NFR-05, NFR-11)

### Plan 008 - End-of-game report (FR-16)

### Plan 009 - Packaging and release (NFR-09), readability check (NFR-03)

## Phase 3 - Playtesting

- [ ] Gate: decide whether the solo build gets its own playtests (NFR-01, NFR-02)
- [ ] Re-tune `config.json` from results ([Q-10](../rules/open-questions.md))

## Issues log

Newest last. Each entry says what happened, its effect and what was done.

| # | Date | Issue | Status |
|---|---|---|---|
| 1 | 2026-11-17 | The handbook PDF couldn't be read with the PDF page renderer (`pdftoppm` isn't installed). Text was extracted with Python's PyMuPDF instead; all 18 pages came through | Worked around. Install Poppler if page images are ever needed |
| 2 | 2026-11-17 | **Gson 2.15.0 has a dependency, `error_prone_annotations`**, which ADR-004 said it didn't. The shaded jar warned about overlapping files | Excluded from the jar (compile-time annotations only); ADR-004 has a correction note. Clean build now has no warnings |
| 3 | 2026-11-17 | Plan 001 asked the validator to enforce 36 squares, 10/12/4/3/7 types and 30 cards. That would hard-code the board's size, against NFR-07 | The validator checks structure only; the counts are asserted by `GameConfigTest`. Recorded in plan 001's Outcome |

## Related

- [Requirements catalogue](../requirements/catalogue.md)
- [Local development](local-development.md)
