---
title: ADR-004 Reading the JSON data files
type: decision
id: ADR-004
status: accepted
sources: [../wiki/sources/data-files.md]
updated: 2026-11-17
---

# ADR-004 - Reading the JSON data files

**Status:** accepted 17 November 2026 (proposed the same day). **Gson, with validation on load.**

## Context

[NFR-07](../wiki/requirements/nfr-07-data-driven-balance.md) requires every balance value and all card content to come from data files. The handover provides three JSON files ([data files](../wiki/architecture/data-files.md)): `config.json` (nested objects and arrays, some `null`s), `board.json` (36 squares) and `climate_cards.json` (30 cards with nested options and source lists, and non-ASCII text such as "Éowyn" and "CO₂e"). Java's standard library has no JSON parser.

The root `AGENTS.md` says no new runtime dependency without an accepted ADR, and NFR-09 wants a single runnable artefact.

## Options

### Option A - Gson (recommended)

Google's JSON library: one jar of about 300 KB with no dependencies of its own, Apache 2.0 licence. Map each file onto small Java records or classes.

- **For:** small, mature and easy to explain ("Gson turns this JSON object into this Java class"). Shaded into the single jar, so NFR-09 still holds.
- **Against:** a dependency to record and keep up to date. Silent defaults for missing fields, so `GameConfig` must validate after loading.

### Option B - Jackson

- **For:** the industry standard, strict by default.
- **Against:** three jars and a larger API than this needs.

### Option C - A small hand-written JSON parser

About 150-200 lines: a recursive-descent parser into `Map`, `List`, `String`, `Long`, `Double`, `Boolean` and `null`.

- **For:** no dependency. Good to explain as an algorithm.
- **Against:** more code to test, and Unicode escapes and number edge cases are easy to get wrong. It's effort spent away from the game.

### Option D - Convert the data to `.properties` files

- **Against:** can't represent the nested cards and lists without inventing a format. Rejected.

## Recommendation

**Gson (Option A)**, with a `GameConfig.validate()` step that checks counts (36 squares, 30 cards, 6 characters), ranges and required keys, and fails at start-up with a clear message. It keeps the data files exactly as the team wrote them.

## Decision

"I choose Gson." - Oscar, 17 November 2026.

Oscar chose the recommended option and didn't add reasons of his own, so the reasons are the recommendation's: one small library with no dependencies of its own, the team's data files kept exactly as written, and a validation step to cover Gson's silent defaults. Oscar may reword this section at any time.

## Consequences

- Gson is the game's only runtime library. Plan 001 adds it to `pom.xml` at a pinned version and records the version and licence (Apache 2.0) in the [system overview](../wiki/architecture/system-overview.md). Maven bundles it into the single runnable jar, so NFR-09 still holds.
- Gson is used only inside the `config` package. The model sees plain Java records, never Gson types, so the library could be swapped without touching the rules.
- `config` gets one record per file section, a `validate()` step, and a test for each validation rule, including deliberately broken files.
- Keys starting with `_` (such as `_note`) are comments and are ignored.

> **Correction found while building plan 001 (17 November 2026):** Gson 2.15.0 does have one dependency of its own, `com.google.errorprone:error_prone_annotations`, which the Context and Option A above didn't mention. It holds annotations used only by compilers and code checkers, so the jar leaves it out and Gson works without it. The decision stands. Also, Gson reads the files as a strict JSON tree that the `config` package maps to records itself, rather than filling records directly, so that every error names its key ([data files](../wiki/architecture/data-files.md)).

## How we'll know it worked

Changing a price in the working `config.json` changes the game with no code change, and a deliberately broken data file stops start-up with a message naming the file and key.

## Questions a marker might ask

**"How does the game meet NFR-07?"** Every number and card is loaded from JSON at start-up through one `GameConfig` class and validated, so re-tuning after a playtest is an edit to a data file, not to the code.
