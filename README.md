# BoardGameBuildSolo

Oscar's solo build of **Net Zero Belfast**, an educational digital board game about UN SDG 13 (Climate Action) for 4-6 players aged 13-16. Players buy polluting or green squares around Belfast from 2026 to 2050, and a shared Climate Meter decides whether the city reaches net zero. Money only counts if it does.

The game was designed by a CSC2058 team at QUB. This repo is a separate solo version and never goes to the team's GitLab repo ([ADR-001](docs/decisions/001-repo-structure-and-harness.md)).

## Start here

| Read | For |
|---|---|
| [AGENTS.md](AGENTS.md) | The rules for AI agents and people working in this repo |
| [docs/wiki/index.md](docs/wiki/index.md) | The wiki: rules, requirements, architecture, UX, decisions and plans |
| [Working with Claude and Codex](docs/wiki/operations/working-with-agents.md) | How to design and build with an agent here |
| [Build progress](docs/wiki/operations/build-progress.md) | What's done, what's next and what's waiting for a decision |

## Layout

| Path | Contents |
|---|---|
| `docs/raw/` | The team handover as received: rules, report draft, data files, diagrams, simulation |
| `docs/` | The wiki: requirements and design, agreed before code |
| `src/` | The game in Java (see [src/AGENTS.md](src/AGENTS.md)). Empty until plan 001 |
| `scripts/` | Development tooling, such as the wiki lint |
| `.claude/` | Claude Code settings and skills |

## Status

**Designing.** The handover is ingested into the wiki. The game will be Java 21 with a Swing UI, built with Maven (ADR-002, ADR-003). The next steps are deciding ADR-004 (JSON parsing) and answering the open rules questions. Then comes plan 001.

## Setting up

1. Install a JDK, version 21 or later. Maven isn't needed: the project will use the Maven Wrapper. See [local development](docs/wiki/operations/local-development.md).
2. Open this folder itself (not a parent folder) in VS Code with Claude Code, or in Codex.
3. Run the check below, then read [Working with Claude and Codex](docs/wiki/operations/working-with-agents.md).

## Checks

```text
java scripts/WikiLint.java
```
