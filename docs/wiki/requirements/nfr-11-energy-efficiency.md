---
title: NFR-11 Energy efficiency
type: requirement
id: NFR-11
priority: could
status: built
sources: [../sources/sub1a-report.md]
updated: 2026-11-18
---

# NFR-11 Energy efficiency

## Summary

> Energy efficiency: while waiting for input, the game shall use under 5% CPU on a lab PC (no busy-waiting loops). *(SUB1a; verified by profiling)*

## Acceptance criteria

- **AC-1** With the game screen open and no input for 60 seconds, average CPU use is under 5% (Task Manager or Activity Monitor).
- **AC-2** No loop waits by polling; the UI is event-driven, and any animation timer stops when the animation ends.

## Decisions (Oscar, 18 November 2026)

1. **Agreed.**

## Build status

Built in [plan 007](../../plans/007-game-screens.md): AC-2, the UI only acts on Swing events and modal dialogs, with no timers or loops, and `LayoutAndTutorialTest.nfr11_ac2_noPollingInUi` fails the build if `Thread.sleep`, `while (true)` or a timer appears in `ui`. AC-1: on 18 November 2026 the title screen used 0.4 CPU-seconds in 10 idle seconds, about 4% of one of the laptop's 12 cores, or about 0.3% of the whole processor. Oscar repeats this on the game screen in his play-through.

## Related

- [NFR-04 Responsiveness](nfr-04-responsiveness.md)
- [Learning outcomes: in the software](../learning-outcomes.md)
