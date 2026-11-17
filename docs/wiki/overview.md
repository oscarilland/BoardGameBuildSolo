---
title: Net Zero Belfast overview
type: overview
status: draft
sources: [sources/handover.md, sources/rules-v4.md, sources/sub1a-report.md]
updated: 2026-11-17
---

# Net Zero Belfast overview

**Net Zero Belfast is an educational digital board game about climate action** (UN SDG 13, especially targets 13.3, climate education, and 13.1, resilience). It's for 4-6 players aged 13-16 sharing one computer, and a game should fit in one school lesson.

## The idea

Northern Ireland's Climate Change Act 2022 makes net zero by 2050 a legal target, with a cut of at least 48% by 2030. The game lets players feel the trade-off at the heart of that: **polluting pays quickly, but emissions are shared, disasters hit everyone, and a city that misses net zero makes money worthless.** That's the tragedy of the commons, and it's the core lesson.

## How it plays

1. Each player picks one of six [characters](rules/setup-and-characters.md), each with different starting money, Green Points (GP) and an ability.
2. Over 12 rounds (2026 to 2050, two years a round, one turn each) players roll two dice and move around a [36-square board](rules/board-and-squares.md) of real Belfast districts.
3. On their square they [act](rules/turn-and-actions.md): buy a cheap, high-income **Polluting** square or a dearer **Green Project**, co-fund or lobby a rival's square, convert polluting to green, buy Resilience, ride the Glider, or draw a [Climate Card](rules/climate-cards.md).
4. At the [end of each round](rules/end-of-round.md) squares pay out, a carbon tax costs GP, and the shared [Climate Meter](rules/climate-meter-and-disasters.md) rises with pollution and falls with green projects. Its danger band sets the chance and cost of floods, storms, heatwaves and droughts. In 2030 and 2040 the meter is checked against interim targets.
5. [At the end](rules/scoring-and-end-of-game.md), if the meter is at 0 or below, Belfast reached net zero and the winner has the highest GP + money ÷ 5. If not, money is worthless and only GP counts.

## What players should learn

Five learning outcomes, from NI's legal targets to why delay is costly. See [learning outcomes](learning-outcomes.md).

## Requirements

17 functional and 11 non-functional requirements, with the team's IDs. See the [requirements catalogue](requirements/catalogue.md).

## How it will be built

A Java 21 desktop app with a Swing UI, built with Maven ([ADR-002](../decisions/002-ui-toolkit.md), [ADR-003](../decisions/003-build-tool-and-java-version.md)). The game logic is a headless model that tests and bots can drive, with a separate UI on top. All balance values and card content are read from data files. See the [system overview](architecture/system-overview.md). The JSON library is still to be decided ([ADR-004](../decisions/004-reading-the-json-data-files.md)).

## Status

**Designing.** The team handover is ingested. Nothing is built yet. See [build progress](operations/build-progress.md).

## Scope boundaries

- One shared computer (hot-seat). No network play, no accounts, no AI opponents in the game itself (bots exist only for testing).
- Facts are real and sourced; the board's square names, prices and districts are placeholders until finalised.
