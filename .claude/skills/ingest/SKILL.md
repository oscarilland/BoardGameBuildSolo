---
name: ingest
description: Compile a raw source in docs/raw/ into the Net Zero Belfast wiki (source receipt, affected pages, index, log, lint). Use when a new file has been added to docs/raw/.
argument-hint: docs/raw/<file or folder>
---

Run the **ingest** operation from `docs/AGENTS.md` for: $ARGUMENTS

1. Read `docs/AGENTS.md` for the schema and ingest steps.
2. Read the whole source, including every file in a folder: images, diagrams, data files and Word or PDF documents. Never edit anything in `docs/raw/`.
3. Follow the ingest steps exactly. Record conflicts on the affected pages, and add rules questions to `docs/wiki/rules/open-questions.md`. The ingest is only complete when the affected knowledge pages, `docs/wiki/index.md` and `docs/wiki/log.md` are updated and `java scripts/WikiLint.java` passes.
4. Report the pages created, the pages updated, any conflicts or uncertainties recorded, and open questions for Oscar.
