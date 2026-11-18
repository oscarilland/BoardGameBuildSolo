---
title: Setup and characters
type: rules
status: built
sources: [../sources/handover.md, ../sources/rules-v4.md, ../sources/data-files.md]
updated: 2026-11-17
---

# Setup and characters

## Summary

4-6 players each take a different one of six characters. The characters start with unequal money and GP, and each has one ability. They're balanced by win rate, not by starting value.

> **Build status:** setup is built in `Game.start` ([plan 002](../../plans/002-players-characters-and-movement.md)): names (up to `game.maxNameLength`, 20), distinct characters, starting money and GP, random first player, everyone on square 0. Every ability is built: Tech Entrepreneur, Council Planner, Student Commuter and Community Activist in [plan 003](../../plans/003-square-actions.md); the Activist's meter effect, the Industrialist's bonus and the Farmer's nature GP in [plan 005](../../plans/005-end-of-round-disasters-and-bankruptcy.md).

## Rules

1. **Players:** 4 to 6 (`game.minPlayers`, `game.maxPlayers`). Each enters a display name and chooses a **different** character.
2. **First player** is chosen at random (`game.firstPlayer: "random"`), then turns follow a fixed order.
3. **Start state:** year 2026 (`game.startYear`), round 1, Climate Meter 50 (`climateMeter.start`).
4. **Starting position:** everyone starts on square 0 (Cave Hill Woodland), with no landing action at the start. The first roll moves them on ([Q-06](open-questions.md), Oscar, 17 November 2026).
5. **Characters** (`characters[]`):

| Character (`id`) | Money | GP | Ability | Parameters |
|---|---|---|---|---|
| Industrialist (`INDUSTRIALIST`) | £40M | 8 | **Just transition:** a polluting square they convert earns an extra £2M per round, on top of normal Green Project income | `convertedBonusIncome: 2` |
| Tech Entrepreneur (`TECH_ENTREPRENEUR`) | £36M | 10 | Green Projects cost £3M less to buy | `greenProjectDiscount: 3` |
| Council Planner (`COUNCIL_PLANNER`) | £38M | 12 | Each Resilience level costs £2M less | `resilienceDiscount: 2` |
| Farmer (`FARMER`) | £36M | 13 | Nature projects earn +1 GP per round | `natureBonusGP: 1` |
| Student Commuter (`STUDENT_COMMUTER`) | £34M | 16 | Glider travel is free, and each ride earns +1 GP | `gliderFare: 0`, `gliderRideGP: 1` |
| Community Activist (`COMMUNITY_ACTIVIST`) | £32M | 18 | Lobbying is free, and a lobbied square adds nothing to the Climate Meter that round | `lobbyCost: 0`, `lobbyCancelsMeter: true` |

## Edge cases

- **Industrialist:** the bonus applies to squares the Industrialist converted, not to Green Projects they bought, and not to squares another player converted. It needs a flag on the square recording who converted it.
- **Tech Entrepreneur:** the discount applies to buying Green Projects only, not to converting (£5M for everyone). On a £8M Green Project they pay £5M.
- **Council Planner:** levels cost £1M, £3M and £5M.
- **Farmer:** the +1 GP is per nature project the Farmer owns, per round, and is lost in a Drought round along with the base GP.
- **Student Commuter:** the +1 GP is earned per ride, at the time of the ride.
- **Community Activist:** lobbying is free, so they can lobby with £0.

## Conflicts or uncertainties

- The class diagram gives `Character` only `priceFor(square)` and `incomeBonus(square)`. Several abilities need other hooks (Glider fare and GP, lobby cost and meter effect, nature GP, Resilience price). The handover allows "similar hooks as needed". See [domain model](../architecture/domain-model.md).
- Balance: the Farmer wins 24% of simulated games against a fair share of about 20%. Left as is until playtests ([Q-10](open-questions.md)).

## Related

- [FR-01 Start game](../requirements/fr-01-start-game.md)
- [FR-02 Starting values and first player](../requirements/fr-02-starting-values-and-first-player.md)
- [Balance and design history](balance-and-history.md)
