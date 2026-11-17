---
title: System overview
type: architecture
status: draft
sources: [../sources/handover.md, ../sources/sub1a-report.md]
updated: 2026-11-17
---

# System overview

## Summary

A single Java 21 desktop application, built with Maven, with four parts: a **headless game model** holding every rule, a **config loader** for the three data files, a thin **UI** on top, and a **simulation driver** that plays the model with bots for testing and re-tuning. The design follows the handover's guidance: model separate from UI, all randomness through one injectable source, data loaded at start-up.

## Components

```text
            ┌──────────────── ui ─────────────────┐
            │ Title · Setup · Game · Pop-ups ·     │   shows state, offers valid actions,
            │ End-of-game report                   │   passes choices back
            └──────────────┬───────────────────────┘
                           │ calls Game, reads state
┌── sim ───────┐   ┌──────▼──────────── model ───────────────┐
│ bots, 20-game│──▶│ Game · Board · Square… · Player ·        │
│ headless run │   │ Character… · ClimateMeter ·              │
└──────────────┘   │ DisasterManager · CardDeck · ClimateCard…│
                   │ CoFundingDeal · Dice(Random)             │
                   └──────▲───────────────────────────────────┘
                          │ built from
                   ┌──────┴──── config ────┐
                   │ GameConfig + loaders   │ ◀── config.json, board.json,
                   │ validate on load       │     climate_cards.json (resources)
                   └────────────────────────┘
```

| Part | Responsibility | Page |
|---|---|---|
| `model` | Every rule: turns, actions, end of round, meter, disasters, bankruptcy, cards, scoring | [Domain model](domain-model.md), [game flow](game-flow.md) |
| `config` | Read and validate the data files; build the board, characters and deck | [Data files](data-files.md) |
| `ui` | Screens and pop-ups in Swing ([ADR-002](../../decisions/002-ui-toolkit.md)) | [Screens and layout](../ux/screens-and-layout.md) |
| `sim` | Bots and a headless runner for NFR-06's 20 games and balance checks | [Balance and design history](../rules/balance-and-history.md) |

## Key design choices

1. **Model knows nothing about the UI** (root `AGENTS.md` hard rule 3). This makes the rules unit-testable ([NFR-06](../requirements/nfr-06-reliability-and-test-coverage.md)) and lets bots drive the same code as players.
2. **One seeded random source**, injected into `Dice` and used for every random choice (first player, dice, disaster roll and type, flood district, storm square, card shuffle). A seed replays a whole game exactly.
3. **Data, not code, for balance** ([NFR-07](../requirements/nfr-07-data-driven-balance.md)). Validated on load so a bad edit fails loudly.
4. **The model returns choices instead of waiting for them** ([game flow](game-flow.md)), so it never blocks on input (NFR-11).
5. **Keep the analysis model's names** so the code can be traced to the SUB1a report ([domain model](domain-model.md)).

## Dependencies

| Library | Version | Licence | Why | Decision |
|---|---|---|---|---|
| Swing | Part of the JDK | JDK licence | The UI; no extra dependency | [ADR-002](../../decisions/002-ui-toolkit.md) (accepted) |
| JUnit 5 | TBD | EPL 2.0 | Tests (test scope only) | Module requirement |
| Maven (through the wrapper) | TBD in plan 001 | Apache 2.0 | Build, test, package (build only) | [ADR-003](../../decisions/003-build-tool-and-java-version.md) (accepted) |
| JaCoCo | TBD | EPL 2.0 | Coverage (build only) | [ADR-003](../../decisions/003-build-tool-and-java-version.md) (accepted) |
| Gson | Pinned in plan 001 | Apache 2.0 | Reading the data files; the only runtime library, used only in `config` | [ADR-004](../../decisions/004-reading-the-json-data-files.md) (accepted) |

## Failure modes

- **Bad data file:** start-up stops with a message naming the file and key.
- **Rules bug:** caught by unit tests and the 20-game headless run; a seed reproduces it.
- **UI shows an illegal action:** the model rejects it anyway ([NFR-08](../requirements/nfr-08-privacy-and-security.md)).

## Explain it in 30 seconds

*Draft for Oscar to reword.* The game is a model-view split. All the rules live in plain Java classes that don't know a screen exists, so I can test every rule with JUnit and let bots play thousands of games. The numbers come from JSON files, so balancing is a data change. The UI only shows the model's state and sends the player's choices back.

## Questions a marker might ask

- "Why separate the model from the UI?" Testability (80% coverage of logic), and the bots for NFR-06 drive the exact same code as the UI.
- "How do you test something random?" Every random choice comes from one seeded source, so a test fixes the seed and the result is deterministic.

## Related

- [Overview](../overview.md)
- [src/AGENTS.md coding rules](../../../src/AGENTS.md)
