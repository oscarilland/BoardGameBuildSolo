---
title: End of round
type: rules
status: built
sources: [../sources/handover.md, ../sources/sub1a-report.md, ../sources/data-files.md]
updated: 2026-11-17
---

# End of round

## Summary

After the last player's turn, six steps run in a fixed order. **The order is a requirement** ([FR-11](../requirements/fr-11-end-of-round-order.md)), because each step changes what the next one sees.

> **Build status:** built in `Game.endRound()` ([plan 005](../../plans/005-end-of-round-disasters-and-bankruptcy.md)). The end-of-round screen comes in plan 007.

## Rules

1. **Income and GP.** For every owned square and co-funding deal:
   - Polluting: +£7M to the owner (`polluting.incomePerRound`).
   - Green Project: +£2M and +2 GP to the owner (`greenProject.incomePerRound`, `gpPerRound`). Farmer: +1 GP more per nature project. Industrialist-converted: +£2M more.
   - Co-funding deal: +1 GP to the funder and +1 GP to the owner.
   - **Skip** squares that are closed (Storm) or flooded (Flood): they earn no money and no GP, and co-funding deals on them pay neither player ([Q-15](open-questions.md)). **Lobbied** squares only lose their money.
   - Nature projects get **no GP** in a Drought round.
   - **Bankrupt players receive no money, but still get GP.**
2. **Carbon tax:** each owner loses 1 GP per Polluting square they own (`polluting.carbonTaxGPPerRound`).
3. **Climate Meter:** `meter += 3 + 2 × (owned Polluting) − 1 × (owned Green Projects)`, where Polluting squares lobbied by the Activist this round don't count (`climateMeter.basePerRound`, `polluting.meterPerRound`, `greenProject.meterPerRound`). Record the new value in the meter's history.
4. **Interim target**, only in round 3 (2030) and round 8 (2040): if the meter is **above** the target (26 in 2030, 11 in 2040), every Polluting square costs its owner 2 GP (`climateMeter.interimTargets`, `interimMissPenaltyGPPerPolluting`).
5. **Disaster check:** roll one die; a disaster happens if the roll ≤ the danger band. Pick one of four types at random. Costs are paid **now**; lost income and closures apply **next** round. See [Climate Meter and disasters](climate-meter-and-disasters.md).
6. **Advance:** year += 2, round += 1. After round 12 (the year reaches 2050), the game ends ([scoring](scoring-and-end-of-game.md)).

## Edge cases

- **Lobbied squares** earn no money, but the rules say nothing about removing their carbon tax or (except for the Activist) their meter rise. Both still apply.
- A Storm or Flood applied in this round's step 5 affects **next** round's step 1. Closures and floods from the previous round are cleared after this round's step 1 has used them.
- A Drought rolled in step 5 affects next round's nature GP.
- Closed and flooded squares still pay carbon tax and still count towards the meter: emissions don't stop when a square is shut ([Q-15](open-questions.md)).
- The meter can go below 0; there's no floor ([Q-02](open-questions.md)).

## Conflicts or uncertainties

- None left. Q-02 and Q-15 were answered by Oscar on 17 November 2026.

## Related

- [FR-11 End-of-round order](../requirements/fr-11-end-of-round-order.md)
- [Game flow: End Round](../architecture/game-flow.md)
- [End Round sequence diagram](../../raw/net-zero-belfast-handover/design/seq_end_round.png)
