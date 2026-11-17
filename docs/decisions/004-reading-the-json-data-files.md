---
title: ADR-004 Reading the JSON data files
type: decision
id: ADR-004
status: proposed
sources: [../wiki/sources/data-files.md]
updated: 2026-11-17
---

# ADR-004 - Reading the JSON data files

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

*Oscar writes this section in his own words: what he chose and why. Until he does, the status stays `proposed`.*

## Consequences

- Gson is listed with its version and licence in the [system overview](../wiki/architecture/system-overview.md).
- `config` gets one record per file section, and a validation test for each rule.

## How we'll know it worked

Changing a price in the working `config.json` changes the game with no code change, and a deliberately broken data file stops start-up with a message naming the file and key.

## Questions a marker might ask

**"How does the game meet NFR-07?"** Every number and card is loaded from JSON at start-up through one `GameConfig` class and validated, so re-tuning after a playtest is an edit to a data file, not to the code.
