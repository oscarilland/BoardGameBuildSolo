---
title: NFR-04 Responsiveness
type: requirement
id: NFR-04
priority: must
status: agreed
sources: [../sources/sub1a-report.md]
updated: 2026-11-18
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
2. **Agreed** (18 November 2026).

## Build status

[plan 007](../../plans/007-game-screens.md): AC-3 is tested by `ResponsivenessTest` (the fastest of six ends of round on a full six-player board must be under 50 ms). AC-1 was timed on Oscar's laptop on 18 November 2026: the window opened 1.1 seconds after launch. AC-2 is checked by hand in Oscar's play-through, so the page stays `agreed`.

[plan 008](../../plans/008-visual-polish.md): the window still opens in 1.2 seconds with the font loading. Animations start at once and are capped (a counter's slide at 1 second, `AnimatorTest`), and any key or click skips them, so the screen always responds straight away.

## Related

- [NFR-11 Energy efficiency](nfr-11-energy-efficiency.md)
- [ADR-002 UI toolkit](../../decisions/002-ui-toolkit.md)
