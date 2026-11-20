package netzerobelfast.ui;

import netzerobelfast.model.Game;
import netzerobelfast.model.Player;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.util.Optional;

/**
 * One player's card in the player panel (FR-15 AC-3 to AC-6), painted: an avatar in their
 * seat colour with their seat number, their name and character, chips for money, GP and
 * squares, their Resilience and co-funding deal, and a "YOUR TURN" or "BANKRUPT" chip. The
 * current player's card glows, and the chip says it in words (FR-15 AC-4).
 */
final class PlayerCard extends CardPanel {

    static final int HEIGHT = 74;

    private final int seat;
    private final String name;
    private final String character;
    private final String[] stats;
    private final String extras;
    private final boolean turn;
    private final Optional<String> warning;

    PlayerCard(StatusText text, Game game, Player player, int width) {
        super(new BorderLayout(), Palette.SURFACE, Palette.BORDER, 1);
        this.seat = StatusText.seat(game, player);
        this.name = player.name();
        this.character = player.character().name();
        // "£21M · 24 GP · 2 green, 1 polluting" as three chips.
        this.stats = text.playerMoney(game, player).split(" · ");
        this.extras = text.playerExtras(game, player);
        this.turn = player == game.currentPlayer() && game.phase() != Game.Phase.GAME_OVER;
        this.warning = text.playerWarning(player);
        if (warning.isPresent()) {
            colours(Palette.BANKRUPT, Palette.WARNING_TEXT, 1);
        } else if (turn) {
            colours(Palette.CURRENT_PLAYER, Palette.CURRENT_BORDER, 2);
            glow(Palette.ACCENT);
        }
        Dimension size = new Dimension(width, HEIGHT + (warning.isPresent() ? 15 : 0));
        setPreferredSize(size);
        setMaximumSize(size);
        setMinimumSize(size);
        getAccessibleContext().setAccessibleName(text.playerTitle(game, player) + ". " + text.playerMoney(game, player)
                + ". " + extras + warning.map(w -> ". " + w).orElse(""));
    }

    @Override
    protected void paintComponent(Graphics graphics) {
        super.paintComponent(graphics);
        Graphics2D g = Ui.smooth(graphics);
        int pad = 9;
        int avatar = 30;
        int right = getWidth() - CardPanel.SHADOW / 2 - pad;

        // Avatar: seat colour and seat number (the same number as the counter on the board).
        g.setPaint(new GradientPaint(pad, pad, Palette.counter(seat).brighter(), pad, pad + avatar, Palette.counter(seat).darker()));
        g.fillOval(pad, pad, avatar, avatar);
        g.setColor(Palette.alpha(Palette.TEXT, 200));
        g.drawOval(pad, pad, avatar, avatar);
        g.setColor(Palette.COUNTER_TEXT);
        g.setFont(Fonts.BOLD.deriveFont(15f));
        FontMetrics big = g.getFontMetrics();
        String number = String.valueOf(seat);
        g.drawString(number, pad + (avatar - big.stringWidth(number)) / 2, pad + (avatar + big.getAscent() - big.getDescent()) / 2);

        // Line 1: name and character, and a chip for whose turn it is or bankruptcy (in words, not just colour).
        int left = pad + avatar + 10;
        int chip = 0;
        if (warning.isPresent()) {
            chip = chipRight(g, "BANKRUPT", right, pad - 2, Palette.WARNING_TEXT, Palette.BACKGROUND);
        } else if (turn) {
            chip = chipRight(g, "YOUR TURN", right, pad - 2, Palette.ACCENT, Palette.ACCENT_TEXT);
        }
        g.setFont(Fonts.BOLD.deriveFont(14f));
        FontMetrics nameMetrics = g.getFontMetrics();
        int line1 = pad + nameMetrics.getAscent();
        g.setColor(Palette.TEXT);
        String shownName = clip(name, nameMetrics, right - chip - left - 8);
        g.drawString(shownName, left, line1);
        g.setFont(Ui.SMALL);
        FontMetrics small = g.getFontMetrics();
        int after = left + nameMetrics.stringWidth(shownName) + 8;
        g.setColor(Palette.MUTED_TEXT);
        g.drawString(clip(character, small, Math.max(0, right - chip - after - 6)), after, line1);

        // Line 2: money and GP, then green and polluting squares with their board icons.
        int line2 = pad + 22;
        int x = left;
        g.setFont(Ui.SMALL_BOLD);
        FontMetrics bold = g.getFontMetrics();
        String[] squares = stats.length > 2 ? stats[2].split(", ") : new String[0];
        String[] parts = {stats[0], stats.length > 1 ? stats[1] : "",
                squares.length > 0 ? squares[0] : "", squares.length > 1 ? squares[1] : ""};
        Icons.Icon[] icons = {null, null, Icons.Icon.LEAF, Icons.Icon.FACTORY};
        java.awt.Color[] tints = {null, null, Palette.GREEN_BAND, Palette.POLLUTING_BAND};
        for (int i = 0; i < parts.length; i++) {
            if (parts[i].isEmpty()) {
                continue;
            }
            if (icons[i] != null) {
                Icons.draw(g, icons[i], x, line2, 12, tints[i]);
                x += 15;
            }
            g.setColor(Palette.TEXT);
            String stat = clip(parts[i], bold, Math.max(0, right - x));
            g.drawString(stat, x, line2 + bold.getAscent() - 1);
            x += bold.stringWidth(stat) + 9;
        }

        // Line 3: Resilience and co-funding deal (FR-15 AC-6); line 4: the bankruptcy warning.
        g.setFont(Ui.SMALL);
        int line3 = line2 + bold.getHeight() + 2 + small.getAscent();
        g.setColor(Palette.MUTED_TEXT);
        g.drawString(clip(extras, small, right - left), left, line3);
        if (warning.isPresent()) {
            g.setColor(Palette.WARNING_TEXT);
            g.setFont(Ui.SMALL_BOLD);
            g.drawString(clip(warning.get(), bold, right - left), left, line3 + small.getHeight() + 1);
        }
        g.dispose();
    }

    /** Draws a chip ending at {@code right}, and returns its width. */
    private static int chipRight(Graphics2D g, String text, int right, int y, java.awt.Color fill, java.awt.Color colour) {
        g.setFont(Ui.SMALL_BOLD);
        int w = g.getFontMetrics().stringWidth(text) + 16;
        Ui.chip(g, text, right - w, y, fill, colour, Ui.SMALL_BOLD);
        return w;
    }

    private static String clip(String text, FontMetrics metrics, int width) {
        if (metrics.stringWidth(text) <= width) {
            return text;
        }
        String shorter = text;
        while (shorter.length() > 1 && metrics.stringWidth(shorter + "…") > width) {
            shorter = shorter.substring(0, shorter.length() - 1);
        }
        return shorter + "…";
    }
}
