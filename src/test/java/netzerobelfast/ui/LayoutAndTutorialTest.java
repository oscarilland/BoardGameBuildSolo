package netzerobelfast.ui;

import netzerobelfast.config.BoardDef;
import netzerobelfast.config.BoardDef.SquareDef;
import netzerobelfast.config.Rules;
import netzerobelfast.model.Game.PlayerSetup;
import netzerobelfast.ui.BoardLayout.Cell;
import netzerobelfast.ui.HowToPlay.Page;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** The board layout, the tutorial, the setup form's rules, and the no-polling rule. */
class LayoutAndTutorialTest {

    private static final BoardDef BOARD = UiGames.CONFIG.board();
    private static final Rules RULES = UiGames.CONFIG.rules();

    // ---- Board layout ----------------------------------------------------------

    @Test
    void everySquareHasItsOwnCell() {
        BoardLayout layout = new BoardLayout(BOARD);
        assertEquals(10, layout.columns());
        assertEquals(7, layout.rows());
        Set<Cell> used = new HashSet<>();
        for (SquareDef square : BOARD.squares()) {
            Cell cell = layout.cell(square.id());
            assertTrue(used.add(cell), "two squares share " + cell);
            assertTrue(cell.column() >= 0 && cell.column() < layout.columns() && cell.row() >= 0 && cell.row() < layout.rows());
        }
        assertEquals(BOARD.squares().size(), layout.cells().size());
    }

    @Test
    void theLoopRunsClockwiseFromTheTopLeft() {
        BoardLayout layout = new BoardLayout(BOARD);
        List<Integer> loop = BOARD.loop();
        assertEquals(new Cell(0, 0), layout.cell(loop.get(0)));
        assertEquals(new Cell(9, 0), layout.cell(loop.get(9)));
        assertEquals(new Cell(9, 1), layout.cell(loop.get(10)));
        assertEquals(new Cell(9, 6), layout.cell(loop.get(15)));
        assertEquals(new Cell(0, 6), layout.cell(loop.get(24)));
        assertEquals(new Cell(0, 1), layout.cell(loop.get(29)));
        // Each loop square is next to the one before it, so a counter's path is easy to follow.
        for (int i = 1; i < loop.size(); i++) {
            Cell a = layout.cell(loop.get(i - 1));
            Cell b = layout.cell(loop.get(i));
            assertEquals(1, Math.abs(a.column() - b.column()) + Math.abs(a.row() - b.row()), "gap between " + a + " and " + b);
        }
    }

    @Test
    void theCityCentreRouteRunsBetweenTheJunctionsOnTheMiddleRow() {
        BoardLayout layout = new BoardLayout(BOARD);
        Cell west = layout.cell(BOARD.westJunction());
        Cell east = layout.cell(BOARD.eastJunction());
        assertEquals(3, west.row());
        assertEquals(3, east.row());
        List<Integer> route = BOARD.cityCentreRoute();
        for (int i = 0; i < route.size(); i++) {
            Cell cell = layout.cell(route.get(i));
            assertEquals(3, cell.row());
            assertEquals(2 + i, cell.column(), "route square " + i);
        }
    }

    @Test
    void aLoopThatCantMakeARingIsRefused() {
        List<Integer> loop = new ArrayList<>(BOARD.loop());
        loop.add(99);
        BoardDef odd = new BoardDef(BOARD.districts(), loop, BOARD.cityCentreRoute(), BOARD.westJunction(),
                BOARD.eastJunction(), BOARD.junctions(), BOARD.gliderStops(), BOARD.cityCentreDirection(), BOARD.squares());
        assertThrows(IllegalArgumentException.class, () -> new BoardLayout(odd));
        assertThrows(IllegalArgumentException.class, () -> new BoardLayout(BOARD).cell(99));
    }

    // ---- How to play (NFR-02 AC-1) -----------------------------------------------

    @Test
    void nfr02_ac1_fivePagesReadableInTwoMinutes() {
        List<Page> pages = HowToPlay.pages(RULES);
        assertTrue(pages.size() >= 4 && pages.size() <= 5, pages.size() + " pages");
        int words = pages.stream().mapToInt(Page::words).sum();
        // 200 words a minute is a slow reading pace for 13-16 year olds' plain text.
        assertTrue(words <= 2 * 200, words + " words");
        Set<HowToPlay.Picture> pictures = new HashSet<>();
        pages.forEach(page -> pictures.add(page.picture()));
        assertEquals(pages.size(), pictures.size(), "each page has its own picture");
    }

    @Test
    void nfr07_tutorialNumbersComeFromTheData() {
        String all = String.join(" ", HowToPlay.pages(RULES).stream().flatMap(p -> p.paragraphs().stream()).toList());
        assertTrue(all.contains("starts at " + RULES.meter().start()), all);
        assertTrue(all.contains(RULES.game().rounds() + " rounds"), all);

        Rules.Meter m = RULES.meter();
        Rules changed = new Rules(RULES.game(), RULES.characters(), RULES.polluting(), RULES.green(), RULES.actions(),
                RULES.resilience(), RULES.quizCorrectGP(),
                new Rules.Meter(61, m.basePerRound(), m.netZeroAtOrBelow(), m.interimTargets(), m.interimMissPenaltyGP(),
                        m.dangerBands(), m.belowZeroBand()),
                RULES.disasters(), RULES.bankruptcy(), RULES.scoring());
        String retuned = String.join(" ", HowToPlay.pages(changed).get(0).paragraphs());
        assertTrue(retuned.contains("starts at 61"), retuned);
    }

    // ---- Setup (FR-01) ---------------------------------------------------------

    private static List<PlayerSetup> four(String name4, String character4) {
        return List.of(new PlayerSetup("Aoife", "INDUSTRIALIST"), new PlayerSetup("Ben", "TECH_ENTREPRENEUR"),
                new PlayerSetup("Chloe", "COUNCIL_PLANNER"), new PlayerSetup(name4, character4));
    }

    @Test
    void fr01_startEnabledOnlyWhenTheFormIsComplete() {
        Rules.Game game = RULES.game();
        assertTrue(SetupRules.missing(four("Dev", "FARMER"), game).isEmpty());
        assertEquals("Player 4 needs a name.", SetupRules.missing(four("   ", "FARMER"), game).orElseThrow());
        assertEquals("Player 4 needs a name.", SetupRules.missing(four(null, "FARMER"), game).orElseThrow());
        assertEquals("Player 4 needs a character.", SetupRules.missing(four("Dev", null), game).orElseThrow());
        assertEquals("Player 4 has a character someone else has chosen.",
                SetupRules.missing(four("Dev", "INDUSTRIALIST"), game).orElseThrow());
        assertEquals("Choose 4 to 6 players.", SetupRules.missing(four("Dev", "FARMER").subList(0, 3), game).orElseThrow());
    }

    // ---- NFR-11 AC-2 -------------------------------------------------------------

    @Test
    void nfr11_ac2_onlyTheAnimatorUsesATimer() throws IOException {
        Path ui = Path.of("src/main/java/netzerobelfast/ui");
        List<String> found = new ArrayList<>();
        List<Path> sources;
        try (Stream<Path> files = Files.list(ui)) {
            sources = files.filter(f -> f.toString().endsWith(".java")).toList();
        }
        assertFalse(sources.isEmpty(), "no UI sources found from " + ui.toAbsolutePath());
        {
            for (Path file : sources) {
                String source = Files.readString(file);
                for (String banned : List.of("Thread.sleep", "while (true)", "java.util.Timer")) {
                    if (source.contains(banned)) {
                        found.add(file.getFileName() + ": " + banned);
                    }
                }
                // Plan 008: animation needs a timer, but only the Animator may have one, and it stops itself (AnimatorTest).
                if (source.contains("javax.swing.Timer") && !file.getFileName().toString().equals("Animator.java")) {
                    found.add(file.getFileName() + ": javax.swing.Timer outside Animator");
                }
            }
        }
        assertTrue(found.isEmpty(), "polling or timers in the UI: " + found);
        assertTrue(Files.readString(ui.resolve("Animator.java")).contains("timer.stop()"), "the Animator must stop its timer");
    }
}
