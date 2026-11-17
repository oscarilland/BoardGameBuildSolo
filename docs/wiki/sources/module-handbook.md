---
title: Source - CSC2058 module handbook
type: source
status: draft
sources: [../../raw/net-zero-belfast-handover/docs/CSC2058_Module_Handbook.pdf]
updated: 2026-11-17
---

# Source - CSC2058 module handbook

## Provenance

- **File:** [CSC2058_Module_Handbook.pdf](../../raw/net-zero-belfast-handover/docs/CSC2058_Module_Handbook.pdf), 18 pages.
- **Dated:** version V02, 1 September 2026, for 2026-27. From the module's teaching team at QUB.
- **Authority:** the module's official brief and assessment rules. Canvas templates and rubrics, which it refers to, are not in the handover.

## What it covers

- **The brief:** an educational game on UN SDG 13 for a defined audience, built by a team with use cases, UML diagrams and version-controlled code. Any object-oriented language; Java strongly recommended. Manual and automated testing (JUnit) with evidence of coverage.
- **Submissions:** SUB1 in week 11 (30%): interim report (10 pages), 3-minute demo video, contribution statements. SUB2 in week 24 (70%): final report (design documentation, class diagram, acceptance test plan, game demo schedule of up to 14 core requirements and 6 value-added features), 7-minute demo video, zipped code repository, contribution statements.
- **GitLab:** the team's EEECS GitLab repo is the evidence of each member's contribution. GitHub is not accepted instead. Staff need Maintainer access. At SUB2 the repo must hold commented source, executables, a README with install and run instructions, and a licence.
- **Generative AI:** must be acknowledged in submissions (the report's declaration).

## What it contributed

- [Project context](../project-context.md): why this solo repo stays separate from the team's GitLab repo.
- [ADR-003](../../decisions/003-build-tool-and-java-version.md): Java recommended; coverage evidence; executables.
- [NFR-06](../requirements/nfr-06-reliability-and-test-coverage.md): JUnit and coverage as module expectations.
- [Requirements catalogue](../requirements/catalogue.md): the SUB2 demo schedule limit of 14 core requirements and 6 value-added features, which will shape which requirements get demonstrated.

## Limitations and caveats

- The handbook is about the team project. This solo repo isn't assessed. It's kept here so that the build stays close to what the module values, and so the rules about commits and AI use are visible.
- It says nothing about the lab PCs' Java version, which [ADR-003](../../decisions/003-build-tool-and-java-version.md) needs.
