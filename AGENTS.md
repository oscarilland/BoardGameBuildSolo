# AGENTS.md - BoardGameBuildSolo

Instructions for AI agents (Claude Code, Codex) and people working in this repository. `CLAUDE.md` imports this file, so every tool follows one set of rules.

## What this repo is

**Net Zero Belfast** is an educational digital board game about UN SDG 13 (Climate Action) for 4-6 players aged 13-16 on one shared computer. Players buy polluting or green squares around a board of Belfast districts from 2026 to 2050, while a shared Climate Meter decides whether the city reaches net zero.

The game was designed by a six-person team for the QUB module CSC2058. **This repo is Oscar's own solo version** of it, and it never goes to the team's GitLab repo ([ADR-001](docs/decisions/001-repo-structure-and-harness.md)). The team's design arrived as a handover, filed unchanged in `docs/raw/net-zero-belfast-handover/`.

## Start here

1. Read [docs/wiki/overview.md](docs/wiki/overview.md) for the game and [docs/wiki/project-context.md](docs/wiki/project-context.md) for why this repo exists.
2. Read [docs/wiki/index.md](docs/wiki/index.md), then only the pages relevant to the task.
3. Before changing anything in `docs/`, read [docs/AGENTS.md](docs/AGENTS.md). Before changing anything in `src/`, read [src/AGENTS.md](src/AGENTS.md).

## Repository map

| Path | Purpose | Rule |
|---|---|---|
| `docs/raw/` | Source material as received, including the team handover | Never edit. Only add new files |
| `docs/wiki/` | Rules, requirements, architecture, UX and operations knowledge | Maintained by agents under `docs/AGENTS.md` |
| `docs/decisions/` | Architecture decision records (ADRs), numbered | Agents draft the options; Oscar decides |
| `docs/plans/` | Build plans, numbered | Code changes need an approved plan |
| `src/` | The game (Java), once building starts | Follow `src/AGENTS.md` |
| `scripts/` | Development tooling, such as the wiki lint | |
| `.claude/` | Claude Code settings and skills | Skills are thin wrappers around the operations in `docs/AGENTS.md` |

## How work flows

`ingest → spec → decide → plan → build → explain → release`

The operations are defined in [docs/AGENTS.md](docs/AGENTS.md#operations). In Claude Code they are the skills `/ingest`, `/spec`, `/decide`, `/plan`, `/build`, `/explain` and `/release`. In Codex, ask for the operation by name, for example "run the plan operation for FR-03".

## Gates

1. **Plan before code.** Don't change `src/` without a plan in `docs/plans/` marked `status: approved`. Small bug fixes that come with a regression test are exempt; record them in `docs/wiki/log.md`.
2. **Oscar decides.** Design choices go in an ADR with options, trade-offs and a recommendation. Open rules questions are listed in [docs/wiki/rules/open-questions.md](docs/wiki/rules/open-questions.md). Don't choose silently, and don't mark an ADR `accepted` until Oscar has written the decision.
3. **Release only on request, and verify it.** A build that compiles is not a game that runs (see below).

## Hard rules

1. **Solo version only.** Never push, copy or submit anything from this repo to the team's QUB GitLab repo or to Canvas, and never present this code as team work. The module judges each student by their own commits, and requires generative AI use to be declared.
2. **Balance values and card content come from data files**, never from code (NFR-07). If a number in the rules isn't in a data file yet, add it to the data file, not to the code.
3. **Game logic stays separate from the user interface.** Model classes never import UI classes, so the whole game can run headless in tests.
4. **All randomness goes through one injected, seedable random source.** Never call `Math.random()` or create a `new Random()` inside game logic.
5. No new runtime dependency, framework or build step without an accepted ADR.
6. Never edit files in `docs/raw/`. The game's working copies of the data files live in `src/` once plan 001 creates them.
7. Keep docs in step with code. The change that alters behaviour also updates the affected wiki pages, `docs/wiki/index.md` and `docs/wiki/log.md`.
8. No personal data beyond display names, and no network access (NFR-08).
9. Where the handover's `HANDOVER.md` and `rules_v4.md` disagree, `HANDOVER.md` wins. It holds clarifications agreed after v4.

## Validation

Use the smallest checks that cover the change while working:

| Change | Check |
|---|---|
| Docs | `java scripts/WikiLint.java` and `git diff --check` |
| Java source | Decided by [ADR-003](docs/decisions/003-build-tool-and-java-version.md). Until then there is no source to check |
| UI | The source checks, then launch the game and play at least one full round by hand |

Run the full set once before a release, not after every edit. Never report a check as passed if it failed or wasn't run.

## Definition of done

1. `git status` checked before and after; unrelated work left alone.
2. Tests cover the changed behaviour, including failure and edge cases, and cite acceptance criteria.
3. The relevant checks above pass.
4. Wiki pages match the code, and `index.md` and `log.md` are updated.
5. UI changes checked by playing the game, at the smallest window size the game supports and at full screen.
6. Final diff reviewed for generated files and unintended changes.

## Release verification

Release only when asked (see the release operation in `docs/AGENTS.md`). After packaging, check that:

- the packaged game starts from a clean folder with only a Java runtime (NFR-09);
- a seeded headless game runs to the end without errors;
- one round can be played through the UI;
- `docs/wiki/operations/` and `docs/wiki/log.md` say what changed and what didn't.

## Git

- Commit straight to `main`. Don't create branches unless asked.
- Commit or push only when Oscar asks. Fetch before pushing. Never force-push.
- Commit messages: an imperative summary line, then a short body explaining why.

## Writing style

UK English. Plain, direct sentences. Hyphens, not em dashes.
