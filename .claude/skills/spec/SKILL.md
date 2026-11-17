---
name: spec
description: Draft or refine a Net Zero Belfast requirement page (FR-xx or NFR-xx) with checkable acceptance criteria, asking Oscar the questions that change the behaviour.
argument-hint: <feature or requirement ID>
---

Run the **spec** operation from `docs/AGENTS.md` for: $ARGUMENTS

1. Read `docs/AGENTS.md`, `docs/wiki/requirements/catalogue.md`, any existing page for this requirement, the rules pages it relies on and `docs/wiki/rules/open-questions.md`.
2. Ask Oscar the questions whose answers change the behaviour, a few at a time. Don't invent answers. Where an open rules question applies, ask it and record the answer on the open questions page too.
3. Write or update the page using the requirement page shape: Summary, Behaviour, Acceptance criteria (`AC-1`, `AC-2` and so on, each checkable, with concrete values), Out of scope, Open questions for Oscar, Related. Keep the requirement ID and wording from the SUB1a report.
4. Keep `status: draft` until Oscar says he agrees with it. Then set `agreed` and update the catalogue.
5. Update `docs/wiki/index.md` and `docs/wiki/log.md`, and run `java scripts/WikiLint.java`.
