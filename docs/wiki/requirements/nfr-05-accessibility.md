---
title: NFR-05 Accessibility
type: requirement
id: NFR-05
priority: should
status: built
sources: [../sources/sub1a-report.md, ../sources/design-diagrams.md]
updated: 2026-11-18
---

# NFR-05 Accessibility

## Summary

> Accessibility: text contrast shall meet WCAG 2.1 AA (at least 4.5:1), and every square type shall be identifiable by a label as well as by colour. *(SUB1a; verified by inspection)*

## Acceptance criteria

- **AC-1** Every text and background colour pair in the UI has a contrast ratio of at least 4.5:1 (3:1 for large text). The colours live in one place, and a test computes every pair's ratio.
- **AC-2** Every square shows its type in text or a distinct icon (for example "Polluting", "Green", a leaf for nature), so the board can be read in greyscale.
- **AC-3** The meter's danger band is shown as a number and a word, not only by the bar's colour.
- **AC-4** Every action can be done with the keyboard alone.

## Decisions (Oscar, 18 November 2026)

1. **AC-4 kept**, although it goes beyond the SUB1a wording: every button can be reached with Tab and pressed with Enter or Space, and the common actions have shortcut keys (for example R to roll, E to end turn).
2. **Agreed.**

## Build status

Built in [plan 007](../../plans/007-game-screens.md): AC-1, every colour is a token in `Palette`, and `PaletteTest` checks every text and background pair; AC-2, every square shows a type label (`StatusText.typeLabel`); AC-3, the band is written as a number and a word, such as "band 3 (high)"; AC-4, every button can be reached with Tab and pressed with Enter or Space, with shortcut keys listed on [screens and layout](../ux/screens-and-layout.md#keys-nfr-05-ac-4) and checked for clashes in `ActionOptionsTest`. A choice option the player can't afford is shown as plain text rather than a disabled button, because Swing greys disabled wrapped text too faintly. Keyboard-only play is part of Oscar's play-through.

## Related

- [UX: screens and layout](../ux/screens-and-layout.md)
- [FR-15 Always-visible status](fr-15-always-visible-status.md)
