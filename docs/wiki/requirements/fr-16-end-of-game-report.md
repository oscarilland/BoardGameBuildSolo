---
title: FR-16 End-of-game report
type: requirement
id: FR-16
priority: must
status: draft
sources: [../sources/sub1a-report.md, ../sources/handover.md]
updated: 2026-11-17
---

# FR-16 End-of-game report

## Summary

> The system shall show an end-of-game report with a graph of the Climate Meter from 2026 to 2050 and each player's share of the city's polluting squares. *(SUB1a; verified manually)*

## Behaviour

After scoring, a report screen shows: a line graph of the meter for every round from 2026 to 2050, with the interim targets and net zero line marked; each player's share of the city's polluting squares across the game; every Legacy Score; and the winner.

## Acceptance criteria

- **AC-1** The graph has one point for the start (2026, meter 50) and one after every round, and marks 26 (2030), 11 (2040) and 0.
- **AC-2** Each player's share is their polluting-square-rounds ÷ the city's total polluting-square-rounds, shown as a percentage; shares add up to 100% (or are all 0 if nobody ever owned a polluting square).
- **AC-3** Scores, the net zero result and the single winner match [FR-14](fr-14-legacy-score-and-winner.md).
- **AC-4** The graph is readable without colour alone: axis labels, and target lines labelled in text.

## Out of scope

"The decisions that moved the meter most", from rules v4's Learning section: stretch item S-02.

## Open questions for Oscar

1. AC-2 assumes "share across the game" means polluting-square-rounds, as the simulation counted. Or should it be squares owned at the end?

## Related

- [Scoring and end of game](../rules/scoring-and-end-of-game.md)
- [Learning outcomes: LO3](../learning-outcomes.md)
