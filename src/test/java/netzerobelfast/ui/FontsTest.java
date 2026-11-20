package netzerobelfast.ui;

import org.junit.jupiter.api.Test;

import java.awt.Font;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Plan 008: the bundled font loads, and a missing one falls back instead of failing. */
class FontsTest {

    @Test
    void interLoadsFromTheJar() {
        assertTrue(Fonts.bundled(), "loaded " + Fonts.REGULAR.getFamily());
        assertTrue(Fonts.SEMIBOLD.getFontName().contains("Inter"), Fonts.SEMIBOLD.getFontName());
        assertTrue(Fonts.BOLD.getFontName().contains("Inter"), Fonts.BOLD.getFontName());
    }

    @Test
    void theLicenceShipsWithTheFont() {
        assertNotNull(Fonts.class.getResource("/fonts/OFL.txt"), "the SIL Open Font License must be in the jar");
    }

    @Test
    void aMissingFontFallsBackToTheSystemSansSerif() {
        Font font = Fonts.load("/fonts/missing.ttf", Font.BOLD);
        assertEquals(Font.SANS_SERIF, font.getName());
        assertTrue(font.isBold());
    }
}
