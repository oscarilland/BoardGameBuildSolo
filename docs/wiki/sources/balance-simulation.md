---
title: Source - Balance simulation (balance_sim.py)
type: source
status: draft
sources: [../../raw/net-zero-belfast-handover/reference/balance_sim.py]
updated: 2026-11-17
---

# Source - Balance simulation (balance_sim.py)

## Provenance

- **File:** [balance_sim.py](../../raw/net-zero-belfast-handover/reference/balance_sim.py), Python 3.
- **Dated:** November 2026. Written with generative AI help (the report's declaration says so).
- **Authority:** reference only, not production code. It's the evidence behind rules v4's numbers.

## What it covers

Plays thousands of games of v4 with crude bots: `rand` (leans green or polluting at random), `exploit` (pollutes as much as possible and converts at the end), `green`, and `planner_res` (a Council Planner who always buys Resilience). It reports net zero success rates, squares per player, bankruptcies, disaster costs, the share of turns with an action, and win rates by character and strategy.

## What it contributed

- The balance results on [balance and design history](../rules/balance-and-history.md).
- A model for the Java `sim` package and the 20-game headless test ([NFR-06](../requirements/nfr-06-reliability-and-test-coverage.md)).
- Answers the simulation implies for some open questions, recorded as "simulation did" on [open questions](../rules/open-questions.md).

## Limitations and caveats

The header lists: no junctions or City Centre route, a random board order, and simplified Climate Cards. Reading the code showed more differences from the rules, so its numbers are a guide, not a spec:

- Each player starts on a random square ([Q-06](../rules/open-questions.md)).
- After a Glider ride the player takes no action on the destination ([Q-07](../rules/open-questions.md)).
- Resilience uses Python's `round()`, which rounds halves to even ([Q-08](../rules/open-questions.md)).
- Glider fares are charged even when the player can't afford them.
- The interim check happens on round indexes 2 and 7 (0-based), which are rounds 3 and 8, as in the rules.
- Co-funding pays the owner +1 GP too, which matches the rules, but a funder's deal isn't ended when they start a new one; bots only co-fund when they have no deal.
- Carbon tax and interim penalties don't count Activist-lobbied squares differently, which is correct, because lobbying only affects money and the meter.
