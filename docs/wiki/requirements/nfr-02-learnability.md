---
title: NFR-02 Learnability
type: requirement
id: NFR-02
priority: must
status: draft
sources: [../sources/sub1a-report.md, ../sources/design-diagrams.md]
updated: 2026-11-17
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

## Open questions for Oscar

1. Tutorial format: a few illustrated screens, or a guided first turn?

## Related

- [UX: screens and layout](../ux/screens-and-layout.md)
- [NFR-03 Readability](nfr-03-readability.md)
