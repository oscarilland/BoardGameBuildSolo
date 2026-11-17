---
title: NFR-11 Energy efficiency
type: requirement
id: NFR-11
priority: could
status: draft
sources: [../sources/sub1a-report.md]
updated: 2026-11-17
---

# NFR-11 Energy efficiency

## Summary

> Energy efficiency: while waiting for input, the game shall use under 5% CPU on a lab PC (no busy-waiting loops). *(SUB1a; verified by profiling)*

## Acceptance criteria

- **AC-1** With the game screen open and no input for 60 seconds, average CPU use is under 5% (Task Manager or Activity Monitor).
- **AC-2** No loop waits by polling; the UI is event-driven, and any animation timer stops when the animation ends.

## Related

- [NFR-04 Responsiveness](nfr-04-responsiveness.md)
- [Learning outcomes: in the software](../learning-outcomes.md)
