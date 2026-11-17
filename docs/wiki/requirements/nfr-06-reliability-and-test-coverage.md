---
title: NFR-06 Reliability and test coverage
type: requirement
id: NFR-06
priority: must
status: draft
sources: [../sources/sub1a-report.md, ../sources/module-handbook.md]
updated: 2026-11-17
---

# NFR-06 Reliability and test coverage

## Summary

> Reliability: game-logic classes shall have JUnit tests with at least 80% line coverage, and 20 complete test games shall run with no crashes. *(SUB1a; verified by JUnit and coverage)*

The module handbook also asks for manual and automated testing "with clear evidence of test coverage".

## Acceptance criteria

- **AC-1** The build measures line coverage of the `model` and `config` packages and fails if it's below 80% ([ADR-003](../../decisions/003-build-tool-and-java-version.md)).
- **AC-2** A test plays 20 complete seeded games headlessly with bots (4, 5 and 6 players, a mix of strategies) to the end-of-game result, with no exceptions.
- **AC-3** Every finished game in AC-2 has exactly one winner, every player's money is 0 or more until loan repayments, and the meter history has 13 values.

## Out of scope

Coverage of UI classes (they're tested by hand).

## Related

- [Balance and design history](../rules/balance-and-history.md)
- [System overview](../architecture/system-overview.md)
