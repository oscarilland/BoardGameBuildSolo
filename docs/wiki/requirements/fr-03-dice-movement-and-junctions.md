---
title: FR-03 Dice, movement and junctions
type: requirement
id: FR-03
priority: must
status: built
sources: [../sources/sub1a-report.md, ../sources/data-files.md]
updated: 2026-11-17
---

# FR-03 Dice, movement and junctions

## Summary

> On each turn the system shall roll two dice and move the player clockwise; at a junction it shall let the player stay on the loop or take the City Centre route. *(SUB1a; verified by JUnit)*

## Behaviour

- The player chooses to roll. Two six-sided dice give 2-12 steps.
- Movement is clockwise along the loop (ids ascending, 29 wraps to 0).
- Whenever a move reaches junction 12 or 27 with steps still to go, the player chooses to stay on the loop or take the two-way City Centre route (30-35). A player who lands exactly on a junction chooses at the start of their next move ([Q-05](../rules/open-questions.md)).

See [board and squares](../rules/board-and-squares.md) and [turn and actions](../rules/turn-and-actions.md).

## Acceptance criteria

- **AC-1** With a fixed seed, `Dice.rollTwo()` returns the same sequence every time, and every value is between 2 and 12.
- **AC-2** A player on square 28 who rolls 4 ends on square 2 (wrapping past 29).
- **AC-3** A player whose move reaches junction 12 is offered the route choice; choosing "stay on the loop" continues to 13, 14 and so on.
- **AC-4** A player on 11 who rolls 4 and takes the City Centre at 12 moves 12 → 35 → 34 → 33 and stops on 33. A player on 26 who rolls 9 and takes it at 27 moves 27 → 30 → … → 35 → 12 → 13 and stops on 13 (each square, including the junction, is one step).
- **AC-5** A move that doesn't reach a junction with steps left asks no question. A player who lands exactly on 12 or 27 is asked at the start of their next move.
- **AC-7** From the City Centre route, the player rejoins the loop at the far junction and carries on clockwise, without being asked again at that junction.
- **AC-6** A bankrupt player doesn't move ([FR-13](fr-13-bankruptcy.md)).

## Out of scope

Animating the counter's move (UI polish, not logic).

## Decisions (Oscar, 17 November 2026)

1. **Two-way cut-through, choice whenever a junction is reached** ([Q-05](../rules/open-questions.md)). AC-4, AC-5 and AC-7 spell out the step counting.
2. **No second question at the far junction (AC-7):** confirmed. A player coming out of the cut-through carries on clockwise.
3. **Agreed.**

## Build status

Built in [plan 002](../../plans/002-players-characters-and-movement.md): AC-1 to AC-5 and AC-7, tested in `BoardTest`, `MovementTest` and `DiceTest`. AC-6 (a bankrupt player doesn't move) was built in [plan 005](../../plans/005-end-of-round-disasters-and-bankruptcy.md), tested in `BankruptcyTest`, so FR-03 is now `built`.

**Confirmed by Oscar (17 November 2026):** a move that ends exactly on the far junction after the cut-through counts as landing on a junction, so the next move starts with the route question (tested in `q05_endingOnTheFarJunctionAsksNextTurn`).

## Related

- [Domain model: Board](../architecture/domain-model.md)
- [Game flow: Take Turn](../architecture/game-flow.md)
