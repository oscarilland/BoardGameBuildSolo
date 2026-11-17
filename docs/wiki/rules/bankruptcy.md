---
title: Bankruptcy
type: rules
status: draft
sources: [../sources/handover.md, ../sources/rules-v4.md, ../sources/data-files.md, ../sources/design-diagrams.md]
updated: 2026-11-17
---

# Bankruptcy

## Summary

Only a compulsory payment can bankrupt a player. A bankrupt player sits out turns and earns no money, then returns with a loan that costs £15M at the end.

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

## Conflicts or uncertainties

- **When "after 2 rounds" ends** isn't defined ([Q-14](open-questions.md)). The simulation counts the round of bankruptcy as 1, so a player bankrupted at the end of round 5 rolls in round 6 and returns automatically in round 7.
- Whether a Choice card option the player can't afford counts as compulsory ([Q-12](open-questions.md)).

## Related

- [FR-13 Bankruptcy](../requirements/fr-13-bankruptcy.md)
- [Scoring and end of game](scoring-and-end-of-game.md)
