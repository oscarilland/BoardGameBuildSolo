---
title: Data files
type: architecture
status: draft
sources: [../sources/data-files.md]
updated: 2026-11-17
---

# Data files

## Summary

Three JSON files hold every balance value, the board, and the card deck ([NFR-07](../requirements/nfr-07-data-driven-balance.md)). The handover's copies in `docs/raw/` stay as received. Plan 001 copies them into the game's resources, and from then on **only the working copies are edited**.

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
- **Validate on load:** 36 squares with unique ids 0-35; the type counts (10, 12, 4, 3, 7); prices within `priceRange`; junctions and Glider stops exist; 6 distinct characters; 30 cards with unique ids, valid `correctIndex`, two options for Choice cards, and at least one source each; danger bands ascending.
- How the JSON is parsed: [ADR-004](../../decisions/004-reading-the-json-data-files.md).

## Known gaps

- No disaster explanation text ([Q-11](../rules/open-questions.md)).
- Quiz reward in two places ([Q-13](../rules/open-questions.md)).
- `_note` keys hold comments; the loader should ignore keys starting with `_`.

## Failure modes

A missing key, wrong type, or count mismatch stops start-up with the file name, the key and what was expected.

## Explain it in 30 seconds

*Draft for Oscar to reword.* Every number in the game lives in three JSON files: the rules' numbers, the board and the cards. At start-up one class loads and checks them, and the rest of the game asks it for values. To re-balance after a playtest you edit a file, not the code.

## Related

- [System overview](system-overview.md)
- [Climate cards](../rules/climate-cards.md)
