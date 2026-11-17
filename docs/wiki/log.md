# Net Zero Belfast wiki log

Append-only. Add new entries at the end, using `## [YYYY-MM-DD] operation | title`.

## [2026-11-17] repository | LLMWiki and agent harness established

Built the harness from FeedWatchBuild's, adapted for a Java desktop game ([ADR-001](../decisions/001-repo-structure-and-harness.md), proposed). Oscar's answers: this repo is a **solo version only** and never goes to the team's GitLab repo, and the first pass is the harness plus a full ingest, with no code. Created the root, `docs/` and `src/` `AGENTS.md` files with `CLAUDE.md` imports, the wiki schema, ADR and plan templates, seven skills, `.claude/settings.json`, `scripts/WikiLint.java` (ported from FeedWatchBuild's PHP lint without the public/private checks), `.gitignore`, `.gitattributes` and a CI workflow that runs the lint. Moved the team handover from `NetZeroBelfast_handover/handover/` to `docs/raw/net-zero-belfast-handover/`, unchanged.

## [2026-11-17] ingest | Team handover (all 22 files)

Read every file in the handover: `HANDOVER.md`, rules v4 (Markdown and Word, which match), the SUB1a report draft, the module handbook (all 18 pages), the three data files, all diagrams (PNGs plus Graphviz, Mermaid and draw.io sources) and the balance simulation. Created seven source receipts, the overview, project context, learning outcomes, ten rules pages, the requirements catalogue with FR-01 to FR-17 and NFR-01 to NFR-11 (SUB1a wording, with drafted acceptance criteria) and stretch items S-01 and S-02, four architecture pages, the screens page and three operations pages.

Recorded on ingest: the board drawing numbers squares from 1 but `board.json` from 0; no data file has the disaster explanations FR-17 needs; unaffordable Choice options aren't covered; the quiz reward is stored twice; "after 2 rounds" for bankruptcy is ambiguous; carbon tax and meter treatment of closed or flooded squares isn't stated; Legacy Score rounding isn't stated; Flood or Storm with no owned squares isn't covered. These are Q-11 to Q-17 on [open questions](rules/open-questions.md), alongside the handover's Q-01 to Q-10. Also noted: the sequence diagrams have model objects talking to the player, which [game flow](architecture/game-flow.md) proposes to handle by returning pending decisions.

## [2026-11-17] decide | ADR-002 to ADR-004 proposed

Drafted ADRs for the UI toolkit (recommends Swing, for NFR-09), the build tool and Java version (recommends the Maven Wrapper, targeting Java 17 until the lab PCs' version is known), and reading the JSON data files (recommends Gson with validation on load). Decisions are left for Oscar.
