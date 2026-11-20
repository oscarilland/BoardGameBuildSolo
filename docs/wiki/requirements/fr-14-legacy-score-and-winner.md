---
title: FR-14 Legacy Score and winner
type: requirement
id: FR-14
priority: must
status: built
sources: [../sources/sub1a-report.md, ../sources/handover.md]
updated: 2026-11-17
---

# FR-14 Legacy Score and winner

## Summary

> After round 12 the system shall calculate every Legacy Score using the net-zero or missed-net-zero formula and declare one winner, breaking ties by Green Projects owned. *(SUB1a; verified by JUnit)*

## Behaviour

See [scoring and end of game](../rules/scoring-and-end-of-game.md).

## Acceptance criteria

- **AC-1** With the meter at 0 after round 12, a player with 30 GP, £45M and one return from bankruptcy scores 30 + (45 − 15) ÷ 5 = 36.
- **AC-2** With the meter at 1, a player with 30 GP, £80M and 2 Polluting squares scores 30 − 10 = 20; money is ignored.
- **AC-3** The end screen says whether Belfast reached net zero.
- **AC-4** The highest Legacy Score wins. If two players tie, the one with more Green Projects wins.
- **AC-5** If players are still tied on Green Projects, the one with fewer Polluting squares wins; if still tied, a seeded die roll-off between them decides, re-rolling ties. Exactly one winner is declared in every game ([Q-01](../rules/open-questions.md)).
- **AC-6** Scores are exact: £23M after repayments adds 4.6, shown as one decimal place. A player on 36.4 beats one on 36.2; neither is rounded to 36 ([Q-16](../rules/open-questions.md)).

## Out of scope

Shared wins; a cooperative "everyone wins" mode.

## Decisions (Oscar, 17 November 2026)

1. Final tie-break: fewer Polluting squares, then a die roll-off ([Q-01](../rules/open-questions.md)).
2. Exact scores, shown to one decimal place ([Q-16](../rules/open-questions.md)).
3. **Agreed** (18 November 2026).

## Build status

**Built** in [plan 006](../../plans/006-scoring-and-balance-simulation.md): AC-1 to AC-6, tested in `ScoringTest` and `EndOfGameReportTest`. The model says whether Belfast reached net zero (AC-3); the end screen that shows it is plan 009.

## Related

- [FR-16 End-of-game report](fr-16-end-of-game-report.md)
- [Learning outcomes: LO3](../learning-outcomes.md)
