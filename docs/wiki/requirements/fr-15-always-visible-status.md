---
title: FR-15 Always-visible status
type: requirement
id: FR-15
priority: must
status: built
sources: [../sources/sub1a-report.md, ../sources/design-diagrams.md]
updated: 2026-11-18
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
- **AC-6** Every player's card also shows their Resilience level and, if they have one, their co-funding deal (for example "Resilience 2 · Funds: Rooftop Solar").

## Decisions (Oscar, 17 and 18 November 2026)

1. The meter has no unit; it's labelled "Climate Meter (2026 = 50)", and can show values below 0 ([Q-03](../rules/open-questions.md), [Q-02](../rules/open-questions.md)).
2. **Every player's** Resilience level and co-funding deal are shown, compactly on each player's card, not only the current player's as in the wireframe (18 November). Rivals' protection and deals matter for co-funding and lobbying choices. This adds **AC-6**.
3. **Agreed** (18 November).

## Build status

Built in [plan 007](../../plans/007-game-screens.md): the top bar, `MeterBar`, the player panel and `StatusText`. AC-1 to AC-6 are tested in `StatusTextTest` (the text) and `ScreensTest` (every screen builds and paints through a whole game). After round 8 the bar marks "2050 net zero 0", so a target stays in view. The SUB1a "verified manually" check is Oscar's play-through, a gate in [build progress](../operations/build-progress.md).

## Related

- [UX: screens and layout](../ux/screens-and-layout.md)
- [Learning outcomes: LO1](../learning-outcomes.md)
