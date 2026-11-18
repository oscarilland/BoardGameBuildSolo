---
title: Screens and layout
type: ux
status: built
sources: [../sources/design-diagrams.md, ../sources/sub1a-report.md]
updated: 2026-11-18
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
| Player panel | Each player: name, character, money, GP, green and polluting counts; current player highlighted; every player's Resilience level and co-funding deal, compactly (Oscar, 18 November 2026) | [FR-15](../requirements/fr-15-always-visible-status.md) |
| Action bar | Roll dice · Buy £N · Co-fund £3M · Lobby £2M · Convert £5M · End turn. Only valid actions are enabled | [NFR-02](../requirements/nfr-02-learnability.md), FR-04 to FR-08 |
| Pop-ups | Cards: text, then options with their effects shown before choosing, then fact and source. Disasters: what happened, costs, the climate explanation | [FR-10](../requirements/fr-10-climate-cards.md), [FR-17](../requirements/fr-17-disaster-explanation.md) |

## As built (plan 007)

- **Window:** at least 1280×720 (Oscar, 18 November 2026), resizable and can be maximised. The board scales to fit.
- **Board:** the loop round a 10 × 7 grid, clockwise from the top-left, and the City Centre route on the middle row between the junctions (`BoardLayout`, from the order in `board.json`). Each square shows its name, a type label, and its price or "Owner N"; junctions have a thick border and ★; "Lobbied" and "Closed" marks show in red. Counters are discs with the player's seat number; the current player's has a ring. A tooltip gives the full details.
- **Player panel:** a card per player with seat, name, character, "(turn)", money, GP, green and polluting counts, Resilience level and co-funding deal; the current player's card has a thick border, and a bankrupt player's says BANKRUPT.
- **Action bar:** what the game is waiting for, where the player is and what they rolled, then one button per offered action with its cost (for example "Buy Harbour Gas Plant £7M (B)"), and End turn, shown but disabled until the move is done.
- **Pop-ups:** quiz and choice cards (an option the player can't afford is shown as plain text marked "(can't afford)"), card results with the fact and source, the round report in step order with the disaster's explanation and source, return rolls, and errors. They sit over the board, so the top bar stays visible.
- **Final scores:** a plain screen with the ranking and winner, until plan 008's report.

### Keys (NFR-05 AC-4)

| Key | Does |
|---|---|
| Tab, Shift+Tab | Move between buttons; the focused button has an amber ring |
| Enter or Space | Press the focused button |
| R | Roll dice, or roll to return from bankruptcy |
| B, C, L, V, P, G | Buy, co-fund, lobby, convert, protect (Resilience), Glider (the first of each kind) |
| E | End turn |
| 1, 2 | Route or direction choice at a junction; in a quiz, 1-4 choose an answer |
| A, B | A choice card's options |
| O | Reopen a card question |
| Left, Right, Escape | Turn the How to play pages; leave it |

### Colour tokens

Every colour is a named token in `ui/Palette.java`, which also lists each text and background pair; `PaletteTest` checks every pair is at least 4.5:1. Band and counter colours never carry meaning on their own.

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
- Minimum window size: 1280×720, decided by Oscar on 18 November 2026.
- After round 8 the meter marks "2050 net zero 0" as its next target. FR-15 AC-2 only names the two interim targets; this keeps a target in view to the end.

## Related

- [ADR-002 UI toolkit](../../decisions/002-ui-toolkit.md)
- [FR-16 End-of-game report](../requirements/fr-16-end-of-game-report.md)
