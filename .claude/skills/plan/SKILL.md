---
name: plan
description: Draft a Net Zero Belfast build plan for one vertical slice, citing requirement IDs and ADRs, ready for Oscar to approve before any code is written.
argument-hint: <requirement IDs, e.g. FR-03 FR-04>
---

Run the **plan** operation from `docs/AGENTS.md` for: $ARGUMENTS

1. Read `docs/AGENTS.md`, `docs/plans/_template.md`, the cited requirement pages, the related rules pages and ADRs, `docs/wiki/rules/open-questions.md`, `docs/wiki/operations/build-progress.md` and `src/AGENTS.md`.
2. Check the requirements are `agreed`, the ADRs they depend on are `accepted`, and the open rules questions they touch are answered. If not, say which are still open and offer to `/spec` or `/decide` them first.
3. Create the next numbered plan from the template. Keep it to one vertical slice that can be built in one or two sittings. List the tasks, the tests (citing acceptance criteria), the docs to update, the verification steps, and "What Oscar should be able to explain".
4. Leave `status: draft`. Only Oscar sets `approved`.
5. Update the index, the log and build progress, and run `java scripts/WikiLint.java`.
