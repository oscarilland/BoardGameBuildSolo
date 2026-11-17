---
title: Domain model
type: architecture
status: draft
sources: [../sources/design-diagrams.md, ../sources/sub1a-report.md, ../sources/handover.md]
updated: 2026-11-17
---

# Domain model

## Summary

The team's analysis class diagram ([PNG](../../raw/net-zero-belfast-handover/design/class_diagram.png), [Graphviz source](../../raw/net-zero-belfast-handover/design/class_diagram.dot)), compiled into a checklist for the code. The SUB1a report commits to these names and methods, so the implementation should keep them recognisable. Changes made while building are recorded under [Changes from the analysis model](#changes-from-the-analysis-model).

## Classes

| Class | Attributes | Methods | Relationships |
|---|---|---|---|
| `Game` (controller) | round, year, currentPlayer | `startGame(players)`, `takeTurn()`, `endTurn()`, `endRound()`, `endGame(): Player` | Composes 1 `Board`, `ClimateMeter`, `CardDeck`, `Dice`, `DisasterManager`; aggregates 4..6 `Player`; reads `GameConfig` |
| `GameConfig` | values: Map, cardFile: String | `load(file)`, `get(key): int` | |
| `Dice` | | `rollTwo(): int`, `rollOne(): int` | Takes an injected seeded `Random` |
| `Board` | squares: Square[36] | `move(from, steps, route): int`, `getSquare(i): Square` | Composes 36 `Square`, 6 `District` |
| `District` | name | | Each `Square` is in 1 district; each district has 6 squares |
| `Square` {abstract} | index, name | `landOn(p, g): Action[]` (polymorphic) | |
| `OwnableSquare` {abstract} | price, closed, lobbied | `buy(p)`, `payOwner()` | owner 0..1 `Player` |
| `PollutingSquare` | income = 7, meterEffect = +2 | `convert(): GreenProjectSquare` | |
| `GreenProjectSquare` | gp = 2, income = 2, isNature | | Funded by 0..* `CoFundingDeal` |
| `ResilienceSquare` | | `buyLevel(p)` | |
| `GliderStop` | | `ride(p, to)` | |
| `ClimateCardSquare` | | | |
| `Player` | name, money, greenPoints, position, resilienceLevel, bankruptRounds, loans | `pay(amount)`, `receive(amount)`, `addGP(n)`, `payCarbonTax()`, `countPolluting(): int`, `legacyScore(netZero): int` | Plays as 1 `Character`; funder of 0..1 `CoFundingDeal` |
| `Character` {abstract} + 6 subclasses | name, startMoney, startGP | `priceFor(square): int`, `incomeBonus(square): int` | Industrialist, TechEntrepreneur, CouncilPlanner, Farmer, StudentCommuter, CommunityActivist |
| `CoFundingDeal` | | `pay()` | 1 funder `Player` ↔ 1 `GreenProjectSquare` |
| `ClimateMeter` | value = 50, history: int[] | `update(polluting, green)`, `adjust(delta)`, `dangerBand(): int`, `meetsTarget(year): boolean` | |
| `DisasterManager` | | `check(band): Disaster`, `apply(d, players, band)` | Uses enum `Disaster {FLOOD, STORM, HEATWAVE, DROUGHT}` |
| `CardDeck` | drawPile: ClimateCard[] | `draw(): ClimateCard`, `reshuffle()` | Composes 30 `ClimateCard` |
| `ClimateCard` {abstract} | id, text, fact, source | `resolve(p, g)` | |
| `QuizCard` | answers: String[4], correct | `checkAnswer(i): boolean` | |
| `ChoiceCard` | optionA, optionB: Effect | `applyOption(choice)` | |
| `EventCard` | effect: Effect | | |

## Key design choices

- **Polymorphism over type checks.** `Square.landOn()` returns the actions available, so `Game` treats every square the same way; `ClimateCard.resolve()` does the same for cards; `Character` subclasses override hooks so an ability needs no change elsewhere.
- **Values in the diagram are defaults, not constants.** `income = 7`, `value = 50` and so on must come from `GameConfig` ([NFR-07](../requirements/nfr-07-data-driven-balance.md)).

## Gaps to settle in plan 001

These are places where the diagram can't be coded literally. Each is a design choice for the plan, not a rules change.

1. **`Action` isn't defined.** `landOn()` returns `Action[]`. Suggest an enum or small record (BUY, CO_FUND, LOBBY, CONVERT, BUY_RESILIENCE, RIDE_GLIDER, DRAW_CARD) with its cost.
2. **`Effect` isn't defined.** Cards need a record of money, GP and meter deltas, matching the data files.
3. **`Character` needs more hooks** than `priceFor()` and `incomeBonus()`: Glider fare and ride GP, lobby cost and meter effect, nature GP bonus, Resilience discount. The handover allows "similar hooks as needed".
4. **Converted-by tracking.** The Industrialist bonus needs the square to remember who converted it.
5. **`PollutingSquare.convert()` returns a new object.** The `Board` must replace the square in place and keep owner, co-funding and position.
6. **`ClimateCard.source` is one String** in the diagram; the data has a list of sources with titles and URLs.
7. **`GameConfig.get(key): int`** can't return lists, strings or nested sections. Suggest typed records that Gson fills ([ADR-004](../../decisions/004-reading-the-json-data-files.md), accepted).
8. **Floods, droughts and closures last one round.** Something must track "this round's" flags and clear them; the diagram only has `closed` and `lobbied` on squares.
9. **Per-round history** of polluting ownership for the end-of-game report ([FR-16](../requirements/fr-16-end-of-game-report.md)).
10. **`legacyScore(): int`** depends on [Q-16](../rules/open-questions.md).

## Changes from the analysis model

Record each change here when it's built, with the reason. The SUB2 class diagram is expected to differ from SUB1a's.

| Class | Analysis model | As built | Why | Plan |
|---|---|---|---|---|
| `Dice` | `rollTwo()`, `rollOne()` | Same, plus a constructor taking a `java.util.Random`, and `random()` to share that source | One seeded source for every random choice, so tests and whole games can be replayed | [001](../../plans/001-walking-skeleton.md) |
| `GameConfig` | `values: Map`, `cardFile`, `load(file)`, `get(key): int` | `load()` (from the jar) and `load(folder)`, returning typed records: `rules()`, `board()`, `cards()`, plus `quizReward(card)` and `summary()` | `get(key): int` can't return lists, text or nested sections (gap 7), and typed records let the compiler catch a misspelt key | [001](../../plans/001-walking-skeleton.md) |
| (new) `Rules`, `BoardDef`, `CardDef` | Not in the diagram | Records holding the three data files' contents | They're the "data" side of `GameConfig`; the model classes in later plans are built from them | [001](../../plans/001-walking-skeleton.md) |
| (new) `ConfigException` | Not in the diagram | The one error type for bad data, naming the file and key | NFR-07 AC-3 | [001](../../plans/001-walking-skeleton.md) |
| `Character` {abstract} + 6 subclasses | `priceFor(square)`, `incomeBonus(square)` | Built from `CharacterDef` by `Character.from(def)`, keyed on the data id. No ability hooks yet | Hooks are added in plan 003, where buying and income first exist | [002](../../plans/002-players-characters-and-movement.md) |
| `Player` | name, money, greenPoints, position, resilienceLevel, bankruptRounds, loans; `pay`, `receive`… | name, character, money, greenPoints, position, and `heading` on the City Centre route. Changed only by `Game` | Other attributes arrive with the plans that use them; `heading` is new, because the cut-through is two-way (Q-05) | [002](../../plans/002-players-characters-and-movement.md) |
| `Board` | `move(from, steps, route): int`, `getSquare(i)` | `next(position, heading, route): Step` moves one square; `square(id)`, `isJunction`, `onCityRoute`; enums `Route` and `Heading` | One route for a whole move only works if a move never meets two choices; moving step by step works for any board in the data | [002](../../plans/002-players-characters-and-movement.md) |
| `Game` | `startGame(players)`, `takeTurn()`, `endTurn()`… | `Game.start(config, dice, setups)`; a `Phase` (awaiting roll, route, direction, or move done) and one method per answer: `roll()`, `chooseRoute()`, `chooseDirection()`, `endTurn()` | The model mustn't call the UI ([game flow](game-flow.md)); `takeTurn()` became `roll()` plus the choices | [002](../../plans/002-players-characters-and-movement.md) |
| (new) `IllegalActionException` | Not in the diagram | Thrown when an action doesn't fit the current phase; the state is unchanged | NFR-08 AC-4 | [002](../../plans/002-players-characters-and-movement.md) |
| `Square` hierarchy | `Square`, `OwnableSquare`, `PollutingSquare`, `GreenProjectSquare`, `ResilienceSquare`, `GliderStop`, `ClimateCardSquare`; `landOn(p, g): Action[]` | As in the diagram; `landOn` returns `List<Action>` before affordability, and `Game` filters it | One place applies "only what you can afford" | [003](../../plans/003-square-actions.md) |
| `Action` | Used by `landOn` but not defined (gap 1) | A record: kind (`BUY`, `CO_FUND`, `LOBBY`, `CONVERT`, `BUY_RESILIENCE`, `RIDE_GLIDER`), cost after discounts, square, Glider destination | The UI and bots choose from `Game.availableActions()`; `Game.take()` accepts only an action from that list | [003](../../plans/003-square-actions.md) |
| `PollutingSquare.convert()` | Returns a `GreenProjectSquare` | Same, and `Board.replace()` swaps it in at the same id. The new square remembers `convertedBy` (gap 4) | Gap 5: the board must hold the new object; deals refer to square ids, so nothing goes stale | [003](../../plans/003-square-actions.md) |
| `Character` hooks | `priceFor(square)`, `incomeBonus(square)` | `buyPrice`, `resilienceCost`, `resilienceCut`, `gliderFare`, `gliderRideGP`, `lobbyCost`, `lobbyCancelsMeter`; the character also holds the `Rules` for defaults | Gap 3: each ability needed its own hook. `priceFor` became `buyPrice`; the income hooks arrive in plan 005 | [003](../../plans/003-square-actions.md) |
| `CoFundingDeal` | funder ↔ `GreenProjectSquare`; `pay()` | A record of funder and square id; `pay()` arrives with the end of the round | Holding the id keeps the deal valid if the square object is swapped | [003](../../plans/003-square-actions.md) |
| `Player` | `pay`, `receive`, `addGP`, `resilienceLevel` | As in the diagram, plus `coFundingDeal()` and `disasterCost(amount)` (Resilience cut, rounded half up with exact decimals) | `disasterCost` keeps the rounding rule (Q-08) in one tested place | [003](../../plans/003-square-actions.md) |

## Explain it in 30 seconds

*Draft for Oscar to reword.* `Game` is the controller. It owns the board, the meter, the deck, the dice and the disaster manager, and it has 4-6 players. Squares and cards are abstract classes with subclasses that override one method, so the game can ask any square "what can I do here?" without knowing its type. Each player plays as a character, and the character's subclass changes prices and bonuses.

## Questions a marker might ask

- "Why is `Square` abstract?" Every square has a position and name, but what happens when you land on it differs; `landOn()` is overridden in each subclass.
- "Composition or aggregation between `Game` and `Player`?" Aggregation in the diagram: players are created at setup and passed in.

## Related

- [Game flow](game-flow.md)
- [Data files](data-files.md)
