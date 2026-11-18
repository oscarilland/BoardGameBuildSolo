---
title: FR-06 Lobbying
type: requirement
id: FR-06
priority: should
status: built
sources: [../sources/sub1a-report.md, ../sources/handover.md]
updated: 2026-11-17
---

# FR-06 Lobbying

## Summary

> On another player's Polluting square, the system shall offer lobbying for £2M, which stops that square's income for the round. *(SUB1a; verified by JUnit)*

## Behaviour

- On another player's Polluting square, the player may pay £2M to lobby it. The square earns no money at the end of **this** round.
- The Community Activist lobbies for free, and their lobbying also stops that square adding +2 to the Climate Meter this round.

## Acceptance criteria

- **AC-1** Lobbying costs £2M, and the lobbied square's owner gets no £7M at the end of that round.
- **AC-2** The lobby lasts one round: the square pays normally at the end of the next round.
- **AC-3** A lobbied square still costs its owner 1 GP of carbon tax.
- **AC-4** Lobbying by anyone except the Activist leaves the square's +2 meter rise in place.
- **AC-5** The Activist's lobbying costs £0, and the meter update that round counts one fewer Polluting square.
- **AC-6** Lobbying isn't offered on your own square or on a Green Project, or with less than £2M (except for the Activist).
- **AC-7** Lobbying isn't offered on a square already lobbied this round, because it would change nothing.

## Out of scope

Lobbying from a distance (only on landing).

## Decisions (Oscar, 17 November 2026)

1. A square already lobbied this round isn't offered for lobbying again (AC-7).
2. **Agreed.**

## Build status

Built in [plan 003](../../plans/003-square-actions.md): the cost and the lobbied mark (AC-1), the mark clearing at the end of the round (AC-2), the Activist's free lobbying (AC-5), and AC-6 and AC-7, tested in `LobbyTest`. The lost income and meter effects (rest of AC-1, AC-3, AC-4) were built in [plan 005](../../plans/005-end-of-round-disasters-and-bankruptcy.md), tested in `IncomeTest`, `CarbonTaxTest` and `MeterUpdateTest`.

## Related

- [Turn and actions](../rules/turn-and-actions.md)
- [End of round](../rules/end-of-round.md)
