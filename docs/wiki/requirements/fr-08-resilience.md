---
title: FR-08 Resilience
type: requirement
id: FR-08
priority: should
status: draft
sources: [../sources/sub1a-report.md, ../sources/handover.md]
updated: 2026-11-17
---

# FR-08 Resilience

## Summary

> On a Resilience square the system shall offer the player's next Resilience level (maximum 3), cutting their disaster payments by 25%, 50% or 75%. *(SUB1a; verified by JUnit)*

## Behaviour

See [board and squares: Resilience](../rules/board-and-squares.md#resilience).

## Acceptance criteria

- **AC-1** A player at level 0 on a Resilience square is offered level 1 for £3M; at level 1, level 2 for £5M; at level 2, level 3 for £7M.
- **AC-2** A player at level 3 is offered nothing.
- **AC-3** Only one level can be bought per landing.
- **AC-4** The Council Planner pays £1M, £3M and £5M.
- **AC-5** With level 2, a Heatwave in band 4 (£4M) costs the player £2M.
- **AC-6** Resilience doesn't reduce Event card costs, lost income, closures or Droughts.
- **AC-7** Reduced costs are rounded half up to whole £M: at level 1, a £2M Storm repair (£1.5M) costs £2M; at level 3, a £2M Heatwave (£0.5M) costs £1M; at level 2, a £3M Flood (£1.5M) costs £2M ([Q-08](../rules/open-questions.md)).

## Out of scope

Shared or district Resilience (the v3 rule).

## Decisions (Oscar, 17 November 2026)

1. **Rounding:** half up to whole £M ([Q-08](../rules/open-questions.md)).

## Related

- [FR-12 Disasters](fr-12-disasters.md)
- [Learning outcomes: LO4](../learning-outcomes.md)
