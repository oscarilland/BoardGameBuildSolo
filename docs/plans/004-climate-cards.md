---
title: Plan 004 Climate cards
type: plan
id: PLAN-004
status: done
requirements: [FR-10, NFR-10, FR-13]
sources: [../wiki/rules/climate-cards.md, ../wiki/rules/bankruptcy.md, ../wiki/architecture/domain-model.md, ../wiki/architecture/game-flow.md]
updated: 2026-11-17
---

# Plan 004 - Climate cards

**Approved by Oscar, 17 November 2026:** "approve plan 004".

## Goal

Landing on a Climate Card square draws a card from the shuffled 30-card deck, and the game handles each type:
- **Quiz:** the player answers, and a correct answer earns GP.
- **Choice:** the player picks option A or B, and its money, GP and meter changes apply at once. An option they can't afford is shown but disabled.
- **Event:** its effect applies straight away. A cost the player can't pay makes them bankrupt.

Afterwards the game holds the card's fact and sources for the screen to show. The shared Climate Meter exists from this plan on, because Choice cards move it.

## Scope

- **Requirements covered:**

| Requirement | Built here | Waits for |
|---|---|---|
| [FR-10](../wiki/requirements/fr-10-climate-cards.md) Climate cards (agreed) | AC-1 to AC-8. AC-4's "visible on the meter bar" and AC-6's on-screen layout are checked by the model holding the values; the screens come in plan 007 | - |
| [NFR-10](../wiki/requirements/nfr-10-fact-accuracy.md) Fact accuracy (agreed) | AC-1 (already in plan 001's validation), AC-3 (the model gives every source's title and web address with the fact) | AC-2: disaster explanations, plan 005. AC-4: the pre-demo re-check, a gate |
| [FR-13](../wiki/requirements/fr-13-bankruptcy.md) Bankruptcy (draft) | Only the trigger: AC-1 and AC-2 for Event cards, and AC-8's "a Choice option can't bankrupt" | Everything else (sitting out, returning, loans): plan 005, where FR-13 gets agreed |
| [FR-08](../wiki/requirements/fr-08-resilience.md) Resilience | AC-6's "Resilience doesn't reduce Event card costs" | The rest of AC-6, with disasters: plan 005 |

- **Decisions relied on:**
  - [Q-12](../wiki/rules/open-questions.md): unaffordable Choice options are disabled.
  - [Q-13](../wiki/rules/open-questions.md): the card's own quiz reward wins.
  - [Q-02](../wiki/rules/open-questions.md): the meter can go below 0.
  - Oscar's FR-10 answers: sources are shown with their title and web address; plan 004 starts bankruptcy.
- **Out of scope:**
  - What bankruptcy does next: no turns, no money income, return and loan (plan 005). Until then a bankrupt player keeps playing normally, so the model is only temporarily incomplete.
  - The meter's round-by-round update, history, danger bands and targets (plan 005).
  - Screens (plan 007).
  - The readability check (NFR-03, plan 009).

## Design

### Classes (names from the [domain model](../wiki/architecture/domain-model.md))

| Class | In this plan |
|---|---|
| `ClimateCard` (abstract) | id, text, fact, sources; built from `CardDef` |
| `QuizCard` | answers, correct index, reward; `checkAnswer(i)` |
| `ChoiceCard` | two `Option`s, each with a label and an `Effect` (money, GP, meter); `applyOption(choice)` |
| `EventCard` | an `Effect` |
| `CardDeck` | `draw()` and `reshuffle()`. Shuffled at game start; when the draw pile is empty, the used cards are reshuffled into it |
| `ClimateMeter` | `value()` and `adjust(delta)` now. `update`, `dangerBand`, `meetsTarget` and the history come in plan 005. Starts at `climateMeter.start` from the data |
| `Player` | Gains `bankrupt()` and package-private `payCompulsory(amount)`: if the amount is more than their money, money becomes £0, the rest is written off, and they're marked bankrupt (FR-13 AC-1). Paying exactly what they have leaves £0 and not bankrupt (AC-2) |

### Randomness without breaking the scripted tests

The deck needs shuffling, and every random choice must come from the game's one seeded source. The deck gets its own `java.util.Random`, seeded once from the game's source (`new Random(random.nextLong())`). A whole game still replays from one seed. It also leaves the dice sequence alone, so plans 002-003's scripted dice tests keep working, because they script only `nextInt`. For tests, a package-private `CardDeck.putOnTop(cardId)` sets which card comes next.

### Turn flow

Landing on a Climate Card square **is** the card draw: it's compulsory, so it isn't an action to choose. When a move ends there, or a test arrives there:

```text
move ends on a Climate Card square → draw
   QUIZ   → AWAITING_QUIZ_ANSWER → answerQuiz(i)     ─┐
   CHOICE → AWAITING_CHOICE      → chooseOption(0|1) ─┼→ MOVE_DONE (card shown; may convert; end turn)
   EVENT  → effect applied at once ───────────────────┘
```

- `lastCard()` returns a `CardResult`: the card, what happened (correct or not, which option, money/GP/meter changes, whether it caused bankruptcy), and the fact and sources (title and web address) for the screen (FR-10 AC-6, NFR-10 AC-3).
- `affordableOptions()` lists the Choice options the player can take. `chooseOption` refuses any other with `IllegalActionException`, changing nothing (FR-10 AC-7).
- After a card, the landing action is used up, but converting is still allowed, as on any other square.
- This is the [Draw Climate Card sequence](../raw/net-zero-belfast-handover/design/seq_draw_climate_card.png) with its "ask the player" arrows turned into phases, as [game flow](../wiki/architecture/game-flow.md) proposed.

## Tasks

1. `ClimateCard` and its three subclasses, built from `CardDef`, plus `CardResult`.
2. `CardDeck`, with a seeded shuffle, reshuffling, and `putOnTop` for tests.
3. `ClimateMeter`, starting value and `adjust` only.
4. `Player.payCompulsory`, `bankrupt()`, and the written-off amount.
5. `Game`:
   - the deck and meter;
   - drawing when a move ends on a card square (including from `arriveForTest`);
   - the phases `AWAITING_QUIZ_ANSWER` and `AWAITING_CHOICE`;
   - `answerQuiz`, `chooseOption`, `affordableOptions`, `lastCard`, `meter`.
6. The headless run answers quizzes and makes Choice picks at random. It also checks that money is never negative, and that a player is bankrupt only at £0.
7. Add the pre-demo fact re-check gate (NFR-10 AC-4) to build progress.
8. Docs (see below).

## Tests

| Test class | Checks | Criteria |
|---|---|---|
| `CardDeckTest` | 30 cards from the data. With a fixed seed the order is the same every time. 30 draws give 30 different cards; the 31st starts a reshuffled pile. `putOnTop` works | FR-10 AC-1, AC-2 |
| `QuizCardTest` | Q01 ("2050" is answer 2): answer 2 gives +2 GP; answer 0 gives nothing. The reward comes from `GameConfig.quizReward`. An answer outside 0-3 is refused with no change | FR-10 AC-3 |
| `ChoiceCardTest` | C01 option A: +£6M, meter 50 → 52 at once. Option B: −£2M, +1 GP. With £1M, only A is affordable; choosing B is refused with no change. C08 "Act now": meter −2 | FR-10 AC-4, AC-7 |
| `EventCardTest` | E01: −£3M. E03: +£3M. A player at Resilience level 3 still pays the full £3M. With £2M, E01 leaves £0 and bankrupt, with £1M written off. With exactly £3M, E01 leaves £0 and not bankrupt | FR-10 AC-5, FR-08 AC-6 (part), FR-13 AC-1, AC-2 |
| `CardFlowTest` | Rolling onto square 2 draws and enters the right phase. After resolving: `MOVE_DONE`, no landing action, converting still offered. `lastCard()` has the fact and every source's title and web address (C05 has two). `endTurn`, `roll` and `take` are refused while a card waits for an answer | FR-10 AC-1, AC-6, NFR-10 AC-3, NFR-08 AC-4 |
| `ClimateMeterTest` | Starts at the data value. `adjust` moves it either way and can go below 0 | Q-02, FR-10 AC-4 |
| `HeadlessTurnsTest` (extended) | Random answers and options as well; money never negative; bankrupt players have £0 | NFR-06 groundwork |
| Existing tests | All of plans 001-003 still pass unchanged | Regression |

Coverage must stay at 80% or more for `model` and `config`.

## Docs to update

- **[Domain model](../wiki/architecture/domain-model.md):** the card classes, `CardDeck`, `ClimateMeter` (partial), `CardResult`, `Player.payCompulsory`/`bankrupt`; changes from the analysis model: `ClimateCard.source` is a list of sources, `resolve(p, g)` is split into draw-then-answer phases, and the deck has its own seeded `Random`.
- **[Game flow](../wiki/architecture/game-flow.md):** the Draw Climate Card sequence as built.
- **[Climate cards](../wiki/rules/climate-cards.md):** `built`. **[Bankruptcy](../wiki/rules/bankruptcy.md):** build status (trigger only).
- **FR-10:** `built`. NFR-10, FR-08 and FR-13: build status notes. The [catalogue](../wiki/requirements/catalogue.md).
- **[Build progress](../wiki/operations/build-progress.md)** (including the fact re-check gate), `index.md`, `log.md`, `src/AGENTS.md`.

## Verification

1. `.\mvnw.cmd verify` passes, with coverage at 80% or more.
2. `java -jar target\net-zero-belfast.jar --check` still passes.
3. Deliberate bugs, each caught by the tests: no reshuffle when the pile is empty; unaffordable Choice options allowed; no written-off floor on compulsory payments.

## What Oscar should be able to explain

- How `ClimateCard` uses polymorphism, and why the draw-then-answer phases replace `resolve()` asking the player directly.
- Why the deck has its own `Random` seeded from the game's, and how a whole game still replays from one seed.
- The difference between a voluntary payment (`pay`: refused if unaffordable) and a compulsory one (`payCompulsory`: floors at £0 and bankrupts).
- Why a Choice card can never bankrupt anyone, and what stops the data breaking that (FR-10 AC-8's validation).
- Why Resilience doesn't reduce Event card costs.

## Risks

| Risk | What we'll do |
|---|---|
| A bankrupt player still plays normally until plan 005 | Accepted and written down. The headless run checks they stay at £0 or above. Plan 005 is next |
| `ClimateMeter` is half-built | Only `value` and `adjust` now. Plan 005 adds the rest against FR-11 and FR-12, with its tests |
| Adding card phases breaks the existing turn flow | Every existing test must pass unchanged, and the headless run exercises the new phases at random |

## Outcome

**Built 17 November 2026.** `mvnw clean verify` passes with no warnings: 210 tests (29 new, one obsolete test removed). Line coverage is 99.3% for `model` (559 of 563 lines) and 99.1% for `config`. `--check` still passes.

### What was built

| Path (`src/main/java/netzerobelfast/model/`) | What it is |
|---|---|
| `ClimateCard` (sealed) with `QuizCard`, `ChoiceCard`, `EventCard` | Built from `CardDef`; a quiz card carries its reward from `GameConfig.quizReward` (Q-13) |
| `CardDeck` | Shuffled at start with its own `Random` seeded from the game's; `draw()`, `reshuffle()`, `remaining()`; `putOnTop(id)` for tests |
| `CardResult` | What happened (answer, correct, option, money/GP/meter changes, made bankrupt), plus the card's fact and sources |
| `ClimateMeter` | Start value from the data and `adjust(delta)`; no floor (Q-02) |
| `Player` | `bankrupt()` and package-private `payCompulsory(amount)`, which returns what was actually paid |
| `Game` | The deck and meter; a card drawn on arrival at a Climate Card square; phases `AWAITING_QUIZ_ANSWER` and `AWAITING_CHOICE`; `answerQuiz`, `chooseOption`, `affordableOptions`, `pendingCard`, `lastCard`, `meter` |

### Checking the tests catch mistakes

| Bug | Tests that failed |
|---|---|
| No reshuffle when the pile is empty | 1: `CardDeckTest.fr10_ac2_noRepeatsUntilEveryCardIsDrawnThenAReshuffle` |
| Unaffordable Choice options allowed | 2: the AC-7 test and the headless run |
| No £0 floor on compulsory payments | 3: two bankruptcy tests and the headless run |

### What differed from the plan

1. **Drawing a card isn't an `Action`.** `ClimateCardSquare.landOn()` still offers nothing; `Game` draws as soon as a player arrives (`landed()`), because the draw is compulsory. That covers moves, Glider rides don't land there, and `arriveForTest`.
2. **The headless run checks money never goes below £0**, but not that a bankrupt player stays at £0. Until plan 005 a bankrupt player keeps playing normally, so they can earn money again.
3. **Existing tests changed in two small ways**, because square 2 is a Climate Card square and now draws a card:
   - incidental "other players roll 2" moves now roll 3;
   - the conversion tests stand on the player's own Polluting square instead of square 2.

   The obsolete test "Climate Card squares offer nothing yet" was replaced by `CardFlowTest`. Nothing these tests check changed.
4. **Event cards apply all three parts of their effect** (money, GP and meter), as the data format allows, though the current deck's events only change money.
5. **`pendingCard()`** gives the screen the card's text, answers or options while the game waits, and `CardResult.moneyChange` records what was actually paid after any write-off.
6. **`ClimateCard` is `sealed`**, so `Game` handles the three card types with a pattern-matching `switch` the compiler checks is complete.
