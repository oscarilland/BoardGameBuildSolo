---
title: FR-13 Bankruptcy
type: requirement
id: FR-13
priority: should
status: draft
sources: [../sources/sub1a-report.md, ../sources/handover.md]
updated: 2026-11-17
---

# FR-13 Bankruptcy

## Summary

> The system shall make a player bankrupt when a compulsory payment would take their money below £0, and apply the bankruptcy rules. *(SUB1a; verified by JUnit)*

## Behaviour

See [bankruptcy](../rules/bankruptcy.md).

## Acceptance criteria

- **AC-1** A player with £3M who must pay £5M for a disaster ends with £0 and is bankrupt. The £2M shortfall is written off.
- **AC-2** A player with £5M who must pay exactly £5M ends with £0 and isn't bankrupt.
- **AC-3** A bankrupt player doesn't move or act on their turn, unless they return that turn.
- **AC-4** At the start of a bankrupt player's turn one die is rolled; on a 6 they return.
- **AC-5** A player who goes bankrupt in round 5 rolls at their round 6 turn; on anything but a 6 they sit out, then return automatically at their round 7 turn without rolling ([Q-14](../rules/open-questions.md)).
- **AC-6** On return they receive £10M, their loan repayments rise by £15M, and they take the rest of that turn normally.
- **AC-7** While bankrupt their squares give GP and charge carbon tax, but pay no money.
- **AC-8** No voluntary action (buy, co-fund, lobby, convert, Resilience, Glider, a Choice card option) can make a player bankrupt.

## Out of scope

Eliminating bankrupt players.

## Decisions (Oscar, 17 November 2026)

1. Automatic return at the second turn after bankruptcy; at most one missed turn ([Q-14](../rules/open-questions.md)).
2. Choice card options can't bankrupt; unaffordable ones are disabled ([Q-12](../rules/open-questions.md)).

## Build status

The trigger is built for Event cards in [plan 004](../../plans/004-climate-cards.md): AC-1 and AC-2 (`EventCardTest`), and AC-8 for Choice cards. Everything else, and agreeing this page, is plan 005.

## Related

- [FR-14 Legacy Score and winner](fr-14-legacy-score-and-winner.md)
- [Game flow: Take Turn](../architecture/game-flow.md)
