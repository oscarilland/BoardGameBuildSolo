---
title: FR-01 Start game
type: requirement
id: FR-01
priority: must
status: draft
sources: [../sources/sub1a-report.md]
updated: 2026-11-17
---

# FR-01 Start game

## Summary

> The system shall let 4-6 players start a game, each entering a name and choosing a different one of the six characters. *(SUB1a; verified by JUnit and manual test)*

## Behaviour

- The setup screen asks for the number of players (4-6, from `game.minPlayers` and `game.maxPlayers`), then a name and a character for each.
- A character already chosen can't be chosen again.
- The game can't start until every player has a valid name and a character.

See [setup and characters](../rules/setup-and-characters.md).

## Acceptance criteria

- **AC-1** A game can be started with 4, 5 or 6 players. Starting with 3 or 7 is rejected.
- **AC-2** Two players can't choose the same character; the model rejects it even if the UI allows it.
- **AC-3** A name that is empty or only spaces is rejected. Names are trimmed and limited in length ([NFR-08](nfr-08-privacy-and-security.md)).
- **AC-4** Two players can't have the same name (after trimming, ignoring case), so the player panel is never ambiguous.

## Out of scope

Saving player profiles between games.

## Open questions for Oscar

1. Maximum name length (suggest 20 characters)?
2. AC-4 (unique names) isn't in the SUB1a wording. Keep it?

## Related

- [FR-02 Starting values and first player](fr-02-starting-values-and-first-player.md)
- [Screens and layout](../ux/screens-and-layout.md)
