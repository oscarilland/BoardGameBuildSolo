---
title: Source - SUB1a Interim System Report draft
type: source
status: draft
sources: [../../raw/net-zero-belfast-handover/docs/SUB1a_Interim_System_Report_draft.docx]
updated: 2026-11-17
---

# Source - SUB1a Interim System Report draft

## Provenance

- **File:** [SUB1a_Interim_System_Report_draft.docx](../../raw/net-zero-belfast-handover/docs/SUB1a_Interim_System_Report_draft.docx)
- **Dated:** November 2026 (references accessed 11-13 November 2026). A draft on the module's template: the title page, team names, AI statement, minutes, contract and two appendices are still placeholders.
- **Authority:** the team's written requirements and analysis model, as submitted (or about to be) for SUB1a. The requirement IDs and wording here are the reference for this wiki.

## What it covers

| Section | Content | Used for |
|---|---|---|
| 1.1 | Problem and vision, SDG 13 targets 13.3 and 13.1 | [Overview](../overview.md) |
| 1.2 | Five requirement sources, including the simulation | [Balance and design history](../rules/balance-and-history.md) |
| 1.3 | FR-01 to FR-17 and NFR-01 to NFR-11, with MoSCoW priority and verification method | Every page in `requirements/` |
| 1.4 | Game concept and LO1-LO5 | [Learning outcomes](../learning-outcomes.md) |
| 1.5 | Design variants considered | [Balance and design history](../rules/balance-and-history.md) |
| 1.6 | Sustainability in play and in the software | [Learning outcomes](../learning-outcomes.md), [NFR-11](../requirements/nfr-11-energy-efficiency.md) |
| 2 | Use case diagram and three written use cases (Take Turn, Draw Climate Card, End Round) | [Game flow](../architecture/game-flow.md) |
| 3.1 | Class diagram commentary | [Domain model](../architecture/domain-model.md) |
| 3.2 | Sequence diagram commentary | [Game flow](../architecture/game-flow.md) |
| 3.3 | Draft layout | [Screens and layout](../ux/screens-and-layout.md) |
| References | Nine sources (Act, DAERA, NISRA x2, Water Report, UN, DfE, Met Office, Sommerville) | [Climate cards](../rules/climate-cards.md) |

## What it contributed

- The exact wording, priority and verification method of all 28 requirements.
- The written use cases' main and alternate flows, which set the order of steps in [game flow](../architecture/game-flow.md).
- "The Glider (FR-09) is omitted [from the use case diagram] for readability", noted on [FR-09](../requirements/fr-09-glider.md).

## Limitations and caveats

- FR-14 here says "breaking ties by Green Projects owned", with no rule for further ties ([Q-01](../rules/open-questions.md)).
- FR-16 here asks for "each player's share of the city's polluting squares". The rules' end-of-game report also mentions "the decisions that moved the meter most", which isn't a requirement ([FR-16](../requirements/fr-16-end-of-game-report.md)).
- The Take Turn use case puts "the player chooses to roll" as a step, so rolling is a player action, not automatic.
- It's a team document: the AI statement still needs writing. Nothing from this solo repo should be added to it.
