---
title: Source - Design diagrams
type: source
status: draft
sources: [../../raw/net-zero-belfast-handover/design/class_diagram.dot]
updated: 2026-11-17
---

# Source - Design diagrams

## Provenance

- **Folder:** [raw/net-zero-belfast-handover/design/](../../raw/net-zero-belfast-handover/design/class_diagram.png). Every diagram is a PNG, and most have an editable source alongside: Graphviz (`.dot`), Mermaid (`.mmd`) or draw.io.
- **Dated:** November 2026, drawn for the SUB1a report.
- **Authority:** the team's analysis model and draft UI. The report commits to the class and method names, so code should keep them recognisable.

| Diagram | Files |
|---|---|
| Use case diagram | [PNG](../../raw/net-zero-belfast-handover/design/use_case_diagram.png), [draw.io](../../raw/net-zero-belfast-handover/design/use_case_diagram.drawio) |
| Analysis class diagram | [PNG](../../raw/net-zero-belfast-handover/design/class_diagram.png), [Graphviz](../../raw/net-zero-belfast-handover/design/class_diagram.dot) |
| Sequence: Take Turn | [PNG](../../raw/net-zero-belfast-handover/design/seq_take_turn.png), [Mermaid](../../raw/net-zero-belfast-handover/design/seq_take_turn.mmd) |
| Sequence: Draw Climate Card | [PNG](../../raw/net-zero-belfast-handover/design/seq_draw_climate_card.png), [Mermaid](../../raw/net-zero-belfast-handover/design/seq_draw_climate_card.mmd) |
| Sequence: End Round | [PNG](../../raw/net-zero-belfast-handover/design/seq_end_round.png), [Mermaid](../../raw/net-zero-belfast-handover/design/seq_end_round.mmd) |
| Board layout | [PNG](../../raw/net-zero-belfast-handover/design/board_layout.png) |
| Main screen wireframe and screen flow | [PNG](../../raw/net-zero-belfast-handover/design/main_screen_wireframe.png) |
| Game loop with learning outcomes | [PNG](../../raw/net-zero-belfast-handover/design/game_loop.png) |

## What it contributed

- [Domain model](../architecture/domain-model.md): every class, attribute, method and multiplicity.
- [Game flow](../architecture/game-flow.md): the three sequences and the use case structure.
- [Board and squares](../rules/board-and-squares.md): the layout.
- [Screens and layout](../ux/screens-and-layout.md): the wireframe and screen flow.
- [Learning outcomes](../learning-outcomes.md): where each LO sits in the game loop.

## Limitations and caveats

- **Board numbering differs.** `board_layout.png` numbers the loop 1-30 and the City Centre C1-C6. `board.json` numbers them 0-29 and 30-35. The order is the same (drawing number = id + 1), so Glider East is "13" in the drawing and id 12 in the data. The data file's ids are the ones to use.
- The board drawing says players may cut through the City Centre "in either direction"; `board.json` says the direction is TBD ([Q-05](../rules/open-questions.md)).
- The sequence diagrams show model objects talking to the human player directly (for example `ClimateCard` showing card text). Coding it literally would tie the model to the UI; see [game flow](../architecture/game-flow.md).
- The use case diagram omits the Glider, and the class diagram has no `Action` class even though `landOn()` returns `Action[]`.
- The wireframe shows a sample state (Round 4, 2032, meter 33, band 3), not real rules data.
