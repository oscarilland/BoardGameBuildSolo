---
title: FR-09 Glider
type: requirement
id: FR-09
priority: should
status: draft
sources: [../sources/sub1a-report.md, ../sources/data-files.md]
updated: 2026-11-17
---

# FR-09 Glider

## Summary

> On a Glider Stop the system shall let the player pay £1M to move to another Glider Stop. *(SUB1a; verified manually)*

The SUB1a use case diagram leaves the Glider out for readability.

## Behaviour

- On Glider East (12), Glider West (27) or Glider City Hall (30), the player may pay £1M to move to either of the other two.
- The Student Commuter rides free and gains +1 GP per ride.

## Acceptance criteria

- **AC-1** A player on square 12 with £1M can ride to 27 or 30, and pays £1M.
- **AC-2** A player with £0 isn't offered the ride (except the Student Commuter).
- **AC-3** The Student Commuter pays £0 and gains +1 GP for each ride.
- **AC-4** Riding is optional.
- **AC-5** After the ride, the player takes the agreed action on the destination ([Q-07](../rules/open-questions.md)).

## Out of scope

Passing over a Glider Stop doesn't allow a ride; only landing does.

## Open questions for Oscar

1. [Q-07](../rules/open-questions.md): no further action after a ride (as the simulation did)?

## Related

- [Turn and actions](../rules/turn-and-actions.md)
- [Board and squares](../rules/board-and-squares.md)
