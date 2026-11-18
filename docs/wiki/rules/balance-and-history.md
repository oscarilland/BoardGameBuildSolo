---
title: Balance and design history
type: rules
status: built
sources: [../sources/handover.md, ../sources/rules-v4.md, ../sources/sub1a-report.md, ../sources/balance-simulation.md]
updated: 2026-11-18
---

# Balance and design history

## Summary

Rules v4's numbers were set by a Python simulation that played thousands of games with simple bots, including bots that try to exploit the rules. These are simulated results, not playtest results, and should be re-checked after the first playtests.

## Simulated results (v4)

| Players' average play | City reaches net zero |
|---|---|
| Mostly polluting | 13% of games |
| Mixed | 54% |
| Mostly green | 73% |

| Strategy test | Win rate (fair share about 21%) |
|---|---|
| Pollute as much as possible, convert at the end | 13% |
| Only ever go green | 17% |
| Council Planner who always buys Resilience | 20% |

- Characters: Industrialist 19%, Tech Entrepreneur 17%, Council Planner 19%, Farmer 24%, Student Commuter 20%, Community Activist 21%.
- About 3 squares owned per player by the end. About 6 in 10 turns have something happen.

## Design variants rejected

| Variant | Why rejected |
|---|---|
| "Environmental Monopoly": bankrupt your rivals | Staff feedback: not distinctive; eliminating rivals contradicts collective action |
| Fully cooperative | The brief requires a winner, and it hides the individual incentives that make climate action hard |
| Two turns per round | About 5 squares each, but fewer turns with an action, worse character balance, twice as long (breaks NFR-01) |

## How v4 differs from v3

| Area | v3 | v4 | Why |
|---|---|---|---|
| Pollution penalty | GP lost only if polluting outnumbered green | Carbon tax of 1 GP per polluting square per round | Owning one of each avoided the penalty |
| Failing net zero | −3 GP per polluting square | Money worthless; −5 GP per polluting square | "Pollute, then convert at the end" won 1 game in 3 |
| Climate Meter | Start 45, polluting +1, green −1 | Start 50, polluting +2, green −1 | Green and polluting play gave almost the same result |
| Green Projects | GP only | +£2M per round as well | Green players ran out of money |
| Co-funding | Green or polluting | Green Projects only | Two players could profit from pollution with no penalty |
| Resilience | Shared, per district | Personal insurance | Rarely useful, and protected rivals too |
| Disasters | Fixed costs | Costs grow with the danger band | Too cheap to notice |
| Bankruptcy | Income continued; £10M loan repaid at £10M | No money while bankrupt; repay £15M | Closer to a reward than a punishment |
| Board | 50 squares | 36 squares | Too many empty turns |
| Learning | Facts on cards, quiz +1 GP | Interim targets, choice cards, quiz +2 GP | Learning was attached, not built in |

Earlier, v2 had an always-dead carbon tax, an off-by-one year count, and rich characters winning over 80% of games.

## Java simulation (plan 006)

`java -jar net-zero-belfast.jar --simulate 5000` plays the built game with computer players ([plan 006](../../plans/006-scoring-and-balance-simulation.md)): 4-6 players, random characters, and a random mix of three strategies. Those are random (leaning green or polluting), green (buys green, converts, co-funds) and polluter (buys polluting, never converts, takes the money option). Results on 18 November 2026, current data:

| Measure | Java | Python (above) |
|---|---|---|
| Net zero: mostly green / mixed / mostly polluting | 58% / 19% / 0% | 73% / 54% / 13% |
| Character win rates (fair about 20%) | Tech Entrepreneur 18%, Industrialist 19%, Activist 20%, Planner 20%, Student 21%, Farmer 22% | 17% to 24%; Farmer 24% |
| Green strategy | 40% | 17% |
| Polluting strategy | 0% (never converts) | 13% (converts at the end) |
| Games with a bankruptcy | 3% (0.03 per game) | - |
| Disasters per game | 7.2 | - |

**Why the two differ.** The Java game is the real one: junctions and the City Centre route, the real board order, all 30 cards with their exact effects, the 2030 and 2040 penalties, and floods and storms that close squares for a round. The Python model simplified all of these. The bots differ too: Java's polluter never converts, and its green player is stricter.

**What it suggests, for Oscar and playtesting:**
- **Characters are balanced.** Every character wins 18-22%. The Farmer is still slightly strong ([Q-10](open-questions.md)).
- **Net zero is harder than the Python model showed**, especially in mixed games (19% against 54%). Players who mostly pollute never reach it.
- **Pure polluting never wins.** When the city misses net zero, money is worthless and the carbon tax eats GP, so the lesson lands hard. Whether a mixed strategy, or polluting then converting late, can still win is worth testing with real players.
- **Bankruptcy is rare** (3% of games), because income is paid before disasters each round (issue 4 in build progress).

Computer players aren't people, so treat these as a guide for re-tuning `config.json` after playtests. **Oscar's decision, 18 November 2026: wait for playtests** ([Q-10](open-questions.md)). No values change now; the first things to watch in playtests are how often a mixed table reaches net zero, and whether the Farmer stays ahead. The Python simulation's own simplifications are listed on its [receipt](../sources/balance-simulation.md).

## Related

- [NFR-01 Session length](../requirements/nfr-01-session-length.md)
- [Setup and characters](setup-and-characters.md)
