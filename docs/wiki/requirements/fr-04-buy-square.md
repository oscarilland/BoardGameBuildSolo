---
title: FR-04 Buy square
type: requirement
id: FR-04
priority: must
status: built
sources: [../sources/sub1a-report.md]
updated: 2026-11-17
---

# FR-04 Buy square

## Summary

> When a player lands on an unowned Polluting or Green Project square, the system shall offer it at its price, and shall not let a player buy a square they cannot afford. *(SUB1a; verified by JUnit)*

## Behaviour

- On an unowned Polluting or Green Project square, the player may buy it at its price from `board.json`. The Tech Entrepreneur pays £3M less for Green Projects.
- Buying is optional. A bought square belongs to the player for the rest of the game.

See [turn and actions](../rules/turn-and-actions.md).

## Acceptance criteria

- **AC-1** A player with £9M on an unowned £9M square can buy it and is left with £0.
- **AC-2** A player with £8M on an unowned £9M square isn't offered "Buy", and the model rejects a buy request.
- **AC-3** The Tech Entrepreneur buying a £10M Green Project pays £7M. The discount doesn't apply to Polluting squares.
- **AC-4** An owned square, Resilience square, Glider Stop or Climate Card square can't be bought.
- **AC-5** After buying, the square's owner is the player, and it counts in their squares in the player panel ([FR-15](fr-15-always-visible-status.md)).

## Out of scope

Selling or trading squares; auctions.

## Decisions (Oscar, 17 November 2026)

1. **Agreed.** A Polluting square bought this turn may also be converted this turn ([FR-07](fr-07-convert-square.md) AC-7).

## Build status

**Built** in [plan 003](../../plans/003-square-actions.md): AC-1 to AC-5, tested in `BuyTest`.

## Related

- [FR-07 Convert square](fr-07-convert-square.md)
- [Board and squares](../rules/board-and-squares.md)
