---
title: Screens and layout
type: ux
status: draft
sources: [../sources/design-diagrams.md, ../sources/sub1a-report.md]
updated: 2026-11-17
---

# Screens and layout

## Summary

Four screens and two kinds of pop-up, from the team's [wireframe and screen flow](../../raw/net-zero-belfast-handover/design/main_screen_wireframe.png). The main game screen keeps the Climate Meter in view at all times, so every decision's effect is seen at once.

## Screen flow

`Title (Start · How to play)` → `Setup (names · characters)` → `Game screen (board · meter · actions)` ⇄ `Card or disaster pop-ups` → `End-of-game report (meter graph · emissions · winner)`

## Game screen

| Area | Content | Requirement |
|---|---|---|
| Top bar | "Net Zero Belfast", "Round N of 12 · Year YYYY", the Climate Meter bar with its value and band, and the interim target marker | [FR-15](../requirements/fr-15-always-visible-status.md), LO1, LO5 |
| Board | The 36 squares in the [board layout](../../raw/net-zero-belfast-handover/design/board_layout.png), with player counters | [FR-03](../requirements/fr-03-dice-movement-and-junctions.md) |
| Player panel | Each player: name, character, money, GP, green and polluting counts; current player highlighted; the current player's Resilience level and co-funding deal | [FR-15](../requirements/fr-15-always-visible-status.md) |
| Action bar | Roll dice · Buy £N · Co-fund £3M · Lobby £2M · Convert £5M · End turn. Only valid actions are enabled | [NFR-02](../requirements/nfr-02-learnability.md), FR-04 to FR-08 |
| Pop-ups | Cards: text, then options with their effects shown before choosing, then fact and source. Disasters: what happened, costs, the climate explanation | [FR-10](../requirements/fr-10-climate-cards.md), [FR-17](../requirements/fr-17-disaster-explanation.md) |

## Rules for the UI

1. **Never colour alone** ([NFR-05](../requirements/nfr-05-accessibility.md)). Squares carry a type label; the meter shows its band as a number; the current player has a border and "(turn)".
2. **Contrast** at least 4.5:1; colours defined once as named tokens.
3. **Show effects before a choice**, and the fact after (wireframe callout 4).
4. **Plain language** for ages 13-16 ([NFR-03](../requirements/nfr-03-readability.md)).
5. **Event-driven, no polling** ([NFR-11](../requirements/nfr-11-energy-efficiency.md)).
6. **Built in Swing** ([ADR-002](../../decisions/002-ui-toolkit.md)). The board and the meter graph are custom-painted panels, so they follow these rules by design rather than by a theme.

## Conflicts or uncertainties

- The wireframe shows a sample state (Round 4, 2032, meter 33, band 3) and placeholder players; not data.
- The wireframe has no Resilience, Glider or Draw Card buttons; they'd appear when on those squares.
- Minimum window size isn't defined. The lab PC screens' resolution is unknown; suggest designing for 1366×768 and up.

## Related

- [ADR-002 UI toolkit](../../decisions/002-ui-toolkit.md)
- [FR-16 End-of-game report](../requirements/fr-16-end-of-game-report.md)
