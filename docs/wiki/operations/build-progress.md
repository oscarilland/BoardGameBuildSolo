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
- [ ] Gate: Oscar confirms [ADR-001](../../decisions/001-repo-structure-and-harness.md) (repo structure and harness)
- [ ] Gate: Oscar answers the [project context](../project-context.md) questions (purpose, target date)

## Phase 1 - Design gate

Needed before plan 001 can be approved.

- [x] Gate: decide [ADR-002](../../decisions/002-ui-toolkit.md) UI toolkit: Swing (17 November 2026)
- [x] Gate: decide [ADR-003](../../decisions/003-build-tool-and-java-version.md) build tool and Java version: Maven Wrapper, Java 21, target machine Oscar's laptop (17 November 2026)
- [ ] Gate: decide [ADR-004](../../decisions/004-reading-the-json-data-files.md) JSON parsing
- [ ] Gate: review FR-01 to FR-17 and NFR-01 to NFR-11 (`/spec`, a few at a time)

Rules questions, needed before the plan that uses them ([open questions](../rules/open-questions.md)):

- [ ] Before plan 002: Q-05 (cut-through), Q-06 (starting positions)
- [ ] Before plan 003: Q-07 (after a Glider ride), Q-08 (Resilience rounding)
- [ ] Before plan 004: Q-12 (unaffordable Choice options), Q-13 (quiz reward)
- [ ] Before plan 005: Q-02 (meter below 0), Q-11 (disaster explanations), Q-14 (bankruptcy return), Q-15 (closed and flooded squares), Q-17 (no owned squares)
- [ ] Before plan 006: Q-01 (final tie-break), Q-16 (score rounding)
- [ ] Any time (data only): Q-03, Q-04, Q-09, Q-10

## Phase 2 - Build (proposed plans)

### Plan 001 - Walking skeleton

- [ ] Project set up per ADR-003; CI builds and tests
- [ ] Working copies of the three data files in resources
- [ ] `GameConfig` loads and validates them ([NFR-07](../requirements/nfr-07-data-driven-balance.md))
- [ ] Seeded `Dice`; coverage rule in the build ([NFR-06](../requirements/nfr-06-reliability-and-test-coverage.md))
- [ ] An empty window opens (proves the UI toolkit and packaging)

### Plan 002 - Players, characters and movement (FR-01, FR-02, FR-03)

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

## Related

- [Requirements catalogue](../requirements/catalogue.md)
- [Local development](local-development.md)
