package netzerobelfast.ui;

import netzerobelfast.config.GameConfig;
import netzerobelfast.config.Rules;
import netzerobelfast.model.Dice;
import netzerobelfast.model.Game;
import netzerobelfast.model.Game.PlayerSetup;
import netzerobelfast.model.GameResult;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.WindowConstants;
import java.awt.CardLayout;
import java.awt.Dimension;
import java.util.List;
import java.util.Random;

/**
 * The game window: one frame that switches between the title, How to play, setup, game and
 * final-scores screens (plan 007).
 *
 * <p>Swing isn't thread-safe: create and change windows only on the event dispatch thread,
 * which is why {@link netzerobelfast.App} calls {@link #show} through
 * {@code SwingUtilities.invokeLater}.
 */
public final class MainWindow {

    /** The smallest window the screens are designed for (Oscar, 18 November 2026). */
    public static final Dimension MINIMUM_SIZE = new Dimension(1280, 720);

    private static final String TITLE = "title";
    private static final String HOW_TO_PLAY = "howToPlay";
    private static final String SETUP = "setup";
    private static final String GAME = "game";
    private static final String GAME_OVER = "gameOver";

    private final GameConfig config;
    private final Random random;
    private final JFrame frame = new JFrame("Net Zero Belfast");
    private final CardLayout cards = new CardLayout();
    private final JPanel screens = new JPanel(cards);
    private final TitleScreen title;
    private final SetupScreen setup;
    private final HowToPlayScreen howToPlay;
    private GameScreen game;
    private GameOverScreen gameOver;

    private MainWindow(GameConfig config, Random random) {
        this.config = config;
        this.random = random;
        Rules rules = config.rules();
        String tagline = "Can Belfast reach net zero by " + rules.game().endYear() + "? A game about climate action for "
                + rules.game().minPlayers() + "-" + rules.game().maxPlayers() + " players.";
        title = new TitleScreen(tagline, this::showSetup, this::showHowToPlay, frame::dispose);
        howToPlay = new HowToPlayScreen(rules, this::showTitle);
        setup = new SetupScreen(rules, this::startGame, this::showTitle);
        screens.add(title, TITLE);
        screens.add(howToPlay, HOW_TO_PLAY);
        screens.add(setup, SETUP);

        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.setContentPane(screens);
        frame.setMinimumSize(MINIMUM_SIZE);
        frame.setSize(MINIMUM_SIZE);
        frame.setLocationRelativeTo(null);
    }

    /**
     * Opens the window on the title screen.
     *
     * @param random the game's one random source, made at start-up (seeded with {@code --seed} for testing)
     */
    public static void show(GameConfig config, Random random) {
        Ui.installDefaults();
        MainWindow window = new MainWindow(config, random);
        window.frame.setVisible(true);
        window.showTitle();
    }

    private void showTitle() {
        cards.show(screens, TITLE);
        title.focusStart();
    }

    private void showHowToPlay() {
        cards.show(screens, HOW_TO_PLAY);
        howToPlay.show(0);
    }

    private void showSetup() {
        cards.show(screens, SETUP);
        setup.reset();
    }

    /** Starts a game, or returns the model's reason for refusing these players (FR-01). */
    private String startGame(List<PlayerSetup> players) {
        Game started;
        try {
            started = Game.start(config, new Dice(random), players);
        } catch (IllegalArgumentException e) {
            return e.getMessage();
        }
        removeOldScreens();
        game = new GameScreen(config, started, this::showGameOver);
        screens.add(game, GAME);
        cards.show(screens, GAME);
        game.refresh();
        return null;
    }

    private void showGameOver(GameResult result) {
        gameOver = new GameOverScreen(game.text().finalScores(game.game(), result), this::showTitle, frame::dispose);
        screens.add(gameOver, GAME_OVER);
        cards.show(screens, GAME_OVER);
        // The finished game's screen goes, so its shortcut keys can't act on it.
        screens.remove(game);
        game = null;
        gameOver.focusPlayAgain();
    }

    private void removeOldScreens() {
        if (game != null) {
            screens.remove(game);
            game = null;
        }
        if (gameOver != null) {
            screens.remove(gameOver);
            gameOver = null;
        }
    }
}
