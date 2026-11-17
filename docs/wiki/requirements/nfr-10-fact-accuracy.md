---
title: NFR-10 Fact accuracy
type: requirement
id: NFR-10
priority: should
status: agreed
sources: [../sources/sub1a-report.md, ../sources/data-files.md]
updated: 2026-11-17
---

# NFR-10 Fact accuracy

## Summary

> Accuracy: every real-world fact shown in the game shall be stored with a cited source in the card data file. *(SUB1a; verified by inspection)*

## Acceptance criteria

- **AC-1** Every card has a non-empty `fact` and at least one source with a title and URL. A load-time check enforces it.
- **AC-2** Disaster explanations ([FR-17](fr-17-disaster-explanation.md)) also carry a source.
- **AC-3** The source's title and web address are shown with the fact on screen ([FR-10](fr-10-climate-cards.md) AC-6).
- **AC-4** Before any demo, every card's fact and source are re-checked, and the working `climate_cards.json` updated where a figure has changed. The check is logged with its date.

## Out of scope

Checking that URLs still work (needs a network, which the game never uses).

## Decisions (Oscar, 17 November 2026)

1. **Re-check before any demo** (AC-4): NISRA and Department for the Economy figures update every year.
2. Sources are shown with their title and web address (AC-3).
3. **Agreed.**

## Build status

AC-1 is enforced by plan 001's validation. AC-3: the model gives every source's title and web address with the fact ([plan 004](../../plans/004-climate-cards.md)); the screen is plan 007. AC-2 (disaster explanations) is plan 005. AC-4 is a gate in build progress.

## Related

- [Climate cards](../rules/climate-cards.md)
