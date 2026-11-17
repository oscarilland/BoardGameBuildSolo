---
title: ADR-002 Desktop UI toolkit
type: decision
id: ADR-002
status: accepted
sources: [../wiki/sources/handover.md, ../wiki/sources/design-diagrams.md]
updated: 2026-11-17
---

# ADR-002 - Desktop UI toolkit

**Status:** accepted 17 November 2026 (proposed the same day). **Swing.**

## Context

The game is a hot-seat desktop app: one board drawn from 36 squares, an always-visible Climate Meter bar, a player panel, action buttons, pop-ups for cards and disasters, and an end-of-game line graph of the meter ([screens and layout](../wiki/ux/screens-and-layout.md)). The handover leaves "JavaFX vs Swing" undecided.

Constraints from the requirements:

- [NFR-09](../wiki/requirements/nfr-09-portability.md): runs on Windows and macOS **with only a Java runtime installed**.
- [NFR-04](../wiki/requirements/nfr-04-responsiveness.md): opens within 5 seconds on a lab PC.
- [NFR-05](../wiki/requirements/nfr-05-accessibility.md): WCAG 2.1 AA contrast; square types identifiable without colour.
- [NFR-11](../wiki/requirements/nfr-11-energy-efficiency.md): no busy-waiting.
- [FR-16](../wiki/requirements/fr-16-end-of-game-report.md): a meter graph.

## Options

### Option A - Swing

Part of every JDK and JRE. The board and graph are drawn with `Graphics2D`.

- **For:** no dependency, so NFR-09 is met by a single jar on any OS. Event-driven, so NFR-11 is natural. Familiar from first-year Java at QUB.
- **Against:** looks dated unless styled. The meter graph must be drawn by hand (about 60 lines). Scaling on high-DPI screens needs care.

### Option B - JavaFX (OpenJFX)

Not part of the JDK since Java 11; added as a library.

- **For:** modern look, CSS styling, a built-in `LineChart` for FR-16, smoother animation.
- **Against:** a runtime dependency with native parts for each OS. Meeting NFR-09 means a separate jar per platform, or bundling a runtime with `jlink`. That's more packaging work, and lab PCs may run it differently.

### Option C - Text console

- **For:** simplest.
- **Against:** can't show a board, meter bar or graph. Fails the wireframe and FR-15.

## Recommendation

**Swing (Option A).** NFR-09's "only a Java runtime" is a requirement the team wrote, and Swing meets it with no packaging risk. The look can be made clean with a small set of colour tokens and custom-painted squares, and the one graph is cheap to draw.

## Decision

"Swing." - Oscar, 17 November 2026.

Oscar chose the recommended option and didn't add reasons of his own, so the reasons are the recommendation's: NFR-09, no dependency, no per-platform packaging. Oscar may reword this section at any time.

## Consequences

- A `ui` package of `JFrame` and `JPanel` screens. Board squares are painted with a type label and an icon or pattern as well as colour (NFR-05). The meter graph is a custom `JPanel`.
- No UI dependency, so ADR-003's packaging is a single runnable jar.
- Colours and fonts are defined once as named constants, so contrast can be checked by a test (NFR-05).
- UI updates must happen on Swing's event dispatch thread. The model stays free of Swing classes.
- High-DPI scaling needs checking on the target screens.

## How we'll know it worked

The packaged game starts on Windows and macOS from a clean folder with only a Java runtime, and the board, meter and graph are readable at 1366×768.

## Questions a marker might ask

**"Why Swing rather than JavaFX?"** Our own requirement NFR-09 says the game must run with only a Java runtime installed. Swing ships with Java, so there was nothing extra to install or package on each operating system.
