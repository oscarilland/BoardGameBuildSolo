---
title: NFR-03 Readability
type: requirement
id: NFR-03
priority: should
status: draft
sources: [../sources/sub1a-report.md]
updated: 2026-11-17
---

# NFR-03 Readability

## Summary

> Readability: all in-game and card text shall score 60 or more on the Flesch Reading Ease scale, suitable for ages 13-16. *(SUB1a; verified by tool check)*

## Acceptance criteria

- **AC-1** Every card's text, options, answers and fact, every disaster explanation, and the tutorial score 60 or more on Flesch Reading Ease.
- **AC-2** The check is automated: a test computes the score for every text field in the data files and fails below 60, listing the offending text.

## Out of scope

Translations.

## Open questions for Oscar

1. AC-2 makes the "tool check" repeatable. Short texts (a two-word answer like "Heating oil") give unstable scores; check answers as one combined text per card?

## Related

- [Climate cards](../rules/climate-cards.md)
- [NFR-10 Fact accuracy](nfr-10-fact-accuracy.md)
