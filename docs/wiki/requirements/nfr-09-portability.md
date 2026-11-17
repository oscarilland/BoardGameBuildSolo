---
title: NFR-09 Portability
type: requirement
id: NFR-09
priority: should
status: draft
sources: [../sources/sub1a-report.md]
updated: 2026-11-17
---

# NFR-09 Portability

## Summary

> Portability: the game shall run on Windows and macOS with only a Java runtime installed. *(SUB1a; verified manually)*

## Acceptance criteria

- **AC-1** One packaged artefact (a runnable jar) starts with `java -jar` on Windows and on macOS, with nothing else installed beyond a Java runtime of the targeted version.
- **AC-2** The data files are inside the artefact, so the game runs from any folder.
- **AC-3** No OS-specific paths or line-ending assumptions.

## Open questions for Oscar

1. Is a Mac available to test AC-1 on macOS?

## Related

- [ADR-002 UI toolkit](../../decisions/002-ui-toolkit.md)
- [ADR-003 Build tool and Java version](../../decisions/003-build-tool-and-java-version.md)
