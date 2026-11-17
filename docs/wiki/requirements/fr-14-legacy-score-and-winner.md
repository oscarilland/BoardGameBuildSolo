---
title: FR-14 Legacy Score and winner
type: requirement
id: FR-14
priority: must
status: draft
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
- **AC-5** Exactly one winner is declared in every game, using the agreed rule for further ties ([Q-01](../rules/open-questions.md)).
- **AC-6** Scores are calculated with the agreed rounding ([Q-16](../rules/open-questions.md)).

## Out of scope

Shared wins; a cooperative "everyone wins" mode.

## Open questions for Oscar

1. [Q-01](../rules/open-questions.md): the final tie-break.
2. [Q-16](../rules/open-questions.md): whole-number or fractional scores.

## Related

- [FR-16 End-of-game report](fr-16-end-of-game-report.md)
- [Learning outcomes: LO3](../learning-outcomes.md)
