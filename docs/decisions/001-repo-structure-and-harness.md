---
title: ADR-001 Repo structure and build harness
type: decision
id: ADR-001
status: accepted
sources: [../wiki/sources/handover.md, ../wiki/sources/module-handbook.md]
updated: 2026-11-17
---

# ADR-001 - Repo structure and build harness

**Status:** accepted 17 November 2026 (proposed the same day). **Option A: FeedWatchBuild's harness, adapted for a Java game.**

## Context

Oscar wants to build the Net Zero Belfast game designed by his CSC2058 team, using the same agent harness as his FeedWatchBuild repo: an LLM wiki in `docs/`, gated operations, and one set of rules for Claude Code and Codex.

Two facts shape this repo:

- **The module judges each student by their own commits on the team's QUB GitLab repo**, doesn't accept GitHub as evidence, and requires generative AI use to be declared ([module handbook](../wiki/sources/module-handbook.md)). Code an agent writes here can't simply become team work.
- **The design already exists.** The team handover has rules v4, 17 functional and 11 non-functional requirements, an analysis class diagram, three sequence diagrams, a wireframe, data files and a balance simulation ([handover receipt](../wiki/sources/handover.md)). The wiki starts by compiling that, not by inventing a design.

Oscar's answers when the harness was set up (17 November 2026):

1. **This repo is a solo version only.** It never goes to the team's GitLab repo.
2. **The first pass is the harness plus a full ingest** of the handover, with no game code.

## Options

### Option A - FeedWatchBuild's harness, adapted for a Java game (recommended)

Keep FeedWatchBuild's structure: root `AGENTS.md` with `CLAUDE.md` imports, `docs/raw → wiki → decisions → plans`, numbered ADRs and plans, the three gates, thin skills, a wiki lint and CI. Change what doesn't fit:

| FeedWatchBuild | Here | Why |
|---|---|---|
| Public and private pages, a publishing ADR | Dropped | This repo is never published |
| PHP wiki lint | `scripts/WikiLint.java`, run as a single-file Java program | One language in the repo, and it needs no build tool |
| `domain/` pages | `rules/` pages, plus an open questions page | The "domain" is the game's rules |
| `showcase/`, interview questions | Dropped; "questions a marker might ask" | The audience is module markers and teammates, not employers |
| Deploy to a web host | Package a runnable game and test it from a clean folder | It's a desktop game (NFR-09) |
| FR and NFR IDs invented in the wiki | The SUB1a report's IDs, unchanged | Tests and plans can then trace to the team's agreed requirements |

- **For:** a harness Oscar already knows works. The gates suit a design that is detailed but has open questions.
- **Against:** a lot of structure for a solo coursework-sized game.

### Option B - Code-first, with the handover as the spec

Start a Java project straight away, and treat `HANDOVER.md` as the only spec.

- **For:** code sooner.
- **Against:** open questions get decided silently in code, and there's no record to explain choices from.

## Recommendation

Option A. The handover is unusually complete, so compiling it costs little, and the open questions (ten in the handover plus four found on ingest) are exactly what the "Oscar decides" gate is for.

## Decision

"I confirm the repo structure." - Oscar, 17 November 2026.

This confirms Option A as built, together with his two earlier answers (solo version only; harness plus a full ingest first). Oscar may reword this section at any time.

## Consequences

- Every rules question waits for Oscar's answer before the code that depends on it.
- The raw data files stay unchanged; the game gets its own working copies (plan 001).
- Nothing here can be reused in the team repo without Oscar rewriting and committing it himself, and declaring AI use.

## How we'll know it worked

Each plan can cite agreed requirements and accepted ADRs, and Oscar can explain every class from its wiki page.

## Questions a marker might ask

**"Why is there a separate solo repo?"** It's a personal build of the team's design, kept apart so that the team's GitLab history shows only each member's own work, as the module requires.
