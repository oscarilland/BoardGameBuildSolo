---
title: FR-10 Climate cards
type: requirement
id: FR-10
priority: must
status: draft
sources: [../sources/sub1a-report.md, ../sources/data-files.md]
updated: 2026-11-17
---

# FR-10 Climate cards

## Summary

> On a Climate Card square the system shall draw a random card, apply its effect (quiz, choice or event) and show its fact and source. *(SUB1a; verified by JUnit and manual test)*

## Behaviour

See [climate cards](../rules/climate-cards.md) and the [Draw Climate Card flow](../architecture/game-flow.md).

## Acceptance criteria

- **AC-1** Landing on any of the 7 Climate Card squares draws one card from the deck loaded from `climate_cards.json`.
- **AC-2** With a fixed seed, the draw order is the same every time. No card is drawn twice until all 30 have been drawn; then the used cards are reshuffled.
- **AC-3** Quiz: choosing `correctIndex` gives +2 GP; any other answer gives nothing.
- **AC-4** Choice: both options' money, GP and meter effects are shown before choosing. After choosing, all three apply, and the meter changes at once (visible on the meter bar immediately).
- **AC-5** Event: the effect applies immediately, with no Resilience reduction. A cost larger than the player's money makes them bankrupt ([FR-13](fr-13-bankruptcy.md)).
- **AC-6** After every card, its fact and source titles are shown ([NFR-10](nfr-10-fact-accuracy.md)).
- **AC-7** A Choice option the player can't afford is handled as agreed in [Q-12](../rules/open-questions.md).

## Out of scope

Editing cards inside the game.

## Open questions for Oscar

1. [Q-12](../rules/open-questions.md): unaffordable Choice options.
2. [Q-13](../rules/open-questions.md): which quiz reward value to read.
3. Show source URLs on screen, or only the source titles?

## Related

- [NFR-03 Readability](nfr-03-readability.md)
- [Learning outcomes](../learning-outcomes.md)
