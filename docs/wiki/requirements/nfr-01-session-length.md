---
title: NFR-01 Session length
type: requirement
id: NFR-01
priority: must
status: draft
sources: [../sources/sub1a-report.md]
updated: 2026-11-17
---

# NFR-01 Session length

## Summary

> Session length: a 4-player game shall finish within 60 minutes (one school lesson), measured over 5 playtests. *(SUB1a; verified by playtest)*

## Behaviour

12 rounds × 4 players = 48 turns plus 12 end-of-round sequences. That leaves about 70 seconds per turn including reading cards. The UI must keep turns quick: one click to roll, only valid actions shown, short pop-ups.

## Acceptance criteria

- **AC-1** Across 5 timed 4-player games with first-time players, every game finishes within 60 minutes from the setup screen to the end-of-game report.
- **AC-2** The game records its own duration (start and end time) so playtests can be timed without a stopwatch. No other data is stored ([NFR-08](nfr-08-privacy-and-security.md)).

## Out of scope

A shorter "quick game" mode.

## Open questions for Oscar

1. This is a team playtest requirement. For the solo build, will you run your own timed playtests, or treat it as a design target only?

## Related

- [Balance and design history](../rules/balance-and-history.md)
- [NFR-02 Learnability](nfr-02-learnability.md)
