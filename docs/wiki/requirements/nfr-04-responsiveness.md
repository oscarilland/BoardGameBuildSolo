---
title: NFR-04 Responsiveness
type: requirement
id: NFR-04
priority: must
status: draft
sources: [../sources/sub1a-report.md]
updated: 2026-11-17
---

# NFR-04 Responsiveness

## Summary

> Responsiveness: on a standard EEECS lab PC the game shall open within 5 seconds and respond to every action within 1 second. *(SUB1a; verified by timed test)*

## Acceptance criteria

- **AC-1** From launching the jar to the title screen takes 5 seconds or less on the reference PC (Oscar's laptop for this solo version).
- **AC-2** Every action (roll, buy, card choice, end turn, end of round) updates the screen within 1 second.
- **AC-3** A headless end-of-round on a seeded 6-player game takes under 50 ms (a proxy test that can run in CI).

## Decisions (Oscar, 17 November 2026)

1. **Reference PC:** Oscar's own laptop. The solo version isn't run on EEECS lab PCs ([ADR-003](../../decisions/003-build-tool-and-java-version.md)).

## Related

- [NFR-11 Energy efficiency](nfr-11-energy-efficiency.md)
- [ADR-002 UI toolkit](../../decisions/002-ui-toolkit.md)
