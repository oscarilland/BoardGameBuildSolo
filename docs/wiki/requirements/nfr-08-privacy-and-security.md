---
title: NFR-08 Privacy and security
type: requirement
id: NFR-08
priority: must
status: draft
sources: [../sources/sub1a-report.md]
updated: 2026-11-17
---

# NFR-08 Privacy and security

## Summary

> Privacy and security: the game shall collect no personal data beyond display names, need no network connection, and validate every input. *(SUB1a; verified by inspection and test)*

## Acceptance criteria

- **AC-1** The only personal data is display names, held in memory and never written to disk.
- **AC-2** The game makes no network connection. Inspection: no networking classes are used, and the game runs with the network disabled.
- **AC-3** Names are trimmed, non-empty and length-limited ([FR-01](fr-01-start-game.md)).
- **AC-4** Every action is checked by the model for being legal now, even when the UI hides illegal ones. Tests send illegal actions straight to the model and expect a rejection.
- **AC-5** Data files are validated on load ([NFR-07](nfr-07-data-driven-balance.md)).

## Out of scope

Encryption (nothing sensitive is stored).

## Related

- [System overview](../architecture/system-overview.md)
