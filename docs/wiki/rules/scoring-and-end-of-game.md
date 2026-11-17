---
title: Scoring and end of game
type: rules
status: draft
sources: [../sources/handover.md, ../sources/rules-v4.md, ../sources/data-files.md]
updated: 2026-11-17
---

# Scoring and end of game

## Summary

After round 12 the city has either reached net zero or not, and that decides whether money counts. There is always exactly one winner.

## Rules

1. The game ends after round 12, when the year reaches 2050 (`game.rounds`, `game.endYear`).
2. **Loan repayments** of £15M per return from bankruptcy are deducted from money first.
3. **Net zero reached** (meter ≤ 0): `Legacy = GP + (money − loan repayments) ÷ 5` (`scoring.moneyDivisor: 5`).
4. **Net zero missed:** money is worthless. Each player loses 5 GP for each Polluting square they still own (`missedPollutingPenaltyGP: 5`). `Legacy = GP`. The end screen says Belfast missed its target.
5. **One winner:** the highest Legacy Score. Ties go to whoever owns more Green Projects. Further ties are undecided ([Q-01](open-questions.md)).
6. **End-of-game report:** a graph of the Climate Meter from 2026 to 2050 (so keep the per-round history), each player's share of the city's polluting squares across the game, scores and the winner ([FR-16](../requirements/fr-16-end-of-game-report.md)).

## Edge cases

- Money ÷ 5 is usually fractional (£23M → 4.6). Whether the score keeps the fraction or rounds isn't stated, and it can decide the winner ([Q-16](open-questions.md)).
- Money after loan repayments can be negative, which lowers a net zero Legacy Score.
- "Share of polluting squares across the game" needs per-round ownership history, not just the final state. The simulation tracks it as polluting-square-rounds per player.

## Conflicts or uncertainties

- The class diagram's `legacyScore(netZero): int` returns a whole number, which implies rounding ([Q-16](open-questions.md)).
- Rules v4's report also mentions "the decisions that moved the meter most"; the requirement doesn't ([FR-16](../requirements/fr-16-end-of-game-report.md)).

## Related

- [FR-14 Legacy Score and winner](../requirements/fr-14-legacy-score-and-winner.md)
- [FR-16 End-of-game report](../requirements/fr-16-end-of-game-report.md)
- [Bankruptcy](bankruptcy.md)
