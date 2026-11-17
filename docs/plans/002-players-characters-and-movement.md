---
title: Plan 002 Players, characters and movement
type: plan
id: PLAN-002
status: done
requirements: [FR-01, FR-02, FR-03]
sources: [../wiki/requirements/fr-01-start-game.md, ../wiki/requirements/fr-02-starting-values-and-first-player.md, ../wiki/requirements/fr-03-dice-movement-and-junctions.md, ../wiki/architecture/game-flow.md, ../wiki/architecture/domain-model.md]
updated: 2026-11-17
---

# Plan 002 - Players, characters and movement

**Approved by Oscar, 17 November 2026:** "i approve plan 002".

## Goal

A game can be started headlessly with 4-6 named players on different characters, each with the right starting money and GP, and a random first player. Players take turns rolling two dice and moving around the real board, choosing a route at the junctions and travelling the two-way City Centre cut-through. There are no landing actions yet: after moving, the player ends their turn.

## Scope

- **Requirements covered (all agreed):**
  - [FR-01](../wiki/requirements/fr-01-start-game.md): AC-1 to AC-4, in the model. The setup screen comes in plan 007.
  - [FR-02](../wiki/requirements/fr-02-starting-values-and-first-player.md): AC-1 to AC-6.
  - [FR-03](../wiki/requirements/fr-03-dice-movement-and-junctions.md): AC-1 to AC-5 and AC-7. AC-6 (a bankrupt player doesn't move) waits for bankruptcy in plan 005.
  - [NFR-08](../wiki/requirements/nfr-08-privacy-and-security.md) AC-3 and AC-4: names validated; illegal actions rejected by the model.
- **Decisions relied on:** [ADR-003](../decisions/003-build-tool-and-java-version.md) (Java 21), [ADR-004](../decisions/004-reading-the-json-data-files.md) (data through `GameConfig`), and the model-UI split in [game flow](../wiki/architecture/game-flow.md).
- **Rules answers used:** [Q-05](../wiki/rules/open-questions.md) (two-way cut-through, choice whenever a junction is reached), [Q-06](../wiki/rules/open-questions.md) (everyone starts on square 0 with no landing action), and Oscar's FR-01 and FR-03 answers (20-character names, unique names, no second question at the far junction).
- **Out of scope:**
  - Landing actions, money changes, abilities, and end-of-round effects (plans 003-005). The round and year still advance after the last player, so turn order can be tested across rounds.
  - Bankruptcy (plan 005).
  - Any screen (plan 007).
  - The Glider. Its open question [Q-18](../wiki/rules/open-questions.md) is needed before plan 003, not this plan.

## Prerequisites

- Plan 001 is built.
- One small data change: the 20-character name limit goes in the working `config.json` as `game.maxNameLength`, so it isn't a number in code (NFR-07).

## Design

This is the first real model code, so the design is spelled out here for Oscar to approve.

### Classes (names from the [domain model](../wiki/architecture/domain-model.md))

| Class | In this plan |
|---|---|
| `Character` (abstract) and six subclasses (`Industrialist`, `TechEntrepreneur`, `CouncilPlanner`, `Farmer`, `StudentCommuter`, `CommunityActivist`) | Built from `CharacterDef` by a factory keyed on the data `id`. They hold name, starting money and GP. The ability hooks (`priceFor`, `incomeBonus` and so on) arrive in plan 003, when abilities first matter |
| `Player` | name, character, money, greenPoints, position, and the direction of travel on the City Centre route. `money` and `greenPoints` start from the character |
| `Board` | Built from `BoardDef`: the loop, the City Centre route and the two junctions all come from the data, so there are no square numbers in the code. It answers "what's the next square?" for one step |
| `Game` | Starts the game, and runs turns as a small state machine |
| `Dice` | From plan 001; also picks the first player |

### Turn phases and decisions

Following [game flow](../wiki/architecture/game-flow.md), the model never waits for a person. It's always in one phase, and only the calls valid in that phase are accepted. Anything else throws `IllegalActionException`, so a UI bug can't break the rules (NFR-08 AC-4).

```text
            roll()                         (move pauses at a junction)
AWAITING_ROLL ──────▶ moving step by step ──────────────────────────▶ AWAITING_ROUTE
     ▲                       │                                             │
     │                       │ steps used up                 chooseRoute(LOOP | CITY_CENTRE)
     │                       ▼                                             │
     │                   MOVE_DONE ◀───────────────────────────────────────┘
     │                       │ endTurn()
     └───────────────────────┘ next player; after the last, round + 1 and year + 2
```

- `Game.start(config, dice, List<PlayerSetup>)` validates the players (FR-01), creates them (FR-02), puts everyone on square 0, and picks the first player with the dice's random source.
- `roll()` rolls two dice and moves one square at a time. If the player is on a junction with steps still to go, the move pauses in `AWAITING_ROUTE`.
- `chooseRoute(route)` resumes the move.
- After each move, `lastMove()` gives the squares passed through and the dice total, which the UI will animate and the tests check.

### Movement rules, as code will apply them

1. On the loop, the next square is the next id in `loop.squareIds`, wrapping at the end.
2. Before a step from a junction, the player chooses: stay on the loop, or enter the City Centre route.
   - From the East junction (12) the route runs 35 → 30, then rejoins the loop at West (27).
   - From the West junction (27) it runs 30 → 35, then rejoins at East (12).
   - All of this comes from `cityCentreRoute` and its `connects` in `board.json`.
3. The choice is asked **only when there are steps left**. A player who lands exactly on a junction is asked at the start of their next move (Q-05).
4. Coming out of the cut-through onto the far junction **in the same move**, there's no question: carry on clockwise (FR-03 AC-7).
5. A player who ends a turn part-way along the City Centre route keeps their direction of travel next turn.
6. A player on the route with no direction, which only a Glider ride can cause (Q-18), gets a "choose direction" decision. Plan 002 builds and tests that decision so plan 003 only has to use it.

### Analysis-model change

`Board.move(from, steps, route): int` takes a single route for the whole move. With this board a move can reach at most one junction choice, but that's a fact about the current data, not a rule. The step-by-step design works for any board in `board.json`. The change is recorded on the domain model page.

## Tasks

1. Add `"maxNameLength": 20` to `game` in the working `config.json`, and parse and validate it (at least 1). Record the change on the data files page.
2. `Character` and its six subclasses, plus `Character.from(CharacterDef)`. An id with no matching subclass throws at start-up with a clear message.
3. `Player`.
4. `Board`: built from `BoardDef`, with a `Route` enum (`LOOP`, `CITY_CENTRE`), a `Heading` for the City Centre route, `isJunction(id)`, and a one-step `next(...)`.
5. `PlayerSetup` record (name, character id) and `Game.start(...)` with all FR-01 checks:
   - the player count is within the configured minimum and maximum;
   - each name is trimmed, not blank, at most `maxNameLength`, and unique ignoring case;
   - each character exists and is used only once.
6. `Game` turn phases: `roll()`, `chooseRoute(...)`, `chooseDirection(...)` (for Q-18), `endTurn()`, `phase()`, `currentPlayer()`, `players()` in turn order, `round()`, `year()`, `lastMove()`, and `IllegalActionException`.
7. A headless driver test that plays 10 full rounds with seeded dice and random route choices, checking nothing throws. It's groundwork for the `sim` package and NFR-06.
8. Docs (see below).

## Tests

| Test | Checks | Criterion |
|---|---|---|
| `GameStartTest.acceptsFourToSixPlayers` | 4, 5 and 6 players start; 3 and 7 are rejected with a message | FR-01 AC-1 |
| `GameStartTest.charactersMustDiffer` | Two players on the same character are rejected; an unknown character id is rejected | FR-01 AC-2 |
| `GameStartTest.namesAreValidated` | "  Aoife  " becomes "Aoife"; "", "   " and a 21-character name are rejected; exactly 20 characters is accepted | FR-01 AC-3, NFR-08 AC-3 |
| `GameStartTest.namesMustBeUnique` | "Sam" and "sam " are rejected together | FR-01 AC-4 |
| `GameStartTest.startingValuesComeFromData` | Each of the six characters starts with its `config.json` money and GP; a test copy with a changed `startMoney` changes it | FR-02 AC-1, AC-2 |
| `GameStartTest.firstPlayerIsRandomButRepeatable` | The same seed always picks the same first player; over 600 seeded 6-player games each seat goes first 70-130 times | FR-02 AC-3 |
| `GameStartTest.turnOrderIsFixed` | After the first player, turns follow seat order and wrap; after the last player, round + 1 and year + 2 | FR-02 AC-4 |
| `GameStartTest.newGameState` | Year 2026, round 1, everyone on square 0, phase `AWAITING_ROLL` | FR-02 AC-5, AC-6 |
| `CharacterTest` | The factory makes the right subclass for each of the six ids, and rejects an unknown id | Domain model |
| `BoardTest.loopWraps` | From 28, four steps on the loop ends on 2 | FR-03 AC-2 |
| `BoardTest.cityCentreFromEast` | From 12, choosing the City Centre: 35, 34, 33… 30, then 27, then 28 | FR-03 AC-4 |
| `BoardTest.cityCentreFromWest` | From 27, choosing the City Centre: 30… 35, then 12, then 13 | FR-03 AC-4 |
| `MovementTest.examplesFromFr03` | On 11, roll 4, City Centre: path 12, 35, 34, 33, ends on 33. On 26, roll 9, City Centre: path 27, 30-35, 12, 13, ends on 13 | FR-03 AC-4 |
| `MovementTest.noQuestionWithoutAJunction` | A move that doesn't reach 12 or 27 with steps left goes straight to `MOVE_DONE` | FR-03 AC-5 |
| `MovementTest.landingOnAJunctionAsksNextTurn` | A move ending exactly on 12 doesn't ask; that player's next roll pauses at once for the route | FR-03 AC-5, Q-05 |
| `MovementTest.noSecondQuestionAtTheFarJunction` | Coming out of the cut-through at 27 with steps left carries on to 28 without asking | FR-03 AC-7 |
| `MovementTest.keepsDirectionOnTheCutThrough` | A player who stops on 33 heading west continues 32, 31… next turn; one heading east continues 34, 35, 12… | Movement rule 5 |
| `MovementTest.directionChoiceWhenNoHeading` | A player placed on 30 with no heading is asked for a direction before moving | Q-18 groundwork |
| `MovementTest.sameSeedSameGame` | Two games with the same seed and the same choices end in the same positions | FR-03 AC-1 |
| `IllegalActionTest` | `chooseRoute` while awaiting a roll, `roll` twice, `endTurn` mid-move, and a route that isn't offered all throw `IllegalActionException`, and the state is unchanged | NFR-08 AC-4 |
| `HeadlessTurnsTest` | 10 rounds × 4-6 players with seeded random choices, no exceptions; every position is a real square | NFR-06 groundwork |
| `GameConfigValidationTest` (two more cases) | `maxNameLength` missing or 0 is rejected | NFR-07 AC-3 |

Coverage must stay at 80% or more for `model` and `config`.

## Docs to update

- **[Domain model](../wiki/architecture/domain-model.md):** `Character`, `Player`, `Board` and `Game` as built; "Changes from the analysis model" records the step-by-step `Board`, the `Game` phases, and the ability hooks arriving in plan 003.
- **[Game flow](../wiki/architecture/game-flow.md):** the proposal becomes "as built" for movement, with the phase diagram.
- **[Turn and actions](../wiki/rules/turn-and-actions.md) and [board and squares](../wiki/rules/board-and-squares.md):** move to `built` for movement.
- **[Data files](../wiki/architecture/data-files.md):** the `maxNameLength` change.
- **FR-01, FR-02, FR-03:** `built` (FR-01's UI part waits for plan 007, noted on the page); the [catalogue](../wiki/requirements/catalogue.md).
- **[Build progress](../wiki/operations/build-progress.md), `index.md`, `log.md`.**

## Verification

1. `.\mvnw.cmd verify` passes, with coverage at 80% or more.
2. `java -jar target\net-zero-belfast.jar --check` still passes.
3. Read the `MovementTest` cases against the [board drawing](../raw/net-zero-belfast-handover/design/board_layout.png). Remember the drawing numbers squares from 1.
4. Nothing visible changes in the window yet; that's expected.

## What Oscar should be able to explain

- Why `Character` is abstract with six subclasses, and how the factory picks one from the data.
- How `Board` knows where the City Centre route goes without any square numbers in the code.
- The turn phases: why the model pauses at a junction instead of asking the player directly, and what happens if the UI calls the wrong method.
- How "no second question at the far junction" is implemented, and why landing exactly on a junction defers the question to the next turn.
- Why a seeded random source makes the first player, and a whole game, repeatable in tests.

## Risks

| Risk | What we'll do |
|---|---|
| Junction edge cases are easy to get subtly wrong | Every rule above has its own test, including both FR-03 worked examples traced square by square |
| The phase machine grows awkward as actions are added in plans 003-005 | Keep phases few and named for what the game is waiting for. Review the design again when drafting plan 003 |
| Character subclasses are empty until plan 003 | Accepted. It keeps the analysis model's structure now, and plan 003 adds the hooks where they're used |

## Outcome

**Built 17 November 2026.** `mvnw clean verify` passes with no warnings: 129 tests (44 new in `model`, plus 2 new config validation cases). Line coverage is 100% for `model` (210 lines) and 99.1% for `config`. `--check` still passes.

### What was built

| Path (`src/main/java/netzerobelfast/`) | What it is |
|---|---|
| `model/Character.java` and six subclasses | `Character.from(def)` picks the subclass by data id; `fromAll` makes the whole cast |
| `model/Player.java` | Name, character, money, GP, position, City Centre heading. Only `Game` changes it |
| `model/Board.java` | Built from `BoardDef`; `next(position, heading, route)` moves one step; `Route` and `Heading` enums |
| `model/Game.java` | `start(...)` with every FR-01 check; phases `AWAITING_ROLL`, `AWAITING_ROUTE`, `AWAITING_DIRECTION`, `MOVE_DONE`; `roll()`, `chooseRoute()`, `chooseDirection()`, `endTurn()`; `lastMove()` |
| `model/IllegalActionException.java` | Thrown for any action the current phase doesn't allow; the state is left unchanged |
| `App.java` | Now also makes every character at start-up, so an unknown id in the data stops the game at launch |
| `src/main/resources/data/config.json` | `"maxNameLength": 20` added to `game` |

Test helpers: `ScriptedRandom` scripts the first player and exact dice totals; `Games` starts games and places counters for set-up.

### Checking the tests catch mistakes

Two deliberate bugs were put into `Game.continueMove()` and the full suite run against each, then the code was restored:

| Bug | Tests that failed |
|---|---|
| Ask at every junction, even straight out of the cut-through (breaks AC-7) | 3: both AC-7 tests and the Q-18 west-exit test |
| Never ask at a junction | 6: both worked examples, landing-on-a-junction, the far-junction case, staying on the loop, and an illegal-action test |

### What differed from the plan, or needed a reading

1. **Ending a move exactly on the far junction** (coming out of the cut-through with no steps left) counts as landing on a junction, so the player is asked at the start of their next move. AC-7 only covers carrying on in the same move. Tested in `q05_endingOnTheFarJunctionAsksNextTurn`. **Confirmed by Oscar, 17 November 2026.**
2. **Junctions are the two ends in `cityCentreRoute.connects`**, not the separate `junctions` list in `board.json`. The validator already requires both ends to be in that list, so they agree; a junction that didn't connect to the route would have nothing to offer.
3. **Turn order is seat order** (the order players were entered), starting from the random first player. `players()` returns seat order; `firstPlayer()` says who started.
4. **The round and year advance after the last player's turn**, as the plan said. There's no end-of-game check yet, so a game can run past round 12 until plan 005/006 add it.
5. **`Character` shares its name with Java's built-in `java.lang.Character`.** Inside the `model` package the game's class wins; elsewhere it's imported explicitly. The analysis model's name is kept, but it's worth knowing when reading the code.
6. The plan's 22 tests became 44 across six test classes, mostly by splitting checks into one behaviour per test.
