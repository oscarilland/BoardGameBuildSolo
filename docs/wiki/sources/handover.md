---
title: Source - Team handover (HANDOVER.md)
type: source
status: draft
sources: [../../raw/net-zero-belfast-handover/HANDOVER.md]
updated: 2026-11-17
---

# Source - Team handover (HANDOVER.md)

## Provenance

- **File:** [raw/net-zero-belfast-handover/HANDOVER.md](../../raw/net-zero-belfast-handover/HANDOVER.md), the index to the whole handover folder.
- **Dated:** 17 November 2026. Written for Claude Code, from Oscar's design work with Claude on claude.ai. Filed here on 17 November 2026.
- **Authority:** the handover's own rule: "where this file and the rules disagree, this file wins". It holds clarifications agreed after rules v4, so it outranks [rules v4](rules-v4.md).
- **Folder contents:** this file, the rules ([receipt](rules-v4.md)), the SUB1a report draft ([receipt](sub1a-report.md)), the module handbook ([receipt](module-handbook.md)), three data files ([receipt](data-files.md)), the design diagrams ([receipt](design-diagrams.md)) and the balance simulation ([receipt](balance-simulation.md)).

## What it covers

| Section | Content | Used for |
|---|---|---|
| 1 | Ground rules: GitLab, commit history, AI declaration, JUnit target | [Project context](../project-context.md), [ADR-001](../../decisions/001-repo-structure-and-harness.md) |
| 2 | The game in one paragraph, LO1-LO5 | [Overview](../overview.md), [learning outcomes](../learning-outcomes.md) |
| 3 | The rules specification, v4 plus clarifications | Every page in `rules/` |
| 4 | FR-01 to FR-17 and NFR-01 to NFR-11, short form | [Requirements catalogue](../requirements/catalogue.md) |
| 5 | Use cases, analysis classes, sequence diagrams, UI, architecture guidance | [Domain model](../architecture/domain-model.md), [game flow](../architecture/game-flow.md), [system overview](../architecture/system-overview.md), [screens](../ux/screens-and-layout.md) |
| 6 | Assumptions: Java, hot-seat, lab PCs; toolkit and Java version undecided | [ADR-002](../../decisions/002-ui-toolkit.md), [ADR-003](../../decisions/003-build-tool-and-java-version.md) |
| 7 | Ten open questions | [Open questions](../rules/open-questions.md) Q-01 to Q-10 |
| 8 | How the numbers were chosen, decision history | [Balance and design history](../rules/balance-and-history.md) |
| 9 | File index | This receipt |

## What it contributed

Clarifications beyond rules v4, now on the rules pages:

- Max one conversion per turn, either on landing or anywhere ([turn and actions](../rules/turn-and-actions.md)).
- Resilience doesn't reduce Event card costs ([climate cards](../rules/climate-cards.md)).
- Bankrupt players still receive GP, and co-funding deals continue if the owner is bankrupt ([bankruptcy](../rules/bankruptcy.md)).
- The exact end-of-round order with the meter formula, and the Activist's lobbying cancelling the +2 ([end of round](../rules/end-of-round.md)).
- Interim targets in round 3 (2030) and round 8 (2040) ([Climate Meter and disasters](../rules/climate-meter-and-disasters.md)).
- The board shape: a 30-square loop with a 6-square City Centre cut-through between junctions 12 and 27 ([board and squares](../rules/board-and-squares.md)).
- Architecture guidance: model separate from UI, one injectable RNG, data loaded at start-up.

## Limitations and caveats

- Says "4-6 players"; the class diagram and report agree.
- The requirement list is abbreviated; the [SUB1a report](sub1a-report.md) has the full wording, which the requirement pages use.
- "Save/resume was dropped from the report for space; treat as a Could" - recorded as stretch item S-01.
- It doesn't mention the disaster explanation text FR-17 needs, or what happens when a player can't afford a Choice card option. Both were found on ingest and added as [Q-11 and Q-12](../rules/open-questions.md).
- It mentions a "Climate Cards Sheet on claude.ai" as the team-editable master for the cards. That sheet isn't in the handover; `climate_cards.json` is the copy used here.
