---
title: Plan 006 Scoring, end-of-game report and balance simulation
type: plan
id: PLAN-006
status: done
requirements: [FR-14, FR-16, NFR-06]
sources: [../wiki/rules/scoring-and-end-of-game.md, ../wiki/rules/balance-and-history.md, ../wiki/sources/balance-simulation.md, ../wiki/architecture/domain-model.md]
updated: 2026-11-18
---

# Plan 006 - Scoring, end-of-game report and balance simulation

**Approved by Oscar, 18 November 2026:** "approve plan 006".

## Goal

When the game reaches `GAME_OVER` it scores itself:
- whether Belfast reached net zero;
- every Legacy Score, kept exact;
- one winner, using the tie-breaks;
- the data for the end-of-game report: the meter's history and each player's share of the polluting squares.

Computer players with three strategies play the 20 test games NFR-06 asks for. `java -jar net-zero-belfast.jar --simulate 1000` prints a balance report, so issue 4 (bankruptcy is rare) and re-tuning after playtests (Q-10) can be answered with numbers. After this plan the whole rules engine is done.

## Scope

- **Requirements covered:**

| Requirement | Built here | Waits for |
|---|---|---|
| [FR-14](../wiki/requirements/fr-14-legacy-score-and-winner.md) Legacy Score and winner (agreed) | AC-1 to AC-6 | The end screen saying "net zero reached" or "missed" (AC-3's screen): plan 008 |
| [FR-16](../wiki/requirements/fr-16-end-of-game-report.md) End-of-game report (agreed) | AC-1 to AC-3 as data: meter history with the target lines, each player's share, scores and winner | Drawing it (AC-4, and the graph itself): plan 008 |
| [NFR-06](../wiki/requirements/nfr-06-reliability-and-test-coverage.md) Reliability and test coverage (agreed) | AC-2 (20 complete seeded games with a mix of strategies) and AC-3 (one winner each, money never negative before repayments, 13 meter values); AC-1 is in place since plan 001 | - |
| [FR-13](../wiki/requirements/fr-13-bankruptcy.md) Bankruptcy | The £15M repayments deducted at scoring | - |

- **Rules answers used:**
  - [Q-01](../wiki/rules/open-questions.md): after most Green Projects, fewest Polluting squares, then a die roll-off.
  - [Q-16](../wiki/rules/open-questions.md): exact scores, shown to one decimal place.
  - Oscar's FR-16 answer: the share counts polluting-square-rounds across the game.
  - Oscar's NFR-06 answer: the computer players also measure balance.
- **Out of scope:**
  - Screens (plans 007 and 008).
  - Changing any balance value. The simulation reports; Oscar decides any re-tune (Q-10).

## Design

### Scoring (`Scoring`, `GameResult`, `LegacyScore`)

| Case | Legacy Score | Data |
|---|---|---|
| Net zero (meter at or below 0 after round 12) | GP + (money − loan repayments) ÷ 5 | `netZeroAtOrBelow`, `moneyDivisor` |
| Missed | GP − 5 × Polluting squares still owned; money ignored | `missedPollutingPenaltyGP` |

- **Exact scores:** a `LegacyScore` stores the score multiplied by the money divisor, as a whole number: 5 × GP + (money − repayments) in a net zero game. So comparisons are exact and nothing is rounded before the winner is chosen (Q-16). `display()` gives one decimal place, for example "36.4". This replaces the class diagram's `legacyScore(netZero): int`, recorded on the domain model page.
- **The winner:** the highest score. If players tie: the most Green Projects owned; then the fewest Polluting squares; then a roll-off. The roll-off rolls one die per tied player with the game's `Dice`, highest wins, and still-tied players roll again. `GameResult` records which tie-break decided it, and any roll-off dice.
- **When:** `Game` scores automatically as it enters `GAME_OVER`, and `result()` returns the `GameResult`. This is the analysis model's `endGame(): Player`, returning more than the winner.

### End-of-game report data (FR-16)

- **Meter:** the 13 values from `ClimateMeter.history()` (2026 and the end of each round), plus the lines to draw: the interim targets (26 in 2030, 11 in 2040) and net zero (0), all from the data.
- **Shares:** at each end of round, every player's owned Polluting squares are added to their polluting-square-rounds (`Player.pollutingSquareRounds()`, counted in step 2 with the carbon tax). A share is that total ÷ the city's total, as a percentage to one decimal place. If nobody ever owned one, every share is 0%.
- **Scores and winner:** the same `GameResult`.

### Computer players (`netzerobelfast.sim`)

- `Strategy` decides every choice the game asks for:
  - route and direction;
  - quiz answer;
  - choice option, from `affordableOptions()`;
  - which of `availableActions()` to take, if any.

  It only ever chooses from what the game offers, so it can't break a rule.
- **Three strategies:**
  - **Random:** like the headless test; leans green or polluting at random.
  - **Green:** buys Green Projects, converts, co-funds, buys Resilience; picks the option that lowers the meter or gains GP; buys a Polluting square only to convert it at once.
  - **Polluter:** buys Polluting squares, never converts, picks the money option, lobbies rivals.
- `Simulator.play(config, seed, seats)` plays one complete game from a seed and returns its `GameResult`, plus counts (bankruptcies, disasters, turns). Each strategy's own choices use a `Random` seeded from the game's seed, so a game replays exactly. The sim isn't game logic, but it keeps the same rule.
- `BalanceReport.run(config, games, seed)` plays many games with 4-6 players, random characters and a random mix of strategies. It reports:
  - net zero rate overall, and when most players are green or polluting;
  - win rate by character and by strategy, next to the fair share;
  - bankruptcies per game and the share of games with one;
  - disasters per game.
- `App --simulate N` prints the report and exits; with no N it plays 1,000 games.

## Tasks

1. `LegacyScore`, `GameResult`, `Scoring` (scores, ranking, tie-breaks, roll-off), and `Game.result()`, set on entering `GAME_OVER`.
2. Polluting-square-rounds counted at each end of round; the end-of-game report data in `GameResult` (meter history, target lines, shares).
3. `netzerobelfast.sim`: `Strategy` and the three strategies, `Simulator`, `BalanceReport`.
4. `App --simulate [N]`.
5. Add `netzerobelfast.sim` to the coverage rule, so the computer players are tested as code too.
6. Run `--simulate 1000` and record the results on [balance and design history](../wiki/rules/balance-and-history.md) next to the Python simulation's, with the differences between the two explained.
7. Docs (see below).

## Tests

| Test class | Checks | Criteria |
|---|---|---|
| `ScoringTest` | Net zero, 30 GP, £45M, one return: 36.0. Missed, 30 GP, £80M, 2 Polluting squares: 20.0, money ignored. The net zero flag. A tie on score goes to most Green Projects, then fewest Polluting squares, then a scripted roll-off that re-rolls a tie. 36.4 beats 36.2, and nothing is rounded to 36. Scores can be negative | FR-14 AC-1 to AC-6, FR-13 (repayment) |
| `GameResultTest` | Entering `GAME_OVER` produces a result once; exactly one winner; ranking in score order | FR-14 AC-5, FR-11 AC-7 |
| `EndOfGameReportTest` | 13 meter values starting at 50; target lines 26 (2030), 11 (2040) and 0 from the data. Owning 2 Polluting squares for 6 rounds counts 12. Shares add up to 100%, or are all 0% when nobody polluted. A late conversion doesn't erase earlier rounds | FR-16 AC-1 to AC-3 |
| `StrategyTest` | Each strategy only picks offered choices and actions. Green never keeps a Polluting square past its turn; Polluter never converts. Same seed, same choices | NFR-08 AC-4, sim |
| `SimulatorTest` | 20 seeded complete games with 4, 5 and 6 players and a mix of strategies: no exceptions, exactly one winner each, money never negative before repayments, 13 meter values. The same seed gives the same result | NFR-06 AC-2, AC-3 |
| `BalanceReportTest` | A 60-game run: win rates by character add up to 100%; rates are between 0% and 100%; the same seed gives the same report | sim |
| `HeadlessTurnsTest` | Unchanged; its games now also end with a result | Regression |

Coverage must stay at 80% or more for `model`, `config` and `sim`.

## Docs to update

- **[Domain model](../wiki/architecture/domain-model.md):** `Scoring`, `GameResult`, `LegacyScore`; `legacyScore(): int` replaced; `endGame()` as the automatic result; the `sim` package.
- **[Game flow](../wiki/architecture/game-flow.md)** (the end of the game), **[system overview](../wiki/architecture/system-overview.md)** (the `sim` part, built).
- **[Scoring and end of game](../wiki/rules/scoring-and-end-of-game.md):** `built`.
- **[Balance and design history](../wiki/rules/balance-and-history.md):** the Java simulation's results.
- **[Local development](../wiki/operations/local-development.md):** `--simulate`.
- **Requirements:** FR-14 and NFR-06 move to `built`; FR-16 keeps a note that its drawing is plan 008; the [catalogue](../wiki/requirements/catalogue.md).
- **[Build progress](../wiki/operations/build-progress.md)** (including issue 4's answer), `index.md`, `log.md`, `src/AGENTS.md`.

## Verification

1. `.\mvnw.cmd verify` passes, with coverage at 80% or more; `--check` still passes.
2. `java -jar target\net-zero-belfast.jar --simulate 1000` prints a report; the same command twice gives the same numbers.
3. Deliberate bugs, each caught by the tests:
   - repayments not deducted;
   - money counted in a missed game;
   - the Green Projects tie-break reversed;
   - shares counted at the end only.

## What Oscar should be able to explain

- The two scoring formulas and why money is worthless if Belfast misses net zero (the core lesson, LO3).
- Why scores are stored multiplied by five, and how that keeps 36.4 from losing to a rounded 36.
- How the tie-breaks guarantee exactly one winner.
- Why a player's share counts every round they polluted, not just the end.
- What the computer players are, how they're kept from breaking rules, and what the balance report can and can't tell you (bots aren't people).

## Risks

| Risk | What we'll do |
|---|---|
| The Java simulation's numbers differ from the Python one's | Expected: Java plays the real board, junctions, cards and rules, and its bots differ. Record both side by side with the reasons; don't change balance values in this plan |
| The bots are too simple to show real balance problems | The report states it's a guide, not a playtest. The strategies follow the Python simulation's, so the two can be compared |
| `--simulate 1000` is slow | One game takes well under 10 ms headless, so 1,000 games is a few seconds; measure it and note it |

## Outcome

**Built 18 November 2026.** `mvnw clean verify` passes with no warnings: 295 tests (27 new). Line coverage is 99.4% for `model` (873 of 878 lines), 99.1% for `config` and 99.0% for the new `sim` package (189 of 191), which is now in the coverage rule. `--check` still passes. `--simulate 1000` takes 0.6 seconds and prints the same report every time.

### What was built

| Path (`src/main/java/netzerobelfast/`) | What it is |
|---|---|
| `model/LegacyScore` | The score × the money divisor, compared exactly; `display()` to one decimal place |
| `model/GameResult` | Net zero, final meter, ranking (`PlayerScore`), winner, `TieBreak`, roll-off dice, meter history, `MeterLine`s, polluting shares |
| `model/Scoring` | Both formulas, the ranking and every tie-break, including a die roll-off that re-rolls ties |
| `model/Game`, `Player` | `result()` set on entering `GAME_OVER`; polluting-square-rounds counted in step 2; `hasConvertedThisTurn()` |
| `sim/Strategy`, `Strategies` | The interface and three strategies: random (with a lean), green, polluter |
| `sim/Simulator`, `BalanceReport` | One seeded game; many games summarised |
| `App` | `--simulate [games]` (1,000 by default, fixed seed 2026) |

### Checking the tests catch mistakes

| Bug | Tests that failed |
|---|---|
| Loan repayments not deducted | 1: FR-14 AC-1 |
| Money counted in a missed game | 6: AC-2, every tie-break test, negative scores, the report's scores |
| Green Projects tie-break reversed | 1: AC-4 |
| Shares counted at the end only | 1: FR-16 AC-2's late-conversion test |

### Balance results (5,000 simulated games)

| Measure | Java (this plan) | Python (handover) |
|---|---|---|
| Net zero: mostly green / mixed / mostly polluting | 58% / 19% / 0% | 73% / 54% / 13% |
| Character win rates (fair share about 20%) | 18-22%; Farmer highest at 22% | 17-24%; Farmer highest at 24% |
| Green strategy wins | 40% | 17% ("only ever go green") |
| Polluting strategy wins | 0% | 13% ("pollute, then convert at the end") |
| Games with a bankruptcy | 3% | Not reported |
| Disasters per game | 7.2 | Not reported |

What this suggests, and why the two differ, is on [balance and design history](../wiki/rules/balance-and-history.md#java-simulation-plan-006). It answers issue 4: bankruptcy happens in about 3 games in 100. No balance value was changed; that's Oscar's call (Q-10).

### What differed from the plan

1. **`Game.hasConvertedThisTurn()` is new and public**, so the green strategy can buy a Polluting square only when it can convert it the same turn. Nothing else in the model changed for the computer players.
2. **Computer players answer quizzes correctly 70% of the time**, as the Python bots did, by checking each answer against the card.
3. **The polluter strategy never converts**, so it isn't the Python "pollute, then convert at the end" exploit bot. That bot is a possible later addition if Oscar wants to test the exploit directly.
4. **Each simulated player takes at most two actions a turn** (a landing action and a conversion), the most the rules allow.
5. **`Game.dice()` is package-private**, so `Scoring` uses the game's seeded dice for the roll-off.
