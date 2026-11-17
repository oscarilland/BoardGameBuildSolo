---
title: Climate Meter and disasters
type: rules
status: draft
sources: [../sources/handover.md, ../sources/rules-v4.md, ../sources/data-files.md]
updated: 2026-11-17
---

# Climate Meter and disasters

## Summary

A single shared meter tracks the city's emissions. It starts at 50, must reach 0 or below by the end for net zero, and its danger band sets both the chance and the cost of disasters.

## Rules

### The meter

1. Starts at 50 (`climateMeter.start`). Updated once per round by the formula on [end of round](end-of-round.md), and immediately by Choice cards. It can go below 0 ([Q-02](open-questions.md)). It has no real-world unit; the screen labels it "Climate Meter (2026 = 50)" ([Q-03](open-questions.md)).
2. **Net zero** is reached if the meter is at 0 or below after round 12 (`netZeroAtOrBelow: 0`).
3. **Interim targets** (`interimTargets`): round 3 (2030) at most 26, a 48% cut from 50 under the Climate Change Act (NI) 2022. Round 8 (2040) at most 11, a 77% cut. **Checked 17 November 2026:** the 77% cut on 1990 levels was set by the Climate Change (2040 Emissions Target) Regulations (NI) 2024, which amend the 2022 Act; the Act itself only required the Department to set a 2040 target ([Q-04](open-questions.md)).

### Danger bands

| Meter | Band | Disaster on a roll of | Data |
|---|---|---|---|
| 10 or below (including below 0) | 1 | 1 | `dangerBands[0].maxMeter: 10`, `belowZeroUsesBand: 1` |
| 11-25 | 2 | 1-2 | `maxMeter: 25` |
| 26-40 | 3 | 1-3 | `maxMeter: 40` |
| 41 and above | 4 | 1-4 | `maxMeter: null` |

The band is also the **cost multiplier**. The band is read after step 3's meter update.

### Disasters

When the die roll is ≤ the band, one of four types is chosen uniformly at random (`disasters.typeChoice`):

| Disaster | Effect | Data |
|---|---|---|
| **Flood** | A random district **that has owned squares** floods. Its squares earn nothing next round. Each owner pays £1M × band for each square they own there | `FLOOD.costPerOwnedSquarePerBand: 1`, `lostIncomeRounds: 1` |
| **Storm** | A random owned square closes next round. Its owner **must** pay £2M × band for repairs | `STORM.repairCostPerBand: 2`, `closedRounds: 1`, `repairMandatory: true` |
| **Heatwave** | Every player pays £1M × band | `HEATWAVE.costPerPlayerPerBand: 1` |
| **Drought** | Nature projects earn no GP next round | `DROUGHT` |

1. Costs are paid immediately and are **compulsory**, so they can cause [bankruptcy](bankruptcy.md).
2. Each player's own Resilience level cuts **their** payment by 25%, 50% or 75% ([board and squares](board-and-squares.md#resilience)).
3. Every disaster shows a one-line explanation of how it links to climate change ([FR-17](../requirements/fr-17-disaster-explanation.md)).

## Edge cases

- **No owned squares** (likely in round 1): a Flood has no district to choose and a Storm has no square, so the disaster is **skipped** that round, as in the simulation ([Q-17](open-questions.md)).
- A Heatwave charges bankrupt players too, but their money is already £0 and can't go lower, so it has no effect on them.
- Choice cards move the meter immediately, so the band can change mid-round. Only the end-of-round band matters for disasters.

## Disaster explanations (draft for Oscar to approve)

FR-17 needs one line per disaster. No data file had them, so these are drafts ([Q-11](open-questions.md)). Once Oscar approves them, plan 001 adds them to the working `config.json` as `disasters.<TYPE>.explanation` with `sources`. The wording avoids claiming more than the sources say.

| Disaster | Draft line | Source | Checked? |
|---|---|---|---|
| Flood | "As the climate warms, the UK is getting wetter, so heavy rain and floods are more likely." | Met Office, [annual climate stocktake](https://metoffice.gov.uk/about-us/news-and-media/media-centre/weather-and-climate-news/2025/annual-climate-stocktake-shows-weather-records-and-extremes-now-the-norm-in-uk-climate) ("As the UK's climate warms, it is also becoming wetter.") | Yes, 17 November 2026 |
| Storm | "The UK isn't clearly getting stormier, but climate change is making severe weather worse, and warmer air means storms bring more rain." | Same Met Office page ("more severe weather events in the UK due to climate change"; "observations do not currently suggest that the UK is becoming windier or stormier") | Partly: the rain point needs its own source, or cut the line after "worse" |
| Heatwave | "Greenhouse gases are warming the air, so heatwaves like Northern Ireland's record 31.3°C are becoming more likely." | Met Office stocktake ("human emissions of greenhouse gasses are warming the atmosphere"), and the Met Office [climate extremes](https://metoffice.gov.uk/public/weather/climate-extremes) page already used by card Q10 | Yes, 17 November 2026 |
| Drought | "Summers are expected to get hotter and drier, which dries out peat bogs and woodland." | Proposed: Met Office UK climate projections (UKCP18) | **No.** The Met Office is cautious about drought trends; find a source for "drier summers" before approving |

## Conflicts or uncertainties

- The Storm and Drought explanation lines need a source check before Oscar approves them (table above).

## Related

- [FR-12 Disasters](../requirements/fr-12-disasters.md)
- [FR-15 Always-visible status](../requirements/fr-15-always-visible-status.md)
- [FR-17 Disaster explanation](../requirements/fr-17-disaster-explanation.md)
- [Learning outcomes](../learning-outcomes.md)
