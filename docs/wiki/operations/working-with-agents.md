---
title: Working with Claude and Codex
type: operation
status: draft
sources: [../../decisions/001-repo-structure-and-harness.md]
updated: 2026-11-17
---

# Working with Claude and Codex

A practical guide for Oscar to designing and building Net Zero Belfast with an AI agent in this repo. It works the same way as FeedWatchBuild.

## Getting started

- **Claude Code:** open the `BoardGameBuildSolo` folder itself (not its parent) in VS Code or a terminal. `CLAUDE.md` loads the rules in `AGENTS.md` automatically. The skills below appear as slash commands.
- **Codex:** open the same folder. Codex reads `AGENTS.md` directly. Ask for operations by name, for example "run the decide operation for Q-05".
- Everything the agents need is in the repo's `AGENTS.md` files and the wiki. Nothing depends on a particular machine's agent memory.

## The workflow

| Step | Claude Code | What happens | Your job |
|---|---|---|---|
| Ingest | `/ingest docs/raw/<file>` | A new source (playtest notes, a new card list, feedback) is compiled into the wiki | Drop the file in `docs/raw/` first |
| Specify | `/spec FR-xx` | A requirement page is refined, and the agent asks you questions | Answer them; say when it's agreed |
| Decide | `/decide ADR-002` or `/decide Q-05` | An ADR, or an open rules question, with options and a recommendation | **Write the decision in your own words** |
| Plan | `/plan FR-xx` | A build plan for one slice | Read it; set `status: approved` |
| Build | `/build docs/plans/NNN-...` | Code, tests and doc updates | Review the diff and play it |
| Explain | `/explain <class or rule>` | A walkthrough, then the agent asks you to explain it back | Practise until you can do it without notes |
| Release | `/release` | Checks, a packaged jar and verification | Only when you're ready |

`/build` and `/release` only run when you type them. Claude won't start them on its own.

## Good habits

- **Start each session with the goal**, for example "today: decide ADR-002 to ADR-004 and answer Q-05 and Q-06".
- **Push back.** If a recommendation doesn't make sense, ask why or choose differently. The ADR records *your* decision.
- **Keep the agent honest:** "show me the test for FR-11 AC-2", "which page did you update?", "run the lint".
- **Small slices.** One plan should be one sitting or two.

## Rules the agents follow

The full list is in the root `AGENTS.md`. The important ones:

- **This is a solo version.** Nothing goes to the team's GitLab repo or Canvas. If you take an idea to the team project, you write and commit it yourself and declare any AI help.
- No code without an approved plan; no design choice without your decision.
- Balance numbers and card text only in data files.
- Model separate from UI; one seeded random source.
- Docs updated in the same change as the code.

## Related

- [Project context](../project-context.md)
- [Build progress](build-progress.md)
