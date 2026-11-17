---
title: FR-02 Starting values and first player
type: requirement
id: FR-02
priority: must
status: built
sources: [../sources/sub1a-report.md, ../sources/data-files.md]
updated: 2026-11-17
---

# FR-02 Starting values and first player

## Summary

> The system shall give each character the starting money, Green Points (GP) and ability defined in the rules, and choose the first player at random. *(SUB1a; verified by JUnit)*

## Behaviour

- Each player starts with their character's `startMoney` and `startGP` from `config.json`, and its ability.
- The first player is chosen with the game's random source; play then follows a fixed order.
- The game starts in year 2026, round 1, with the Climate Meter at 50.

See [setup and characters](../rules/setup-and-characters.md).

## Acceptance criteria

- **AC-1** Each of the six characters starts with exactly the money and GP in `config.json` (Industrialist £40M and 8 GP ... Community Activist £32M and 18 GP).
- **AC-2** Changing a character's `startMoney` in the data file changes the starting money with no code change.
- **AC-3** With a fixed seed the first player is always the same; across 600 seeded games with 6 players, each player position goes first between 70 and 130 times.
- **AC-4** After the first player, turns follow a fixed order that doesn't change during the game.
- **AC-5** A new game starts at year 2026, round 1, meter 50, read from `config.json`.
- **AC-6** Every player starts on square 0 and takes no landing action there at the start; their first roll moves them on ([Q-06](../rules/open-questions.md)).

## Out of scope

Choosing the first player by a dice roll-off on screen.

## Decisions (Oscar, 17 November 2026)

1. **Starting position:** everyone on square 0, with no landing action ([Q-06](../rules/open-questions.md)).
2. **Agreed.**

## Build status

**Built** in [plan 002](../../plans/002-players-characters-and-movement.md): AC-1 to AC-6, tested in `GameStartTest`.

## Related

- [FR-01 Start game](fr-01-start-game.md)
- [NFR-07 Data-driven balance](nfr-07-data-driven-balance.md)
