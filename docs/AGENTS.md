# docs/AGENTS.md - LLMWiki schema and operations

Read this before changing anything in `docs/`. It applies to Claude Code and Codex alike.

`docs/` follows the LLM Wiki pattern: sources are read once and compiled into maintained Markdown pages, rather than rediscovered for every question. Net Zero Belfast arrived as a detailed team handover, so the wiki starts **compiled** (rules, requirements and the analysis model from the handover) and grows **authored** design (ADRs, plans, architecture) that has to be agreed **before** code is written.

## Layers

| Layer | Path | What it is | Who writes it |
|---|---|---|---|
| Raw sources | `raw/` | Source material as received: the team handover, notes, playtest results | People add files; nobody edits them |
| Wiki | `wiki/` | Current knowledge: rules, requirements, architecture, UX, operations | Agents, reviewed by Oscar |
| Decisions | `decisions/` | ADRs, numbered, never renumbered | Agents draft; Oscar decides |
| Plans | `plans/` | Build plans, the gate between design and code | Agents draft; Oscar approves |
| Schema | this file | The rules | Changed deliberately |

## Wiki layout

| Path | Content |
|---|---|
| `wiki/index.md` | Every page in `docs/` (except `raw/`), one line each. Read it first |
| `wiki/log.md` | Append-only history |
| `wiki/overview.md` | The game on one page |
| `wiki/project-context.md` | Why this solo repo exists, and its constraints |
| `wiki/learning-outcomes.md` | What players should learn, and where the game teaches it |
| `wiki/rules/` | The game rules, one topic per page, plus the open rules questions and balance history |
| `wiki/requirements/` | `catalogue.md` plus one page per requirement (`fr-01-...`, `nfr-01-...`) |
| `wiki/architecture/` | System overview, domain model, game flow, data files |
| `wiki/ux/` | Screens, layout and accessibility rules |
| `wiki/operations/` | Local set-up, build progress, working with agents |
| `wiki/sources/` | One receipt per raw source: provenance, coverage, limitations |

## Frontmatter

Every page in `docs/` except `raw/`, `index.md`, `log.md`, `AGENTS.md`, `CLAUDE.md`, `README.md` and `_template.md` files starts with:

```yaml
---
title: Human readable title
type: overview | context | rules | requirement | architecture | ux | operation | source | decision | plan
status: (see below)
sources: [relative/path.md]      # optional; where the content came from
updated: YYYY-MM-DD
---
```

Requirements also have `id: FR-02` (or `NFR-01`) and `priority: must | should | could`. Decisions have `id: ADR-002`. Plans have `id: PLAN-001` and `requirements: [FR-01, NFR-03]`.

**Status by type:**

| Type | Allowed status | Meaning |
|---|---|---|
| Wiki pages | `draft` → `agreed` → `built` | `agreed` = Oscar has reviewed it. `built` = the code now matches it |
| `decision` | `proposed` → `accepted`, or `superseded` / `rejected` | Only Oscar moves an ADR to `accepted` |
| `plan` | `draft` → `approved` → `done` | Only Oscar approves |

This repo is never published, so pages have no public or private marking.

## Naming and links

- Files: `kebab-case.md`, one topic per page. Split anything heading past about 200 lines.
- Requirements: `fr-NN-short-name.md` and `nfr-NN-short-name.md`. IDs come from the team's SUB1a report and are never changed or reused. New requirements continue the sequence (FR-18, NFR-12).
- Decisions and plans: `NNN-short-name.md` with a three-digit sequence. Never renumber. Templates are `_template.md`.
- Links: relative Markdown links only, such as `[buying squares](../requirements/fr-04-buy-square.md)`. Don't use `[[wikilinks]]`, because GitHub doesn't render them and the lint rejects them.
- Log headings: `## [YYYY-MM-DD] operation | title`, where `operation` is one of `ingest`, `query`, `spec`, `decide`, `plan`, `build`, `explain`, `release`, `lint`, `repository`.

## Page shapes

Keep to these sections so pages are predictable. Leave a section out if it really has nothing to say.

- **Rules:** Summary · Rules (numbered, with the values and the data file key they come from) · Edge cases · Conflicts or uncertainties · Related
- **Requirement:** Summary · Behaviour · Acceptance criteria (`AC-1`, `AC-2` and so on, each checkable) · Out of scope · Open questions for Oscar · Related
- **Architecture component:** Summary · Responsibilities · Interfaces (classes, methods, data files) · Key design choices (link ADRs) · Failure modes · Explain it in 30 seconds · Questions a marker might ask · Related
- **Source receipt:** Provenance (date, author, authority) · What it covers · What it contributed (pages) · Limitations and caveats
- **Decision:** see [decisions/_template.md](decisions/_template.md)
- **Plan:** see [plans/_template.md](plans/_template.md)

## Operations

Each operation finishes by updating `wiki/index.md`, appending to `wiki/log.md` and running `java scripts/WikiLint.java`. The operation isn't finished until the lint passes.

### ingest `<raw file>`

1. Read the whole source, including images, diagrams and data files. Record its provenance, authority and date.
2. Create or update its receipt in `wiki/sources/`.
3. Search `wiki/index.md` and update the existing pages it affects. Create new pages only when nothing suitable exists.
4. Record contradictions or uncertainty on the affected page under **Conflicts or uncertainties**, and add rules questions to `wiki/rules/open-questions.md`. Don't silently pick one version.
5. Add cross-links, then update the index, log and lint.

Filing or converting a file is preparation, not ingestion. The ingest is complete only when the knowledge pages have been updated.

### query `<question>`

Read `wiki/index.md`, then the smallest relevant set of pages, then the code if needed. Answer with links. If the answer is durable knowledge, file it into the right page using the ingest rules. Otherwise leave it in the chat.

### spec `<feature>`

Draft or refine a requirement page. Ask Oscar the questions that change the behaviour, record his answers, and write the acceptance criteria as checkable statements. Leave it as `draft` until Oscar agrees it, then set `agreed` and update `requirements/catalogue.md`.

### decide `<question>`

Draft an ADR from the template: context, two or three real options with trade-offs, and a recommendation. Leave the **Decision** section for Oscar to write in his own words, and keep the status `proposed` until he has. When accepted, update the affected architecture pages. For a rules question, record Oscar's answer on `rules/open-questions.md` and the affected rules page instead.

### plan `<requirement IDs>`

Draft `plans/NNN-name.md` from the template for one vertical slice. Cite requirement IDs and ADRs, list the tasks, tests and wiki pages to update, and include "What Oscar should be able to explain". Status `draft`; Oscar approves.

### build `<plan>`

Only for a plan with `status: approved`. Follow `src/AGENTS.md`. When finished: tests pass, affected wiki pages are updated and moved to `built`, the plan is marked `done` with notes on anything that differed from it, and the log is updated.

### explain `<component or concept>`

Walk Oscar through the relevant rules or architecture page and the code behind it. Then ask him to explain it back, and ask the questions a marker or teammate is likely to ask. Improve the page's "Explain it in 30 seconds" section with what was learned.

### release

Run the full validation, package the game only when asked, verify it as described in the root `AGENTS.md`, update `wiki/operations/` and log what changed.

### lint

Run `java scripts/WikiLint.java`. It checks links, orphans, index coverage, frontmatter and log headings. Then do the semantic part yourself: contradictions, stale claims, requirements without acceptance criteria, rules pages that disagree with the data files, and pages still in `draft` that the code has overtaken.
