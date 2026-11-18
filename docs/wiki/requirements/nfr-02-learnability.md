---
title: NFR-02 Learnability
type: requirement
id: NFR-02
priority: must
status: agreed
sources: [../sources/sub1a-report.md, ../sources/design-diagrams.md]
updated: 2026-11-18
---

# NFR-02 Learnability

## Summary

> Learnability: at least 4 of 5 first-time players shall complete their first turn without help after a tutorial of 2 minutes or less. *(SUB1a; verified by user test)*

## Behaviour

The title screen has "How to play" (a tutorial of 2 minutes or less). On the game screen only actions valid on the current square are enabled, and each button names its cost (for example "Buy £9M").

## Acceptance criteria

- **AC-1** The "How to play" tutorial can be read or clicked through in 2 minutes or less.
- **AC-2** At any moment, only actions the current player can legally take and afford are enabled.
- **AC-3** In a user test with 5 first-time players, at least 4 complete their first turn without help.

## Decisions (Oscar, 18 November 2026)

1. **Tutorial format:** 4-5 short illustrated screens reached from the title screen (the goal, a turn, the squares, the Climate Meter, how you win), not a guided first turn. Any numbers in them come from the data files, so they stay right after a re-tune.
2. **Agreed.**

## Build status

Built in [plan 007](../../plans/007-game-screens.md): AC-1, five pages from `HowToPlay`, 279 words in all (under 1.5 minutes at 200 words a minute), with every number read from the data (`LayoutAndTutorialTest`); AC-2, the action bar shows one button per action in `availableActions()`, so only legal, affordable actions appear, and End turn is disabled until the move is done (`ActionOptionsTest`). AC-3, the user test, is a Phase 3 gate, so the page stays `agreed`.

## Related

- [UX: screens and layout](../ux/screens-and-layout.md)
- [NFR-03 Readability](nfr-03-readability.md)
