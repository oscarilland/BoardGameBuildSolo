---
title: Turn and actions
type: rules
status: draft
sources: [../sources/handover.md, ../sources/rules-v4.md, ../sources/sub1a-report.md, ../sources/data-files.md]
updated: 2026-11-17
---

# Turn and actions

## Summary

On their turn a player rolls two dice, moves clockwise, chooses a route at junctions, acts on the square they land on, may convert one polluting square, then ends the turn. After the last player, the round ends.

## Rules

### The turn

1. If the player is bankrupt, the [bankruptcy](bankruptcy.md) rules run first, and may end the turn.
2. The player chooses to roll **two dice** (`game.dice: "2d6"`) and moves that many squares clockwise. At a junction they choose to stay on the loop or take the City Centre cut-through.
3. They may take **one** action on the square they land on:

| Square | Action | Cost | Data key |
|---|---|---|---|
| Unowned Polluting or Green Project | Buy | Its price (less the Tech Entrepreneur discount for green) | `price` in `board.json` |
| Another player's Green Project | Co-fund | £3M to the bank | `squares.coFunding.cost` |
| Another player's Polluting square | Lobby | £2M (Activist: free) | `squares.lobbying.cost` |
| Own Polluting square | Convert it | £5M | `squares.convert.cost` |
| Resilience | Buy own next level | £3M / £5M / £7M | `squares.resilience.levelCosts` |
| Glider Stop | Ride to any other Glider Stop | £1M (Student: free) | `squares.glider.fare` |
| Climate Card | Draw a card (not optional) | Free | - |

4. If they didn't convert in step 3, they may convert **one** Polluting square they own **anywhere** on the board for £5M. Maximum one conversion per turn in total (`squares.convert.maxPerTurn`).
5. The player ends the turn. After the last player, [end of round](end-of-round.md) runs.

### Actions in detail

- **Buy:** only an unowned square, and only if the player can afford it. A player can never choose a payment they can't afford.
- **Co-fund:** pay £3M to the bank. From the next end of round, the funder and the owner each get +1 GP per round (`gpPerRoundEach`). **One active deal per player** (`maxActiveDealsPerPlayer`): starting a new deal ends the old one. A deal pays nothing in a round when its square is closed by a Storm, and continues if the owner is bankrupt. Polluting squares can't be co-funded (`allowedOn: "greenProject"`).
- **Lobby:** the square earns **no money** at the end of **this** round. When the Community Activist lobbies, the square also adds nothing to the Climate Meter that round.
- **Convert:** the owner turns a Polluting square into a Green Project. It keeps its owner. If the Industrialist converted it, it earns +£2M a round extra from then on.
- **Glider:** the player moves to another Glider Stop. The Student Commuter rides free and gains +1 GP.

## Edge cases

- Landing on your own Green Project, or your own Polluting square when you have already converted this turn: no landing action.
- Landing on another player's square when it's closed or flooded: co-funding and lobbying are still allowed by the rules as written.
- After a rival converts a Polluting square, it's a Green Project, so other players can then co-fund it instead of lobbying it.
- The Climate Card draw is the only compulsory landing action, and it can make the player bankrupt ([bankruptcy](bankruptcy.md)).

## Conflicts or uncertainties

- Junction choice when **passing** a junction rather than landing on it, and the cut-through's direction ([Q-05](open-questions.md)).
- Whether the player acts on the destination stop after a Glider ride ([Q-07](open-questions.md)).
- Rules v4 lists "convert" as both a landing action and a separate step; HANDOVER clarifies the one-per-turn limit covers both.
- Lobbying the same square twice in a round has no extra effect; whether the UI should offer it isn't stated.

## Related

- [FR-03 Dice, movement and junctions](../requirements/fr-03-dice-movement-and-junctions.md)
- [FR-04 Buy square](../requirements/fr-04-buy-square.md)
- [FR-05 Co-funding](../requirements/fr-05-co-funding.md)
- [FR-06 Lobbying](../requirements/fr-06-lobbying.md)
- [FR-07 Convert square](../requirements/fr-07-convert-square.md)
- [FR-09 Glider](../requirements/fr-09-glider.md)
- [Game flow](../architecture/game-flow.md)
