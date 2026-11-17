# Net Zero Belfast — Handover for Claude Code

Handover of every design decision made so far, for building the game. Read this first, then `docs/rules_v4.md` for the player-facing wording. Where this file and the rules disagree, **this file wins** (it includes clarifications agreed after v4).

- **Project:** CSC2058 (Software Engineering and Systems Development), QUB. Team of 6. Educational digital board game on **UN SDG 13: Climate Action**.
- **Status (17 Nov 2026):** Rules v4 agreed, 30-card deck written, SUB1a Interim System Report drafted (requirements, use cases, class and sequence diagrams). No code yet.
- **Owner of this handover:** Oscar (GitHub: oscarilland).

---

## 1. Ground rules for working on this repo (read before coding)

These come from the module handbook (`docs/CSC2058_Module_Handbook.pdf`) and matter for marks:

1. **Repo is on QUB GitLab, not GitHub.** Staff need *Maintainer* access. GitHub repos do not count as evidence.
2. **Commit history is used to judge each student's individual contribution.** Each team member must make and commit their own work. Do not batch other people's work into one person's commits, and do not rewrite history.
3. **Generative AI use must be declared** in the team's submissions, per the module's AI policy on Canvas. Rules, simulation, cards, diagrams and report text were drafted with Claude; code produced with Claude Code must be declared the same way. Team members must understand, review and be able to explain any code they commit.
4. Automated testing is a core module requirement (target: JUnit, ≥80% line coverage of game logic — NFR-06).

---

## 2. The game in one paragraph

4–6 players (ages 13–16) share one computer. Each picks one of six characters and moves around a 36-square board of real Belfast districts for 12 rounds (2026 → 2050, 2 years per round, 1 turn per player per round). They buy **Polluting** squares (cheap, high income, harm the climate) or **Green Projects** (dearer, earn Green Points), convert, co-fund, lobby, buy Resilience and draw Climate Cards. A shared **Climate Meter** rises with pollution and falls with green projects; its danger band drives disasters. If Belfast reaches **net zero** (meter ≤ 0) at the end, the winner has the highest GP + money ÷ 5. If it misses, **money is worthless** and only GP counts. The core lesson is the tragedy of the commons: polluting pays individually but costs everyone.

Learning outcomes (from the report): **LO1** NI's legal targets (48% cut by 2030, net zero by 2050) · **LO2** NI's main emission sources · **LO3** short-term profit vs shared long-term harm · **LO4** mitigation (Green Projects) vs adaptation (Resilience) · **LO5** delay makes impacts costlier.

---

## 3. Rules specification (v4 + agreed clarifications)

All numbers are in `data/config.json` and must be **read from data, not hard-coded** (NFR-07). Money is in £M (integers).

### 3.1 Setup
- 4–6 players, each a **different** character. First player chosen at random; then fixed order.
- Year 2026, round 1, Climate Meter 50.

| Character | Money | GP | Ability |
|---|---|---|---|
| Industrialist | 40 | 8 | **Just transition:** a polluting square they convert earns an extra £2M per round (on top of normal Green Project income) |
| Tech Entrepreneur | 36 | 10 | Green Projects cost £3M less to buy |
| Council Planner | 38 | 12 | Each Resilience level costs £2M less |
| Farmer | 36 | 13 | Nature projects earn +1 GP per round |
| Student Commuter | 34 | 16 | Glider is free, and each Glider ride earns +1 GP |
| Community Activist | 32 | 18 | Lobbying is free, and a lobbied square adds nothing to the Climate Meter that round |

Starting Legacy Scores are deliberately unequal; balance was set by simulated win rates (each 17–24%).

### 3.2 Squares (36 total; layout in `data/board.json`, picture in `design/board_layout.png`)

| Type | Count | Price | Each round (if owned) |
|---|---|---|---|
| Polluting | 10 | £4–7M | +£7M; carbon tax −1 GP; meter +2 |
| Green Project | 12 (4 nature) | £8–12M | +£2M; +2 GP; meter −1 |
| Resilience | 4 | £3/£5/£7M per level | personal upgrade (see 3.4) |
| Glider Stop | 3 | £1M fare | move to any other Glider Stop |
| Climate Card | 7 | free | draw a card |

- **Owned squares cannot be bought.** On another player's **Green Project** you may **co-fund**; on their **Polluting** square you may **lobby**.
- **Convert:** owner turns a Polluting square into a Green Project for £5M. **Max one conversion per turn** (either on landing or anywhere).
- **Co-fund:** pay £3M to the bank; funder and owner each get +1 GP per round. **One active deal per player** (a new deal ends the old). Pays nothing in a round the square is closed by a Storm; continues if the owner is bankrupt. Polluting squares **cannot** be co-funded.
- **Lobby:** £2M (Activist: free) — the polluting square earns no money at the end of **this** round. Activist's lobbying also cancels that square's +2 meter for the round.
- **Board shape:** a clockwise loop of 30 squares (ids 0–29) through North, Harbour & Titanic Quarter, East, South and West Belfast, plus a 6-square **City Centre** cut-through (ids 30–35) between two junctions: **Glider East (12)** and **Glider West (27)**. At a junction the player chooses: stay on the loop or take the cut-through.

### 3.3 Turn
1. (If bankrupt: see 3.6.)
2. Roll **two dice**, move clockwise; choose route at a junction.
3. Act on the square landed on: buy / co-fund / lobby / buy a Resilience level / ride the Glider / draw a Climate Card.
4. If you did not convert in step 3, you may convert one Polluting square you own anywhere.
5. End turn. After the last player, run End Round.

### 3.4 Resilience
Personal insurance, bought one level at a time when landing on a Resilience square. Level 1/2/3 cuts **the money you pay for disasters** by 25% / 50% / 75%. It does **not** prevent lost income, closed squares, Droughts, or **Event-card** costs. Round costs to whole £M (suggest: round half up — confirm).

### 3.5 End of round — **this order is a requirement (FR-11)**
1. Pay income and GP from squares and co-funding deals (skip closed, flooded and lobbied squares; nature projects get no GP in a Drought round; Farmer +1 GP per nature project; Industrialist-converted squares +£2M; bankrupt players receive **no money** but still get GP).
2. **Carbon tax:** each owner loses 1 GP per Polluting square owned.
3. **Climate Meter:** `+3` (existing emissions) `+2 × owned Polluting` (minus any Activist-lobbied this round) `−1 × owned Green Projects`.
4. **Interim target** (only in the **2030** round = round 3, and the **2040** round = round 8): if meter > target (26 in 2030, 11 in 2040), every Polluting square costs its owner 2 GP.
5. **Disaster check:** roll 1d6; disaster if roll ≤ danger band. Pick one of four types at random. Costs are paid **now**; lost income / closures apply **next** round.
6. Year += 2, round += 1. After round 12 (year reaches 2050) → End Game.

**Danger band** (also the disaster cost multiplier): meter ≤10 → 1 · 11–25 → 2 · 26–40 → 3 · 41+ → 4 (below 0 uses band 1).

| Disaster | Effect |
|---|---|
| Flood | Random district **that has owned squares**: its squares earn nothing next round; each owner pays £1M × band per square they own there |
| Storm | Random owned square closes next round; owner **must** pay £2M × band repairs |
| Heatwave | Every player pays £1M × band |
| Drought | Nature projects earn no GP next round |

Resilience cuts each player's disaster payment. Every disaster shows a one-line climate explanation (FR-17).

### 3.6 Bankruptcy
- Triggered when a **compulsory** payment (disaster or Climate Card) would take money below 0: money → 0, remainder written off. Players can never choose to buy something they can't afford.
- While bankrupt: no movement or actions; squares still earn GP and pay carbon tax, but earn **no money**.
- Start of each of their turns: roll 1d6; on a **6** they return. After **2 rounds** they return automatically.
- On return: receive a **£10M loan**; **£15M** is deducted from their money at game end (before scoring).

### 3.7 Climate Cards (`data/climate_cards.json`, 30 cards: 12 quiz, 9 choice, 9 event)
- Draw at random; reshuffle used cards when the pile is empty.
- **Quiz:** 4 answers; correct = +2 GP; wrong = nothing.
- **Choice:** player picks option A or B; apply its `money`, `gp` and `meter` deltas. **Meter changes apply immediately.**
- **Event:** apply `effect` immediately. Resilience does **not** reduce Event costs. A payment below £0 → bankruptcy.
- Always show the card's **fact and source** afterwards (FR-10, NFR-10).

### 3.8 End of game and scoring
- **Net zero reached** (meter ≤ 0 after round 12): `Legacy = GP + (money − loan repayments) / 5`.
- **Missed:** money is worthless; each player loses 5 GP per Polluting square still owned; `Legacy = GP`. End screen states Belfast missed its target.
- **Always exactly one winner:** highest Legacy; ties broken by most Green Projects owned (further ties: TBD — see §7).
- End-of-game report: Climate Meter graph 2026–2050 (keep per-round history), each player's share of the city's polluting squares across the game, scores, winner (FR-16).

---

## 4. Requirements (from SUB1a §1.3 — keep these IDs; tests should reference them)

**Functional (MoSCoW):** FR-01 start game, 4–6 players, distinct characters (M) · FR-02 starting values + random first player (M) · FR-03 two dice, clockwise, junction choice (M) · FR-04 buy unowned square, no buying if unaffordable (M) · FR-05 co-fund £3M, one deal (M) · FR-06 lobby £2M (S) · FR-07 convert one per turn £5M (M) · FR-08 Resilience levels (S) · FR-09 Glider £1M (S) · FR-10 Climate Cards with fact + source (M) · FR-11 end-of-round order (M) · FR-12 disasters by danger band, Resilience cut (M) · FR-13 bankruptcy (S) · FR-14 Legacy Score, one winner, tie-break (M) · FR-15 always show year, round, meter + band, each player's money/GP/squares (M) · FR-16 end-of-game report (M) · FR-17 disaster explanation (S). *(Save/resume was dropped from the report for space; treat as a Could.)*

**Non-functional:** NFR-01 4-player game ≤ 60 min · NFR-02 4/5 first-time players complete a turn unaided after ≤2-min tutorial · NFR-03 text Flesch Reading Ease ≥ 60 · NFR-04 opens ≤ 5 s, responds ≤ 1 s on a lab PC · NFR-05 WCAG 2.1 AA contrast, square types identifiable without colour · NFR-06 JUnit ≥ 80% line coverage of game logic; 20 full test games without crashes · NFR-07 balance values and cards in data files · NFR-08 no personal data beyond display names, no network, validate all input · NFR-09 runs on Windows and macOS with only a Java runtime · NFR-10 every fact stored with a source · NFR-11 < 5% CPU while idle (no busy-waiting).

---

## 5. Design (from SUB1a §2–3 — `design/`)

**Use cases** (`design/use_case_diagram.png`): actor Player. Take Turn «include» Move Player; extended by Buy Square, Co-fund Green Project, Lobby Polluting Square, Buy Resilience Level, Draw Climate Card (extension point *Landing action*), Convert Square (*Conversion*), End Round (*Round complete*). End Round «include» Resolve Disaster; extended by View End-of-Game Report (*Game over*).

**Analysis classes** (`design/class_diagram.png`, source `class_diagram.dot`). The report commits to these names and methods; keep them recognisable in code:

- `Game` (controller): round, year, currentPlayer; `startGame(players)`, `takeTurn()`, `endTurn()`, `endRound()`, `endGame(): Player`. Composes Board, ClimateMeter, CardDeck, Dice, DisasterManager; aggregates 4..6 Player; reads GameConfig.
- `GameConfig`: `load(file)`, `get(key)`.
- `Dice`: `rollTwo()`, `rollOne()`. **Inject a seeded `Random`** so tests are deterministic.
- `Board`: 36 Squares, 6 Districts; `move(from, steps, route): int`, `getSquare(i)`.
- `Square` {abstract}: index, name; `landOn(p, g): Action[]` (polymorphic).
  - `OwnableSquare` {abstract}: price, owner 0..1, closed, lobbied; `buy(p)`, `payOwner()`.
    - `PollutingSquare`: `convert(): GreenProjectSquare`. `GreenProjectSquare`: isNature.
  - `ResilienceSquare` `buyLevel(p)` · `GliderStop` `ride(p, to)` · `ClimateCardSquare`.
- `Player`: name, money, greenPoints, position, resilienceLevel, bankruptRounds, loans; `pay`, `receive`, `addGP`, `payCarbonTax`, `countPolluting`, `legacyScore(netZero)`.
- `Character` {abstract} + 6 subclasses overriding `priceFor(square)`, `incomeBonus(square)` (and similar hooks as needed).
- `CoFundingDeal`: funder Player ↔ GreenProjectSquare; `pay()`.
- `ClimateMeter`: value, history; `update(polluting, green)`, `adjust(delta)`, `dangerBand()`, `meetsTarget(year)`.
- `DisasterManager`: `check(band): Disaster`, `apply(d, players, band)`; enum `Disaster {FLOOD, STORM, HEATWAVE, DROUGHT}`.
- `CardDeck`: `draw()`, `reshuffle()`. `ClimateCard` {abstract} `resolve(p, g)` → `QuizCard` `checkAnswer(i)`, `ChoiceCard` `applyOption(choice)`, `EventCard`.

**Sequence diagrams** (`design/seq_*.png`/`.mmd`): Take Turn, Draw Climate Card, End Round — implement to match these flows.

**UI** (`design/main_screen_wireframe.png`, `design/game_loop.png`): screens Title → Setup (names, characters) → Game screen (board, Climate Meter bar with band and 2030 target marker always visible at top, player panel, only-valid-actions buttons, card/disaster pop-ups showing effects before choosing then fact + source) → End-of-game report.

**Architecture guidance:** keep game logic (model) fully separate from the UI so it can be unit-tested headlessly and driven by tests/simulation; UI calls `Game` and renders state. All randomness through one injectable RNG. Load `config.json`, `board.json`, `climate_cards.json` at startup.

---

## 6. Assumptions in the report (confirm with the team before relying on them)
- **Java** desktop app (JUnit tests), single shared computer (hot-seat), runs on EEECS lab PCs, Windows + macOS. **UI toolkit (JavaFX vs Swing) and Java version are not decided.**
- Playtests with classmates (and ideally 13–16-year-olds) before SUB2 to validate NFR-01–03.

## 7. Open questions (TBD — do not silently decide; ask the team)
1. Tie after the Green Projects tie-break.
2. Can the meter go below 0? (Simulation allowed it; display/band treat < 0 as band 1.)
3. Real unit for the meter (e.g. kt CO₂e scaled to Belfast) — display only.
4. Check the current official **2040** target; 11 assumes a 77% cut.
5. City Centre cut-through: two-way or one-way? Exactly how "choose at a junction" works when passing (not landing on) a junction.
6. **Starting positions** are not defined in the rules (simulation used random squares). Suggest all start on one square — decide.
7. After a Glider ride, does the player act on the destination stop? (Simulation: no further action.)
8. Rounding of Resilience-reduced costs.
9. Final square names, prices within ranges, and district assignments (`board.json` values are placeholders).
10. Farmer is slightly strong in simulation (24% vs fair ~20%) — re-tune after playtests.

## 8. How the numbers were chosen
`reference/balance_sim.py` (Python, reference only) plays thousands of games with simple bots. v4 results: city reaches net zero in 13% / 54% / 73% of games when players mostly pollute / mix / mostly go green; "pollute then convert at the end" wins only 13% (fair ≈ 21%); characters win 17–24%; ~3 squares owned per player; ~6 in 10 turns have an action. Useful for re-tuning `config.json` after playtests. Its simplifications are listed in its header.

**Decision history (why v4 looks like this):** v2 had an always-dead carbon tax, an off-by-one year count and rich characters winning >80%. v3 fixed those but players dodged the pollution penalty by buying in pairs and won by polluting then converting late. v4 introduced the per-square carbon tax, worthless money on failure, polluting +2 vs green −1 on the meter, Green Project income, personal Resilience, band-scaled disasters, a 36-square board and interim targets. Rejected variants: "environmental Monopoly", fully cooperative (brief requires a winner), two turns per round (worse balance, twice as long).

---

## 9. File index

| Path | What it is |
|---|---|
| `HANDOVER.md` | This file — the source of truth for implementation |
| `docs/rules_v4.md` / `.docx` | Player-facing rules v4 (Markdown conversion + original) |
| `docs/SUB1a_Interim_System_Report_draft.docx` | Interim report: requirements, use cases, class/sequence diagrams, wireframe |
| `docs/CSC2058_Module_Handbook.pdf` | Module handbook (assessment, GitLab, testing, AI rules) |
| `data/config.json` | Every balance value (NFR-07) |
| `data/board.json` | 36 squares: id, name, type, district, price, nature flag; loop, junctions, Glider stops |
| `data/climate_cards.json` | 30 cards with structured effects, facts and source URLs |
| `design/use_case_diagram.*` | Use case diagram (PNG + editable draw.io) |
| `design/class_diagram.*` | Analysis class diagram (PNG + Graphviz source) |
| `design/seq_*.{png,mmd}` | Sequence diagrams: Take Turn, Draw Climate Card, End Round (Mermaid source) |
| `design/board_layout.png` | Draft board drawing |
| `design/main_screen_wireframe.png`, `design/game_loop.png` | UI wireframe + screen flow; game loop with learning outcomes |
| `reference/balance_sim.py` | Balance simulation used to tune v4 (reference only) |

Other live material: the Climate Cards Sheet on claude.ai (team-editable; regenerate `climate_cards.json` from it if cards change).
