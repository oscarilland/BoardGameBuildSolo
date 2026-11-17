---
title: Game flow
type: architecture
status: draft
sources: [../sources/design-diagrams.md, ../sources/sub1a-report.md]
updated: 2026-11-17
---

# Game flow

## Summary

How the use cases and the three sequence diagrams map onto the model. It also covers one design point the diagrams leave open: the model must **ask for** player choices without calling the UI.

## Use cases

From the [use case diagram](../../raw/net-zero-belfast-handover/design/use_case_diagram.png). The only actor is Player (everyone shares one computer).

- **Start Game.**
- **Take Turn** «include» **Move Player**. Extended at **Landing action** by Buy Square, Co-fund Green Project, Lobby Polluting Square, Buy Resilience Level, Draw Climate Card; at **Conversion** by Convert Square; at **Round complete** by End Round.
- **End Round** «include» **Resolve Disaster**; extended at **Game over** by View End-of-Game Report.
- The Glider ([FR-09](../requirements/fr-09-glider.md)) is omitted from the diagram for readability.

## Sequences

### Take Turn ([diagram](../../raw/net-zero-belfast-handover/design/seq_take_turn.png))

1. `Game.takeTurn()`. If the player is bankrupt: `Dice.rollOne()`; on a 6 or after 2 rounds, `p.receive(10)` as a loan and continue; otherwise the turn ends.
2. `Dice.rollTwo()` → steps. If a junction is on the way, ask the player for the route.
3. `Board.move(p.position, steps, route)` → new position; `Board.getSquare(newPos)`.
4. `square.landOn(p, g)` → the available actions. Show the square and actions.
5. Optionally, the player chooses an action (an extending use case runs). Conversion is offered if not used.
6. `Game.endTurn()`: after the last player, `endRound()`; otherwise the next player.

### Draw Climate Card ([diagram](../../raw/net-zero-belfast-handover/design/seq_draw_climate_card.png))

1. `ClimateCardSquare.landOn()` → `CardDeck.draw()`, reshuffling if the pile is empty.
2. `card.resolve(p, g)` shows the card text. Then, by type: Quiz → answer → `checkAnswer(i)` → `addGP(2)` if correct. Choice → pick A or B → `applyOption(choice)` → player money and GP, and `ClimateMeter.adjust(delta)`. Event → `receive` or `pay`.
3. `pay()` makes the player bankrupt if money would go below 0.
4. Show the fact and source.

### End Round ([diagram](../../raw/net-zero-belfast-handover/design/seq_end_round.png))

`payOwner()` for each owned square (skipping closed, flooded and lobbied) → `pay()` for each co-funding deal → `payCarbonTax()` for each player → `ClimateMeter.update(polluting, green)` → in 2030 and 2040, `meetsTarget(year)` and the penalty → `dangerBand()` → `DisasterManager.check(band)` → `apply(d, players, band)` → `endGame()` if the year reaches 2050, otherwise year += 2 and round += 1. See [FR-11](../requirements/fr-11-end-of-round-order.md).

## Key design choice: choices without calling the UI

In the diagrams, model objects talk to the human directly ("ask for route", "show card text", `ClimateCard` → Player). Coded literally, `ClimateCard` would open a dialog, which breaks the model-UI split and makes bots and tests impossible.

**Built for movement in [plan 002](../../plans/002-players-characters-and-movement.md).** `Game` has a `Phase`: `AWAITING_ROLL` → (`AWAITING_ROUTE` or `AWAITING_DIRECTION` while moving, if there's a choice) → `MOVE_DONE` → `endTurn()` → the next player's `AWAITING_ROLL`. An action that doesn't fit the phase throws `IllegalActionException` and changes nothing.

**Actions after moving, built in [plan 003](../../plans/003-square-actions.md):** in `MOVE_DONE`, `availableActions()` lists the landing actions from `square.landOn(player, game)` (one per turn) and a `CONVERT` for each of the player's Polluting squares (one conversion per turn), keeping only what they can afford. `take(action)` accepts only an action from that list. The UI's buttons and the bots both come from that list, so this is the extension point for "Landing action" and "Conversion" in the use case diagram. Climate Cards (plan 004) and the end of the round (plan 005) will add more.

The approach: the model works as a small state machine. Each step either completes or returns a **pending decision** (choose route, choose landing action, answer quiz, choose option A or B, convert or not, end turn) with its legal options. The UI, or a bot, calls back with the choice, and the model validates it and continues. The sequence diagrams stay true as a description of the order of calls; only the "ask the user" arrows become a returned decision.

- **For:** one code path for players, bots and tests; the model never blocks (NFR-11); every action is validated (NFR-08).
- **Against:** a little more structure than direct method calls. The SUB2 sequence diagrams should show it.

## Explain it in 30 seconds

*Draft for Oscar to reword.* A turn is roll, move, land, act, maybe convert, end. When the model needs a decision, like which route or which card option, it stops and says "here are your options". Whoever is playing, a person through the UI or a bot in a test, picks one, and the model checks it's legal and carries on.

## Questions a marker might ask

- "Your sequence diagram has the card talking to the player. Where's that in the code?" The card returns a pending choice; the UI displays it and passes the answer back, so the model never depends on the UI.

## Related

- [Domain model](domain-model.md)
- [Turn and actions](../rules/turn-and-actions.md)
- [End of round](../rules/end-of-round.md)
