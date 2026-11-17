---
title: Source - Data files (config, board, climate cards)
type: source
status: draft
sources: [../../raw/net-zero-belfast-handover/data/config.json, ../../raw/net-zero-belfast-handover/data/board.json, ../../raw/net-zero-belfast-handover/data/climate_cards.json]
updated: 2026-11-17
---

# Source - Data files (config, board, climate cards)

## Provenance

- **Files:** [config.json](../../raw/net-zero-belfast-handover/data/config.json), [board.json](../../raw/net-zero-belfast-handover/data/board.json), [climate_cards.json](../../raw/net-zero-belfast-handover/data/climate_cards.json).
- **Dated:** November 2026, with rules v4. The cards' facts were "checked Nov 2026".
- **Authority:** the values the game must read (NFR-07). `config.json` is tuned by simulation and expected to change after playtests. `board.json`'s names, prices and districts are placeholders ([Q-09](../rules/open-questions.md)). The cards were exported from a team-editable sheet that isn't in the handover.

## What it covers

- `config.json`: player counts, rounds and years, the six characters with their parameters, square economics, conversion, co-funding, lobbying, Glider, Resilience, quiz reward, the Climate Meter (start, base rise, interim targets, danger bands), the four disasters, bankruptcy and scoring.
- `board.json`: six districts, the 30-square loop, the City Centre route (30-35) between junctions 12 and 27, the Glider stops (12, 27, 30), and all 36 squares with id, name, type, district, price and nature flag.
- `climate_cards.json`: 30 cards (Q01-Q12 quiz, C01-C09 choice, E01-E09 event), each with text, effects, a fact and one or more sources.

## What it contributed

- Every value on the `rules/` pages, cited by data key.
- The schema notes on [data files](../architecture/data-files.md).
- The card summary on [climate cards](../rules/climate-cards.md).

## Limitations and caveats

- **Counts checked on ingest:** 36 squares: 10 polluting, 12 green (4 nature), 4 resilience, 3 Glider stops, 7 Climate Card squares. 30 cards: 12 quiz, 9 choice, 9 event. All match the rules.
- **Prices checked on ingest:** polluting £4-7M and green £8-12M, all within range.
- Some values are descriptions, not data: `scoring.netZero`, `disasters.roll` and `typeChoice` are strings explaining a rule. The code must implement those rules and only read the numbers.
- The quiz reward is in two places: `squares.climateCard.quizCorrectGP` and every quiz card's `reward.gp` ([Q-13](../rules/open-questions.md)).
- There is no text for the disaster explanations FR-17 requires ([Q-11](../rules/open-questions.md)).
- Fact figures (NISRA, DfE) update every year, so they will need re-checking.
