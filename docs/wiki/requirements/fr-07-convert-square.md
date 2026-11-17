---
title: FR-07 Convert square
type: requirement
id: FR-07
priority: must
status: agreed
sources: [../sources/sub1a-report.md, ../sources/handover.md]
updated: 2026-11-17
---

# FR-07 Convert square

## Summary

> The system shall let a player convert one Polluting square they own into a Green Project per turn for £5M. *(SUB1a; verified by JUnit)*

## Behaviour

- A player may convert one Polluting square they own, for £5M, either when they land on it or anywhere on the board after acting on their square. At most one conversion per turn.
- The converted square becomes a Green Project with the same owner. It isn't a nature project.
- If the Industrialist converts it, it earns +£2M per round on top of the normal Green Project income.

## Acceptance criteria

- **AC-1** A player who owns a Polluting square and has £5M can convert it; they pay £5M and the square is now a Green Project they own.
- **AC-2** A second conversion in the same turn is refused, whether the first happened on landing or afterwards.
- **AC-3** A player with £4M can't convert.
- **AC-4** At the next end of round, the converted square pays +£2M and +2 GP and lowers the meter by 1, instead of +£7M, −1 GP and +2.
- **AC-5** A square converted by the Industrialist pays +£4M a round; one bought as a Green Project by the Industrialist pays +£2M.
- **AC-6** A bankrupt player can't convert.
- **AC-7** A Polluting square bought this turn can be converted in the same turn, as the after-landing conversion, if the player can afford it.
- **AC-8** A converted square is a normal Green Project, not a nature project.

## Out of scope

Converting Green Projects back to Polluting.

## Decisions (Oscar, 17 November 2026)

1. **Buy and convert in one turn:** allowed, as the rules are written and as the balance simulation played (AC-7).
2. **Converted squares aren't nature projects** (AC-8).
3. **Agreed.**

## Build status

Built in [plan 003](../../plans/003-square-actions.md): AC-1, AC-2, AC-3, AC-7, AC-8, and the converted-by mark for AC-5, tested in `ConvertTest`. The converted square's income (AC-4, AC-5) and the bankruptcy rule (AC-6) are plan 005.

## Related

- [Turn and actions](../rules/turn-and-actions.md)
- [Domain model: PollutingSquare.convert()](../architecture/domain-model.md)
