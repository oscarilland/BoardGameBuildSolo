---
name: decide
description: Draft or progress a Net Zero Belfast design decision - an ADR with options, trade-offs and a recommendation, or an open rules question - leaving the decision for Oscar to make.
argument-hint: <design question, ADR number or open question number>
---

Run the **decide** operation from `docs/AGENTS.md` for: $ARGUMENTS

1. Read `docs/AGENTS.md`, `docs/decisions/_template.md`, and the requirements, rules and architecture pages the question affects.
2. **Technical questions** (toolkit, libraries, structure) get an ADR. For a new question, create the next numbered ADR from the template; for an existing one, work from it.
3. **Rules questions** (from `docs/wiki/rules/open-questions.md`) don't get an ADR unless Oscar asks. Lay out the options, what the balance simulation or the rules imply, and a recommendation, then record his answer on the open questions page and the affected rules page.
4. Give two or three real options with honest trade-offs, and a recommendation with its main reason. Explain them in plain language and answer Oscar's questions.
5. **Never write an ADR's Decision section yourself.** Oscar writes it in his own words. Only after he has done so, set `status: accepted` and update the affected architecture pages.
6. Update the index and log, and run `java scripts/WikiLint.java`.
