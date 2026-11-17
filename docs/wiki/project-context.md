---
title: Project context
type: context
status: draft
sources: [sources/handover.md, sources/module-handbook.md]
updated: 2026-11-17
---

# Project context

## Where the game comes from

Net Zero Belfast was designed by a team of six for **CSC2058 Software Engineering and Systems Development** at Queen's University Belfast (2026-27). The brief: an educational game on UN SDG 13 for a defined audience, built in an object-oriented language (Java strongly recommended) with use cases, UML, version control and automated tests. See the [module handbook receipt](sources/module-handbook.md).

By 17 November 2026 the team had agreed rules v4, written a 30-card deck, and drafted the SUB1a Interim System Report with requirements, use cases, and class and sequence diagrams. No code existed. Oscar packaged all of it as a [handover](sources/handover.md).

## What this repo is

**Oscar's solo version of the game** (Oscar, 17 November 2026). It's separate from the team project:

- It **never goes to the team's GitLab repo**, and nothing from it is submitted to Canvas.
- The module judges each student by their own commits on GitLab, doesn't accept GitHub as evidence, and requires generative AI use to be declared. Code an agent writes here therefore can't become team work. If Oscar wants to use an idea from here in the team project, he writes and commits it there himself and declares any AI help.
- Because it's a solo version, Oscar decides the open rules questions himself. The team may decide them differently; the [open questions page](rules/open-questions.md) records Oscar's answers for this version only.

The harness for working here is described in [ADR-001](../decisions/001-repo-structure-and-harness.md).

## Constraints carried over from the team design

- Java desktop app, hot-seat on one computer, offline.
- Runs on Windows and macOS with only a Java runtime ([NFR-09](requirements/nfr-09-portability.md)). The team aimed at EEECS lab PCs; **this solo version targets Oscar's own laptop**, with Java 21 ([ADR-003](../decisions/003-build-tool-and-java-version.md)).
- Game logic tested with JUnit, at least 80% line coverage ([NFR-06](requirements/nfr-06-reliability-and-test-coverage.md)).
- Balance values and cards in data files ([NFR-07](requirements/nfr-07-data-driven-balance.md)).
- Keep the analysis model's class and method names recognisable ([domain model](architecture/domain-model.md)).

## Open questions for Oscar

- What is this solo version for: learning the codebase before the team builds it, a reference implementation, a personal portfolio piece, or something else? The answer changes how much polish, documentation and playtesting it needs.
- Is there a target date, for example ahead of the team's SUB2 work in semester 2?
