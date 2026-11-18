package netzerobelfast.ui;

import netzerobelfast.config.GameConfig;
import netzerobelfast.model.CardResult;
import netzerobelfast.model.ChoiceCard;
import netzerobelfast.model.Game;
import netzerobelfast.model.Game.ReturnAttempt;
import netzerobelfast.model.GameResult;
import netzerobelfast.model.Player;
import netzerobelfast.model.QuizCard;
import netzerobelfast.model.RoundReport;
import netzerobelfast.ui.ActionOptions.Button;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * The main game screen (FR-15): the top bar with the round, year and Climate Meter; the board;
 * the player panel; and the action bar. It implements the controller's {@link GameController.View}:
 * it redraws from the model when told to, and shows the pop-ups.
 */
final class GameScreen extends JPanel implements GameController.View {

    /** Shortcut keys the action bar can use; each is bound once, and finds its button when pressed. */
    private static final String KEYS = "RBCLVPGEO12";

    private static final int PANEL_WIDTH = 300;

    private final StatusText text;
    private final GameController controller;
    private final Consumer<GameResult> onGameOver;

    private final JLabel roundLabel = Ui.label("", Ui.HEADING, Palette.HEADER_TEXT);
    private final JLabel meterTitle = Ui.label("", Ui.BOLD, Palette.HEADER_TEXT);
    private final MeterBar meter;
    private final BoardPanel board;
    private final JPanel players = new JPanel();
    private final JLabel prompt = Ui.label("", Ui.BOLD, Palette.TEXT);
    private final JLabel where = Ui.label("", Ui.BODY, Palette.MUTED_TEXT);
    private final JPanel buttons = new JPanel(new GridLayout(0, 3, 8, 6));
    private List<Button> current = List.of();
    private final List<JButton> currentButtons = new ArrayList<>();

    GameScreen(GameConfig config, Game game, Consumer<GameResult> onGameOver) {
        super(new BorderLayout());
        this.text = new StatusText(config);
        this.onGameOver = onGameOver;
        this.meter = new MeterBar(config.rules().meter());
        this.board = new BoardPanel(config.board(), text);
        this.controller = new GameController(game, this);
        setBackground(Palette.BACKGROUND);

        add(topBar(), BorderLayout.NORTH);
        board.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        add(board, BorderLayout.CENTER);
        add(playerPanel(), BorderLayout.EAST);
        add(actionBar(), BorderLayout.SOUTH);

        for (char key : KEYS.toCharArray()) {
            Ui.bindKey(this, String.valueOf(key), "key" + key, () -> press(key));
        }
        refresh();
    }

    private JComponent topBar() {
        JPanel bar = new JPanel(new BorderLayout(24, 0));
        bar.setBackground(Palette.HEADER);
        bar.setBorder(BorderFactory.createEmptyBorder(10, 18, 8, 18));
        JPanel left = new JPanel();
        left.setOpaque(false);
        left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS));
        left.add(Ui.label("Net Zero Belfast", Ui.HEADING, Palette.HEADER_TEXT));
        left.add(roundLabel);
        roundLabel.setFont(Ui.BOLD);
        bar.add(left, BorderLayout.WEST);
        JPanel right = new JPanel(new BorderLayout(12, 0));
        right.setOpaque(false);
        right.add(meterTitle, BorderLayout.WEST);
        right.add(meter, BorderLayout.CENTER);
        bar.add(right, BorderLayout.CENTER);
        return bar;
    }

    private JComponent playerPanel() {
        players.setLayout(new BoxLayout(players, BoxLayout.Y_AXIS));
        players.setBackground(Palette.BACKGROUND);
        players.setBorder(BorderFactory.createEmptyBorder(8, 0, 8, 8));
        JScrollPane scroll = new JScrollPane(players);
        scroll.setBorder(null);
        scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        scroll.setPreferredSize(new Dimension(PANEL_WIDTH, 100));
        scroll.getViewport().setBackground(Palette.BACKGROUND);
        return scroll;
    }

    private JComponent actionBar() {
        JPanel bar = new JPanel(new BorderLayout(0, 6));
        bar.setBackground(Palette.SURFACE);
        bar.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, Palette.BORDER), BorderFactory.createEmptyBorder(8, 12, 10, 12)));
        JPanel words = new JPanel(new GridLayout(2, 1));
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
        meterTitle.setText(text.meterTitle());
        meter.show(game.meter().value(), text.meterValue(game), text.nextTarget(game));
        board.show(game);
        refreshPlayers(game);
        prompt.setText(text.prompt(game));
        where.setText(game.phase() == Game.Phase.GAME_OVER ? "" : text.whereLine(game));
        refreshButtons(game);
    }

    private void refreshPlayers(Game game) {
        players.removeAll();
        for (Player player : game.players()) {
            players.add(playerCard(game, player));
            players.add(Box.createVerticalStrut(6));
        }
        players.revalidate();
        players.repaint();
    }

    /** One player's card: highlighted with a thick border and "(turn)" for the current player (FR-15 AC-4). */
    private JComponent playerCard(Game game, Player player) {
        boolean turn = player == game.currentPlayer() && game.phase() != Game.Phase.GAME_OVER;
        Color background = player.bankrupt() ? Palette.BANKRUPT : turn ? Palette.CURRENT_PLAYER : Palette.SURFACE;
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(background);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(turn ? Palette.CURRENT_BORDER : Palette.BORDER, turn ? 3 : 1),
                BorderFactory.createEmptyBorder(turn ? 3 : 5, turn ? 7 : 9, turn ? 3 : 5, turn ? 7 : 9)));
        int width = PANEL_WIDTH - 60;
        card.add(Ui.wrapped(text.playerTitle(game, player), Ui.BOLD, Palette.TEXT, width));
        card.add(Ui.wrapped(text.playerMoney(game, player), Ui.BODY, Palette.TEXT, width));
        card.add(Ui.wrapped(text.playerExtras(game, player), Ui.SMALL, Palette.MUTED_TEXT, width));
        text.playerWarning(player).ifPresent(warning ->
                card.add(Ui.wrapped(warning, Ui.BOLD, Palette.WARNING_TEXT, width)));
        card.setAlignmentX(LEFT_ALIGNMENT);
        card.setMaximumSize(new Dimension(PANEL_WIDTH, card.getPreferredSize().height));
        return card;
    }

    private void refreshButtons(Game game) {
        buttons.removeAll();
        currentButtons.clear();
        current = ActionOptions.forGame(game);
        JButton focus = null;
        for (Button option : current) {
            JButton button = Ui.button(option.text(), () -> controller.perform(option.command()));
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

    /** A shortcut key: presses the first enabled button with that key, if there is one. */
    private void press(char key) {
        for (int i = 0; i < current.size(); i++) {
            if (current.get(i).key() == key && current.get(i).enabled()) {
                currentButtons.get(i).doClick();
                return;
            }
        }
    }

    @Override
    public void showError(String message) {
        Dialogs.tell(board, "That can't be done now", List.of(message));
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
        return Dialogs.choose(board, "Quiz card", List.of(card.text(), "Answer correctly to earn " + card.reward() + " GP."),
                options, enabled, keys);
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
        return Dialogs.choose(board, "Choice card", List.of(card.text()), options, enabled, keys);
    }

    @Override
    public void showCardResult(CardResult result) {
        String title = switch (result.card()) {
            case QuizCard q -> "Quiz card";
            case ChoiceCard c -> "Choice card";
            default -> "Event card";
        };
        List<String> body = new ArrayList<>();
        if (!(result.card() instanceof QuizCard) && !(result.card() instanceof ChoiceCard)) {
            body.add(result.card().text());
        }
        body.add(StatusText.cardOutcome(result));
        body.add("Fact: " + result.fact());
        body.add(StatusText.sourcesText(result.sources()));
        Dialogs.tell(board, title, body);
    }

    @Override
    public void showRoundReport(RoundReport report) {
        List<String> lines = text.roundReport(controller.game(), report);
        Dialogs.tell(board, lines.get(0), lines.subList(1, lines.size()));
    }

    @Override
    public void showReturn(Player player, ReturnAttempt attempt) {
        Dialogs.tell(board, attempt.returned() ? "Back in the game" : "Still bankrupt", List.of(text.returnText(player, attempt)));
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
}
