---
title: FR-07 Convert square
type: requirement
id: FR-07
priority: must
status: draft
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

## Out of scope

Converting Green Projects back to Polluting.

## Open questions for Oscar

1. Does a converted square count as a nature project for the Farmer? The rules say converting creates a normal Green Project; the simulation agrees.

## Related

- [Turn and actions](../rules/turn-and-actions.md)
- [Domain model: PollutingSquare.convert()](../architecture/domain-model.md)
