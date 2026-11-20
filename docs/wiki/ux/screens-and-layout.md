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

## As built (plans 007 and 008)

Plan 007 built the screens; [plan 008](../../plans/008-visual-polish.md) gave them a dark game look, the Inter font and light animation, at Oscar's request ("more sleek and like an actual online game", 18 November 2026). What each screen shows didn't change.

- **Window:** at least 1280×720 (Oscar, 18 November 2026), resizable and can be maximised. The board scales to fit.
- **Look:** a deep navy page, slate cards with soft shadows, pill-shaped buttons (the main action in teal-green), and the Inter font, bundled in the jar. Pop-ups are rounded cards with a header coloured by kind (quiz blue, choice amber, event violet, round report teal, disaster red) over a dimmed game.
- **Title screen:** a painted night skyline of Belfast with two harbour gantry cranes, and a band of light in the meter's colours.
- **Board:** the loop round a 10 × 7 grid, clockwise from the top-left, and the City Centre route on the middle row between the junctions (`BoardLayout`, from the order in `board.json`). Each tile has its type's colour band and icon, its name, and its price or owner (a badge in the owner's seat colour with their number). On a tall tile the type is also written; on a small one (1280×720) the icon alone shows it, and the tooltip says it in words. Junctions have a brighter border and ★; "Lobbied" and "Closed" show in red. The current player's square glows, and the tile under the mouse is outlined. Counters are shaded discs with the seat number; the current player's has an amber ring.
- **Top bar:** the logo, "Round N of 12 · Year YYYY", and the meter: a rounded bar in the band colours, a glowing marker, a dashed line and label for the next target, and a chip such as "56 · band 4 (severe)".
- **Player panel:** a card per player with an avatar in their seat colour and number, name and character, money, GP, green and polluting squares (with the board's icons), and Resilience and co-funding deal. The current player's card glows and has a "YOUR TURN" chip; a bankrupt player's has a "BANKRUPT" chip and says how to return.
- **Action bar:** what the game is waiting for, where the player is and what they rolled, then one button per offered action with its cost (for example "Buy Harbour Gas Plant £7M (B)"), and End turn, shown but disabled until the move is done.
- **Pop-ups:** quiz and choice cards (an option the player can't afford is plain text marked "(can't afford)"), card results with the fact and source, the round report in step order with the disaster's explanation and source, return rolls, and errors. They sit over the board, so the top bar stays visible.
- **Final scores:** the winner, the outcome, the scoring rule and the ranking on one card, until plan 009's report.

### Animation (plan 008)

| What | How long | Notes |
|---|---|---|
| Dice tumble, then "Rolled N" | 0.45 s | The faces shown while tumbling are only for show; the real roll is already in the model |
| Counter slides along its path, with a small hop each square | 70 ms a square, at least 0.28 s, at most 1 s | Follows the real path, including the City Centre; a Glider ride goes straight there |
| Meter marker eases to its new value | 0.4 s | The chip counts with it |
| Hover on buttons and tiles | instant | No timer |

One `Animator` owns the only timer and stops it when the last animation ends. Any key or click skips to the end. A pop-up waits for the counter to arrive (through a secondary event loop, not a busy-wait), and while it waits, keys and clicks only skip.

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
| Any key, or a click | Skip an animation |

### Colour tokens

Every colour is a named token in `ui/Palette.java`, which also lists each text and background pair actually drawn; `PaletteTest` checks every pair is at least 4.5:1 (the lowest is the disabled button text at 4.9:1). Band, seat and tile colours never carry meaning on their own.

## Rules for the UI

1. **Never colour alone** ([NFR-05](../requirements/nfr-05-accessibility.md)). Squares carry a type icon, and a label where there's room; the meter shows its band as a number and a word; the current player has a "YOUR TURN" chip.
2. **Contrast** at least 4.5:1; colours defined once as named tokens.
3. **Show effects before a choice**, and the fact after (wireframe callout 4).
4. **Plain language** for ages 13-16 ([NFR-03](../requirements/nfr-03-readability.md)).
5. **Event-driven, no polling** ([NFR-11](../requirements/nfr-11-energy-efficiency.md)). The only timer is the `Animator`'s, and it stops when nothing is moving.
6. **Built in Swing** ([ADR-002](../../decisions/002-ui-toolkit.md)). The board and the meter graph are custom-painted panels, so they follow these rules by design rather than by a theme.

## Conflicts or uncertainties

- The wireframe shows a sample state (Round 4, 2032, meter 33, band 3) and placeholder players; not data.
- The wireframe has no Resilience, Glider or Draw Card buttons; they'd appear when on those squares.
- Minimum window size: 1280×720, decided by Oscar on 18 November 2026.
- After round 8 the meter marks "2050 net zero 0" as its next target. FR-15 AC-2 only names the two interim targets; this keeps a target in view to the end.

## Related

- [ADR-002 UI toolkit](../../decisions/002-ui-toolkit.md)
- [FR-16 End-of-game report](../requirements/fr-16-end-of-game-report.md)
