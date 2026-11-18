---
title: Bankruptcy
type: rules
status: built
sources: [../sources/handover.md, ../sources/rules-v4.md, ../sources/data-files.md, ../sources/design-diagrams.md]
updated: 2026-11-17
---

# Bankruptcy

## Summary

Only a compulsory payment can bankrupt a player. A bankrupt player sits out turns and earns no money, then returns with a loan that costs £15M at the end.

> **Build status:** built. The trigger came in [plan 004](../../plans/004-climate-cards.md); sitting out, the return roll, the automatic return and the loan in [plan 005](../../plans/005-end-of-round-disasters-and-bankruptcy.md). The loan repayment is deducted at scoring (plan 006).

## Rules

1. **Trigger:** a **compulsory** payment (a disaster cost or a Climate Card) would take the player's money below 0 (`bankruptcy.trigger`). Their money becomes 0 and the rest is written off.
2. Players can never choose to buy something they can't afford, so voluntary actions never cause bankruptcy.
3. **While bankrupt:** no movement and no actions. Their squares still earn GP and still pay the carbon tax, but earn **no money** (`noMoneyIncomeWhileBankrupt`). Co-funding deals on their squares continue.
4. **Return:** at the start of each of their turns, roll one die. On a 6 (`escapeRoll`) they return. After 2 rounds (`maxRounds`) they return automatically.
5. **On return:** they receive a £10M loan (`loanOnReturn`), and £15M (`repayAtEnd`) is deducted from their money at game end, before scoring. They then carry on with that turn (the [Take Turn sequence](../../raw/net-zero-belfast-handover/design/seq_take_turn.png) continues to the dice roll).

## Edge cases

- A player can go bankrupt more than once; each return adds another £15M to repay.
- A Storm's repair on a bankrupt owner's square: they have £0, so it's written off.
- If a Climate Card or disaster payment exactly equals their money, they reach £0 without going bankrupt.
- The loan repayment can make final money negative. In a net zero game that lowers the Legacy Score; in a missed game money is ignored anyway.

## Decisions (Oscar, 17 November 2026)

- **Automatic return** ([Q-14](open-questions.md)), as in the simulation: at their first turn after going bankrupt the player rolls one die; on a 6 they return. If not, they return automatically at their next turn. A player bankrupted at the end of round 5 rolls in round 6 and, if that fails, returns in round 7. So they miss at most one turn.
- **Choice cards never bankrupt** ([Q-12](open-questions.md)): an option the player can't afford is disabled. Only Event cards and disasters are compulsory payments.

## Related

- [FR-13 Bankruptcy](../requirements/fr-13-bankruptcy.md)
- [Scoring and end of game](scoring-and-end-of-game.md)
