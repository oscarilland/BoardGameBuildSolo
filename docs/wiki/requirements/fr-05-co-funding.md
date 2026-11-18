---
title: FR-05 Co-funding
type: requirement
id: FR-05
priority: must
status: built
sources: [../sources/sub1a-report.md, ../sources/handover.md]
updated: 2026-11-17
---

# FR-05 Co-funding

## Summary

> On another player's Green Project, the system shall offer co-funding for £3M, allowing one active deal per player. *(SUB1a; verified by JUnit)*

## Behaviour

- On another player's Green Project, the player may pay £3M to the bank to co-fund it.
- At each end of round, the funder and the owner each get +1 GP from the deal.
- One active deal per player: starting a new deal ends the old one.
- A deal pays nothing in a round when its square is closed by a Storm. It continues if the owner is bankrupt.

See [turn and actions](../rules/turn-and-actions.md).

## Acceptance criteria

- **AC-1** Landing on another player's Green Project with £3M or more offers co-funding; paying it takes £3M and creates a deal.
- **AC-2** Co-funding isn't offered on your own square, on any Polluting square, or with less than £3M.
- **AC-3** At the end of the round, a deal gives +1 GP to the funder and +1 GP to the owner.
- **AC-4** A player with a deal who co-funds a second square ends the first deal; only the new one pays.
- **AC-5** In a round when the funded square is closed by a Storm or flooded, neither player gets the deal's GP ([Q-15](../rules/open-questions.md)).
- **AC-6** If the owner is bankrupt, the deal still pays both players their GP.
- **AC-7** Co-funding isn't offered on the square the player is already co-funding, because it would change nothing.

## Out of scope

Deals between more than two players; ending a deal without starting a new one.

## Decisions (Oscar, 17 November 2026)

1. A deal on a flooded square pays nothing, the same as a closed one ([Q-15](../rules/open-questions.md)).
2. Actions that would change nothing aren't offered (AC-7).
3. **Agreed.**

## Build status

Built in [plan 003](../../plans/003-square-actions.md): AC-1, AC-2, AC-4 and AC-7, tested in `CoFundTest`. The deal's GP (AC-3, AC-5, AC-6) was built in [plan 005](../../plans/005-end-of-round-disasters-and-bankruptcy.md), tested in `IncomeTest`.

## Related

- [Domain model: CoFundingDeal](../architecture/domain-model.md)
- [FR-11 End-of-round order](fr-11-end-of-round-order.md)
