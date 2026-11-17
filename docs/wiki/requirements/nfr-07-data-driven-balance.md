---
title: NFR-07 Data-driven balance
type: requirement
id: NFR-07
priority: must
status: agreed
sources: [../sources/sub1a-report.md, ../sources/data-files.md]
updated: 2026-11-17
---

# NFR-07 Data-driven balance

## Summary

> Maintainability: all balance values and Climate Card content shall be read from data files, so they can be changed without changing code. *(SUB1a; verified by inspection)*

## Acceptance criteria

- **AC-1** `config.json`, `board.json` and `climate_cards.json` are loaded at start-up through `GameConfig`.
- **AC-2** Changing any number in `config.json` (for example `polluting.incomePerRound` from 7 to 6) changes the game's behaviour with no code change; a test proves it for at least one value per section.
- **AC-3** A missing or invalid value stops start-up with a message naming the file and key.
- **AC-4** Game logic contains no balance numbers. Inspection: a search for numeric literals in `model` finds only 0, 1 and documented exceptions.

- **AC-5** The disaster explanations live in the working `config.json` (`disasters.<TYPE>.explanation`), not in code ([Q-11](../rules/open-questions.md)).

## Decisions (Oscar, 17 November 2026)

1. Quiz reward: the card's `reward.gp`, with `quizCorrectGP` as the default ([Q-13](../rules/open-questions.md)).
2. Disaster explanations go in the working `config.json` ([Q-11](../rules/open-questions.md)).
3. **Agreed** as part of "all recommended", 17 November 2026.

## Related

- [Data files](../architecture/data-files.md)
- [ADR-004 Reading the JSON data files](../../decisions/004-reading-the-json-data-files.md)
