---
title: Data files
type: architecture
status: built
sources: [../sources/data-files.md]
updated: 2026-11-17
---

# Data files

## Summary

Three JSON files hold every balance value, the board, and the card deck ([NFR-07](../requirements/nfr-07-data-driven-balance.md)). The handover's copies in `docs/raw/` stay as received. The game reads its **working copies in `src/main/resources/data/`**, packaged inside the jar ([plan 001](../../plans/001-walking-skeleton.md)). Only the working copies are edited.

**Changes to the working copies so far:**

| File | Key | Change | Why |
|---|---|---|---|
| `board.json` | `cityCentreRoute.direction` | "TBD: …" → "two-way (Q-05, decided by Oscar on 17 Nov 2026)" | [Q-05](../rules/open-questions.md) |

## Files

| File | Raw copy | Holds |
|---|---|---|
| `config.json` | [raw](../../raw/net-zero-belfast-handover/data/config.json) | Game length, characters, square economics, actions, Resilience, Climate Meter, disasters, bankruptcy, scoring |
| `board.json` | [raw](../../raw/net-zero-belfast-handover/data/board.json) | Districts, the loop, the City Centre route, junctions, Glider stops, 36 squares |
| `climate_cards.json` | [raw](../../raw/net-zero-belfast-handover/data/climate_cards.json) | 30 cards with effects, facts and sources |

## Schemas (as delivered)

### config.json

| Section | Keys |
|---|---|
| `game` | `minPlayers` 4, `maxPlayers` 6, `rounds` 12, `startYear` 2026, `yearsPerRound` 2, `endYear` 2050, `turnsPerRound` 1, `firstPlayer` "random", `dice` "2d6" |
| `characters[]` | `id`, `name`, `startMoney`, `startGP`, `ability` (text), `params` (ability-specific numbers) |
| `squares.polluting` | `priceRange` [4, 7], `incomePerRound` 7, `carbonTaxGPPerRound` 1, `meterPerRound` 2 |
| `squares.greenProject` | `priceRange` [8, 12], `incomePerRound` 2, `gpPerRound` 2, `meterPerRound` −1 |
| `squares.convert`, `coFunding`, `lobbying`, `glider` | Costs and limits |
| `squares.resilience` | `levelCosts` [3, 5, 7], `disasterCostCut` [0.25, 0.5, 0.75], `maxLevel` 3 |
| `squares.climateCard` | `quizCorrectGP` 2, `choiceMeterChangeTiming` "immediate" |
| `climateMeter` | `start` 50, `basePerRound` 3, `netZeroAtOrBelow` 0, `interimTargets[]` (year, round, maxMeter), `interimMissPenaltyGPPerPolluting` 2, `dangerBands[]` (band, maxMeter, last is `null`), `belowZeroUsesBand` 1 |
| `disasters` | `FLOOD`, `STORM`, `HEATWAVE`, `DROUGHT` with their per-band costs and durations |
| `bankruptcy` | `escapeRoll` 6, `maxRounds` 2, `loanOnReturn` 10, `repayAtEnd` 15 |
| `scoring` | `missedPollutingPenaltyGP` 5, `moneyDivisor` 5, plus text descriptions |

### board.json squares

`id`, `name`, `type` (`POLLUTING`, `GREEN_PROJECT`, `RESILIENCE`, `GLIDER_STOP`, `CLIMATE_CARD`), `district`, and for ownable squares `price` and (green only) `isNature`.

### climate_cards.json cards

`id`, `type` (`QUIZ`, `CHOICE`, `EVENT`), `text`, then by type: `answers[4]`, `correctIndex` and `reward.gp`; or `options[2]` each with `label`, `money`, `gp`, `meter`; or `effect` with `money`, `gp`, `meter`. All have `fact` and `sources[]` (`title`, `url`).

## Key design choices

- **Text keys are documentation, not rules.** Strings like `scoring.netZero` or `disasters.roll` describe a rule; the code implements the rule and reads only the numbers.
- **Loading, as built** (`netzerobelfast.config`):
  1. Gson reads each file into a JSON tree in **strict** mode: unquoted keys, comments and trailing text are errors.
  2. `ConfigParser` maps the tree to records (`Rules`, `BoardDef`, `CardDef`) through `JsonNode`, which checks every key exists with the right type and knows its path. Keys starting with `_` are skipped.
  3. `ConfigValidator` checks the values make sense together. The first problem stops loading with a `ConfigException` such as `board.json: squares[1].price is 99, expected 4-7 for POLLUTING`.
- **Why not let Gson fill the records directly?** Gson puts 0 or null into a missing field without complaint, and its errors don't name the key ([ADR-004](../../decisions/004-reading-the-json-data-files.md)).
- **Validation rules, as built:**
  - **config.json:** players 1 or more and min ≤ max; rounds and years per round 1 or more, and they reach `endYear`; at least `maxPlayers` characters, unique ids, no negative starting money; price ranges low to high; Resilience costs and cuts match `maxLevel`, with cuts above 0 and at most 1; interim target rounds inside the game; danger bands numbered 1, 2, 3… with rising limits and only the top one open-ended; `belowZeroUsesBand` is a real band.
  - **board.json:** squares listed in id order from 0; each district exists; Polluting and Green Project squares have a price inside their range, other squares have none; `isNature` only on Green Projects; every square on exactly one of the loop and the City Centre route; junctions on the loop, and both `connects` ends are junctions; Glider stops are `GLIDER_STOP` squares, every `GLIDER_STOP` square is listed, and there are at least 2.
  - **climate_cards.json:** at least one card; unique ids; every card has text, a fact, and at least one source with a title and URL; quiz cards have at least 2 answers, a valid `correctIndex` and no negative reward; Choice cards have exactly 2 options, at least one with `money` of 0 or more (Q-12); Event cards have an effect.
- **Counts are tested, not validated.** 36 squares, the 10/12/4/3/7 type split, 30 cards and 6 characters are design facts about the current data, so `GameConfigTest` asserts them rather than the validator enforcing them. Changing the board's size is then a data change plus a test update, not a code change (NFR-07).

## Known gaps

- No disaster explanation text yet; it's added in plan 005 once Oscar approves the lines ([Q-11](../rules/open-questions.md)).
- Quiz reward in two places: `GameConfig.quizReward(card)` uses the card's value, falling back to `quizCorrectGP` ([Q-13](../rules/open-questions.md)).

## Failure modes

A missing key, wrong type, or count mismatch stops start-up with the file name, the key and what was expected.

## Explain it in 30 seconds

*Draft for Oscar to reword.* Every number in the game lives in three JSON files: the rules' numbers, the board and the cards. At start-up one class loads and checks them, and the rest of the game asks it for values. To re-balance after a playtest you edit a file, not the code.

## Related

- [System overview](system-overview.md)
- [Climate cards](../rules/climate-cards.md)
