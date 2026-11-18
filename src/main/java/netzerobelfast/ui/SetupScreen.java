package netzerobelfast.ui;

import netzerobelfast.config.Rules;
import netzerobelfast.model.Character;
import netzerobelfast.model.Game.PlayerSetup;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.AbstractDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

/**
 * The setup screen (FR-01): how many players, then a name and a character for each. Start is
 * enabled only when the form is complete ({@link SetupRules}); {@code Game.start} makes the
 * final check, and its message is shown if it refuses.
 */
final class SetupScreen extends JPanel {

    private static final String CHOOSE = "Choose a character";

    private final Rules.Game rules;
    private final List<Character> characters;
    private final Function<List<PlayerSetup>, String> onStart;
    private final JComboBox<Integer> count = new JComboBox<>();
    private final JPanel rows = new JPanel(new GridBagLayout());
    private final List<JTextField> names = new ArrayList<>();
    private final List<JComboBox<String>> choices = new ArrayList<>();
    private final List<JLabel> abilities = new ArrayList<>();
    private final JLabel status = Ui.label(" ", Ui.BOLD, Palette.MUTED_TEXT);
    private final JButton start;

    /**
     * @param onStart starts the game with these players, and returns null, or the model's reason for refusing
     */
    SetupScreen(Rules rules, Function<List<PlayerSetup>, String> onStart, Runnable onBack) {
        super(new BorderLayout());
        this.rules = rules.game();
        this.characters = Character.fromAll(rules);
        this.onStart = onStart;
        setBackground(Palette.BACKGROUND);
        setBorder(BorderFactory.createEmptyBorder(24, 48, 24, 48));

        JPanel top = new JPanel();
        top.setOpaque(false);
        top.setLayout(new BoxLayout(top, BoxLayout.Y_AXIS));
        JLabel heading = Ui.label("Who's playing?", Ui.TITLE.deriveFont(32f), Palette.TEXT);
        top.add(heading);
        top.add(Box.createVerticalStrut(12));
        JPanel countRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        countRow.setOpaque(false);
        JLabel countLabel = Ui.label("Number of players:  ", Ui.BOLD, Palette.TEXT);
        countLabel.setLabelFor(count);
        countRow.add(countLabel);
        for (int n = this.rules.minPlayers(); n <= this.rules.maxPlayers(); n++) {
            count.addItem(n);
        }
        count.setFont(Ui.BODY);
        count.addActionListener(e -> buildRows());
        countRow.add(count);
        countRow.setAlignmentX(LEFT_ALIGNMENT);
        heading.setAlignmentX(LEFT_ALIGNMENT);
        top.add(countRow);
        top.add(Box.createVerticalStrut(12));
        add(top, BorderLayout.NORTH);

        rows.setOpaque(false);
        JPanel rowsHolder = new JPanel(new BorderLayout());
        rowsHolder.setOpaque(false);
        rowsHolder.add(rows, BorderLayout.NORTH);
        add(rowsHolder, BorderLayout.CENTER);

        start = Ui.button("Start the game", this::start);
        JButton back = Ui.button("Back", onBack);
        JPanel bottom = new JPanel(new BorderLayout());
        bottom.setOpaque(false);
        bottom.add(status, BorderLayout.CENTER);
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        buttons.setOpaque(false);
        buttons.add(back);
        buttons.add(start);
        bottom.add(buttons, BorderLayout.EAST);
        add(bottom, BorderLayout.SOUTH);
        Ui.bindKey(this, "ESCAPE", "back", onBack);

        buildRows();
    }

    /** Clears the form, ready for a new game. */
    void reset() {
        count.setSelectedIndex(0);
        buildRows();
        for (JTextField name : names) {
            name.setText("");
        }
        names.get(0).requestFocusInWindow();
    }

    private void buildRows() {
        int wanted = (Integer) count.getSelectedItem();
        // Keep what's already typed when the count changes.
        List<String> oldNames = names.stream().map(JTextField::getText).toList();
        List<Integer> oldChoices = choices.stream().map(JComboBox::getSelectedIndex).toList();
        rows.removeAll();
        names.clear();
        choices.clear();
        abilities.clear();
        for (int i = 0; i < wanted; i++) {
            GridBagConstraints c = new GridBagConstraints();
            c.gridy = i * 2;
            c.insets = new Insets(8, 0, 0, 12);
            c.anchor = GridBagConstraints.WEST;
            JLabel label = Ui.label("Player " + (i + 1), Ui.BOLD, Palette.TEXT);
            rows.add(label, c);

            JTextField name = new JTextField(16);
            name.setFont(Ui.BODY);
            ((AbstractDocument) name.getDocument()).setDocumentFilter(new MaxLength(rules.maxNameLength()));
            name.getAccessibleContext().setAccessibleName("Player " + (i + 1) + " name");
            label.setLabelFor(name);
            if (i < oldNames.size()) {
                name.setText(oldNames.get(i));
            }
            name.getDocument().addDocumentListener(new Changed());
            c.gridx = 1;
            rows.add(name, c);

            JComboBox<String> choice = new JComboBox<>();
            choice.addItem(CHOOSE);
            for (Character character : characters) {
                choice.addItem(character.name());
            }
            choice.setFont(Ui.BODY);
            choice.getAccessibleContext().setAccessibleName("Player " + (i + 1) + " character");
            if (i < oldChoices.size()) {
                choice.setSelectedIndex(oldChoices.get(i));
            }
            c.gridx = 2;
            rows.add(choice, c);

            JLabel ability = Ui.label(" ", Ui.SMALL, Palette.MUTED_TEXT);
            GridBagConstraints a = new GridBagConstraints();
            a.gridx = 1;
            a.gridy = i * 2 + 1;
            a.gridwidth = 3;
            a.anchor = GridBagConstraints.WEST;
            rows.add(ability, a);

            // An empty last column takes the spare width, so the rows sit on the left.
            GridBagConstraints filler = new GridBagConstraints();
            filler.gridx = 3;
            filler.gridy = i * 2;
            filler.weightx = 1;
            rows.add(Box.createHorizontalGlue(), filler);

            names.add(name);
            choices.add(choice);
            abilities.add(ability);
            choice.addActionListener(e -> update());
        }
        rows.revalidate();
        rows.repaint();
        update();
    }

    private List<PlayerSetup> entered() {
        List<PlayerSetup> entered = new ArrayList<>();
        for (int i = 0; i < names.size(); i++) {
            int index = choices.get(i).getSelectedIndex();
            String id = index <= 0 ? null : characters.get(index - 1).id();
            entered.add(new PlayerSetup(names.get(i).getText(), id));
        }
        return entered;
    }

    /** Shows each chosen character's start and ability, and enables Start only when the form is complete. */
    private void update() {
        for (int i = 0; i < choices.size(); i++) {
            int index = choices.get(i).getSelectedIndex();
            if (index <= 0) {
                abilities.get(i).setText(" ");
            } else {
                Character character = characters.get(index - 1);
                abilities.get(i).setText("Starts with £" + character.startMoney() + "M and " + character.startGP()
                        + " GP. " + character.ability());
            }
        }
        var missing = SetupRules.missing(entered(), rules);
        start.setEnabled(missing.isEmpty());
        status.setForeground(Palette.MUTED_TEXT);
        status.setText(missing.orElse("Ready to start."));
    }

    private void start() {
        String refused = onStart.apply(entered());
        if (refused != null) {
            status.setForeground(Palette.WARNING_TEXT);
            status.setText(refused);
        }
    }

    private final class Changed implements DocumentListener {
        @Override
        public void insertUpdate(DocumentEvent e) {
            update();
        }

        @Override
        public void removeUpdate(DocumentEvent e) {
            update();
        }

        @Override
        public void changedUpdate(DocumentEvent e) {
            update();
        }
    }

    /** Stops a name field growing past the longest name the data allows (FR-01 AC-3). */
    private static final class MaxLength extends DocumentFilter {
        private final int max;

        MaxLength(int max) {
            this.max = max;
        }

        @Override
        public void insertString(FilterBypass fb, int offset, String text, AttributeSet attrs) throws BadLocationException {
            replace(fb, offset, 0, text, attrs);
        }

        @Override
        public void replace(FilterBypass fb, int offset, int length, String text, AttributeSet attrs) throws BadLocationException {
            int room = max - (fb.getDocument().getLength() - length);
            String allowed = text == null ? "" : text.substring(0, Math.max(0, Math.min(room, text.length())));
            super.replace(fb, offset, length, allowed, attrs);
        }
    }
}
