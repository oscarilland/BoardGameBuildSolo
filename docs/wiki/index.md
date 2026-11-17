# Net Zero Belfast wiki index

Every page in `docs/`, one line each. Read this first, then open only what you need.

## Start here

- [Overview](overview.md) - the game on one page
- [Project context](project-context.md) - why this solo repo exists, and its constraints
- [Learning outcomes](learning-outcomes.md) - LO1-LO5 and where the game teaches them
- [Requirements catalogue](requirements/catalogue.md) - every requirement, its priority and status
- [Open rules questions](rules/open-questions.md) - Q-01 to Q-18, all answered by Oscar
- [Build progress](operations/build-progress.md) - the checklist and issues log

## Rules

- [Setup and characters](rules/setup-and-characters.md) - players, first player, the six characters
- [Board and squares](rules/board-and-squares.md) - the 36-square board, square types, Resilience
- [Turn and actions](rules/turn-and-actions.md) - roll, move, act, convert; buy, co-fund, lobby, Glider
- [End of round](rules/end-of-round.md) - the six steps in order
- [Climate Meter and disasters](rules/climate-meter-and-disasters.md) - meter, interim targets, danger bands, disasters
- [Bankruptcy](rules/bankruptcy.md) - trigger, sitting out, return and loan
- [Climate cards](rules/climate-cards.md) - quiz, choice and event cards; the 30-card deck
- [Scoring and end of game](rules/scoring-and-end-of-game.md) - Legacy Score, net zero, tie-break, report
- [Open rules questions](rules/open-questions.md) - Oscar's answers to Q-01 to Q-18
- [Balance and design history](rules/balance-and-history.md) - simulation results and v3 → v4 changes

## Requirements

- [FR-01 Start game](requirements/fr-01-start-game.md) - must, agreed
- [FR-02 Starting values and first player](requirements/fr-02-starting-values-and-first-player.md) - must, built
- [FR-03 Dice, movement and junctions](requirements/fr-03-dice-movement-and-junctions.md) - must, agreed
- [FR-04 Buy square](requirements/fr-04-buy-square.md) - must, built
- [FR-05 Co-funding](requirements/fr-05-co-funding.md) - must, agreed
- [FR-06 Lobbying](requirements/fr-06-lobbying.md) - should, agreed
- [FR-07 Convert square](requirements/fr-07-convert-square.md) - must, agreed
- [FR-08 Resilience](requirements/fr-08-resilience.md) - should, agreed
- [FR-09 Glider](requirements/fr-09-glider.md) - should, built
- [FR-10 Climate cards](requirements/fr-10-climate-cards.md) - must, draft
- [FR-11 End-of-round order](requirements/fr-11-end-of-round-order.md) - must, draft
- [FR-12 Disasters](requirements/fr-12-disasters.md) - must, draft
- [FR-13 Bankruptcy](requirements/fr-13-bankruptcy.md) - should, draft
- [FR-14 Legacy Score and winner](requirements/fr-14-legacy-score-and-winner.md) - must, draft
- [FR-15 Always-visible status](requirements/fr-15-always-visible-status.md) - must, draft
- [FR-16 End-of-game report](requirements/fr-16-end-of-game-report.md) - must, draft
- [FR-17 Disaster explanation](requirements/fr-17-disaster-explanation.md) - should, draft
- [NFR-01 Session length](requirements/nfr-01-session-length.md) - must, draft
- [NFR-02 Learnability](requirements/nfr-02-learnability.md) - must, draft
- [NFR-03 Readability](requirements/nfr-03-readability.md) - should, draft
- [NFR-04 Responsiveness](requirements/nfr-04-responsiveness.md) - must, draft
- [NFR-05 Accessibility](requirements/nfr-05-accessibility.md) - should, draft
- [NFR-06 Reliability and test coverage](requirements/nfr-06-reliability-and-test-coverage.md) - must, agreed
- [NFR-07 Data-driven balance](requirements/nfr-07-data-driven-balance.md) - must, agreed
- [NFR-08 Privacy and security](requirements/nfr-08-privacy-and-security.md) - must, draft
- [NFR-09 Portability](requirements/nfr-09-portability.md) - should, draft
- [NFR-10 Fact accuracy](requirements/nfr-10-fact-accuracy.md) - should, draft
- [NFR-11 Energy efficiency](requirements/nfr-11-energy-efficiency.md) - could, draft

## Architecture

- [System overview](architecture/system-overview.md) - model, config, UI and sim; key choices; dependencies
- [Domain model](architecture/domain-model.md) - the analysis classes, and gaps to settle in plan 001
- [Game flow](architecture/game-flow.md) - use cases, the three sequences, choices without calling the UI
- [Data files](architecture/data-files.md) - built: config, board and card schemas; loading and validation as built

## UX

- [Screens and layout](ux/screens-and-layout.md) - screen flow, game screen areas, UI rules

## Operations

- [Working with Claude and Codex](operations/working-with-agents.md) - the workflow and skills
- [Local development](operations/local-development.md) - tools and checks
- [Build progress](operations/build-progress.md) - checklist, proposed plans, issues log

## Decisions

- [ADR-001 Repo structure and build harness](../decisions/001-repo-structure-and-harness.md) - accepted
- [ADR-002 Desktop UI toolkit](../decisions/002-ui-toolkit.md) - accepted: Swing
- [ADR-003 Build tool and Java version](../decisions/003-build-tool-and-java-version.md) - accepted: Maven Wrapper, Java 21
- [ADR-004 Reading the JSON data files](../decisions/004-reading-the-json-data-files.md) - accepted: Gson

## Plans

- [Plan 001 Walking skeleton](../plans/001-walking-skeleton.md) - done: Maven, data loading and validation, seeded dice, coverage rule, runnable jar, empty window
- [Plan 002 Players, characters and movement](../plans/002-players-characters-and-movement.md) - done: FR-01 to FR-03 headless; turn phases, junctions, two-way City Centre route
- [Plan 003 Square actions](../plans/003-square-actions.md) - done: FR-04 to FR-09; square classes, offered actions, character abilities

See [build progress](operations/build-progress.md) for the proposed order of later plans.

## Sources

- [Team handover (HANDOVER.md)](sources/handover.md) - the index to the handover, and the source of truth
- [Rules v4](sources/rules-v4.md) - player-facing rules (Markdown and Word)
- [SUB1a Interim System Report draft](sources/sub1a-report.md) - requirements, use cases, analysis model
- [CSC2058 module handbook](sources/module-handbook.md) - brief, submissions, GitLab and AI rules
- [Data files](sources/data-files.md) - config, board and climate cards
- [Design diagrams](sources/design-diagrams.md) - use case, class, sequence, board, wireframe, game loop
- [Balance simulation](sources/balance-simulation.md) - the Python model behind v4's numbers
