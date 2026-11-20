package netzerobelfast.ui;

import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.GraphicsEnvironment;
import java.io.IOException;
import java.io.InputStream;

/**
 * The game's font, Inter (SIL Open Font License 1.1, see {@code fonts/OFL.txt}), loaded once
 * from inside the jar. If a file can't be loaded, that weight falls back to the system
 * sans-serif, so the game always starts.
 */
final class Fonts {

    /** The three weights the game uses, by file in {@code src/main/resources/fonts/}. */
    static final Font REGULAR = load("/fonts/Inter-Regular.ttf", Font.PLAIN);
    static final Font SEMIBOLD = load("/fonts/Inter-SemiBold.ttf", Font.BOLD);
    static final Font BOLD = load("/fonts/Inter-Bold.ttf", Font.BOLD);

    private Fonts() {}

    /** Loads a font file from the jar at size 1, or the system sans-serif if it can't. */
    static Font load(String resource, int fallbackStyle) {
        try (InputStream in = Fonts.class.getResourceAsStream(resource)) {
            if (in == null) {
                return fallback(fallbackStyle);
            }
            Font font = Font.createFont(Font.TRUETYPE_FONT, in);
            GraphicsEnvironment.getLocalGraphicsEnvironment().registerFont(font);
            return font;
        } catch (IOException | FontFormatException e) {
            return fallback(fallbackStyle);
        }
    }

    private static Font fallback(int style) {
        return new Font(Font.SANS_SERIF, style, 1);
    }

    /** True if the bundled font loaded, rather than the fallback. */
    static boolean bundled() {
        return REGULAR.getFamily().startsWith("Inter");
    }
}
