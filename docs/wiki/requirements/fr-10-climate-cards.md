---
title: FR-10 Climate cards
type: requirement
id: FR-10
priority: must
status: built
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
- **AC-3** Quiz: choosing `correctIndex` gives the card's `reward.gp` (+2 in the current deck), or `quizCorrectGP` if the card has no reward; any other answer gives nothing ([Q-13](../rules/open-questions.md)).
- **AC-4** Choice: both options' money, GP and meter effects are shown before choosing. After choosing, all three apply, and the meter changes at once (visible on the meter bar immediately).
- **AC-5** Event: the effect applies immediately, with no Resilience reduction. A cost larger than the player's money makes them bankrupt ([FR-13](fr-13-bankruptcy.md)).
- **AC-6** After every card, its fact is shown with each source's title and web address (the address in smaller text, not a link) ([NFR-10](nfr-10-fact-accuracy.md)).
- **AC-7** A Choice option that costs more than the player's money is shown but disabled, and the model rejects it. A player with £1M on C01 can take "Take the deal" (+£6M) but not "Retrofit" (−£2M) ([Q-12](../rules/open-questions.md)).
- **AC-8** Loading fails if any Choice card has no option with `money` of 0 or more, so a player always has a choice.

## Out of scope

Editing cards inside the game.

## Decisions (Oscar, 17 November 2026)

1. Unaffordable Choice options are disabled, so Choice cards never bankrupt ([Q-12](../rules/open-questions.md)).
2. The card's own quiz reward wins; the config value is the default ([Q-13](../rules/open-questions.md)).

3. **Sources on screen:** the title and the web address, the address in smaller text and not clickable, because the game never goes online (NFR-08) (AC-6).
4. **Event cards and bankruptcy:** plan 004 builds the compulsory payment (money to £0, the rest written off, the player marked bankrupt) for AC-5; sitting out and returning are plan 005.
5. **Agreed.**

## Build status

**Built** in [plan 004](../../plans/004-climate-cards.md): AC-1 to AC-8, tested in `CardDeckTest`, `QuizCardTest`, `ChoiceCardTest`, `EventCardTest` and `CardFlowTest`. The model holds everything the screen needs (card text, options with their effects, the fact and every source); the screen itself is plan 007.

## Related

- [NFR-03 Readability](nfr-03-readability.md)
- [Learning outcomes](../learning-outcomes.md)
