---
title: Plan 007 Game screens
type: plan
id: PLAN-007
status: approved
requirements: [FR-01, FR-15, NFR-02, NFR-04, NFR-05, NFR-11]
sources: [../wiki/ux/screens-and-layout.md, ../wiki/architecture/game-flow.md, ../wiki/sources/design-diagrams.md]
updated: 2026-11-18
---

# Plan 007 - Game screens

## Goal

At the end of this plan the game can be played from start to finish on one computer: a title screen, a short "How to play", a setup screen for 4-6 players, and a game screen with the board, the Climate Meter, the player panel, the action buttons and pop-ups for cards, junctions, round reports and return rolls. Today the window is empty and the game only runs headless.

## Scope

- **Requirements covered:**
  - [FR-01](../wiki/requirements/fr-01-start-game.md): the setup screen (the "manual" half; AC-1 to AC-4 are already enforced by `Game.start`).
  - [FR-15](../wiki/requirements/fr-15-always-visible-status.md) AC-1 to AC-6: top bar, meter with target marker, every player's status including Resilience and deal, current and bankrupt players marked by more than colour.
  - [NFR-02](../wiki/requirements/nfr-02-learnability.md) AC-1 and AC-2: a 4-5 screen tutorial, and only legal, affordable actions enabled. AC-3 (the user test) is a Phase 3 gate.
  - [NFR-04](../wiki/requirements/nfr-04-responsiveness.md) AC-1 to AC-3: timed start-up and actions on Oscar's laptop, plus the headless 50 ms end-of-round test.
  - [NFR-05](../wiki/requirements/nfr-05-accessibility.md) AC-1 to AC-4: colour tokens with a contrast test, square type labels, the band as a number and word, keyboard-only play.
  - [NFR-11](../wiki/requirements/nfr-11-energy-efficiency.md) AC-1 and AC-2: event-driven, no polling, idle CPU checked.
- **Decisions relied on:** [ADR-002](../decisions/002-ui-toolkit.md) (Swing), [ADR-003](../decisions/003-build-tool-and-java-version.md) (Maven, Java 21, Oscar's laptop as the reference PC), [ADR-004](../decisions/004-reading-the-json-data-files.md) (data through `GameConfig` only).
- **Open rules questions answered first:** none open. Q-02 and Q-03 (the meter label and values below 0) are already decided.
- **Out of scope:**
  - The end-of-game report's meter graph and emissions shares ([FR-16](../wiki/requirements/fr-16-end-of-game-report.md), plan 008). This plan ends on a plain final-scores screen with the winner, so a game can finish.
  - Animation of counters, sound, saving a game, a full-screen toggle (the window is resizable and can be maximised).
  - The readability check ([NFR-03](../wiki/requirements/nfr-03-readability.md)) and packaging (NFR-09), plan 009.
  - Any rule change. If a screen needs information the model doesn't offer, add a read-only accessor to the model with a test; never compute a rule in `ui`.

## Prerequisites

1. Plans 001-006 done (they are). The model already offers everything the screens read: `phase()`, `availableActions()`, `affordableOptions()`, `pendingCard()`, `lastCard()`, `lastRound()`, `lastReturn()`, `lastMove()`, `result()`, `meter()`, `players()`, `currentPlayer()`, `round()`, `year()`.
2. **Minimum window size: 1280×720**, confirmed by Oscar on approval (18 November 2026), up from plan 001's 1024×700, because the board is 10 squares wide beside a player panel and labels must stay readable. 1366×768 laptops fit it with the taskbar showing.

## Design

**One controller, many views.** `GameController` (in `ui`) holds the `Game`. Every button, key and pop-up calls one controller method, which calls the matching `Game` method (`roll`, `take`, `chooseRoute`, `answerQuiz` and so on), then tells every view to refresh once. Views only read the model. If the model throws `IllegalActionException`, the controller shows its message and nothing changes. Nothing waits in a loop: Swing calls us when a key or click happens ([NFR-11](../wiki/requirements/nfr-11-energy-efficiency.md) AC-2).

**Pop-ups follow the phase.** After each call the controller looks at what the model now says:

| Model state after the call | What the UI shows |
|---|---|
| `AWAITING_ROUTE` / `AWAITING_DIRECTION` | A choice bar on the action bar: "Stay on the loop" / "Cut through the City Centre", or "Towards West" / "Towards East" |
| `AWAITING_QUIZ_ANSWER` | Card pop-up with the question and answer buttons |
| `AWAITING_CHOICE` | Card pop-up with both options and their effects; unaffordable options disabled with "(can't afford)" |
| `lastCard()` changed | The card's outcome, then its fact and source title and web address (FR-10, NFR-10) |
| `lastRound()` changed | Round report: income, carbon tax, meter change, interim target, disaster and its explanation (FR-11, FR-17) |
| `AWAITING_RETURN_ROLL`, then `lastReturn()` | "Roll to return" for a bankrupt player, then the result |
| `GAME_OVER` | Final scores and winner from `result()` (plan 008 replaces this with the full report) |

**Presenters keep the logic testable.** Swing windows can't be opened in a headless CI build, so anything worth testing lives in plain classes with no Swing in them: `StatusText` (top bar and player card lines), `ActionOptions` (the buttons for the current phase, their labels such as "Buy £9M", whether each is enabled, and its shortcut key), `HowToPlay` (the tutorial pages), `BoardLayout` (where each square sits) and `Palette` (the colour tokens). The Swing classes just draw what these return.

**Board geometry is layout, not balance.** `BoardLayout` places the loop's squares round a 10 × 7 ring in loop order, clockwise from top-left, and the City Centre route along the middle row between the two junctions, as in the [board layout](../raw/net-zero-belfast-handover/design/board_layout.png). It works from `board.json`'s loop and route order, so it stays right if squares are renamed or re-priced. Grid positions are a UI concern, so they live in code with a comment saying so.

**Tutorial numbers come from the data.** `HowToPlay` builds its five pages (the goal; a turn; the squares; the Climate Meter and disasters; how you win) from fixed sentences with numbers filled in from `GameConfig` (rounds, years, starting meter, targets, the money ÷ 5 rule). A re-tune changes the tutorial automatically (NFR-07).

**Colours in one place.** `Palette` defines every colour as a named token (background, text, muted text, square types, bands, focus ring, the current-player border) and lists every text/background pair the UI uses. Square types also carry a text label ("Polluting", "Green", "Nature", "Resilience", "Glider", "Card"), so the board reads in greyscale.

**Keyboard.** Every button is reachable with Tab and pressed with Enter or Space, with a visible focus ring. Shortcuts: R roll, B buy, C co-fund, L lobby, V convert, P protect (buy Resilience), G Glider, E end turn, 1-4 for quiz answers, A or B for choice options. Shortcuts are bound through Swing's key bindings on the game screen, not with key listeners.

**Seed for testing.** `App` gains `--seed N`, so a game started from the window can be replayed exactly while testing by hand. Without it the seed is random, as now.

## Tasks

Sitting 1 - screens and the main loop:

1. `Palette` with tokens and declared pairs; a `Contrast` helper using the WCAG 2.1 relative luminance formula.
2. `MainWindow` becomes a `CardLayout` of screens, minimum size as confirmed, title "Net Zero Belfast".
3. `TitleScreen` (Start, How to play, Quit) and `HowToPlayScreen` (Back, Next, page "n of 5") from `HowToPlay`.
4. `SetupScreen`: player count 4-6 from `game.minPlayers`/`maxPlayers`, a name field (limited to `game.maxNameLength`) and character list per player, each character's starting money, GP and ability shown. Start is disabled until every row is complete; on Start, `Game.start` is the final check and its message is shown if it refuses (FR-01 AC-2 to AC-4).
5. `GameController` and the refresh-on-every-call pattern; `App --seed N`.
6. `GameScreen` with `TopBar` (title, "Round N of 12 · Year YYYY", `MeterBar` with value, "band N · word", the next target marker and "Climate Meter (2026 = 50)"), `BoardPanel` (custom-painted squares with number, name, type label, owner initial and counters), `PlayerPanel` and `ActionBar`.

Sitting 2 - choices, pop-ups and keyboard:

7. Route and direction choices on the action bar.
8. `CardDialog` (quiz, choice, event), `RoundReportDialog`, return-roll prompt, final-scores screen. Dialogs are modal, sit over the board and leave the top bar visible (FR-15 AC-1).
9. Key bindings and focus order for every screen and dialog; focus ring colour from `Palette`.
10. A read-only model accessor only if a screen proves to need one, each with a test.

## Tests

All in `src/test/java/netzerobelfast/ui/` and run headless:

- `PaletteTest`: `nfr05_ac1_everyTextPairMeetsContrast` computes every declared pair (4.5:1, or 3:1 for pairs marked large text); `Contrast` checked against known values (black on white 21:1, #777 on white about 4.48:1).
- `StatusTextTest`: `fr15_ac1_topBarShowsRoundYearMeterAndBand`; `fr15_ac2_targetMarkerIs26ThenEleven` (rounds 1-3, 4-8, then none); meter at 0 and below 0; `fr15_ac3_playerLineCountsGreenAndPolluting`; `fr15_ac4_currentPlayerMarkedInText` ("(turn)"); `fr15_ac5_bankruptMarked`; `fr15_ac6_resilienceAndDealShown`, including no deal; `nfr05_ac3_bandHasNumberAndWord`.
- `ActionOptionsTest`: `nfr02_ac2_onlyOfferedActionsEnabled` for each phase (roll only before moving; landing actions and End turn after; route choices at a junction); `buyLabelNamesCost`; exactly affordable and £1M short (from `availableActions`, so the UI never decides affordability); `nfr05_ac4_everyActionHasUniqueShortcut`; choice options the player can't afford are disabled.
- `GameControllerTest`: drives a scripted game (the model's `ScriptedRandom` and `Games` helpers, reached by a small test-only bridge if needed) through the controller with a fake view: each call refreshes exactly once; an illegal call leaves the state unchanged and reports the message; the pop-up decision table above, one test per row.
- `HowToPlayTest`: `nfr02_ac1_fivePagesOrFewer` and a word count that reads in 2 minutes or less at 200 words a minute; numbers follow `GameConfig` (change the starting meter with `TestData`, the text changes).
- `BoardLayoutTest`: every square has one cell and no two overlap; the loop runs clockwise; the City Centre route sits between its two junctions; `nfr05_ac2_everySquareTypeHasALabel`.
- `SetupRulesTest`: Start disabled with an empty name, a duplicate character or too few rows; name field limit from config.
- `nfr11_ac2_noPollingInUi`: scans the `ui` sources for `Thread.sleep`, `while (true)` and `javax.swing.Timer`, and fails if any appear.
- Model: `nfr04_ac3_endOfRoundUnder50ms` on a seeded 6-player game (best of several runs, to avoid one slow CI run failing it).

The coverage rule stays on `model`, `config` and `sim`; the presenters are tested but `ui` isn't added to the 80% rule, because the Swing drawing classes can't run headless.

## Docs to update

- FR-01, FR-15, NFR-05 (AC-1 to AC-4) and NFR-11 AC-2 to `built`; NFR-02 and NFR-04 keep build status notes for their user and timed tests.
- [Screens and layout](../wiki/ux/screens-and-layout.md): the final layout, window size, shortcut keys and palette tokens; to `agreed` or `built`.
- [System overview](../wiki/architecture/system-overview.md) and [game flow](../wiki/architecture/game-flow.md): the controller and presenters.
- `src/AGENTS.md` current state; [local development](../wiki/operations/local-development.md) for `--seed`; README status.
- Catalogue, index, log and [build progress](../wiki/operations/build-progress.md).

## Verification

1. `.\mvnw.cmd verify` passes, and the wiki lint passes.
2. `java -jar target\net-zero-belfast.jar --seed 2026`: go from the title through How to play and setup with four players, and play **at least one full round** by hand, including a card, a junction choice and the round report. Do it at the minimum window size and maximised.
3. Play one more round with the keyboard only (NFR-05 AC-4).
4. Time start-up to the title screen and a few actions with a stopwatch on Oscar's laptop (NFR-04 AC-1, AC-2).
5. Leave the game screen idle for 60 seconds and read the CPU in Task Manager (NFR-11 AC-1).
6. Play a seeded game to the end (or near it, using the bots in a test) to see the final-scores screen.
7. Record the results in the plan's Outcome. The agent can run the tests and launch the jar, but **Oscar plays the round himself** before the plan is marked done.

## What Oscar should be able to explain

1. Why the UI never applies a rule itself, and what happens if a player presses a key for an action the model doesn't allow.
2. How a button press becomes a model call and a screen refresh, and why there's no loop waiting for input (NFR-11).
3. Why the testable logic is in presenter classes and not in the Swing panels, and why `ui` isn't in the coverage rule.
4. How the contrast test works and why colour is never the only signal (NFR-05).
5. Where the tutorial's numbers come from, and what happens to it if the starting meter is re-tuned.
6. Why the board's grid positions are in code but square names and prices are in `board.json`.

## Risks

- **Too big for two sittings.** If sitting 1 runs long, stop after task 6 with a playable game that uses simple message dialogs, and do the styled pop-ups and keyboard in a follow-up.
- **Small screens.** If 36 labelled squares don't fit legibly at 1280×720, shorten the on-square text to number, short name and type label, with the full name in a tooltip and a square-details line on the action bar.
- **Swing focus quirks** can break keyboard-only play inside dialogs. Each dialog gets a default button and Escape handling, checked by hand.
- **Timing tests flake in CI.** The 50 ms test takes the best of several runs, and the start-up timing stays a manual check.

## Outcome

**Built on 18 November 2026; waiting for Oscar's play-through (Verification step 7) before it is marked `done`.**

What was built: everything in the Tasks, in one sitting. `ui` has 19 classes: the presenters (`Palette`, `Contrast`, `StatusText`, `ActionOptions`, `BoardLayout`, `HowToPlay`, `SetupRules`), `GameController`, and the Swing screens (`MainWindow`, `TitleScreen`, `HowToPlayScreen`, `SetupScreen`, `GameScreen`, `BoardPanel`, `MeterBar`, `Dialogs`, `GameOverScreen`, `Ui`). `App` has `--seed N`. No model code changed; tests reach model set-up through a new public test helper, `model.TestGames`.

Checks: 351 tests pass (56 new), coverage rule met, clean build with no warnings, `--check` passes, wiki lint passes. Four deliberate bugs (End turn always enabled, the target marker a round early, the round report never shown, muted text too light) were each caught. The jar opened its window 1.1 seconds after launch (NFR-04 AC-1) and, idle on the title screen, used about 4% of one core (NFR-11 AC-1).

Differences from the plan:

1. **A crash found and fixed before anyone saw it.** Rendering the screens to images showed the game screen threw on Start: a custom-painted `JComponent` has no accessibility context. `BoardPanel` and `MeterBar` now extend `JPanel`, and a new `ScreensTest` builds and paints every screen headless, through a whole game, so a crash like this fails the build.
2. **Screens were checked as images, not by hand.** The agent can't click in a window, so it rendered the title, tutorial, setup and game screens at 1280×720 and 1920×1040, and the round-report and choice pop-ups, and fixed what they showed: a player panel that scrolled sideways, action labels cut off (now three buttons a row), a clipped tutorial heading, web addresses running off pop-ups (now split after "/" or "-" without changing a character, `UiTextTest`), and the setup form centred instead of left.
3. **Unaffordable choice options are plain text**, marked "(can't afford)", not disabled buttons: Swing draws disabled wrapped text too faintly to meet 4.5:1.
4. **An "Answer the card" button (O)** reopens a quiz or choice if it was ever left unanswered, so the game can't get stuck.
5. **After round 8 the meter marks "2050 net zero 0"** rather than nothing, so a target stays in view.
6. **Shortcut keys:** the first action of each kind has a key; a second Glider stop or square to convert is reached with Tab.

Still to do by Oscar: play a full round by hand at 1280×720 and maximised, one round by keyboard only, time a few actions (NFR-04 AC-2), and check idle CPU on the game screen (NFR-11 AC-1).
