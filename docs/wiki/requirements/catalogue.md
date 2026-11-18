---
title: Requirements catalogue
type: overview
status: draft
sources: [../sources/sub1a-report.md, ../sources/handover.md]
updated: 2026-11-17
---

# Requirements catalogue

Every requirement, its priority and its status. IDs and wording come from the team's SUB1a report (section 1.3) and are never changed or reused. Priorities are MoSCoW: Must, Should, Could. A requirement is `draft` until Oscar has reviewed it, `agreed` once he has, and `built` when the code matches it.

The acceptance criteria on each page were written on ingest from the rules and data files, and updated with Oscar's answers to the open rules questions (17 November 2026). Pages still marked Draft need Oscar's review (`/spec`).

## Functional requirements

| ID | Requirement | Priority | Verified by | Status |
|---|---|---|---|---|
| [FR-01](fr-01-start-game.md) | Start a game with 4-6 players and distinct characters | Must | JUnit, manual | Agreed (rules built; screen in plan 007) |
| [FR-02](fr-02-starting-values-and-first-player.md) | Starting values and a random first player | Must | JUnit | Built |
| [FR-03](fr-03-dice-movement-and-junctions.md) | Two dice, clockwise movement, junction choice | Must | JUnit | Built |
| [FR-04](fr-04-buy-square.md) | Buy an unowned square; no buying if unaffordable | Must | JUnit | Built |
| [FR-05](fr-05-co-funding.md) | Co-fund a Green Project for £3M, one deal per player | Must | JUnit | Built |
| [FR-06](fr-06-lobbying.md) | Lobby a Polluting square for £2M | Should | JUnit | Built |
| [FR-07](fr-07-convert-square.md) | Convert one Polluting square per turn for £5M | Must | JUnit | Built |
| [FR-08](fr-08-resilience.md) | Resilience levels cut disaster payments | Should | JUnit | Built |
| [FR-09](fr-09-glider.md) | Glider ride for £1M | Should | Manual | Built |
| [FR-10](fr-10-climate-cards.md) | Climate Cards with fact and source | Must | JUnit, manual | Built |
| [FR-11](fr-11-end-of-round-order.md) | End-of-round steps in a fixed order | Must | JUnit | Built |
| [FR-12](fr-12-disasters.md) | Disasters by danger band, cut by Resilience | Must | JUnit | Built |
| [FR-13](fr-13-bankruptcy.md) | Bankruptcy | Should | JUnit | Built |
| [FR-14](fr-14-legacy-score-and-winner.md) | Legacy Score and one winner | Must | JUnit | Draft |
| [FR-15](fr-15-always-visible-status.md) | Year, round, meter, band and every player's status always visible | Must | Manual | Draft |
| [FR-16](fr-16-end-of-game-report.md) | End-of-game report | Must | Manual | Draft |
| [FR-17](fr-17-disaster-explanation.md) | One-line explanation for each disaster | Should | Manual | Agreed |

## Non-functional requirements

| ID | Requirement | Priority | Verified by | Status |
|---|---|---|---|---|
| [NFR-01](nfr-01-session-length.md) | A 4-player game within 60 minutes | Must | Playtest | Draft |
| [NFR-02](nfr-02-learnability.md) | 4 in 5 first-time players complete a turn unaided | Must | User test | Draft |
| [NFR-03](nfr-03-readability.md) | Text scores 60+ on Flesch Reading Ease | Should | Tool check | Draft |
| [NFR-04](nfr-04-responsiveness.md) | Opens within 5 s, responds within 1 s | Must | Timed test | Draft |
| [NFR-05](nfr-05-accessibility.md) | WCAG 2.1 AA contrast; square types not by colour alone | Should | Inspection | Draft |
| [NFR-06](nfr-06-reliability-and-test-coverage.md) | 80% line coverage of game logic; 20 games without crashes | Must | JUnit, coverage | Agreed |
| [NFR-07](nfr-07-data-driven-balance.md) | Balance values and cards in data files | Must | Inspection | Agreed |
| [NFR-08](nfr-08-privacy-and-security.md) | No personal data, no network, validate all input | Must | Inspection, test | Draft |
| [NFR-09](nfr-09-portability.md) | Windows and macOS with only a Java runtime | Should | Manual | Draft |
| [NFR-10](nfr-10-fact-accuracy.md) | Every fact stored with a source | Should | Inspection | Agreed |
| [NFR-11](nfr-11-energy-efficiency.md) | Under 5% CPU while idle | Could | Profiling | Draft |

## Stretch ideas

Not requirements. Each needs `/spec` before it can be planned.

| ID | Idea | Source |
|---|---|---|
| S-01 | Save and resume a game | Dropped from the SUB1a report for space; the handover says "treat as a Could" |
| S-02 | End-of-game report: "the decisions that moved the meter most" | Rules v4's Learning section; not in FR-16 |

## Module note

At SUB2 the team's game demo schedule may name at most 14 core requirements and 6 value-added features. That affects the team project, not this solo build, but it's a useful guide to which requirements matter most.

## Related

- [Overview](../overview.md)
- [Open rules questions](../rules/open-questions.md)
