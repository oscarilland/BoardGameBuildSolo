package netzerobelfast.ui;

import netzerobelfast.config.BoardDef.SquareType;

import java.awt.Color;
import java.util.List;
import java.util.Map;

/**
 * Every colour the screens use, as named tokens, in one place (NFR-05 AC-1). Nothing in
 * {@code ui} writes a colour literal anywhere else.
 *
 * <p>{@link #TEXT_PAIRS} lists every text colour drawn on every background, so
 * {@code PaletteTest} can check each pair's contrast. Add a new pair here whenever a screen
 * draws text in a new combination.
 *
 * <p>Colour is never the only signal (NFR-05 AC-2, AC-3): squares also carry a type label, the
 * meter band is written as a number and a word, and the current player has a border and "(turn)".
 */
public final class Palette {

    private Palette() {}

    // Page and text.
    public static final Color BACKGROUND = new Color(0xFAF9F6);
    public static final Color SURFACE = new Color(0xFFFFFF);
    public static final Color TEXT = new Color(0x1F2933);
    public static final Color MUTED_TEXT = new Color(0x52606D);
    public static final Color BORDER = new Color(0x9AA5B1);

    // The top bar.
    public static final Color HEADER = new Color(0x1F3A5F);
    public static final Color HEADER_TEXT = new Color(0xFFFFFF);

    // Buttons.
    public static final Color BUTTON = new Color(0x2563A8);
    public static final Color BUTTON_TEXT = new Color(0xFFFFFF);
    public static final Color BUTTON_DISABLED = new Color(0xE4E7EB);
    public static final Color BUTTON_DISABLED_TEXT = new Color(0x52606D);
    public static final Color FOCUS = new Color(0xB45309);

    // Players: the current player's card and a bankrupt player's card.
    public static final Color CURRENT_PLAYER = new Color(0xE3EEFB);
    public static final Color CURRENT_BORDER = new Color(0x2563A8);
    public static final Color BANKRUPT = new Color(0xFDECEA);
    public static final Color WARNING_TEXT = new Color(0x9B1C1C);

    // Pop-ups.
    public static final Color CARD = new Color(0xFFFBEB);

    // Square fills by type, light enough for dark text.
    public static final Color POLLUTING = new Color(0xDDD6D1);
    public static final Color GREEN = new Color(0xD3EBCB);
    public static final Color NATURE = new Color(0xA8D69A);
    public static final Color RESILIENCE = new Color(0xCFE0F2);
    public static final Color GLIDER = new Color(0xE6D9F2);
    public static final Color CLIMATE_CARD = new Color(0xF8E7A8);

    // Danger bands on the meter, low to severe. Drawn without text on them; the band is written beside the bar.
    public static final List<Color> BANDS = List.of(
            new Color(0x6BBF73), new Color(0xF2D05C), new Color(0xF0A04B), new Color(0xD9534F));

    // Player counters, by seat. Each counter also shows its seat number, so colour isn't the only signal.
    public static final List<Color> COUNTERS = List.of(
            new Color(0x1D4ED8), new Color(0xB91C1C), new Color(0x047857),
            new Color(0x7C3AED), new Color(0x9A3412), new Color(0x0F766E));
    public static final Color COUNTER_TEXT = new Color(0xFFFFFF);

    /** The fill for a square of this type; nature projects have their own darker green. */
    public static Color squareFill(SquareType type, boolean nature) {
        return switch (type) {
            case POLLUTING -> POLLUTING;
            case GREEN_PROJECT -> nature ? NATURE : GREEN;
            case RESILIENCE -> RESILIENCE;
            case GLIDER_STOP -> GLIDER;
            case CLIMATE_CARD -> CLIMATE_CARD;
        };
    }

    /** The colour of danger band {@code band} (1 is the lowest); bands beyond the list use the last colour. */
    public static Color band(int band) {
        return BANDS.get(Math.max(0, Math.min(band, BANDS.size()) - 1));
    }

    /** A text colour on a background, and whether the text is large (which needs only 3:1). */
    public record Pair(String name, Color text, Color background, boolean large) {}

    /** Every text colour drawn on every background in the UI. */
    public static final List<Pair> TEXT_PAIRS = textPairs();

    private static List<Pair> textPairs() {
        List<Pair> pairs = new java.util.ArrayList<>(List.of(
                new Pair("text on background", TEXT, BACKGROUND, false),
                new Pair("muted text on background", MUTED_TEXT, BACKGROUND, false),
                new Pair("text on surface", TEXT, SURFACE, false),
                new Pair("muted text on surface", MUTED_TEXT, SURFACE, false),
                new Pair("header text", HEADER_TEXT, HEADER, false),
                new Pair("button text", BUTTON_TEXT, BUTTON, false),
                new Pair("disabled button text", BUTTON_DISABLED_TEXT, BUTTON_DISABLED, false),
                new Pair("text on current player", TEXT, CURRENT_PLAYER, false),
                new Pair("muted text on current player", MUTED_TEXT, CURRENT_PLAYER, false),
                new Pair("text on bankrupt player", TEXT, BANKRUPT, false),
                new Pair("warning on bankrupt player", WARNING_TEXT, BANKRUPT, false),
                new Pair("warning on surface", WARNING_TEXT, SURFACE, false),
                new Pair("text on card", TEXT, CARD, false),
                new Pair("muted text on card", MUTED_TEXT, CARD, false),
                new Pair("focus ring on surface", FOCUS, SURFACE, true)));
        Map<String, Color> squares = Map.of(
                "polluting", POLLUTING, "green", GREEN, "nature", NATURE,
                "resilience", RESILIENCE, "glider", GLIDER, "climate card", CLIMATE_CARD);
        squares.forEach((name, fill) -> {
            pairs.add(new Pair("text on " + name + " square", TEXT, fill, false));
            pairs.add(new Pair("warning on " + name + " square", WARNING_TEXT, fill, false));
        });
        for (int i = 0; i < COUNTERS.size(); i++) {
            pairs.add(new Pair("counter " + (i + 1) + " number", COUNTER_TEXT, COUNTERS.get(i), false));
        }
        return List.copyOf(pairs);
    }
}
