package netzerobelfast.ui;

import netzerobelfast.model.Game;
import org.junit.jupiter.api.Test;

import javax.swing.JPanel;
import java.awt.image.BufferedImage;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

/**
 * Builds and paints every screen without opening a window, so a screen that crashes while
 * being built or drawn fails the build. Panels can be made headless; only windows can't.
 * (The first version of the game screen crashed on Start: a bare JComponent has no
 * accessibility context. This test would have caught it.)
 */
class ScreensTest {

    private static void paint(JPanel panel) {
        panel.setSize(MainWindow.MINIMUM_SIZE);
        layOut(panel);
        BufferedImage image = new BufferedImage(panel.getWidth(), panel.getHeight(), BufferedImage.TYPE_INT_RGB);
        panel.printAll(image.createGraphics());
    }

    private static void layOut(java.awt.Container container) {
        container.doLayout();
        for (java.awt.Component child : container.getComponents()) {
            if (child instanceof java.awt.Container inner) {
                layOut(inner);
            }
        }
    }

    @Test
    void everyScreenBuildsAndPaints() {
        Ui.installDefaults();
        assertDoesNotThrow(() -> paint(new TitleScreen("tagline", () -> {}, () -> {}, () -> {})));
        HowToPlayScreen howTo = new HowToPlayScreen(UiGames.CONFIG.rules(), () -> {});
        for (int page = 0; page < HowToPlay.pages(UiGames.CONFIG.rules()).size(); page++) {
            howTo.show(page);
            assertDoesNotThrow(() -> paint(howTo));
        }
        assertDoesNotThrow(() -> paint(new SetupScreen(UiGames.CONFIG.rules(), players -> null, () -> {})));
        assertDoesNotThrow(() -> paint(new GameOverScreen(List.of("a", "b"), () -> {}, () -> {})));
    }

    @Test
    void theGameScreenBuildsAndPaintsThroughAWholeGame() {
        Ui.installDefaults();
        Game game = UiGames.seeded(4);
        GameScreen screen = new GameScreen(UiGames.CONFIG, game, result -> {});
        paint(screen);
        int guard = 0;
        while (game.phase() != Game.Phase.GAME_OVER && guard++ < 5_000) {
            UiGames.step(game);
            if (guard % 7 == 0) {
                screen.refresh();
                paint(screen);
            }
        }
        screen.refresh();
        assertDoesNotThrow(() -> paint(screen));
    }
}
