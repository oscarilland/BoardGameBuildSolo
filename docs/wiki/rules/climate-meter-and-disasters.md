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

1. Starts at 50 (`climateMeter.start`). Updated once per round by the formula on [end of round](end-of-round.md), and immediately by Choice cards.
2. **Net zero** is reached if the meter is at 0 or below after round 12 (`netZeroAtOrBelow: 0`).
3. **Interim targets** (`interimTargets`): round 3 (2030) at most 26, a 48% cut from 50 under the Climate Change Act (NI) 2022. Round 8 (2040) at most 11, which assumes a 77% cut ([Q-04](open-questions.md)).

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

- **No owned squares** (likely in round 1): a Flood has no district to choose and a Storm has no square. The simulation skips the disaster; the rules don't say whether to pick another type instead ([Q-17](open-questions.md)).
- A Heatwave charges bankrupt players too, but their money is already £0 and can't go lower, so it has no effect on them.
- Choice cards move the meter immediately, so the band can change mid-round. Only the end-of-round band matters for disasters.

## Conflicts or uncertainties

- Whether the meter can go below 0 ([Q-02](open-questions.md)) and its real unit ([Q-03](open-questions.md)).
- The official 2040 target ([Q-04](open-questions.md)).
- **No explanation text exists** for FR-17 in any data file ([Q-11](open-questions.md)).

## Related

- [FR-12 Disasters](../requirements/fr-12-disasters.md)
- [FR-15 Always-visible status](../requirements/fr-15-always-visible-status.md)
- [FR-17 Disaster explanation](../requirements/fr-17-disaster-explanation.md)
- [Learning outcomes](../learning-outcomes.md)
