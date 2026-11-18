---
title: Open rules questions
type: rules
status: agreed
sources: [../sources/handover.md, ../sources/rules-v4.md, ../sources/data-files.md, ../sources/balance-simulation.md, ../sources/design-diagrams.md]
updated: 2026-11-17
---

# Open rules questions

Rules questions that must be answered before the code that depends on them is built. Q-01 to Q-10 come from the handover (section 7). Q-11 to Q-17 were found while ingesting it.

**In this solo version, Oscar decides.** The team may answer differently for the team game. Record each answer here, with the date, and update the affected rules page. Don't decide silently in code. Use `/decide Q-NN` to work through a new one, numbering on from Q-18.

## Answered (Oscar, 17 November 2026)

Oscar accepted the recommended answer to Q-01 to Q-17 ("all recommended"), and answered Q-18 separately. The "Applies to" column lists the pages that now carry the answer.

| # | Question | Answer | Applies to |
|---|---|---|---|
| Q-01 | Who wins if players are still tied after the Green Projects tie-break? | Fewest Polluting squares owned; if still tied, a die roll-off between the tied players (re-roll ties) | [Scoring](scoring-and-end-of-game.md), [FR-14](../requirements/fr-14-legacy-score-and-winner.md) |
| Q-02 | Can the meter go below 0? | Yes. There's no floor; values below 0 use danger band 1 | [Climate Meter](climate-meter-and-disasters.md), [FR-11](../requirements/fr-11-end-of-round-order.md), [FR-12](../requirements/fr-12-disasters.md) |
| Q-03 | A real unit for the meter? | No unit for now. Label it "Climate Meter (2026 = 50)". Display only | [Climate Meter](climate-meter-and-disasters.md), [FR-15](../requirements/fr-15-always-visible-status.md) |
| Q-04 | The official 2040 target? | **Checked:** at least 77% below 1990 levels, set by the Climate Change (2040 Emissions Target) Regulations (NI) 2024, which amend the 2022 Act. The data value 11 (50 × 23% = 11.5, taken down to 11) stands | [Climate Meter](climate-meter-and-disasters.md) |
| Q-05 | Cut-through direction, and when the route choice is offered | **Two-way.** Whenever a move reaches junction 12 or 27 with steps still to go, the player chooses: stay on the loop, or take the City Centre. From 12 the route runs 35 → 30 and rejoins at 27; from 27 it runs 30 → 35 and rejoins at 12; either way play continues clockwise. A player who lands exactly on a junction makes the choice at the start of their next move | [Board and squares](board-and-squares.md), [turn and actions](turn-and-actions.md), [FR-03](../requirements/fr-03-dice-movement-and-junctions.md) |
| Q-06 | Starting positions | Everyone starts on square 0 (Cave Hill Woodland), with no landing action at the start. The first roll moves them on | [Setup](setup-and-characters.md), [FR-02](../requirements/fr-02-starting-values-and-first-player.md) |
| Q-07 | Action after a Glider ride? | None. The ride ends the landing action | [Turn and actions](turn-and-actions.md), [FR-09](../requirements/fr-09-glider.md) |
| Q-08 | Rounding of Resilience-reduced costs | Round half up to whole £M (£1.5M → £2M) | [Board and squares](board-and-squares.md), [FR-08](../requirements/fr-08-resilience.md), [FR-12](../requirements/fr-12-disasters.md) |
| Q-09 | Final square names, prices, districts | Keep the placeholders in `board.json` for now. Data only, so code isn't blocked | [Board and squares](board-and-squares.md) |
| Q-10 | Re-tune the Farmer? | Not now. Revisit after playtests | [Setup](setup-and-characters.md), [balance](balance-and-history.md) |
| Q-11 | Disaster explanation text | The agent drafts four lines with sources; Oscar approves them; plan 005 adds them to the working `config.json` as `disasters.<TYPE>.explanation` and `sources`. **Approved 17 November 2026**, with checked Met Office sources: see [disaster explanations](climate-meter-and-disasters.md#disaster-explanations) | [FR-17](../requirements/fr-17-disaster-explanation.md), [NFR-07](../requirements/nfr-07-data-driven-balance.md), [NFR-10](../requirements/nfr-10-fact-accuracy.md) |
| Q-12 | Unaffordable Choice card options | Disabled: a player can't choose an option that costs more than their money. Every card in the deck has an option that gains money, so there's always a choice. Choice cards never bankrupt; only Event cards and disasters can | [Climate cards](climate-cards.md), [bankruptcy](bankruptcy.md), [FR-10](../requirements/fr-10-climate-cards.md), [FR-13](../requirements/fr-13-bankruptcy.md) |
| Q-13 | Which quiz reward value? | The card's own `reward.gp`; `squares.climateCard.quizCorrectGP` is the default for a card with no reward | [Climate cards](climate-cards.md), [FR-10](../requirements/fr-10-climate-cards.md), [NFR-07](../requirements/nfr-07-data-driven-balance.md) |
| Q-14 | When "after 2 rounds" ends | As in the simulation: at their first turn after going bankrupt the player rolls; on a 6 they return. If not, they return automatically at their next turn. So a player misses at most one turn | [Bankruptcy](bankruptcy.md), [FR-13](../requirements/fr-13-bankruptcy.md) |
| Q-15 | Closed and flooded squares: tax, meter, co-funding | They still pay carbon tax and still count towards the meter (emissions don't stop). They earn nothing, including co-funding GP for both players | [End of round](end-of-round.md), [FR-05](../requirements/fr-05-co-funding.md), [FR-11](../requirements/fr-11-end-of-round-order.md) |
| Q-16 | Legacy Score rounding | Kept exact and compared exactly; shown to one decimal place (for example 36.4) | [Scoring](scoring-and-end-of-game.md), [FR-14](../requirements/fr-14-legacy-score-and-winner.md) |
| Q-17 | Flood or Storm with no owned squares | Skipped: no disaster that round, as in the simulation | [Climate Meter](climate-meter-and-disasters.md), [FR-12](../requirements/fr-12-disasters.md) |
| Q-18 | After a Glider ride to **Glider City Hall (30)**, which way does the player's next move go along the two-way City Centre route? (Found while planning plan 002) | **The player decides:** at the start of their next move they choose towards Glider West (27) or towards Glider East (12). Oscar, 17 November 2026: "the player should be able to decide their direction". Already supported by `Game`'s `AWAITING_DIRECTION` phase (plan 002) | [Board and squares](board-and-squares.md), [FR-09](../requirements/fr-09-glider.md) |

## Still open

None. Add new questions here as Q-19 onwards.

## Related

- [Project context](../project-context.md)
- [Balance and design history](balance-and-history.md)
