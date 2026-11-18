---
title: FR-11 End-of-round order
type: requirement
id: FR-11
priority: must
status: built
sources: [../sources/sub1a-report.md, ../sources/handover.md]
updated: 2026-11-17
---

# FR-11 End-of-round order

## Summary

> At the end of each round the system shall, in order: pay income and GP, apply the carbon tax, update the Climate Meter, check the interim target (2030 and 2040 only), check for a disaster, and advance the year by 2. *(SUB1a; verified by JUnit)*

## Behaviour

See [end of round](../rules/end-of-round.md) for each step's detail.

## Acceptance criteria

- **AC-1** End of round runs after the last player's turn, and only then.
- **AC-2** The six steps run in the stated order. A test with a seeded game records each step and checks the sequence.
- **AC-3** Income: an owned Polluting square pays +£7M; a Green Project pays +£2M and +2 GP; closed and flooded squares pay nothing (no money, no GP, no co-funding GP); lobbied squares pay no money; bankrupt owners get GP but no money.
- **AC-9** Closed and flooded Polluting squares still cost their owner carbon tax and still count +2 towards the meter ([Q-15](../rules/open-questions.md)).
- **AC-10** The meter can end a round below 0 (for example −3); it isn't floored ([Q-02](../rules/open-questions.md)).
- **AC-4** Carbon tax: a player with 3 Polluting squares loses 3 GP.
- **AC-5** Meter: with 4 owned Polluting squares (1 lobbied by the Activist) and 5 owned Green Projects, the meter changes by 3 + 2×3 − 5 = +4.
- **AC-6** Interim target: in round 3 with the meter at 27, a player with 2 Polluting squares loses 4 GP; with the meter at 26, nothing happens. The same check runs in round 8 against 11, and in no other round.
- **AC-7** Year and round advance by 2 and 1; after round 12 the game ends.
- **AC-8** The meter's history holds one value per round, for [FR-16](fr-16-end-of-game-report.md).

## Out of scope

Animating each step (UI polish).

## Decisions (Oscar, 17 November 2026)

1. The meter can go below 0 ([Q-02](../rules/open-questions.md)).
2. Closed and flooded squares still pay carbon tax and move the meter, but earn nothing ([Q-15](../rules/open-questions.md)).
3. **Agreed.**

## Build status

**Built** in [plan 005](../../plans/005-end-of-round-disasters-and-bankruptcy.md): AC-1 to AC-10, tested in `RoundOrderTest`, `IncomeTest`, `CarbonTaxTest`, `MeterUpdateTest`, `InterimTargetTest` and `ClimateMeterTest`. AC-7's end of game stops in `GAME_OVER`; scoring is plan 006.

## Related

- [FR-12 Disasters](fr-12-disasters.md)
- [Game flow: End Round](../architecture/game-flow.md)
