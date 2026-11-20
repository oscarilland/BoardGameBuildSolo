package netzerobelfast.ui;

import netzerobelfast.config.BoardDef.SquareType;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Every colour the screens use, as named tokens, in one place (NFR-05 AC-1). Nothing in
 * {@code ui} writes a colour literal anywhere else. The theme is dark, like an online board
 * game (plan 008).
 *
 * <p>{@link #TEXT_PAIRS} lists every text colour drawn on every background, so
 * {@code PaletteTest} can check each pair's contrast. Add a new pair here whenever a screen
 * draws text in a new combination.
 *
 * <p>Colour is never the only signal (NFR-05 AC-2, AC-3): squares also carry a type label and
 * icon, the meter band is written as a number and a word, and the current player has a
 * "YOUR TURN" chip.
 */
public final class Palette {

    private Palette() {}

    // Page and text.
    public static final Color BACKGROUND = new Color(0x0B1220);
    public static final Color SURFACE = new Color(0x151F32);
    public static final Color RAISED = new Color(0x1E2A42);
    public static final Color TEXT = new Color(0xF1F5F9);
    public static final Color MUTED_TEXT = new Color(0xA3B1C6);
    public static final Color BORDER = new Color(0x2E3D5C);
    public static final Color SHADOW = new Color(0, 0, 0, 110);

    // The top bar.
    public static final Color HEADER = new Color(0x0F1A2E);
    public static final Color HEADER_TEXT = TEXT;

    // Buttons: the accent for the main action, a quieter one for the rest.
    public static final Color ACCENT = new Color(0x2DD4A3);
    public static final Color ACCENT_HOVER = new Color(0x5EE6BE);
    public static final Color ACCENT_TEXT = new Color(0x062019);
    public static final Color BUTTON = new Color(0x2A3A5C);
    public static final Color BUTTON_HOVER = new Color(0x354A73);
    public static final Color BUTTON_TEXT = TEXT;
    public static final Color BUTTON_DISABLED = new Color(0x1A2438);
    public static final Color BUTTON_DISABLED_TEXT = new Color(0x8592A8);
    public static final Color FOCUS = new Color(0xFBBF24);

    // Players: the current player's card and a bankrupt player's card.
    public static final Color CURRENT_PLAYER = new Color(0x16304A);
    public static final Color CURRENT_BORDER = ACCENT;
    public static final Color BANKRUPT = new Color(0x3A1A22);
    public static final Color WARNING_TEXT = new Color(0xFF8A8A);

    // Pop-ups: the card body, and a header colour for each kind.
    public static final Color CARD = RAISED;
    public static final Color HEADER_QUIZ = new Color(0x1D4ED8);
    public static final Color HEADER_CHOICE = new Color(0xB45309);
    public static final Color HEADER_EVENT = new Color(0x6D28D9);
    public static final Color HEADER_ROUND = new Color(0x0F766E);
    public static final Color HEADER_DISASTER = new Color(0xB91C1C);
    public static final Color HEADER_LABEL = new Color(0xFFFFFF);
    public static final Color DIM = new Color(5, 8, 15, 150);

    // Square tiles: a dark tint for the body (text sits on it) and a bright band and icon colour.
    public static final Color POLLUTING = new Color(0x2E2723);
    public static final Color GREEN = new Color(0x16301F);
    public static final Color NATURE = new Color(0x0F2E1C);
    public static final Color RESILIENCE = new Color(0x132A45);
    public static final Color GLIDER = new Color(0x261E45);
    public static final Color CLIMATE_CARD = new Color(0x33280F);
    public static final Color POLLUTING_BAND = new Color(0xC08A65);
    public static final Color GREEN_BAND = new Color(0x4ADE80);
    public static final Color NATURE_BAND = new Color(0x16A34A);
    public static final Color RESILIENCE_BAND = new Color(0x60A5FA);
    public static final Color GLIDER_BAND = new Color(0xA78BFA);
    public static final Color CLIMATE_CARD_BAND = new Color(0xFBBF24);

    // Danger bands on the meter, low to severe. The band chip writes the band in BACKGROUND text on these.
    public static final List<Color> BANDS = List.of(
            new Color(0x4ADE80), new Color(0xFACC15), new Color(0xFB923C), new Color(0xF87171));

    // Player counters, by seat. Each counter also shows its seat number, so colour isn't the only signal.
    public static final List<Color> COUNTERS = List.of(
            new Color(0x1D4ED8), new Color(0xB91C1C), new Color(0x047857),
            new Color(0x7C3AED), new Color(0x9A3412), new Color(0x0F766E));
    public static final Color COUNTER_TEXT = new Color(0xFFFFFF);

    /** The tile body for a square of this type; nature projects have their own darker green. */
    public static Color squareFill(SquareType type, boolean nature) {
        return switch (type) {
            case POLLUTING -> POLLUTING;
            case GREEN_PROJECT -> nature ? NATURE : GREEN;
            case RESILIENCE -> RESILIENCE;
            case GLIDER_STOP -> GLIDER;
            case CLIMATE_CARD -> CLIMATE_CARD;
        };
    }

    /** The bright band and icon colour for a square of this type. */
    public static Color squareBand(SquareType type, boolean nature) {
        return switch (type) {
            case POLLUTING -> POLLUTING_BAND;
            case GREEN_PROJECT -> nature ? NATURE_BAND : GREEN_BAND;
            case RESILIENCE -> RESILIENCE_BAND;
            case GLIDER_STOP -> GLIDER_BAND;
            case CLIMATE_CARD -> CLIMATE_CARD_BAND;
        };
    }

    /** The colour of danger band {@code band} (1 is the lowest); bands beyond the list use the last colour. */
    public static Color band(int band) {
        return BANDS.get(Math.max(0, Math.min(band, BANDS.size()) - 1));
    }

    /** The counter colour for a seat (1 to 6). */
    public static Color counter(int seat) {
        return COUNTERS.get((seat - 1) % COUNTERS.size());
    }

    /** A colour with a new alpha, for glows and shadows. */
    public static Color alpha(Color color, int alpha) {
        return new Color(color.getRed(), color.getGreen(), color.getBlue(), alpha);
    }

    /** A text colour on a background, and whether the text is large (which needs only 3:1). */
    public record Pair(String name, Color text, Color background, boolean large) {}

    /** Every text colour drawn on every background in the UI. */
    public static final List<Pair> TEXT_PAIRS = textPairs();

    private static List<Pair> textPairs() {
        List<Pair> pairs = new ArrayList<>();
        Map<String, Color> surfaces = Map.of(
                "background", BACKGROUND, "surface", SURFACE, "raised surface", RAISED, "header", HEADER,
                "current player", CURRENT_PLAYER, "bankrupt player", BANKRUPT);
        surfaces.forEach((name, background) -> {
            pairs.add(new Pair("text on " + name, TEXT, background, false));
            pairs.add(new Pair("muted text on " + name, MUTED_TEXT, background, false));
            pairs.add(new Pair("warning on " + name, WARNING_TEXT, background, false));
        });
        // Buttons only ever carry their own text colour.
        pairs.add(new Pair("button text", BUTTON_TEXT, BUTTON, false));
        pairs.add(new Pair("button text, hovered", BUTTON_TEXT, BUTTON_HOVER, false));
        pairs.add(new Pair("accent button text", ACCENT_TEXT, ACCENT, false));
        pairs.add(new Pair("accent button text, hovered", ACCENT_TEXT, ACCENT_HOVER, false));
        pairs.add(new Pair("disabled button text", BUTTON_DISABLED_TEXT, BUTTON_DISABLED, false));
        pairs.add(new Pair("focus ring on surface", FOCUS, SURFACE, true));
        pairs.add(new Pair("focus ring on background", FOCUS, BACKGROUND, true));
        Map<String, Color> headers = Map.of("quiz", HEADER_QUIZ, "choice", HEADER_CHOICE, "event", HEADER_EVENT,
                "round report", HEADER_ROUND, "disaster", HEADER_DISASTER);
        headers.forEach((name, header) -> pairs.add(new Pair(name + " header label", HEADER_LABEL, header, false)));
        Map<String, Color> tiles = Map.of(
                "polluting", POLLUTING, "green", GREEN, "nature", NATURE,
                "resilience", RESILIENCE, "glider", GLIDER, "climate card", CLIMATE_CARD);
        tiles.forEach((name, fill) -> {
            pairs.add(new Pair("text on " + name + " tile", TEXT, fill, false));
            pairs.add(new Pair("muted text on " + name + " tile", MUTED_TEXT, fill, false));
            pairs.add(new Pair("warning on " + name + " tile", WARNING_TEXT, fill, false));
        });
        for (int i = 0; i < BANDS.size(); i++) {
            pairs.add(new Pair("band " + (i + 1) + " chip", BACKGROUND, BANDS.get(i), false));
        }
        pairs.add(new Pair("YOUR TURN chip", ACCENT_TEXT, ACCENT, false));
        pairs.add(new Pair("BANKRUPT chip", BACKGROUND, WARNING_TEXT, false));
        for (int i = 0; i < COUNTERS.size(); i++) {
            pairs.add(new Pair("counter " + (i + 1) + " number", COUNTER_TEXT, COUNTERS.get(i), false));
        }
        return List.copyOf(pairs);
    }
}
