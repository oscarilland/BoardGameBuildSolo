---
title: Plan 008 Visual polish
type: plan
id: PLAN-008
status: approved
requirements: [NFR-02, NFR-04, NFR-05, NFR-11, FR-15]
sources: [../wiki/ux/screens-and-layout.md, 007-game-screens.md]
updated: 2026-11-18
---

# Plan 008 - Visual polish

## Goal

After this plan the game looks and feels like a modern online board game rather than a form: a dark theme with glowing, rounded tiles and cards, a bundled font, painted icons, and light animation (dice that tumble, counters that slide along their path, a meter that eases to its new value). Nothing about the rules or what each screen offers changes. Oscar asked for it after trying plan 007 ("make it look better, more sleek and like an actual online game", 18 November 2026).

## Scope

- **Oscar's choices (18 November 2026):** a dark game theme; light animation; hand-painted Swing, with no theme library; a bundled free font.
- **Requirements kept, and re-checked:**
  - [NFR-05](../wiki/requirements/nfr-05-accessibility.md) AC-1 to AC-4: every new colour pair is contrast-tested, colour is still never the only signal, and every action can still be done from the keyboard.
  - [NFR-11](../wiki/requirements/nfr-11-energy-efficiency.md) AC-2: "any animation timer stops when the animation ends". Plan 007's test that bans timers in the UI becomes a test that only the animator uses one, and that it stops.
  - [NFR-04](../wiki/requirements/nfr-04-responsiveness.md) AC-2: the screen still responds at once. An animation starts straight away, lasts at most 1 second, and any key or click skips it.
  - [NFR-02](../wiki/requirements/nfr-02-learnability.md) AC-2 and [FR-15](../wiki/requirements/fr-15-always-visible-status.md): the same information is shown, only styled.
- **Decisions relied on:** [ADR-002](../decisions/002-ui-toolkit.md) (Swing). No new ADR: hand-painted Swing adds no library. The font is a resource file, not code, and its licence is recorded in the [system overview](../wiki/architecture/system-overview.md).
- **Out of scope:** sound; a light theme or theme toggle; the FlatLaf library; the end-of-game report (now [plan 009](#renumbering)); any rule or model change.

## Prerequisites

1. Plan 007 is built. Because plan 008 restyles the same screens, **one play-through after plan 008 covers both plans**, and both are marked `done` together (agreed by Oscar when approving, 18 November 2026).
2. The font: **Inter** (SIL Open Font License 1.1), the static Regular, SemiBold and Bold files with their `OFL.txt`, downloaded once from the official Inter release on GitHub (agreed on approval) and committed under `src/main/resources/fonts/`. The game never uses the network (NFR-08). Inter is designed for screens, and is clear at small sizes on the board.

## Design

**Theme.** `Palette` keeps every colour as a named token and every text/background pair in `TEXT_PAIRS`, now for a dark theme:

| Token | Proposed | Used for |
|---|---|---|
| Background | deep navy, about #0B1220 | the page |
| Surface, raised surface | slate, about #151F32 and #1E2A42 | cards, tiles, the action bar |
| Text, muted text | near-white #F1F5F9, slate grey #A3B1C6 | all text (both pass 4.5:1 on the surfaces) |
| Accent | bright teal-green, about #2DD4A3, with dark text | the main button (Roll, End turn), focus ring |
| Square types | tinted dark tiles with a bright coloured top band and icon | Polluting (warm grey-brown), Green (green), Nature (deep green), Resilience (blue), Glider (violet), Card (amber) |
| Danger bands | green, yellow, orange, red, brighter than now | the meter |

Exact values are set while building, and the contrast test decides: any pair under 4.5:1 fails the build.

**Font.** A `Fonts` class loads Inter from the jar once at start-up. If loading fails, it falls back to the system sans-serif, so the game still starts.

**Painted pieces** (in `ui`, Java2D only):

- `GameButton`: a pill-shaped button with hover, pressed and disabled states and a focus ring, all painted by us, so the disabled text also meets contrast. It replaces the plain `JButton` styling.
- `CardPanel`: a rounded card with a soft shadow, used for player cards, pop-ups and the setup rows.
- `Icons`: small vector icons for the square types (factory, leaf, tree, shield, tram, card) and for money, GP and the meter. They sit next to each type label, which stays (NFR-05 AC-2).
- **Board:** rounded tiles with shadows, the type's top band and icon, the owner shown as a badge in their seat colour with their number, counters as shaded tokens, a glow under the current player's square, and a highlight on the tile under the mouse. The board's static parts are painted once into an image and reused, so redrawing stays cheap.
- **Top bar:** a rounded gradient meter with a glowing marker, a "band 3 · high" chip, and the round and year as a chip.
- **Player panel:** each player as a card with an avatar circle (seat colour, number and character initial), stat chips for money, GP and squares, and a small line for Resilience and the co-funding deal. The current player's card glows and carries a "YOUR TURN" chip (text, not just colour).
- **Pop-ups:** undecorated rounded cards over a dimmed game screen, with a header coloured by card type (Quiz, Choice, Event, Round report, Disaster). The keys are unchanged.
- **Title screen:** a painted Belfast skyline silhouette over a night-sky gradient, with the meter's colours as a band of light, and a large logo.

**Animation.** One `Animator` class owns the only `javax.swing.Timer` in the UI, at about 60 frames a second. Each animation has a fixed length and a draw function. When the last running animation finishes, the timer stops, so an idle game uses no CPU (NFR-11 AC-2).

| Animation | Length | Starts when |
|---|---|---|
| Dice tumble, then show the two faces | 0.5 s | after Roll |
| Counter slides along `lastMove().path()`, square by square | 70 ms a square, at most 1 s | after the move |
| Meter eases to its new value | 0.4 s | after any meter change |
| Hover glow on buttons and tiles | instant, no timer | on mouse over |

The model changes at once; animation is only how the change is drawn. Any key or click skips every running animation to its end. Pop-ups wait for animations to finish, so a card appears after the counter arrives. The controller asks the view to "settle" first. The Swing view does this with an AWT `SecondaryLoop`, which keeps handling events (including the animation) and returns when the animator finishes. It doesn't busy-wait, and the fake view in tests returns at once.

## Tasks

Sitting 1 - theme, font and painted pieces:

1. Add Inter and `OFL.txt` to resources; `Fonts` with fallback; licence row in the system overview.
2. New dark `Palette` tokens and `TEXT_PAIRS`; update `PaletteTest`'s list of checked backgrounds.
3. `GameButton`, `CardPanel`, `Icons`; switch every screen to them.
4. Restyle the board, top bar, player panel, action bar, pop-ups, setup, How to play, title and final-scores screens.

Sitting 2 - animation and checks:

5. `Animator`; dice, counter-slide and meter animations; skip on any key or click.
6. `GameController.View.settle()`, called before pop-ups; the Swing `SecondaryLoop` version and the no-op test version.
7. Render every screen and pop-up to images at 1280×720 and 1920×1040 (as in plan 007), and fix what they show.

## Tests

- `PaletteTest`: every pair in the new dark `TEXT_PAIRS` meets 4.5:1 (3:1 for large text), including the disabled button text, chips, badges and the text on each tile tint and card header.
- `FontsTest`: Inter loads from the jar; a missing font file falls back to the system sans-serif without an exception.
- `AnimatorTest`: driven with a fake clock rather than real time: an animation reaches exactly its end state; the timer stops when the last animation ends (NFR-11 AC-2); `skip()` jumps every animation to its end at once; two animations at once both finish.
- `nfr11_ac2_onlyTheAnimatorUsesATimer` replaces `noPollingInUi`: `javax.swing.Timer` may appear only in `Animator.java`, and `Thread.sleep`, `while (true)` and `java.util.Timer` nowhere in `ui`.
- `CounterPathTest`: a slide follows `lastMove().path()` square by square, including through the City Centre route and a Glider ride; its length is capped at 1 second.
- `GameControllerTest`: `settle()` is called before every pop-up, and never when there's no pop-up.
- `ScreensTest`: every restyled screen and pop-up builds and paints headless through a whole game, as now.
- The existing `StatusText`, `ActionOptions` and layout tests stay unchanged; they prove the information on screen hasn't changed.

## Docs to update

- [Screens and layout](../wiki/ux/screens-and-layout.md): the dark theme, tokens, icons, animation and the keys (unchanged), with an image of the game screen if useful.
- [NFR-11](../wiki/requirements/nfr-11-energy-efficiency.md) and [NFR-04](../wiki/requirements/nfr-04-responsiveness.md) build status: the animator and its timer rule.
- [System overview](../wiki/architecture/system-overview.md): the Inter licence; `src/AGENTS.md`'s UI note (only `Animator` may use a timer).
- Index, log, build progress, README.

<a id="renumbering"></a>**Renumbering:** the proposed order in build progress had plan 008 as the end-of-game report. No report plan has been drafted, so it becomes plan 009, and readability and packaging become plan 010. Live pages and code comments that point to "plan 008" for the report are updated. Finished plans and the log keep their original wording.

## Verification

1. `.\mvnw.cmd verify` passes; wiki lint passes.
2. Screens rendered to images at both sizes and checked.
3. **Oscar plays**, with `--seed 2026`, covering plans 007 and 008:
   - a full round at 1280×720 and maximised;
   - a round by keyboard only;
   - a few actions timed (they respond at once, and animations end within 1 second);
   - one minute idle on the game screen in Task Manager, which should be under 5% CPU.
4. The game still opens within 5 seconds with the font loading (NFR-04 AC-1).

## What Oscar should be able to explain

1. How the game shows animation without breaking "no busy-waiting": who starts the timer, and what stops it.
2. Why the model changes at once and animation is only how it's drawn, so a skipped animation can never change the game.
3. How a pop-up waits for the counter to arrive without freezing the window (the `SecondaryLoop`).
4. How a dark theme can still pass WCAG contrast, and how the build proves it.
5. Why there's no theme library, and what licence the font has and why that allows shipping it.

## Risks

- **Painting cost on slow PCs.** Shadows and gradients can be slow in Java2D. The static board is painted once into an image, shadows are pre-rendered, and the idle CPU check catches a timer left running.
- **Too much at once.** If sitting 1 runs long, stop after task 4: a restyled game without animation is still a complete improvement, and animation can follow.
- **Keyboard focus inside undecorated pop-ups.** Each pop-up keeps a default button and Escape handling, checked in the play-through.
- **The font file.** If the Inter download can't be verified, use the system font (Segoe UI) and record it; the `Fonts` fallback already covers it.

## Outcome

**Built on 18 November 2026; waiting for Oscar's play-through, which also covers plan 007, before both are marked `done`.**

What was built: everything in the Tasks, in one sitting.

- **Font:** Inter 4.1 Regular, SemiBold and Bold from the official release zip (SHA-256 `9883fdd4…b11e`), with its `OFL.txt`, in `src/main/resources/fonts/`. `.gitattributes` marks `*.ttf` as binary.
- **New classes:** `Fonts`, `GameButton`, `CardPanel`, `Icons`, `PlayerCard`, `DarkComboBox` and `Animator`.
- **Restyled screens:** the dark `Palette`, and every screen and pop-up.
- **Controller:** `GameController.View` gains `settle()`, called before any pop-up. `GameScreen.settle()` uses an AWT `SecondaryLoop`.
- **Model:** no model code changed.

Checks:

- **Tests:** 365 pass (14 new or replaced).
- **Build:** the coverage rule is met, the clean build has no warnings, and `--check` and the wiki lint pass.
- **Planted bugs:** five deliberate bugs were each caught: the timer never stops; skip leaves a chained animation running; pop-ups don't wait; muted text too dark; slides over a second.
- **Timing:** the jar opened its window in 1.2 seconds (NFR-04 AC-1). Idle on the title screen it used 0.27 CPU-seconds in 20 seconds, about 1.3% of one core (NFR-11 AC-1).

Differences from the plan, and what the checks found:

1. **Two bugs caught by the new tests before anyone saw them.**
   - Skipping an animation didn't finish the counter's slide, because the dice start the slide when they end. `skip()` now repeats until nothing is running.
   - The palette declared muted and warning text on button colours that are never drawn; one failed at 4.06:1. The declared pairs now match what is drawn, and buttons only carry their own text colour.
2. **Screens checked as images.** The title, tutorial, setup, game screen (at rest, mid-roll and mid-slide), round-report and choice pop-ups and final scores were rendered at 1280×720 and 1920×1040, and these were fixed:
   - tile text overlapping at 1280×720;
   - the "Rolled N" chip covering the City Centre label;
   - the year clipped in the top bar;
   - player cards too tall for six players;
   - truncated square counts;
   - light Metal arrows on the drop-downs;
   - a doubled "A A:" on choice options;
   - a game screen replaying the last roll when it was built.
3. **Small tiles show the type by icon only.** At 1280×720 a tile is about 62 pixels high, and the type word squeezed the price to "£…". NFR-05 AC-2 allows "text or a distinct icon", so small tiles use the icon and the tooltip gives the word; tall tiles show both.
4. **Bold text in HTML labels:** Inter Bold shares its family name with Inter Regular, so Swing's HTML labels show it as Regular. Wrapped labels use SemiBold instead (noted in `src/AGENTS.md`).
5. **Dice faces are for show.** The model keeps the dice total, not each die, so the tumbling faces are decorative and the result is shown as "Rolled N". No model change was made to store faces.
6. **No shipyard name on the title screen.** The two gantry cranes are drawn plain, so the painting suggests Belfast without using a company's name.

Still to do by Oscar, for plans 007 and 008 together:
- a full round at 1280×720 and maximised;
- a round by keyboard only;
- a few actions timed;
- one minute idle on the game screen in Task Manager.
