---
title: FR-15 Always-visible status
type: requirement
id: FR-15
priority: must
status: draft
sources: [../sources/sub1a-report.md, ../sources/design-diagrams.md]
updated: 2026-11-17
---

# FR-15 Always-visible status

## Summary

> The system shall always show the year, round, Climate Meter value and danger band, and each player's money, GP and squares. *(SUB1a; verified manually)*

## Behaviour

The game screen's top bar shows "Round N of 12 · Year YYYY" and the Climate Meter as a coloured bar with its value, its band, and the 2030 target marker. The player panel lists every player's name, character, money, GP and squares (green and polluting counts), with the current player highlighted. See [screens and layout](../ux/screens-and-layout.md).

## Acceptance criteria

- **AC-1** On the game screen, the year, round, meter value and band are visible at all times, including while a card or disaster pop-up is open.
- **AC-2** The meter bar marks the next interim target (26 until round 3, then 11 until round 8).
- **AC-3** Every player's money, GP and number of green and polluting squares are visible, and update immediately after each action and at the end of each round.
- **AC-4** The current player is highlighted by more than colour alone (for example a border and "(turn)") ([NFR-05](nfr-05-accessibility.md)).
- **AC-5** A bankrupt player is visibly marked as bankrupt.

## Open questions for Oscar

1. Also show each player's Resilience level and co-funding deal for everyone, or only the current player (as in the wireframe)?
2. The meter's display unit ([Q-03](../rules/open-questions.md)).

## Related

- [UX: screens and layout](../ux/screens-and-layout.md)
- [Learning outcomes: LO1](../learning-outcomes.md)
