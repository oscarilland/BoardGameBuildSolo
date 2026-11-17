---
name: explain
description: Teach Oscar a Net Zero Belfast rule, class or design choice, then have him explain it back and practise the questions a marker or teammate might ask.
argument-hint: <component, class or rule>
---

Run the **explain** operation from `docs/AGENTS.md` for: $ARGUMENTS

1. Find the relevant rules or architecture page in `docs/wiki/index.md`, and the code behind it if it's built.
2. Explain it in plain language: what it does, why it's designed that way (link the ADR or requirement), and how it fails. Use a small concrete example, such as a specific meter value, a dice roll or a player's money.
3. Ask Oscar to explain it back in his own words. Gently correct anything missing or wrong.
4. Ask two or three questions a marker or teammate might ask, one at a time, and give feedback on his answers.
5. With his agreement, improve the page's "Explain it in 30 seconds" and "Questions a marker might ask" sections. Then update the log and run `java scripts/WikiLint.java`.
