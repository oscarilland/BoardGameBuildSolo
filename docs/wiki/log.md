# Net Zero Belfast wiki log

Append-only. Add new entries at the end, using `## [YYYY-MM-DD] operation | title`.

## [2026-11-17] repository | LLMWiki and agent harness established

Built the harness from FeedWatchBuild's, adapted for a Java desktop game ([ADR-001](../decisions/001-repo-structure-and-harness.md), proposed). Oscar's answers: this repo is a **solo version only** and never goes to the team's GitLab repo, and the first pass is the harness plus a full ingest, with no code. Created the root, `docs/` and `src/` `AGENTS.md` files with `CLAUDE.md` imports, the wiki schema, ADR and plan templates, seven skills, `.claude/settings.json`, `scripts/WikiLint.java` (ported from FeedWatchBuild's PHP lint without the public/private checks), `.gitignore`, `.gitattributes` and a CI workflow that runs the lint. Moved the team handover from `NetZeroBelfast_handover/handover/` to `docs/raw/net-zero-belfast-handover/`, unchanged.

## [2026-11-17] ingest | Team handover (all 22 files)

Read every file in the handover: `HANDOVER.md`, rules v4 (Markdown and Word, which match), the SUB1a report draft, the module handbook (all 18 pages), the three data files, all diagrams (PNGs plus Graphviz, Mermaid and draw.io sources) and the balance simulation. Created seven source receipts, the overview, project context, learning outcomes, ten rules pages, the requirements catalogue with FR-01 to FR-17 and NFR-01 to NFR-11 (SUB1a wording, with drafted acceptance criteria) and stretch items S-01 and S-02, four architecture pages, the screens page and three operations pages.

Recorded on ingest: the board drawing numbers squares from 1 but `board.json` from 0; no data file has the disaster explanations FR-17 needs; unaffordable Choice options aren't covered; the quiz reward is stored twice; "after 2 rounds" for bankruptcy is ambiguous; carbon tax and meter treatment of closed or flooded squares isn't stated; Legacy Score rounding isn't stated; Flood or Storm with no owned squares isn't covered. These are Q-11 to Q-17 on [open questions](rules/open-questions.md), alongside the handover's Q-01 to Q-10. Also noted: the sequence diagrams have model objects talking to the player, which [game flow](architecture/game-flow.md) proposes to handle by returning pending decisions.

## [2026-11-17] decide | ADR-002 to ADR-004 proposed

Drafted ADRs for the UI toolkit (recommends Swing, for NFR-09), the build tool and Java version (recommends the Maven Wrapper, targeting Java 17 until the lab PCs' version is known), and reading the JSON data files (recommends Gson with validation on load). Decisions are left for Oscar.

## [2026-11-17] decide | ADR-002 accepted: Swing

Oscar chose Swing for the UI, the recommended option. Recorded his decision in [ADR-002](../decisions/002-ui-toolkit.md) as he gave it, with the recommendation's reasons (NFR-09, no dependency, no per-platform packaging). Updated the consequences, `src/AGENTS.md`, the [system overview](architecture/system-overview.md), [screens and layout](ux/screens-and-layout.md), the [overview](overview.md), the index, [build progress](operations/build-progress.md) and the README.

## [2026-11-17] decide | ADR-003 accepted: Maven Wrapper, Java 21

Oscar chose Maven with the Maven Wrapper, targeting Java 21. The draft recommended Java 17 because the team's requirements target EEECS lab PCs; Oscar noted this solo version runs on his own laptop (JDK 21), so the recommendation was revised to 21 before he decided. Recorded in [ADR-003](../decisions/003-build-tool-and-java-version.md) with its consequences. Updated the root and `src/` `AGENTS.md` (stack and validation commands), the [system overview](architecture/system-overview.md), [local development](operations/local-development.md), [NFR-04](requirements/nfr-04-responsiveness.md) (reference PC: Oscar's laptop), [NFR-09](requirements/nfr-09-portability.md) (Java 21 or later), [project context](project-context.md), the [overview](overview.md), the index, [build progress](operations/build-progress.md) and the README.

## [2026-11-17] decide | ADR-004 accepted: Gson

Oscar chose Gson, the recommended option, to read the three JSON data files. Recorded in [ADR-004](../decisions/004-reading-the-json-data-files.md) as he gave it, with the recommendation's reasons. Consequences added: Gson is the only runtime library, used only in the `config` package, which hands the model plain records; validation after loading is required because Gson fills missing fields silently; keys starting with `_` are ignored. Updated `src/AGENTS.md`, the [system overview](architecture/system-overview.md), [data files](architecture/data-files.md), [domain model](architecture/domain-model.md), the [overview](overview.md), the index, [build progress](operations/build-progress.md) and the README. All three stack decisions are now made.

## [2026-11-17] decide | Open rules questions Q-01 to Q-17 answered

Oscar accepted the recommended answer to every open rules question ("all recommended"). Recorded on [open questions](rules/open-questions.md) and carried into the rules pages ([setup](rules/setup-and-characters.md), [board](rules/board-and-squares.md), [turn](rules/turn-and-actions.md), [end of round](rules/end-of-round.md), [Climate Meter](rules/climate-meter-and-disasters.md), [bankruptcy](rules/bankruptcy.md), [cards](rules/climate-cards.md), [scoring](rules/scoring-and-end-of-game.md)) and the acceptance criteria of FR-02, FR-03, FR-05, FR-08 to FR-15 and FR-17. Q-04 was checked on the web: the 77% 2040 target comes from the Climate Change (2040 Emissions Target) Regulations (NI) 2024, not the 2022 Act itself, so the value 11 stands. For Q-11 the agent drafted four disaster explanation lines; the Flood and Heatwave lines are sourced to the Met Office, while Storm and Drought need a source check before Oscar approves them (the Met Office says the UK isn't measurably getting stormier). NFR-06 and NFR-07 marked agreed, as proposed and accepted.

## [2026-11-17] decide | ADR-001 accepted

Oscar confirmed the repo structure ("I confirm the repo structure"). [ADR-001](../decisions/001-repo-structure-and-harness.md) is accepted, Option A. All four ADRs are now accepted.

## [2026-11-17] plan | Plan 001 walking skeleton drafted

Drafted [plan 001](../plans/001-walking-skeleton.md): a Maven project with the wrapper at the repo root, working copies of the data files (with the Q-05 two-way note), Gson records and `GameConfig` validation with clear errors, a seeded `Dice`, the JaCoCo 80% rule, a shaded runnable jar with a `--check` mode, an empty Swing window, and a CI build step. Covers NFR-07 AC-1 and AC-3, NFR-06 AC-1, and NFR-09 AC-2 as a packaging proof. Disaster explanations are left to plan 005. Waiting for Oscar's approval and confirmation of the package name (`netzerobelfast`) and jar name.

## [2026-11-17] build | Plan 001 walking skeleton built

Oscar approved [plan 001](../plans/001-walking-skeleton.md) (jar `net-zero-belfast.jar`, package `netzerobelfast`) and it is now `done`. Added the Maven project and wrapper (Maven 3.9.16, checksum-pinned), working data files in `src/main/resources/data/` (only the Q-05 direction changed), the `netzerobelfast.config` loader and validator, a seeded `Dice`, the launcher with `--check` and an empty Swing window, and a CI build step. 83 tests pass; line coverage is 99.1% for `config` and 100% for `model`, and forcing the threshold to 100% fails the build as it should. Verified `--check`, a broken price stopping start-up with a named key, and the jar running alone from an empty folder. Differences from the plan are in its Outcome: counts are tested rather than validated (NFR-07), Gson reads a strict tree mapped by hand, broken data is made in tests, and Gson's `error_prone_annotations` dependency is left out of the jar (correction note added to [ADR-004](../decisions/004-reading-the-json-data-files.md)). Updated [data files](architecture/data-files.md) (now `built`), [domain model](architecture/domain-model.md), [system overview](architecture/system-overview.md), [local development](operations/local-development.md), [build progress](operations/build-progress.md), both `AGENTS.md` files, the [overview](overview.md), the index and the README.

## [2026-11-17] spec | FR-01, FR-02 and FR-03 agreed

Oscar answered the remaining questions: display names up to 20 characters after trimming, unique names kept (FR-01 AC-4), and no second route question at the far junction after the City Centre cut-through (FR-03 AC-7). All three requirements are now `agreed`. While planning, found [Q-18](rules/open-questions.md): which way a player on Glider City Hall (30) moves after arriving by Glider, now the cut-through is two-way. It blocks plan 003, not plan 002.

## [2026-11-17] plan | Plan 002 players, characters and movement drafted

Drafted [plan 002](../plans/002-players-characters-and-movement.md): `Character` and six subclasses from the data, `Player`, a `Board` built from `board.json` that moves one step at a time, and `Game` as a small state machine (awaiting roll, awaiting route, move done) that pauses for junction choices instead of calling the UI. Covers FR-01 AC-1 to AC-4, FR-02 AC-1 to AC-6, and FR-03 except AC-6 (bankruptcy, plan 005). Adds `game.maxNameLength` to the working `config.json`. Records the change from `Board.move(from, steps, route)` to step-by-step movement. Waiting for Oscar's approval.

## [2026-11-17] build | Plan 002 players, characters and movement built

Oscar approved [plan 002](../plans/002-players-characters-and-movement.md) and it is now `done`. Added `Character` and six subclasses, `Player`, a `Board` built from `board.json` that moves one step at a time, `Game` with setup checks and a turn phase machine (roll, route choice, direction choice, end turn), and `IllegalActionException`. Added `game.maxNameLength` (20) to the working `config.json`. 129 tests pass with no warnings; `model` coverage is 100%. Two deliberate bugs in the junction logic were each caught by the tests. One reading for Oscar to confirm: a move ending exactly on the far junction after the cut-through means the next move starts with the route question. FR-02 is `built`; FR-01 waits for its screen (plan 007) and FR-03 for AC-6 (plan 005). Updated the [domain model](architecture/domain-model.md), [game flow](architecture/game-flow.md), [data files](architecture/data-files.md), the rules pages' build status, the requirement pages, [build progress](operations/build-progress.md), `src/AGENTS.md`, the [overview](overview.md), the index and the README.

## [2026-11-17] decide | Far-junction reading confirmed; Q-18 answered

Oscar confirmed that ending a move exactly on the far junction after the cut-through means the next move starts with the route question (FR-03). He answered [Q-18](rules/open-questions.md): after a Glider ride to Glider City Hall (30), the player decides their direction at the start of their next move, which `Game`'s `AWAITING_DIRECTION` phase already supports. Recorded on the open questions, [board and squares](rules/board-and-squares.md), [FR-03](requirements/fr-03-dice-movement-and-junctions.md), [FR-09](requirements/fr-09-glider.md), plan 002's Outcome and build progress. No rules questions are open.

## [2026-11-17] spec | FR-04 to FR-09 agreed

Oscar answered: actions that would change nothing (re-lobbying a lobbied square, re-co-funding your own deal) aren't offered (new FR-05 AC-7, FR-06 AC-7); a Polluting square can be bought and converted in the same turn, as the rules and the simulation allow (FR-07 AC-7); converted squares aren't nature projects (FR-07 AC-8). FR-09 gained AC-6 for Q-18. FR-04 to FR-09 are now `agreed`.

## [2026-11-17] plan | Plan 003 square actions drafted

Drafted [plan 003](../plans/003-square-actions.md): the analysis model's square classes with `landOn()`, an `Action` list from `Game.availableActions()` that `take()` checks against, `CoFundingDeal`, `Player` money, GP and Resilience, and character hooks for the Tech Entrepreneur, Council Planner, Student Commuter and Community Activist (and the Industrialist's converted-by mark). End-of-round effects of these actions wait for plan 005; the plan lists which acceptance criteria are built here and which wait. Waiting for Oscar's approval.

## [2026-11-17] build | Plan 003 square actions built

Oscar approved [plan 003](../plans/003-square-actions.md) (his message said "plan 004" while starting plan 003's build; recorded as plan 003) and it is now `done`. Added the square classes with `landOn()`, `Action`, `CoFundingDeal`, `Game.availableActions()` and `take()`, `Player` money, GP, Resilience and `disasterCost`, and character ability hooks read from the data. 182 tests pass with no warnings; `model` coverage is 99.5%. Deliberate bugs in the affordability check, the one-conversion limit and the one-landing-action limit were each caught. FR-04 and FR-09 are `built`; FR-05 to FR-08 stay `agreed` until their end-of-round effects in plan 005. Updated the [domain model](architecture/domain-model.md), [game flow](architecture/game-flow.md), the rules pages' build status, the requirement pages and catalogue, [build progress](operations/build-progress.md), `src/AGENTS.md`, the [overview](overview.md), the index and the README.

## [2026-11-17] spec | FR-10 and NFR-10 agreed

Oscar answered: sources are shown with their title and web address (not clickable), FR-10 AC-6 and NFR-10 AC-3; plan 004 starts bankruptcy (the compulsory payment and trigger) because Event cards need it; and facts are re-checked before any demo (new NFR-10 AC-4, and a gate in build progress). FR-10 and NFR-10 are now `agreed`.

## [2026-11-17] plan | Plan 004 Climate cards drafted

Drafted [plan 004](../plans/004-climate-cards.md): the card classes, a `CardDeck` shuffled by its own `Random` seeded from the game's (so a game still replays from one seed and the scripted dice tests are untouched), a partial `ClimateMeter`, quiz and choice phases with `CardResult` holding the fact and sources, and `Player.payCompulsory` for the bankruptcy trigger. Covers FR-10 AC-1 to AC-8, NFR-10 AC-3, and FR-13 AC-1 and AC-2 for Event cards. Waiting for Oscar's approval.

## [2026-11-17] build | Plan 004 Climate cards built

Oscar approved [plan 004](../plans/004-climate-cards.md) and it is now `done`. Added the sealed `ClimateCard` hierarchy, `CardDeck` (its own `Random` seeded from the game's), `CardResult`, a partial `ClimateMeter`, `Player.payCompulsory` and `bankrupt()`, and in `Game` the card drawn on arrival, the quiz and choice phases, `affordableOptions`, `pendingCard` and `lastCard`. 210 tests pass with no warnings; `model` coverage is 99.3%. Deliberate bugs (no reshuffle, unaffordable options allowed, no £0 floor) were each caught. Existing tests that rolled onto square 2 (now a card draw) were changed to roll 3 or stand elsewhere, with nothing they check changed. FR-10 is `built`; NFR-10, FR-08 and FR-13 have build status notes. A bankrupt player keeps playing normally until plan 005.

## [2026-11-17] spec | FR-11, FR-12, FR-13 and FR-17 agreed; disaster lines approved

Found checked sources for the Storm and Drought explanation lines: the Met Office article "The influence of climate change on severe weather" (18 January 2024), on heavier rain from warmer air, slightly stronger winter windstorms, and more frequent and longer droughts. The Drought line was rewritten to say only what that source says. Oscar approved all four lines ([disaster explanations](rules/climate-meter-and-disasters.md#disaster-explanations)). He also decided that a bankrupt player who rolls to return then takes a normal roll in the same turn (FR-13). FR-11, FR-12, FR-13 and FR-17 are now `agreed`.

## [2026-11-17] plan | Plan 005 end of round, disasters and bankruptcy drafted

Drafted [plan 005](../plans/005-end-of-round-disasters-and-bankruptcy.md): `Game.endRound()` with the six steps in order and a `RoundReport`, a complete `ClimateMeter`, `DisasterManager` with next round's effects, the explanations in the working `config.json` (validated, NFR-10 AC-2), full bankruptcy with a return roll and automatic return, and `GAME_OVER` after round 12. It also finishes the end-of-round parts of FR-05 to FR-08 and FR-03 AC-6. Waiting for Oscar's approval.

## [2026-11-18] build | Plan 005 end of round, disasters and bankruptcy built

Oscar approved [plan 005](../plans/005-end-of-round-disasters-and-bankruptcy.md) and it is now `done`. Added `Game.endRound()` with the six FR-11 steps in order and a `RoundReport`; a complete `ClimateMeter`; `Disaster`, `DisasterManager` and `DisasterResult` with lasting effects whose durations come from the data; full bankruptcy (`AWAITING_RETURN_ROLL`, `rollToReturn()`, the automatic return, the loan); `GAME_OVER` after round 12; and the `incomeBonus`/`natureBonusGP` character hooks. The approved explanations and sources, and `DROUGHT.lostGPRounds`, are in the working `config.json` and validated. 268 tests pass with no warnings; `model` coverage is 99.4%. Five deliberate bugs were each caught, and one seeded game's round reports were read against the rules. FR-03, FR-05 to FR-08 and FR-11 to FR-13 are now `built`; FR-17 and NFR-10 keep notes for their remaining criteria. Found: bankruptcy almost never happens in headless play (issue 4 in [build progress](operations/build-progress.md)), a balance question for playtests.

## [2026-11-18] spec | FR-14 and FR-16 agreed

Oscar answered: a player's share of the polluting squares (FR-16 AC-2) counts polluting-square-rounds across the whole game, so a late conversion doesn't erase earlier pollution; and the computer players for NFR-06's 20 games also measure balance through a `--simulate` option (recorded on NFR-06). FR-14 and FR-16 are now `agreed`.

## [2026-11-18] plan | Plan 006 scoring, end-of-game report and balance simulation drafted

Drafted [plan 006](../plans/006-scoring-and-balance-simulation.md): `Scoring`, `GameResult` and `LegacyScore` (stored as score × 5 so comparisons are exact), the tie-breaks with a die roll-off, the end-of-game report data, a `sim` package with random, green and polluter strategies, `Simulator`, `BalanceReport` and `--simulate N`. Covers FR-14 AC-1 to AC-6, FR-16 AC-1 to AC-3 as data, and NFR-06 AC-2 and AC-3. Waiting for Oscar's approval.

## [2026-11-18] build | Plan 006 scoring, end-of-game report and balance simulation built

Oscar approved [plan 006](../plans/006-scoring-and-balance-simulation.md) and it is now `done`; **the rules engine is complete**. Added `LegacyScore` (score × 5, compared exactly), `GameResult` and `Scoring` (both formulas, every tie-break, a re-rolling die roll-off), polluting-square-rounds for the report's shares, and the `sim` package: three strategies, `Simulator` and `BalanceReport`, with `--simulate [games]`. 295 tests pass with no warnings; coverage is 99.4% (`model`), 99.1% (`config`) and 99.0% (`sim`, now in the coverage rule). Four deliberate scoring bugs were each caught. In 5,000 simulated games, characters win 18-22% each, the green strategy 40% and the pure polluter 0%, Belfast reaches net zero in 58%, 19% and 0% of mostly-green, mixed and mostly-polluting games, and bankruptcy happens in 3% of games; recorded on [balance and design history](rules/balance-and-history.md#java-simulation-plan-006) next to the Python results, with no values changed. FR-14 and NFR-06 are `built`; FR-16's data is built and its drawing is plan 008.

## [2026-11-18] spec | FR-15, NFR-02, NFR-04, NFR-05 and NFR-11 agreed

Oscar answered: every player's Resilience level and co-funding deal are shown compactly on the player panel, not only the current player's (new FR-15 AC-6); "How to play" is 4-5 short illustrated screens rather than a guided first turn, with numbers taken from the data; and NFR-05 AC-4 (every action by keyboard alone) is kept. FR-15, NFR-02, NFR-04, NFR-05 and NFR-11 are now `agreed`; the screens page records the panel change.

## [2026-11-18] plan | Plan 007 game screens drafted

Drafted [plan 007](../plans/007-game-screens.md): title, How to play, setup and game screens in Swing, with a `GameController` that calls the model and refreshes every view once per action, presenter classes (`StatusText`, `ActionOptions`, `HowToPlay`, `BoardLayout`, `Palette`) that hold the testable logic headless, pop-ups chosen by the model's phase, keyboard shortcuts, `--seed`, and a plain final-scores screen until plan 008. Proposes a minimum window of 1280×720 for Oscar to confirm. Waiting for Oscar's approval.

## [2026-11-18] build | Plan 007 game screens built

Oscar approved [plan 007](../plans/007-game-screens.md) with a minimum window of 1280×720, and it is built; it stays `approved` until Oscar has played a round by hand. Added the title, How to play (five pages, 279 words, numbers from the data), setup, game and final-scores screens in Swing; `GameController` as the only door into the model; presenter classes holding the testable logic; `Palette` with a contrast test on every text pair; keyboard shortcuts; and `--seed N`. 351 tests pass with no warnings; four deliberate UI bugs were each caught. Rendering the screens to images found a crash on Start (a bare `JComponent` has no accessibility context; issue 5 in [build progress](operations/build-progress.md)), fixed with a new `ScreensTest`, and several layout problems, fixed. The window opens in 1.1 seconds and idles at about 4% of one core. FR-01, FR-15, NFR-05 and NFR-11 are `built`; NFR-02 and NFR-04 have build status notes for their user test and hand timing. Updated the [screens page](ux/screens-and-layout.md) (now `built`, with the keys and colour tokens), the [system overview](architecture/system-overview.md), [game flow](architecture/game-flow.md), [local development](operations/local-development.md), `src/AGENTS.md`, the README, the overview, the catalogue and the index.

## [2026-11-18] decide | Q-10 balance: wait for playtests

Oscar read plan 006's simulation results and decided to change no balance values until real players have tried the game. Recorded on [Q-10](rules/open-questions.md), [balance and design history](rules/balance-and-history.md#java-simulation-plan-006) and [build progress](operations/build-progress.md), with the two things to watch in playtests: net zero in mixed games and the Farmer's win rate.

## [2026-11-18] plan | Plan 008 visual polish drafted

After trying plan 007, Oscar asked for the game to look "more sleek and like an actual online game". He chose a dark game theme, light animation, hand-painted Swing (no theme library, so no ADR) and a bundled free font. Drafted [plan 008](../plans/008-visual-polish.md): a dark `Palette` still contrast-tested, Inter (SIL Open Font License) in the jar with a fallback, painted buttons, cards and icons, and an `Animator` whose one timer stops when the last animation ends (NFR-11 AC-2), with pop-ups waiting for animations through a `SecondaryLoop`. The proposed end-of-game report and packaging plans move from 008 and 009 to 009 and 010; live pages and two code comments were updated, and finished plans and earlier log entries keep their wording. Proposes that one play-through after plan 008 covers plan 007 too. Waiting for Oscar's approval.

## [2026-11-18] build | Plan 008 visual polish built

Oscar approved [plan 008](../plans/008-visual-polish.md), agreeing that one play-through covers plans 007 and 008, and that Inter is downloaded once from its official release. Both plans stay `approved` until that play-through.

What changed:
- **Look:** a dark theme in `Palette`, still contrast-tested on every pair; the Inter font (SIL Open Font License) in the jar, with a fallback; painted buttons, cards, icons and player cards; a skyline title screen; and pop-ups with coloured headers over a dimmed game.
- **Animation:** an `Animator` whose one timer stops when the last animation ends. Dice tumble, counters slide along their real path, and the meter eases. Pop-ups wait for animations through a `SecondaryLoop`, and any key or click skips them.

Checks:
- 365 tests pass with no warnings, and five deliberate bugs were each caught.
- The new tests caught two real bugs: skip left the chained slide running, and a declared contrast pair failed.
- Rendering the screens at both sizes found layout problems, all fixed. At 1280×720, small tiles now show the type by icon, which NFR-05 AC-2 allows.
- The window opens in 1.2 seconds and idles at about 1.3% of one core.

Updated the [screens page](ux/screens-and-layout.md) (look, animation, keys and tokens), NFR-04, NFR-05 and NFR-11 build notes, the [system overview](architecture/system-overview.md) (the Inter licence), `src/AGENTS.md`, the README, the overview, build progress and the index.
