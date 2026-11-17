---
title: NFR-10 Fact accuracy
type: requirement
id: NFR-10
priority: should
status: draft
sources: [../sources/sub1a-report.md, ../sources/data-files.md]
updated: 2026-11-17
---

# NFR-10 Fact accuracy

## Summary

> Accuracy: every real-world fact shown in the game shall be stored with a cited source in the card data file. *(SUB1a; verified by inspection)*

## Acceptance criteria

- **AC-1** Every card has a non-empty `fact` and at least one source with a title and URL. A load-time check enforces it.
- **AC-2** Disaster explanations ([FR-17](fr-17-disaster-explanation.md)) also carry a source.
- **AC-3** The source is shown with the fact on screen ([FR-10](fr-10-climate-cards.md)).

## Out of scope

Checking that URLs still work (needs a network, which the game never uses).

## Open questions for Oscar

1. Facts were checked in November 2026. Re-check before any demo, since NISRA and DfE figures update yearly?

## Related

- [Climate cards](../rules/climate-cards.md)
