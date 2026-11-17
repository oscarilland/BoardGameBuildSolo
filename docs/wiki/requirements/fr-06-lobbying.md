---
title: FR-06 Lobbying
type: requirement
id: FR-06
priority: should
status: draft
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

## Out of scope

Lobbying from a distance (only on landing).

## Open questions for Oscar

1. Should a square already lobbied this round be lobbied again (no extra effect)? Suggest not offering it.

## Related

- [Turn and actions](../rules/turn-and-actions.md)
- [End of round](../rules/end-of-round.md)
