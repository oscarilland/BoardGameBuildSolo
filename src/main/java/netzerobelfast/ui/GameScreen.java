package netzerobelfast.ui;

import netzerobelfast.config.GameConfig;
import netzerobelfast.model.CardResult;
import netzerobelfast.model.ChoiceCard;
import netzerobelfast.model.Game;
import netzerobelfast.model.Game.Move;
import netzerobelfast.model.Game.ReturnAttempt;
import netzerobelfast.model.GameResult;
import netzerobelfast.model.Player;
import netzerobelfast.model.QuizCard;
import netzerobelfast.model.RoundReport;
import netzerobelfast.ui.ActionOptions.Button;
import netzerobelfast.ui.ActionOptions.Command;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.KeyEventDispatcher;
import java.awt.KeyboardFocusManager;
import java.awt.SecondaryLoop;
import java.awt.Toolkit;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * The main game screen (FR-15): the top bar with the round, year and Climate Meter; the board;
 * the player panel; and the action bar. It implements the controller's {@link GameController.View}:
 * it redraws from the model when told to, animates what changed (plan 008), and shows the pop-ups.
 *
 * <p>The model has already changed when an animation starts. Any key or click skips the
 * animation, and while a pop-up is waiting for an animation to end, keys and clicks only skip:
 * they never start a second action.
 */
final class GameScreen extends JPanel implements GameController.View {

    /** Shortcut keys the action bar can use; each is bound once, and finds its button when pressed. */
    private static final String KEYS = "RBCLVPGEO12";

    private static final int PANEL_WIDTH = 320;
    private static final long DICE_MILLIS = 450;
    private static final long STEP_MILLIS = 70;
    private static final long MAX_SLIDE_MILLIS = 1000;
    private static final long METER_MILLIS = 400;

    private final StatusText text;
    private final GameController controller;
    private final Consumer<GameResult> onGameOver;
    private final Animator animator;

    private final JLabel roundLabel = Ui.label("", Ui.BOLD, Palette.TEXT);
    private final JLabel meterTitle = Ui.label("", Ui.SMALL_BOLD, Palette.MUTED_TEXT);
    private final MeterBar meter;
    private final BoardPanel board;
    private final JPanel players = new JPanel();
    private final JLabel prompt = Ui.label("", Fonts.BOLD.deriveFont(16f), Palette.TEXT);
    private final JLabel where = Ui.label("", Ui.BODY, Palette.MUTED_TEXT);
    private final JPanel buttons = new JPanel(new GridLayout(0, 3, 10, 8));
    private List<Button> current = List.of();
    private final List<GameButton> currentButtons = new ArrayList<>();

    // What was last drawn, to see what changed and animate it.
    private final Map<Player, Integer> positionsSeen = new HashMap<>();
    private Move moveSeen;
    private int meterSeen;
    private boolean settling;
    private final KeyEventDispatcher skipOnKey = this::skipOnKey;

    GameScreen(GameConfig config, Game game, Consumer<GameResult> onGameOver) {
        this(config, game, onGameOver, Animator.swing());
    }

    GameScreen(GameConfig config, Game game, Consumer<GameResult> onGameOver, Animator animator) {
        super(new BorderLayout());
        this.text = new StatusText(config);
        this.onGameOver = onGameOver;
        this.animator = animator;
        this.meter = new MeterBar(config.rules().meter());
        this.board = new BoardPanel(config.board(), text);
        this.controller = new GameController(game, this);
        setBackground(Palette.BACKGROUND);

        add(topBar(), BorderLayout.NORTH);
        board.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        board.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                animator.skip();
            }
        });
        add(board, BorderLayout.CENTER);
        add(playerPanel(), BorderLayout.EAST);
        add(actionBar(), BorderLayout.SOUTH);

        for (char key : KEYS.toCharArray()) {
            Ui.bindKey(this, String.valueOf(key), "key" + key, () -> press(key));
        }
        for (Player player : game.players()) {
            positionsSeen.put(player, player.position());
        }
        meterSeen = game.meter().value();
        // Don't replay a roll that happened before this screen was built.
        moveSeen = game.lastMove().orElse(null);
        meter.marker(meterSeen);
        refresh();
    }

    @Override
    public void addNotify() {
        super.addNotify();
        KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher(skipOnKey);
    }

    @Override
    public void removeNotify() {
        KeyboardFocusManager.getCurrentKeyboardFocusManager().removeKeyEventDispatcher(skipOnKey);
        animator.skip();
        super.removeNotify();
    }

    /** Any key skips running animations. The key still does its normal job unless a pop-up is waiting. */
    private boolean skipOnKey(KeyEvent event) {
        if (event.getID() == KeyEvent.KEY_PRESSED && animator.busy()) {
            animator.skip();
            return settling;
        }
        return false;
    }

    private JComponent topBar() {
        JPanel bar = new JPanel(new BorderLayout(28, 0)) {
            @Override
            protected void paintComponent(Graphics graphics) {
                Graphics2D g = Ui.smooth(graphics);
                g.setPaint(new GradientPaint(0, 0, Palette.HEADER, getWidth(), 0, Palette.SURFACE));
                g.fillRect(0, 0, getWidth(), getHeight());
                g.setColor(Palette.alpha(Palette.ACCENT, 90));
                g.fillRect(0, getHeight() - 2, getWidth(), 2);
                g.dispose();
            }
        };
        bar.setBorder(BorderFactory.createEmptyBorder(10, 18, 10, 18));
        JPanel left = new JPanel();
        left.setOpaque(false);
        left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS));
        JLabel logo = new JLabel("NET ZERO BELFAST") {
            @Override
            protected void paintComponent(Graphics graphics) {
                Graphics2D g = Ui.smooth(graphics);
                Icons.draw(g, Icons.Icon.LEAF, 0, (getHeight() - 18) / 2.0, 18, Palette.ACCENT);
                g.dispose();
                super.paintComponent(graphics);
            }
        };
        logo.setFont(Fonts.BOLD.deriveFont(19f));
        logo.setForeground(Palette.TEXT);
        logo.setBorder(BorderFactory.createEmptyBorder(0, 26, 0, 10));
        left.add(logo);
        left.add(Box.createVerticalStrut(2));
        // Room on the right, because smoothed text can draw a little wider than Swing measured it.
        roundLabel.setBorder(BorderFactory.createEmptyBorder(0, 26, 0, 10));
        left.add(roundLabel);
        left.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 12));
        bar.add(left, BorderLayout.WEST);
        JPanel right = new JPanel(new BorderLayout(0, 2));
        right.setOpaque(false);
        right.add(meterTitle, BorderLayout.NORTH);
        right.add(meter, BorderLayout.CENTER);
        bar.add(right, BorderLayout.CENTER);
        return bar;
    }

    private JComponent playerPanel() {
        players.setLayout(new BoxLayout(players, BoxLayout.Y_AXIS));
        players.setBackground(Palette.BACKGROUND);
        players.setBorder(BorderFactory.createEmptyBorder(8, 0, 8, 10));
        JScrollPane scroll = new JScrollPane(players);
        scroll.setBorder(null);
        scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        scroll.setPreferredSize(new Dimension(PANEL_WIDTH, 100));
        scroll.getViewport().setBackground(Palette.BACKGROUND);
        return scroll;
    }

    private JComponent actionBar() {
        JPanel bar = new JPanel(new BorderLayout(0, 8)) {
            @Override
            protected void paintComponent(Graphics graphics) {
                Graphics2D g = Ui.smooth(graphics);
                g.setColor(Palette.SURFACE);
                g.fillRect(0, 0, getWidth(), getHeight());
                g.setColor(Palette.BORDER);
                g.fillRect(0, 0, getWidth(), 1);
                g.dispose();
            }
        };
        bar.setBorder(BorderFactory.createEmptyBorder(10, 16, 12, 16));
        JPanel words = new JPanel(new GridLayout(2, 1, 0, 2));
        words.setOpaque(false);
        words.add(prompt);
        words.add(where);
        bar.add(words, BorderLayout.NORTH);
        buttons.setOpaque(false);
        bar.add(buttons, BorderLayout.CENTER);
        return bar;
    }

    // ---- GameController.View -----------------------------------------------------

    @Override
    public void refresh() {
        Game game = controller.game();
        roundLabel.setText(text.roundLine(game));
        meterTitle.setText(text.meterTitle().toUpperCase(java.util.Locale.ROOT));
        meter.show(game.meter().value(), game.meter().dangerBand(), text.meterValue(game), text.nextTarget(game));
        board.dice(0, -1);
        board.show(game);
        refreshPlayers(game);
        prompt.setText(text.prompt(game));
        where.setText(game.phase() == Game.Phase.GAME_OVER ? "" : text.whereLine(game));
        refreshButtons(game);
        animateChanges(game);
    }

    /** Starts animations for what changed since the last redraw: the dice, counters and the meter. */
    private void animateChanges(Game game) {
        Move move = game.lastMove().orElse(null);
        boolean newMove = move != null && move != moveSeen;
        moveSeen = move;

        List<Runnable> slides = new ArrayList<>();
        for (Player player : game.players()) {
            int before = positionsSeen.getOrDefault(player, player.position());
            int now = player.position();
            positionsSeen.put(player, now);
            if (before == now) {
                continue;
            }
            List<Integer> path = slidePath(before, now, newMove ? move : null);
            long millis = slideMillis(path.size() - 1);
            slides.add(() -> animator.play(millis, t -> board.slide(player, path, t), board::endSlide));
        }

        if (newMove) {
            int total = move.diceTotal();
            animator.play(DICE_MILLIS, t -> board.dice(total, t), () -> slides.forEach(Runnable::run));
        } else {
            slides.forEach(Runnable::run);
        }

        int value = game.meter().value();
        if (value != meterSeen) {
            double from = meter.marker();
            meterSeen = value;
            animator.play(METER_MILLIS, t -> meter.marker(from + (value - from) * t), null);
        } else if (!animator.busy()) {
            meter.marker(value);
        }
    }

    /**
     * The squares a counter slides through, start first: the move's path if it ended here,
     * otherwise straight from where it was (a Glider ride).
     */
    static List<Integer> slidePath(int before, int now, Move move) {
        List<Integer> path = new ArrayList<>();
        path.add(before);
        if (move != null && !move.path().isEmpty() && move.path().get(move.path().size() - 1) == now) {
            path.addAll(move.path());
        } else {
            path.add(now);
        }
        return path;
    }

    /** How long a slide of {@code steps} squares takes: a steady pace, never more than a second (NFR-04). */
    static long slideMillis(int steps) {
        return Math.min(MAX_SLIDE_MILLIS, STEP_MILLIS * Math.max(4, steps));
    }

    private void refreshPlayers(Game game) {
        players.removeAll();
        for (Player player : game.players()) {
            PlayerCard card = new PlayerCard(text, game, player, PANEL_WIDTH - 24);
            card.setAlignmentX(LEFT_ALIGNMENT);
            players.add(card);
            players.add(Box.createVerticalStrut(4));
        }
        players.revalidate();
        players.repaint();
    }

    private void refreshButtons(Game game) {
        buttons.removeAll();
        currentButtons.clear();
        current = ActionOptions.forGame(game);
        GameButton focus = null;
        for (Button option : current) {
            boolean main = option.enabled() && (option.command() instanceof Command.Roll
                    || option.command() instanceof Command.RollToReturn || option.command() instanceof Command.EndTurn
                    || option.command() instanceof Command.OpenCard);
            GameButton button = main ? Ui.primary(option.text(), () -> act(option))
                    : Ui.button(option.text(), () -> act(option));
            button.setEnabled(option.enabled());
            buttons.add(button);
            currentButtons.add(button);
            if (focus == null && option.enabled()) {
                focus = button;
            }
        }
        buttons.revalidate();
        buttons.repaint();
        if (focus != null) {
            focus.requestFocusInWindow();
        }
    }

    /** A button press: finish any animation first, then act, unless a pop-up is waiting for one. */
    private void act(Button option) {
        animator.skip();
        if (settling) {
            return;
        }
        controller.perform(option.command());
    }

    /** A shortcut key: presses the first enabled button with that key, if there is one. */
    private void press(char key) {
        if (settling) {
            animator.skip();
            return;
        }
        for (int i = 0; i < current.size(); i++) {
            if (current.get(i).key() == key && current.get(i).enabled()) {
                currentButtons.get(i).doClick();
                return;
            }
        }
    }

    /**
     * Waits for animations to finish without freezing the window: a secondary event loop keeps
     * handling events, including the animation's frames, and returns when the animator is idle.
     */
    @Override
    public void settle() {
        if (!animator.busy()) {
            return;
        }
        SecondaryLoop loop = Toolkit.getDefaultToolkit().getSystemEventQueue().createSecondaryLoop();
        settling = true;
        animator.whenIdle(loop::exit);
        try {
            if (animator.busy()) {
                loop.enter();
            }
        } finally {
            settling = false;
        }
    }

    @Override
    public void showError(String message) {
        Dialogs.tell(board, Dialogs.Tone.INFO, "That can't be done now", List.of(message));
    }

    @Override
    public int askQuiz(QuizCard card) {
        List<String> options = new ArrayList<>();
        List<Boolean> enabled = new ArrayList<>();
        List<Character> keys = new ArrayList<>();
        for (int i = 0; i < card.answers().size(); i++) {
            options.add(card.answers().get(i));
            enabled.add(true);
            keys.add((char) ('1' + i));
        }
        return Dialogs.choose(board, Dialogs.Tone.QUIZ, "Quiz card",
                List.of(card.text(), "Answer correctly to earn " + card.reward() + " GP."), options, enabled, keys);
    }

    @Override
    public int askChoice(ChoiceCard card, List<Integer> affordable) {
        List<String> options = new ArrayList<>();
        List<Boolean> enabled = new ArrayList<>();
        List<Character> keys = new ArrayList<>();
        for (int i = 0; i < card.options().size(); i++) {
            boolean canPay = affordable.contains(i);
            options.add(StatusText.optionText(card, i) + (canPay ? "" : " (can't afford)"));
            enabled.add(canPay);
            keys.add((char) ('A' + i));
        }
        return Dialogs.choose(board, Dialogs.Tone.CHOICE, "Choice card", List.of(card.text()), options, enabled, keys);
    }

    @Override
    public void showCardResult(CardResult result) {
        Dialogs.Tone tone = switch (result.card()) {
            case QuizCard q -> Dialogs.Tone.QUIZ;
            case ChoiceCard c -> Dialogs.Tone.CHOICE;
            default -> Dialogs.Tone.EVENT;
        };
        String title = switch (tone) {
            case QUIZ -> "Quiz card";
            case CHOICE -> "Choice card";
            default -> "Event card";
        };
        List<String> body = new ArrayList<>();
        if (tone == Dialogs.Tone.EVENT) {
            body.add(result.card().text());
        }
        body.add(StatusText.cardOutcome(result));
        body.add("Fact: " + result.fact());
        body.add(StatusText.sourcesText(result.sources()));
        Dialogs.tell(board, tone, title, body);
    }

    @Override
    public void showRoundReport(RoundReport report) {
        List<String> lines = text.roundReport(controller.game(), report);
        Dialogs.tell(board, report.disaster() == null ? Dialogs.Tone.ROUND : Dialogs.Tone.DISASTER,
                lines.get(0), lines.subList(1, lines.size()));
    }

    @Override
    public void showReturn(Player player, ReturnAttempt attempt) {
        Dialogs.tell(board, attempt.returned() ? Dialogs.Tone.ROUND : Dialogs.Tone.DISASTER,
                attempt.returned() ? "Back in the game" : "Still bankrupt", List.of(text.returnText(player, attempt)));
    }

    @Override
    public void showGameOver(GameResult result) {
        onGameOver.accept(result);
    }

    StatusText text() {
        return text;
    }

    Game game() {
        return controller.game();
    }

    Animator animator() {
        return animator;
    }
}
