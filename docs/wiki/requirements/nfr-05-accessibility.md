---
title: NFR-05 Accessibility
type: requirement
id: NFR-05
priority: should
status: draft
sources: [../sources/sub1a-report.md, ../sources/design-diagrams.md]
updated: 2026-11-17
---

# NFR-05 Accessibility

## Summary

> Accessibility: text contrast shall meet WCAG 2.1 AA (at least 4.5:1), and every square type shall be identifiable by a label as well as by colour. *(SUB1a; verified by inspection)*

## Acceptance criteria

- **AC-1** Every text and background colour pair in the UI has a contrast ratio of at least 4.5:1 (3:1 for large text). The colours live in one place, and a test computes every pair's ratio.
- **AC-2** Every square shows its type in text or a distinct icon (for example "Polluting", "Green", a leaf for nature), so the board can be read in greyscale.
- **AC-3** The meter's danger band is shown as a number and a word, not only by the bar's colour.
- **AC-4** Every action can be done with the keyboard alone.

## Open questions for Oscar

1. AC-4 goes beyond the SUB1a wording. Keep it?

## Related

- [UX: screens and layout](../ux/screens-and-layout.md)
- [FR-15 Always-visible status](fr-15-always-visible-status.md)
