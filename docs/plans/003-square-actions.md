---
title: Plan 003 Square actions
type: plan
id: PLAN-003
status: done
requirements: [FR-04, FR-05, FR-06, FR-07, FR-08, FR-09]
sources: [../wiki/rules/turn-and-actions.md, ../wiki/rules/board-and-squares.md, ../wiki/rules/setup-and-characters.md, ../wiki/architecture/domain-model.md, ../wiki/architecture/game-flow.md]
updated: 2026-11-17
---

# Plan 003 - Square actions

**Approved by Oscar, 17 November 2026:** "approve plan 004 /build docs/plans/003-square-actions.md". Plan 004 didn't exist yet, and the same message started this plan's build, so this is read as approval of plan 003.

## Goal

After moving, a player can act on the square they landed on: buy it, co-fund it, lobby it, buy a Resilience level, or ride the Glider. They can also convert one of their Polluting squares, and the character abilities that change these actions work. The game offers only the actions that are legal, affordable and would change something. A UI or a bot just picks from that list.

Money and GP change when actions are paid for. What squares **earn** each round (income, GP, lobbying's effect, the Industrialist's bonus) is plan 005's end of round. This plan records everything that plan needs.

## Scope

- **Requirements covered (all agreed):**

| Requirement | Built here | Waits for |
|---|---|---|
| [FR-04](../wiki/requirements/fr-04-buy-square.md) Buy | AC-1 to AC-5 | - |
| [FR-05](../wiki/requirements/fr-05-co-funding.md) Co-fund | AC-1, AC-2, AC-4 (the new deal replaces the old), AC-7 | AC-3, AC-5, AC-6 (deal pays out): plan 005 |
| [FR-06](../wiki/requirements/fr-06-lobbying.md) Lobby | AC-1 (cost, and the square marked lobbied for this round), AC-2 (the mark clears when the round ends), AC-5 (free for the Activist, and marked as cancelling the meter rise), AC-6, AC-7 | AC-1's lost income, AC-3, AC-4: plan 005 |
| [FR-07](../wiki/requirements/fr-07-convert-square.md) Convert | AC-1, AC-2, AC-3, AC-7, AC-8, and the record of who converted (for AC-5) | AC-4, AC-5's income: plan 005. AC-6 (bankrupt): plan 005 |
| [FR-08](../wiki/requirements/fr-08-resilience.md) Resilience | AC-1 to AC-4. AC-5 and AC-7 through a tested "what do I pay for this disaster?" calculation | AC-6 (what Resilience doesn't cover): plans 004-005 |
| [FR-09](../wiki/requirements/fr-09-glider.md) Glider | AC-1 to AC-6 | - |

- **Abilities built:**
  - **Tech Entrepreneur:** Green Projects cost £3M less.
  - **Council Planner:** Resilience levels cost £2M less.
  - **Student Commuter:** free Glider rides, +1 GP per ride.
  - **Community Activist:** free lobbying, which also cancels the meter rise.
  - **Industrialist:** squares they convert are marked for the +£2M bonus.
  - **Farmer:** no change here; the nature GP bonus is in plan 005.
- **Decisions relied on:**
  - [Q-07](../wiki/rules/open-questions.md): no action after a Glider ride.
  - [Q-08](../wiki/rules/open-questions.md): Resilience costs round half up.
  - [Q-18](../wiki/rules/open-questions.md): direction chosen after riding to City Hall.
  - Oscar's FR-04 to FR-09 answers: actions that change nothing aren't offered; a player can buy and convert in the same turn; converted squares aren't nature projects.
- **Out of scope:**
  - Climate Card squares: landing on one offers nothing until plan 004.
  - End-of-round income, GP and meter (plan 005).
  - Bankruptcy and disasters (plan 005).
  - Screens (plan 007).

## Design

### Squares become objects

`Board` currently returns the data records (`SquareDef`). This plan gives it the analysis model's square classes, built from the data:

| Class | Holds | `landOn(player, game)` offers |
|---|---|---|
| `Square` (abstract) | id, name, district | - |
| `OwnableSquare` (abstract) | price, owner | - |
| `PollutingSquare` | `lobbiedThisRound`, and whether the Activist lobbied it | unowned: **Buy**; another player's, not yet lobbied: **Lobby** |
| `GreenProjectSquare` | nature flag, who converted it (if anyone) | unowned: **Buy**; another player's, not already funded by this player: **Co-fund** |
| `ResilienceSquare` | - | **Buy Resilience level** (the player's next level) |
| `GliderStop` | - | **Ride the Glider** to each other stop (one action per destination) |
| `ClimateCardSquare` | - | nothing yet (plan 004) |

`PollutingSquare.convert(player)` returns a `GreenProjectSquare` with the same id, name, district, price and owner, not nature, remembering who converted it. `Board` swaps it in place.

### Actions are a list to choose from

- `Action` record: what it is (`BUY`, `CO_FUND`, `LOBBY`, `BUY_RESILIENCE`, `RIDE_GLIDER`, `CONVERT`), its cost after the character's discounts, the square it acts on, and for the Glider the destination.
- `Game.availableActions()` (in `MOVE_DONE`) returns:
  - the landing actions from `square.landOn(...)`, if the player hasn't used one this turn;
  - plus `CONVERT` for each Polluting square they own, if they haven't converted this turn.

  Only affordable actions are listed (FR-04 AC-2).
- `Game.take(action)` checks the action is in that list (otherwise `IllegalActionException`, no change) and applies it.
- **Converting doesn't use up the landing action**, because the rules cap conversions at one per turn either way. Landing on your own Polluting square and converting it is the same thing as converting it "anywhere".
- After a Glider ride, the landing action is used up (Q-07). If the destination is City Hall (30), the player's heading is cleared, so their next move asks for a direction (Q-18).

This is the analysis model's `landOn(p, g): Action[]` made concrete. The UI's buttons (plan 007) and the bots (plan 006) both come from `availableActions()`, so they can never offer something the rules don't allow (NFR-02 AC-2, NFR-08 AC-4).

### Players and characters

- **`Player` gains:**
  - `pay(amount)`, for voluntary payments only: it refuses if the player can't afford it;
  - `addGP(n)`;
  - `resilienceLevel`;
  - their `CoFundingDeal`, if any;
  - `disasterCost(amount)`, which applies their Resilience cut and rounds half up, using exact decimal arithmetic.
- **`CoFundingDeal`:** funder and square. Starting a new one replaces the old.
- **`Character` gains hooks.** By default they read `config.json`; the subclasses override them using their own `params` from the data, with no numbers in code:
  - `buyPrice(square)`
  - `resilienceCost(level)`
  - `gliderFare()`
  - `gliderRideGP()`
  - `lobbyCost()`
  - `lobbyCancelsMeter()`

### End of the round (for now)

When the round advances in `endTurn()`, the lobbied marks are cleared. Plan 005 moves this into the full end-of-round sequence.

## Tasks

1. Square classes and `Board` holding them; `Board.replace(square)` for conversion.
2. `Action` and its kinds; `landOn` on each square class.
3. Character hooks and the four subclasses that override them.
4. `Player`: `pay`, `addGP`, Resilience level, deal, `disasterCost`.
5. `CoFundingDeal`.
6. `Game.availableActions()`, `Game.take(action)`, and the per-turn "landing action used" and "converted" flags, reset at each roll; clearing lobbied marks at the round boundary.
7. The headless run from plan 002 now also picks random actions, to shake out illegal states.
8. Docs (see below).

## Tests

| Test class | Checks | Criteria |
|---|---|---|
| `BuyTest` | £9M on a £9M square: bought, £0 left. £8M: no Buy offered, and `take` is refused. Tech Entrepreneur pays £7M for a £10M Green Project but full price for Polluting. Owned, Resilience, Glider and card squares offer no Buy. The new owner is recorded | FR-04 AC-1 to AC-5 |
| `CoFundTest` | Offered on another player's Green Project with £3M or more; costs £3M. Not offered on your own square, on Polluting squares, with £2M, or on the square you already fund. A second deal replaces the first | FR-05 AC-1, AC-2, AC-4, AC-7 |
| `LobbyTest` | Costs £2M and marks the square lobbied for this round. The Activist pays £0, can lobby with £0, and the mark says the meter rise is cancelled. Not offered on your own square, Green Projects, with £1M (non-Activist), or on an already-lobbied square. The mark clears when the round ends | FR-06 AC-1, AC-2, AC-5, AC-6, AC-7 |
| `ConvertTest` | Converting costs £5M and makes a Green Project with the same owner, price and position, not nature. A second conversion in the turn is refused, either order. £4M: not offered. Buy a Polluting square then convert it in the same turn. Converted by the Industrialist: marked for the bonus; bought as Green by the Industrialist: not marked | FR-07 AC-1, AC-2, AC-3, AC-5 (marking), AC-7, AC-8 |
| `ResilienceTest` | Levels 1, 2, 3 offered for £3M, £5M, £7M; nothing at level 3; one level per landing. Council Planner pays £1M, £3M, £5M. `disasterCost`: level 2 on £4M is £2M; level 1 on £2M is £2M (£1.5M rounds up); level 3 on £2M is £1M; level 2 on £3M is £2M | FR-08 AC-1 to AC-5, AC-7 |
| `GliderTest` | On 12 with £1M: rides to 27 or 30 offered, each costs £1M. £0: none offered. Student Commuter: £0 fare and +1 GP. Optional: ending the turn without riding is fine. After the ride, no landing action is offered, but converting still is. After riding to 30, the next roll asks for a direction | FR-09 AC-1 to AC-6 |
| `ActionRulesTest` | One landing action per turn. `take` with an action not on the list is refused with no change. No actions before the move is done. The flags reset next turn. A Climate Card square offers nothing (for now) | NFR-08 AC-4, the turn rules |
| `CharacterHooksTest` | Each hook reads its value from the data: a test copy of `config.json` with a different discount changes the price | NFR-07 AC-2 |
| `HeadlessTurnsTest` (extended) | Random actions as well as random routes, 30 games × 10 rounds: no exceptions, money never negative, every owned square has exactly one owner | NFR-06 groundwork |

Coverage must stay at 80% or more for `model` and `config`.

## Docs to update

- **[Domain model](../wiki/architecture/domain-model.md):** the square classes, `Action`, `CoFundingDeal`, the `Player` and `Character` additions; "Changes from the analysis model" records `Action` as a record with kinds (gap 1), `convert()` swapping the square in place (gap 5), the converted-by mark (gap 4), and conversion not using the landing action.
- **[Game flow](../wiki/architecture/game-flow.md):** `availableActions()` and `take()`.
- **[Turn and actions](../wiki/rules/turn-and-actions.md), [board and squares](../wiki/rules/board-and-squares.md), [setup and characters](../wiki/rules/setup-and-characters.md):** build status.
- **FR-04 and FR-09:** `built`. FR-05 to FR-08 stay `agreed`, with a build status listing what waits for plan 005.
- **[Build progress](../wiki/operations/build-progress.md), `index.md`, `log.md`, `src/AGENTS.md`.**

## Verification

1. `.\mvnw.cmd verify` passes, with coverage at 80% or more.
2. `java -jar target\net-zero-belfast.jar --check` still passes.
3. As in plan 002, put deliberate bugs into two of the rules (the affordability check, and the one-conversion limit) and check the tests catch each one.

## What Oscar should be able to explain

- How `landOn()` uses polymorphism: why `Game` doesn't need to know which kind of square the player is on.
- Why actions are a list that the game offers, rather than buttons the UI decides on, and how that makes illegal actions impossible from the UI.
- How a character's ability changes a price without `Game` knowing which character it is.
- What happens to the square object when a Polluting square is converted, and why it's swapped rather than changed.
- Why `disasterCost` uses exact decimals, and what "round half up" does to £1.5M.
- Which effects of these actions you can't see yet, and why they wait for the end of the round in plan 005.

## Risks

| Risk | What we'll do |
|---|---|
| The plan is bigger than plan 002 | The six actions share one mechanism (`landOn`, `availableActions`, `take`), so most of the work is that mechanism, built once. If it runs long, finish with Buy, Convert and the Glider, and move the rest to a plan 003b |
| Plan 005 needs state this plan doesn't record | The table above lists what plan 005 will read: owner, lobbied (and by whom), converted-by, deals, Resilience level. Review it when drafting plan 005 |
| Swapping a converted square could leave stale references (for example a co-funding deal on the old object) | Deals and owners refer to square **ids**, not objects, and are looked up on the board when needed |

## Outcome

**Built 17 November 2026**, in one sitting, with no split into a plan 003b. `mvnw clean verify` passes with no warnings: 182 tests, 53 of them new. Line coverage is 99.5% for `model` (400 of 402 lines) and 99.1% for `config`. `--check` still passes.

### What was built

| Path (`src/main/java/netzerobelfast/model/`) | What it is |
|---|---|
| `Square`, `OwnableSquare`, `PollutingSquare`, `GreenProjectSquare`, `ResilienceSquare`, `GliderStop`, `ClimateCardSquare` | The analysis model's squares, made by `Board` from the data. Each `landOn()` returns its landing actions |
| `Action` | A record: kind, cost after discounts, square, Glider destination |
| `CoFundingDeal` | A record: funder and square id |
| `Board` | Now holds the square objects: `square(id)`, `squares()`, `gliderStops()`, `pollutingOwnedBy(player)`, `replace(square)`, `clearLobbying()` |
| `Player` | Adds `resilienceLevel`, `coFundingDeal`, `disasterCost(amount)`, and package-private `pay`, `receive`, `addGP` |
| `Character` and subclasses | Hooks `buyPrice`, `resilienceCost`, `resilienceCut`, `gliderFare`, `gliderRideGP`, `lobbyCost`, `lobbyCancelsMeter`; four subclasses override them with their data `params`. `Industrialist.convertedBonusIncome()` and `Farmer.natureBonusGP()` are ready for plan 005 |
| `Game` | `availableActions()`, `take(action)`, the per-turn flags, `rules()`; lobbying cleared when a round ends |

### Checking the tests catch mistakes

Each deliberate bug was put into `Game`, the suite run, and the code restored:

| Bug | Tests that failed |
|---|---|
| Unaffordable actions offered | 7: one per action, plus the headless run (money went negative) |
| No one-conversion limit | 1: `ConvertTest.fr07_ac2_onlyOneConversionPerTurn` |
| No one-landing-action limit | 3: the action rules, Glider and Resilience tests for it |

### What differed from the plan

1. **`Character` now holds the `Rules`**, so a hook's default can read the normal value. The factory became `Character.from(def, rules)` and `Character.fromAll(rules)`.
2. **Subclasses read their `params` in their constructors.** A missing ability parameter therefore stops the game at start-up (`App` already makes every character), not mid-game.
3. **The affordability filter lives in one place**, `Game.availableActions()`. `landOn()` returns every action the square allows, and `Game` removes the unaffordable ones, so no square class repeats that rule.
4. **`Game.arriveForTest(square)`** (package-private) puts the current player on a square, ready to act, without scripting dice. Only tests use it.
5. **Any Glider ride clears the player's heading**, not only a ride to City Hall. On the loop a heading means nothing, and at City Hall it makes the next move ask for a direction (Q-18).
6. `Player.pay()` refuses a payment the player can't afford. `take()` checks first, so in practice it never triggers; it's a second line of defence until compulsory payments (plan 005) add their own method.

### What plan 005 reads from this plan

Owner on each `OwnableSquare`; `PollutingSquare.lobbiedThisRound()` and `meterRiseCancelled()`; `GreenProjectSquare.nature()` and `convertedBy()`; each player's `coFundingDeal()` and `disasterCost()`; `Industrialist.convertedBonusIncome()`; `Farmer.natureBonusGP()`. Plan 005 should move `board.clearLobbying()` from `endTurn()` into its end-of-round sequence.
