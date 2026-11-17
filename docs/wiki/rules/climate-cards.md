---
title: Climate cards
type: rules
status: draft
sources: [../sources/handover.md, ../sources/data-files.md, ../sources/sub1a-report.md]
updated: 2026-11-17
---

# Climate cards

## Summary

Seven Climate Card squares draw from a deck of 30 cards: 12 quiz, 9 choice and 9 event. Every card ends by showing a real fact and its source. They carry most of the game's factual teaching.

## Rules

1. **Draw** a card at random. When the pile is empty, reshuffle the used cards.
2. **Quiz** (Q01-Q12): four answers; correct (`correctIndex`) = +2 GP; wrong = nothing.
3. **Choice** (C01-C09): the player sees both options' effects, picks A or B, and the option's `money`, `gp` and `meter` deltas apply. **Meter changes apply immediately.**
4. **Event** (E01-E09): the `effect` applies immediately. Resilience does **not** reduce Event costs. A payment that would go below £0 causes [bankruptcy](bankruptcy.md).
5. **Always show the card's fact and source afterwards** (FR-10, NFR-10).

## The deck

| Type | Cards | Topics |
|---|---|---|
| Quiz | Q01-Q12 | The 2050 target and 48% by 2030 (LO1); agriculture as the largest source, renewables at 47% and mostly wind, oil heating (LO2); peatland; Lough Neagh; record heat; Connswater Greenway; Belfast's offshore wind terminal |
| Choice | C01-C09 | A cheap polluting option (+£4-7M, meter +1 or +2) against a green option (−£2-3M, +1-2 GP, meter 0 to −2). C08 "wait and cut later" vs "act now" teaches LO5 |
| Event | E01-E09 | Five costs (−£2-3M: storm, heatwave, algae bloom, roadworks, heating bills) and four gains (+£2-3M: wind contract, green grant, windy month, investors) |

Sources include DAERA, NISRA, the Department for the Economy, the Met Office, Ulster Wildlife, the Institution of Civil Engineers and Wikipedia. Facts were checked in November 2026; NISRA and DfE figures update yearly.

## Edge cases

- Event cards only change money in the current deck (`gp` and `meter` are 0), but the data format allows all three.
- A Choice option with a cost: can a player pick it with less money than it costs ([Q-12](open-questions.md))?
- With a seeded random source, the draw order must be reproducible in tests.

## Conflicts or uncertainties

- The quiz reward is stored twice: `squares.climateCard.quizCorrectGP` in `config.json` and `reward.gp` on every quiz card ([Q-13](open-questions.md)).
- The class diagram gives `ClimateCard` one `source: String`; the data has a list of sources with titles and URLs (C05 has two).
- The handover calls a "Climate Cards Sheet on claude.ai" the team-editable master. It isn't in the handover.

## Related

- [FR-10 Climate cards](../requirements/fr-10-climate-cards.md)
- [NFR-10 Fact accuracy](../requirements/nfr-10-fact-accuracy.md)
- [NFR-03 Readability](../requirements/nfr-03-readability.md)
- [Data files](../architecture/data-files.md)
- [Learning outcomes](../learning-outcomes.md)
