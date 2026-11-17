---
title: FR-17 Disaster explanation
type: requirement
id: FR-17
priority: should
status: draft
sources: [../sources/sub1a-report.md, ../sources/data-files.md]
updated: 2026-11-17
---

# FR-17 Disaster explanation

## Summary

> When a disaster occurs, the system shall show a one-line explanation of how it links to climate change. *(SUB1a; verified manually)*

## Behaviour

The disaster pop-up shows what happened, who paid what (after Resilience), what applies next round, and one line explaining the climate link, with its source.

## Acceptance criteria

- **AC-1** Every disaster pop-up shows a one-line explanation specific to its type (Flood, Storm, Heatwave, Drought).
- **AC-2** The explanation text is read from a data file, not from code ([NFR-07](nfr-07-data-driven-balance.md)), with a source ([NFR-10](nfr-10-fact-accuracy.md)).
- **AC-3** The text scores 60 or more on Flesch Reading Ease ([NFR-03](nfr-03-readability.md)).

## Out of scope

Different explanations for different bands.

## Decisions (Oscar, 17 November 2026)

1. The agent drafts the four lines with sources; Oscar approves them; they go in the working `config.json` as `disasters.<TYPE>.explanation` and `sources` ([Q-11](../rules/open-questions.md)).

## Open questions for Oscar

1. Approve the [draft lines](../rules/climate-meter-and-disasters.md#disaster-explanations-draft-for-oscar-to-approve). The Storm and Drought lines still need a source check.

## Related

- [FR-12 Disasters](fr-12-disasters.md)
- [Climate Meter and disasters](../rules/climate-meter-and-disasters.md)
