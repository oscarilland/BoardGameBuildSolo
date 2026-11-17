# src/AGENTS.md - coding rules

Read this before changing anything in `src/`. The root `AGENTS.md` gates still apply: no change here without an approved plan in `docs/plans/`.

> **Current state:** plans [001](../docs/plans/001-walking-skeleton.md) to [004](../docs/plans/004-climate-cards.md) are built: data loading and validation, seeded `Dice`, and in `model` the characters (with ability hooks), players, the square classes, the board, the Climate Card deck, a partial `ClimateMeter`, and a `Game` that starts games and runs turns as a phase machine: rolling and moving (with junction choices), drawing and resolving cards, and offering and applying the actions after moving. No end-of-round effects yet, and bankruptcy is only the trigger; the window is still empty. Base package: `netzerobelfast`.
>
> **Squares that start something on arrival:** a Climate Card square draws a card in `Game.landed()`. In tests, `Games.arrive` triggers that too, so use `Games.nextCard` to choose the card, or arrive somewhere else.
>
> **Testing tip:** `ScriptedRandom` and `Games` in `src/test/java/netzerobelfast/model/` script exact dice, place counters, give squares owners, set money and put a player on a square ready to act (`Games.arrive`). Use them instead of hunting for seeds.
>
> **Adding an action:** give the square class a `landOn` result and handle the new `Action.Kind` in `Game.take`. Never let the UI or a bot apply an effect directly; everything goes through `availableActions()` and `take()`.

## Stack

- **Java 21**, built with **Maven through the Maven Wrapper** (`mvnw`), so no Maven install is needed ([ADR-003](../docs/decisions/003-build-tool-and-java-version.md)). Standard Maven layout: `src/main/java`, `src/main/resources`, `src/test/java`.
- Desktop UI: **Swing**, part of the JDK, with no UI library ([ADR-002](../docs/decisions/002-ui-toolkit.md)). Update the UI only on the event dispatch thread.
- Data files: **Gson**, used only inside `config`, with validation after loading ([ADR-004](../docs/decisions/004-reading-the-json-data-files.md)). It's the only runtime library.
- Tests: JUnit 5. JaCoCo measures line coverage, and the build fails if the game logic drops below 80% (NFR-06).
- Offline only. No network calls, no telemetry, no personal data beyond display names (NFR-08).

## Layout

Packages live under `src/main/java`, with tests in matching packages under `src/test/java`. Plan 001 sets the base package name.

| Package | Purpose | Rule |
|---|---|---|
| `model` | `Game`, `Board`, `Square` and subclasses, `Player`, `Character` and subclasses, `ClimateMeter`, `DisasterManager`, `CardDeck`, `ClimateCard` and subclasses, `CoFundingDeal`, `Dice` | Pure logic. No UI classes, no file access, no static state. Randomness comes in through `Dice` |
| `config` | `GameConfig` and the loaders for `config.json`, `board.json` and `climate_cards.json` | The only place that reads files, and the only place that uses Gson. Validates every value and fails with a clear message. Hands the model plain records |
| `ui` | Screens: title, setup, game, pop-ups, end-of-game report | Thin: shows state from `model`, offers only valid actions, passes the player's choices back |
| `sim` | Headless game driver with simple bots | Used by tests (NFR-06: 20 full games without crashes) and for re-tuning balance |
| `src/main/resources/data/` | Working copies of the three data files | Copied from `docs/raw/` by plan 001. Edit these copies, never the raw files, and record why in the log |
| `src/test/java/.../config/TestData` | Copies the real data into a temp folder so a test can change one value | Use it for any data-driven test; don't add fixture copies of the data files |

## Code rules

1. **Keep the analysis model's names.** The SUB1a report commits to the classes and methods in [the domain model](../docs/wiki/architecture/domain-model.md). Keep them recognisable, and record any change on that page.
2. **No balance numbers in code.** Prices, income, meter changes, thresholds, card text and character values come from the data files through `GameConfig` (NFR-07). A literal number in game logic needs a comment saying why it isn't data.
3. **Randomness:** one `java.util.Random` (or a wrapper) created at start-up with an optional seed, and passed in. Tests always use a fixed seed. Shuffling, dice, disaster choice and first player all use it.
4. **The model never waits for a person.** Where a player must choose (route at a junction, quiz answer, card option, buy or not), the model returns the options and a separate call applies the choice. The UI and the bots both use the same calls. See [game flow](../docs/wiki/architecture/game-flow.md).
5. **Validate input at the boundary.** Names: trimmed, non-empty, length-limited, unique. Every action is checked by the model for being legal now, even if the UI already hid illegal ones.
6. **Money and GP are `int`** (whole £M and whole points). Rounding rules are explicit and tested; see the open question on Resilience rounding.
7. **No busy-waiting** (NFR-11). The UI waits on events, never in a polling loop.
8. Keep it readable over clever. Oscar has to explain every line, so add a short comment where the *why* isn't obvious, not where the code already says *what*.

## Tests

- Name tests after the behaviour and cite the requirement and acceptance criterion, for example `fr11_ac2_carbonTaxRunsAfterIncome`.
- Rules logic: normal flow, boundary values (meter at 10, 11, 25, 26, 40, 41, 0 and below 0), exactly affordable and £1M short, bankrupt players, empty card pile, last round.
- End of round: one test that checks the full FR-11 order with a seeded game.
- A seeded headless run of 20 full games with bots must finish without exceptions (NFR-06).

## Licences

Record every third-party library in `docs/wiki/architecture/system-overview.md` with its version and licence.
