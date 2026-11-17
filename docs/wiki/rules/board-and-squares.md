---
title: Board and squares
type: rules
status: draft
sources: [../sources/handover.md, ../sources/data-files.md, ../sources/design-diagrams.md]
updated: 2026-11-17
---

# Board and squares

## Summary

36 squares in six Belfast districts: a clockwise loop of 30 squares, plus a 6-square City Centre cut-through between two junctions. Five square types, each with a different action.

## Rules

1. **Shape** (`board.json`): the loop is ids 0-29, clockwise by ascending id, with 29 wrapping to 0. The **City Centre** route is ids 30-35. Square 30 is next to the West junction (27) and 35 next to the East junction (12).
2. **Junctions:** Glider East (12) and Glider West (27). At a junction the player chooses to stay on the loop or take the cut-through ([turn and actions](turn-and-actions.md)).
3. **Districts** (`districts`): North (0-5), Harbour & Titanic Quarter (6-11), East (12-17), South (18-23), West (24-29), City Centre (30-35). Six squares each.
4. **Glider stops:** 12, 27 and 30 (`gliderStops`).
5. **Square types:**

| Type | Count | Price | Each round, if owned | Data keys |
|---|---|---|---|---|
| Polluting | 10 | £4-7M | +£7M; carbon tax −1 GP; meter +2 | `squares.polluting.*` |
| Green Project | 12, of which 4 are nature | £8-12M | +£2M; +2 GP; meter −1 | `squares.greenProject.*` |
| Resilience | 4 | £3M / £5M / £7M per level | A personal upgrade ([Resilience](#resilience)) | `squares.resilience.*` |
| Glider Stop | 3 | £1M fare | Move to any other Glider Stop | `squares.glider.fare` |
| Climate Card | 7 | Free | Draw a card ([climate cards](climate-cards.md)) | `squares.climateCard.*` |

6. **Owned squares can't be bought.** On another player's Green Project you may [co-fund](turn-and-actions.md); on their Polluting square you may [lobby](turn-and-actions.md).
7. Nature projects (`isNature: true`): Cave Hill Woodland (0), Connswater Greenway (14), Lagan Valley Woodland (23), Black Mountain Peatland (25).

### Resilience

1. When landing on any Resilience square, a player may buy **their own next level**, one level per landing. Maximum level 3 (`maxLevel`).
2. Level 1, 2 and 3 cost £3M, £5M and £7M (`levelCosts`), and cut **the money the player pays for disasters** by 25%, 50% and 75% (`disasterCostCut`).
3. Resilience does **not** prevent lost income, closed squares, Droughts, or Event card costs.
4. Reduced costs are rounded to whole £M; the rounding rule is open ([Q-08](open-questions.md)).

## Edge cases

- Resilience squares, Glider stops and Climate Card squares can't be owned.
- A Resilience square is never "used up": every player can buy from any of the four.
- The Glider is optional; a player with less than £1M can't ride.

## Conflicts or uncertainties

- **Numbering:** the [board drawing](../../raw/net-zero-belfast-handover/design/board_layout.png) numbers squares 1-30 and C1-C6. `board.json` uses 0-35. Same order; the data ids are authoritative.
- Square names, prices within the ranges, and district assignments are placeholders ([Q-09](open-questions.md)).
- The cut-through's direction is open: the drawing says either direction, `board.json` says TBD ([Q-05](open-questions.md)).

## Related

- [FR-03 Dice, movement and junctions](../requirements/fr-03-dice-movement-and-junctions.md)
- [FR-04 Buy square](../requirements/fr-04-buy-square.md)
- [FR-08 Resilience](../requirements/fr-08-resilience.md)
- [FR-09 Glider](../requirements/fr-09-glider.md)
- [Data files](../architecture/data-files.md)
