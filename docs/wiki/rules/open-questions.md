---
title: Open rules questions
type: rules
status: draft
sources: [../sources/handover.md, ../sources/rules-v4.md, ../sources/data-files.md, ../sources/balance-simulation.md, ../sources/design-diagrams.md]
updated: 2026-11-17
---

# Open rules questions

Rules questions that must be answered before the code that depends on them is built. Q-01 to Q-10 come from the handover (section 7). Q-11 to Q-17 were found while ingesting it.

**In this solo version, Oscar decides.** The team may answer differently for the team game. Record each answer here, with the date, and update the affected rules page. Don't decide silently in code. Use `/decide Q-NN` to work through one.

## Questions

| # | Question | Blocks | What the sources say or imply | Answer |
|---|---|---|---|---|
| Q-01 | Who wins if players are still tied after the Green Projects tie-break? | [FR-14](../requirements/fr-14-legacy-score-and-winner.md) | Nothing. The simulation took the first player in list order | Open |
| Q-02 | Can the meter go below 0? | [FR-11](../requirements/fr-11-end-of-round-order.md), [FR-15](../requirements/fr-15-always-visible-status.md) | The simulation allowed it; display and band treat below 0 as band 1 | Open |
| Q-03 | A real unit for the meter (for example kt CO₂e scaled to Belfast)? | [FR-15](../requirements/fr-15-always-visible-status.md) (display only) | Display only; no effect on logic | Open |
| Q-04 | The current official 2040 target, to replace the assumed 11 (a 77% cut)? | [FR-11](../requirements/fr-11-end-of-round-order.md) | Data value only (`interimTargets[1].maxMeter`) | Open |
| Q-05 | Is the City Centre cut-through two-way or one-way? How does "choose at a junction" work when passing a junction rather than landing on it? | [FR-03](../requirements/fr-03-dice-movement-and-junctions.md) | The drawing says either direction; `board.json` says TBD and "one-way may be simpler to code". The simulation had no junctions | Open |
| Q-06 | Where do players start? | [FR-02](../requirements/fr-02-starting-values-and-first-player.md) | Not defined. The simulation used random squares. The handover suggests all start on one square | Open |
| Q-07 | After a Glider ride, does the player act on the destination stop? | [FR-09](../requirements/fr-09-glider.md) | The simulation: no further action. Every destination is another Glider Stop, so the only possible action is another ride | Open |
| Q-08 | How are Resilience-reduced costs rounded? | [FR-08](../requirements/fr-08-resilience.md), [FR-12](../requirements/fr-12-disasters.md) | The handover suggests round half up. The simulation used Python's round-half-to-even | Open |
| Q-09 | Final square names, prices within the ranges, and district assignments | [FR-03](../requirements/fr-03-dice-movement-and-junctions.md), [FR-04](../requirements/fr-04-buy-square.md) | `board.json` values are placeholders. Data only, so code can proceed | Open |
| Q-10 | Re-tune the Farmer (24% win rate against a fair 20%) after playtests? | None (data only) | Values in `config.json` | Open |
| Q-11 | What is the one-line climate explanation for each disaster, and where is it stored? | [FR-17](../requirements/fr-17-disaster-explanation.md), [NFR-07](../requirements/nfr-07-data-driven-balance.md), [NFR-10](../requirements/nfr-10-fact-accuracy.md) | No data file has the text. NFR-07 and NFR-10 imply it belongs in a data file with a source | Open |
| Q-12 | Can a player choose a Choice card option that costs more money than they have? If so, does it bankrupt them? | [FR-10](../requirements/fr-10-climate-cards.md), [FR-13](../requirements/fr-13-bankruptcy.md) | "Players can never choose to buy something they can't afford" suggests the option is disabled. But the bankruptcy rule names "a Climate Card" as a compulsory payment, which fits Event cards. The simulation charged it anyway | Open |
| Q-13 | Which value is the quiz reward: `config.json`'s `quizCorrectGP` or each card's `reward.gp`? | [FR-10](../requirements/fr-10-climate-cards.md), [NFR-07](../requirements/nfr-07-data-driven-balance.md) | Both are 2 today. A per-card reward allows harder questions to pay more; one config value is simpler | Open |
| Q-14 | When exactly does "after 2 rounds they return automatically" happen? | [FR-13](../requirements/fr-13-bankruptcy.md) | The simulation counts the round of bankruptcy as round 1, so the player rolls once and then returns automatically at their next turn | Open |
| Q-15 | Do closed or flooded squares still pay carbon tax and still move the meter? Does a co-funding deal on a flooded square pay? | [FR-11](../requirements/fr-11-end-of-round-order.md) | The rules only exempt them from income. The simulation counted them for tax and meter, and paid co-funding on flooded squares but not closed ones | Open |
| Q-16 | Is the Legacy Score a whole number or does it keep the fraction from money ÷ 5? | [FR-14](../requirements/fr-14-legacy-score-and-winner.md) | The class diagram says `legacyScore(netZero): int`; the simulation kept fractions | Open |
| Q-17 | If a Flood or Storm is rolled when no squares are owned, is the disaster skipped or is another type chosen? | [FR-12](../requirements/fr-12-disasters.md) | The simulation skipped it | Open |

## Related

- [Project context](../project-context.md)
- [Balance and design history](balance-and-history.md)
