---
title: FR-12 Disasters
type: requirement
id: FR-12
priority: must
status: draft
sources: [../sources/sub1a-report.md, ../sources/handover.md]
updated: 2026-11-17
---

# FR-12 Disasters

## Summary

> The system shall trigger a disaster with the probability set by the Climate Meter's danger band, scale its cost by that band, and reduce each player's cost by their Resilience level. *(SUB1a; verified by JUnit)*

## Behaviour

See [Climate Meter and disasters](../rules/climate-meter-and-disasters.md).

## Acceptance criteria

- **AC-1** The danger band is 1 for a meter of 10 or less (including below 0), 2 for 11-25, 3 for 26-40 and 4 for 41 or more. Tested at 0, −3, 10, 11, 25, 26, 40 and 41.
- **AC-2** A disaster happens when a 1d6 roll is less than or equal to the band. With a seeded die, band 2 and a roll of 2 gives a disaster; a roll of 3 doesn't.
- **AC-3** The type is chosen uniformly at random from the four. Over 4,000 seeded disasters, each type occurs between 900 and 1,100 times.
- **AC-4** Flood in band 3: a random district with owned squares is chosen; each owner pays £3M per square they own there; those squares earn nothing next round.
- **AC-5** Storm in band 2: a random owned square closes next round; its owner pays £4M.
- **AC-6** Heatwave in band 4: every player pays £4M, less their own Resilience cut.
- **AC-7** Drought: nature projects earn no GP next round. No money is paid.
- **AC-8** Disaster payments are compulsory and can cause bankruptcy ([FR-13](fr-13-bankruptcy.md)).
- **AC-9** The disaster's explanation is shown ([FR-17](fr-17-disaster-explanation.md)).

## Out of scope

More than one disaster per round.

## Open questions for Oscar

1. [Q-17](../rules/open-questions.md): Flood or Storm with no owned squares.
2. [Q-08](../rules/open-questions.md): rounding of Resilience-reduced costs.

## Related

- [FR-08 Resilience](fr-08-resilience.md)
- [Domain model: DisasterManager](../architecture/domain-model.md)
