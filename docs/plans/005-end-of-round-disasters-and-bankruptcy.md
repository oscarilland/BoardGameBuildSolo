---
title: Plan 005 End of round, disasters and bankruptcy
type: plan
id: PLAN-005
status: done
requirements: [FR-11, FR-12, FR-13, FR-17, FR-05, FR-06, FR-07, FR-08, FR-03, NFR-10]
sources: [../wiki/rules/end-of-round.md, ../wiki/rules/climate-meter-and-disasters.md, ../wiki/rules/bankruptcy.md, ../wiki/architecture/domain-model.md, ../wiki/architecture/game-flow.md]
updated: 2026-11-17
---

# Plan 005 - End of round, disasters and bankruptcy

**Approved by Oscar, 18 November 2026:** "approve plan 005".

## Goal

After the last player's turn, the six end-of-round steps run in order:
1. squares and deals pay out;
2. the carbon tax;
3. the Climate Meter update;
4. the interim target check (2030 and 2040 only);
5. a possible disaster, with its climate explanation;
6. the year and round advance.

Bankruptcy becomes complete: a bankrupt player sits out, rolls to return, and comes back with a loan. After round 12 the game stops in a "game over" state, ready for scoring in plan 006. From here, every rule in the handover except scoring works headlessly.

## Scope

- **Requirements covered (all agreed):**

| Requirement | Built here |
|---|---|
| [FR-11](../wiki/requirements/fr-11-end-of-round-order.md) End-of-round order | AC-1 to AC-10 (AC-7: the game stops after round 12; scoring is plan 006) |
| [FR-12](../wiki/requirements/fr-12-disasters.md) Disasters | AC-1 to AC-11 (AC-9's on-screen part is plan 007) |
| [FR-13](../wiki/requirements/fr-13-bankruptcy.md) Bankruptcy | AC-3 to AC-7 (AC-1, AC-2 and AC-8 came in plan 004), and disaster payments as a trigger |
| [FR-17](../wiki/requirements/fr-17-disaster-explanation.md) Disaster explanation | AC-1, AC-2: the approved lines and sources in the data, attached to each disaster. AC-3 (readability) is plan 009 |
| [NFR-10](../wiki/requirements/nfr-10-fact-accuracy.md) | AC-2: each explanation has a source, enforced on load |
| Left over from plan 003 | [FR-05](../wiki/requirements/fr-05-co-funding.md) AC-3, AC-5, AC-6 (deals pay out); [FR-06](../wiki/requirements/fr-06-lobbying.md) AC-1's lost income, AC-3, AC-4; [FR-07](../wiki/requirements/fr-07-convert-square.md) AC-4, AC-5 (including the Industrialist bonus), AC-6; [FR-08](../wiki/requirements/fr-08-resilience.md) AC-6 (Resilience doesn't stop lost income, closures or Droughts) |
| Left over from plan 002 | [FR-03](../wiki/requirements/fr-03-dice-movement-and-junctions.md) AC-6: a bankrupt player doesn't move |

- **Rules answers used:**
  - [Q-02](../wiki/rules/open-questions.md): meter below 0.
  - [Q-04](../wiki/rules/open-questions.md): 2040 target 11.
  - [Q-08](../wiki/rules/open-questions.md): Resilience rounding.
  - [Q-11](../wiki/rules/open-questions.md): the approved explanation lines.
  - [Q-14](../wiki/rules/open-questions.md): automatic return.
  - [Q-15](../wiki/rules/open-questions.md): closed and flooded squares.
  - [Q-17](../wiki/rules/open-questions.md): no owned squares means no disaster.
  - Oscar's FR-13 answer: a returning player takes a normal roll in the same turn.
- **Out of scope:**
  - Scoring, the winner and the end-of-game report (plan 006).
  - Screens (plan 007).
  - The 20-game headless test as an NFR-06 check (plan 006); this plan's headless run already plays full games.

## Prerequisites

The working `config.json` gains, for each disaster, `explanation` (the approved line) and `sources` (title and web address), copied from [disaster explanations](../wiki/rules/climate-meter-and-disasters.md#disaster-explanations).

## Design

### The end of the round

`Game.endTurn()` already notices when the last player has finished. It now calls `endRound()`, which runs the steps in order (the [End Round sequence](../raw/net-zero-belfast-handover/design/seq_end_round.png)):

| Step | What happens | Notes |
|---|---|---|
| 1 Income | Every owned square that isn't closed or in the flooded district: Polluting +£7M unless lobbied; Green Project +£2M and +2 GP; the Industrialist +£2M more on squares they converted; the Farmer +1 GP per nature project. Nature projects get no GP in a Drought round. Co-funding deals: +1 GP to funder and owner, unless the square is closed or flooded | Bankrupt owners get the GP but no money |
| 2 Carbon tax | −1 GP per owned Polluting square | Closed, flooded and lobbied squares still pay (Q-15) |
| 3 Meter | `ClimateMeter.update(polluting, green)`: +3, +2 per owned Polluting square (except those the Activist lobbied this round), −1 per owned Green Project. Records the history | Closed and flooded squares still count (Q-15) |
| 4 Interim target | Only in each target's round (3 and 8, from the data): if the meter is above the target, −2 GP per Polluting square owned | |
| 5 Disaster | Roll one die; if it's at or below the danger band, pick one of four types, apply it, and attach its explanation and sources | Costs are paid now and are compulsory; closures, floods and Droughts apply next round |
| 6 Advance | Clear lobbying; year + 2, round + 1. After the last round: `GAME_OVER` | Moved here from `endTurn` |

The round's flood, closure and Drought are kept as "next round's effects". Step 1 reads them, then step 5 replaces them.

### Disasters (`DisasterManager`, enum `Disaster`)

- `dangerBand()` comes from `ClimateMeter` and the data's bands; below 0 uses the configured band.
- `check(band)` rolls `Dice.rollOne()` and, on a disaster, picks the type with the game's random source.
- `apply(...)`:
  - **Flood:** a random district that has owned squares. Each owner pays £1M × band × their squares there, as one payment cut by their Resilience. That district earns nothing next round.
  - **Storm:** a random owned square closes next round. Its owner pays £2M × band, cut by Resilience.
  - **Heatwave:** every player pays £1M × band, each cut by their own Resilience.
  - **Drought:** nature projects earn no GP next round.
  - A Flood or Storm with no owned squares does nothing (Q-17).
- All payments use `payCompulsory`, so disasters can bankrupt.

### Bankruptcy, complete

- `Player` gains `turnsBankrupt` and `loanRepayments`.
- When the turn passes to a bankrupt player, the game decides what they do:
  - **First turn after going bankrupt:** the phase is `AWAITING_RETURN_ROLL`, and `rollToReturn()` rolls one die. On the escape value (6, from the data) they're back. Otherwise the turn ends: phase `MOVE_DONE`, with no actions available.
  - **Next turn** (`maxRounds` from the data, counted as the simulation did, Q-14): they're back automatically, with no roll.
  - **On return:** they receive the loan (£10M), their repayments rise by £15M, and the phase becomes `AWAITING_ROLL` for a normal turn (Oscar's answer).
- While bankrupt:
  - `availableActions()` is empty, so no converting (FR-07 AC-6);
  - they don't move (FR-03 AC-6);
  - their squares give GP but no money (FR-13 AC-7).

### What the screen will need

`lastRound()` returns a `RoundReport` for the end-of-round pop-up (plan 007):
- the round and year;
- the meter before and after, and the band;
- the interim target result, if it was a target round;
- the disaster die, type, target, and each player's payment;
- the explanation and sources;
- the steps in the order they ran.

## Tasks

1. Data: add `explanation` and `sources` to each disaster in the working `config.json`; parse them into `Rules.Disasters`; validate that each has non-empty text and at least one source with a title and web address (NFR-10 AC-2).
2. `ClimateMeter`: `update`, `history`, `dangerBand`, and the target check, all from the data.
3. `Disaster` enum and `DisasterManager`; next round's effects (flooded district, closed square, Drought).
4. `Game.endRound()` with the six steps in order and `RoundReport`; lobbying cleared in step 6; `GAME_OVER` after the last round.
5. Income hooks: `Industrialist` bonus on squares they converted; `Farmer` nature GP.
6. Bankruptcy: `Player.turnsBankrupt`, `loanRepayments`; `AWAITING_RETURN_ROLL`, `rollToReturn()`, automatic return; no moves or actions while bankrupt.
7. `ScriptedRandom` gains raw values, so tests can script the disaster die, type and target.
8. The headless run plays complete 12-round games to `GAME_OVER`.
9. Docs (see below).

## Tests

| Test class | Checks | Criteria |
|---|---|---|
| `IncomeTest` | Polluting +£7M; Green +£2M and +2 GP. Lobbied: no money, and back the next round. Industrialist-converted: +£4M; converted by someone else: +£2M. Farmer +3 GP per nature project. Drought: no nature GP (and no Farmer bonus), other GP unaffected. Closed or flooded: nothing, including co-funding GP. Co-funding +1 GP each; continues if the owner is bankrupt. Bankrupt owner: GP, no money | FR-11 AC-3, FR-05 AC-3, AC-5, AC-6, FR-06 AC-1 to AC-3, FR-07 AC-4, AC-5, FR-13 AC-7, FR-08 AC-6 |
| `CarbonTaxTest` | 3 Polluting squares cost 3 GP; lobbied, closed and flooded squares still pay | FR-11 AC-4, AC-9, FR-06 AC-3 |
| `MeterUpdateTest` | 4 Polluting (1 Activist-lobbied) and 5 Green: +4. A non-Activist lobby still counts. Closed squares count. The meter can go below 0. One history entry per round, starting from the start value | FR-11 AC-5, AC-8, AC-9, AC-10, FR-06 AC-4, AC-5 |
| `InterimTargetTest` | Round 3, meter 27: 2 Polluting squares cost 4 GP. Meter 26: nothing. Round 8 against 11. No check in other rounds | FR-11 AC-6 |
| `DangerBandTest` | Bands at −3, 0, 10, 11, 25, 26, 40, 41 | FR-12 AC-1 |
| `DisasterTest` | Band 2: a roll of 2 is a disaster, 3 isn't. Types are uniform over 4,000 seeded checks. Flood in band 3: £3M per square owned there, nothing earned next round. Storm in band 2: £4M and closed next round. Heatwave in band 4 with Resilience level 2: £2M. Drought. No owned squares: Flood or Storm skipped. Payments can bankrupt. Each disaster carries its explanation and sources from the data. Resilience doesn't stop the closure or the Drought | FR-12 AC-1 to AC-11, FR-17 AC-1, AC-2, FR-08 AC-6 |
| `RoundOrderTest` | End of round runs only after the last player. The report lists the six steps in order. Order matters: income before tax; the band comes from the updated meter; the target uses the updated meter. Year and round advance; after round 12 `GAME_OVER`, and every action is refused | FR-11 AC-1, AC-2, AC-7 |
| `BankruptcyTest` | A disaster payment bankrupts. The first turn after: `AWAITING_RETURN_ROLL`; a 6 returns them with £10M and £15M of repayments, then a normal roll; anything else ends the turn with no actions. The next turn: automatic return. Bankrupt: no move, no conversion, GP but no money. Going bankrupt twice gives £30M of repayments | FR-13 AC-3 to AC-7, FR-03 AC-6, FR-07 AC-6 |
| `GameConfigValidationTest` (more cases) | A disaster without an explanation, or without a source, is rejected | NFR-10 AC-2, NFR-07 AC-3 |
| `HeadlessTurnsTest` (extended) | 30 seeded complete games to `GAME_OVER`: no exceptions; money never negative; bankrupt players at £0; 13 meter history values | NFR-06 groundwork |

Coverage must stay at 80% or more for `model` and `config`.

## Docs to update

- **[Domain model](../wiki/architecture/domain-model.md):** `ClimateMeter` complete, `DisasterManager`, `Disaster`, `RoundReport`; bankruptcy fields on `Player`; changes from the analysis model (where the payout logic lives, next round's effects, the return-roll phase).
- **[Game flow](../wiki/architecture/game-flow.md):** End Round as built; the bankrupt turn; `GAME_OVER`.
- **Rules pages:** [end of round](../wiki/rules/end-of-round.md), [Climate Meter and disasters](../wiki/rules/climate-meter-and-disasters.md) and [bankruptcy](../wiki/rules/bankruptcy.md) to `built`; build status on [turn and actions](../wiki/rules/turn-and-actions.md) and [board and squares](../wiki/rules/board-and-squares.md).
- **[Data files](../wiki/architecture/data-files.md):** the explanation change.
- **Requirements:**
  - FR-03, FR-05 to FR-08, FR-11 to FR-13 move to `built`, where every AC is now met.
  - FR-17 and NFR-10 keep notes for their remaining ACs.
  - The [catalogue](../wiki/requirements/catalogue.md).
- **[Build progress](../wiki/operations/build-progress.md), `index.md`, `log.md`, `src/AGENTS.md`.**

## Verification

1. `.\mvnw.cmd verify` passes, with coverage at 80% or more; `--check` still passes.
2. Deliberate bugs, each caught by the tests:
   - tax before income;
   - the band read before the meter update;
   - lobbied squares still paying;
   - no Resilience cut on disasters;
   - a bankrupt player able to move.
3. Read one seeded game's round reports by eye against the [end of round](../wiki/rules/end-of-round.md) page.

## What Oscar should be able to explain

- The six end-of-round steps and why their order matters, with an example where swapping two changes the result.
- How "next round's effects" work: why a Flood or Storm rolled now affects the following round's income.
- How the danger band sets both a disaster's chance and its cost.
- How Resilience cuts a payment, and what it doesn't protect against.
- A bankrupt player's turns, from going bankrupt to returning with a loan, and why the loan costs £15M at the end.
- Where the disaster explanations come from, and why they're in the data rather than the code.

## Risks

| Risk | What we'll do |
|---|---|
| This is the largest plan so far | Build in the order of the tasks; the end-of-round order and income first, then disasters, then bankruptcy, with the tests passing at each stage. If it runs long, finish steps 1-4 and 6 and move disasters and bankruptcy to a plan 005b |
| Flood payments: per square or per owner? | One payment per owner (£1M × band × squares), cut once, so rounding happens once. Shown in a test so Oscar can see it; an easy change if he prefers per square |
| A bankrupt player could earn money another way and break the £0 invariant | While bankrupt they take no turns, so no cards or actions; Heatwave takes nothing from £0. The headless run checks it |

## Outcome

**Built 18 November 2026**, in one sitting, with no split into a plan 005b. `mvnw clean verify` passes with no warnings: 268 tests (58 new). Line coverage is 99.4% for `model` (770 of 775 lines) and 99.1% for `config`. `--check` still passes.

### What was built

| Path | What it is |
|---|---|
| `src/main/resources/data/config.json` | Each disaster gains `explanation` and `sources` (the approved lines); `DROUGHT` gains `lostGPRounds: 1` |
| `config`: `Rules.Disasters`, `Rules.Explanation`, parser, validator | The explanations parsed and validated (text, at least one source); every effect's duration at least 1 |
| `model/ClimateMeter` | `update`, `history`, `dangerBand`, `targetFor`, `meetsTarget`, `netZero`, all from the data |
| `model/Disaster`, `DisasterManager`, `DisasterResult` | The die, the type, the target, payments cut by Resilience and compulsory; the lasting effects (flooded districts, closed squares, Drought) counted in rounds |
| `model/RoundReport` | The round, meter before and after, band, target result, die, disaster and the steps in order |
| `model/Game` | `endRound()` (the six steps), `startTurn()`, `rollToReturn()`, phases `AWAITING_RETURN_ROLL` and `GAME_OVER`, `lastRound()`, `lastReturn()`, `disasters()` |
| `model/Player` | `turnsBankrupt()`, `loanRepayments()`, `missTurn()`, `returnFromBankruptcy()` |
| `model/Character`, `Industrialist`, `Farmer` | Hooks `incomeBonus(square, owner)` (the analysis model's name) and `natureBonusGP()` |

### Checking the tests catch mistakes

| Bug | Tests that failed |
|---|---|
| Danger band read before the meter update (which also moves the target check) | 3: both interim target tests and the band-order test |
| Lobbied squares still pay | 1: the lobbying income test |
| No Resilience cut on disaster payments | 3: Flood, Storm and Heatwave cut tests |
| Bankrupt players move | 5: every bankrupt-turn test |
| Closed and flooded squares still earn | 3: Flood, Storm and the one-round-effect test |

The plan also listed "tax before income". It was replaced, because GP from income and tax adds up the same in either order, so no test could fail on it. The step order is still checked directly by `RoundOrderTest.fr11_ac2_theStepsRunInOrder`.

One seeded game was also printed round by round and read against the rules. The meter, targets, disaster costs (Storm £8M in band 4; Flood £3M at Resilience 1) and the 13-value history all matched.

### What differed from the plan

1. **Disaster durations come from the data.** The plan had "next round" for Flood, Storm and Drought. The data has `lostIncomeRounds` and `closedRounds` (both 1), and Drought now has `lostGPRounds: 1`, so no duration is a number in code. Effects count down after each round's income.
2. **Income uses character hooks.** `incomeBonus(square, owner)` is the analysis model's `incomeBonus`, overridden by the Industrialist; `natureBonusGP()` is overridden by the Farmer. `Game` never checks which character a player is.
3. **Five existing tests now script the end-of-round die.** `ScriptedRandom.noDisaster()` scripts a 6, which is above every band, wherever a round ends. Nothing those tests check changed.
4. **The headless run plays 30 complete 12-round games to `GAME_OVER`**, checking money never negative, bankrupt players at £0, and 13 meter values.
5. **Bankruptcy is rare.** In 60 complete headless games (30 with random players, 30 with players who took every action they could afford), no one went bankrupt: each end of round pays income before the disaster step, so money is topped up before a disaster can take it. The rules are covered by `BankruptcyTest` directly. **For Oscar and playtesting:** if bankruptcy should be a real risk, the balance needs looking at (Q-10, issue 4 in build progress).
6. **`endRound()` is package-private** so tests can run the end of a round without scripting every player's turn. `endTurn()` calls it after the last player.
7. **Floods pick from districts in board order**, then a random one of those with owned squares. Heatwaves record a payment for every player, including £0 for a bankrupt one.
