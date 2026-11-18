package netzerobelfast.ui;

import org.junit.jupiter.api.Test;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** NFR-05 AC-1: every text and background pair the UI uses meets WCAG 2.1 AA contrast. */
class PaletteTest {

    @Test
    void contrast_matchesKnownValues() {
        assertEquals(21.0, Contrast.ratio(Color.BLACK, Color.WHITE), 0.001);
        assertEquals(1.0, Contrast.ratio(Color.WHITE, Color.WHITE), 0.001);
        // #777777 on white is the classic "just fails" grey: about 4.48.
        assertEquals(4.48, Contrast.ratio(new Color(0x777777), Color.WHITE), 0.01);
        // Order doesn't matter.
        assertEquals(Contrast.ratio(Color.RED, Color.WHITE), Contrast.ratio(Color.WHITE, Color.RED), 1e-9);
    }

    @Test
    void nfr05_ac1_everyTextPairMeetsContrast() {
        List<String> failures = new ArrayList<>();
        for (Palette.Pair pair : Palette.TEXT_PAIRS) {
            double needed = pair.large() ? Contrast.LARGE_TEXT : Contrast.NORMAL_TEXT;
            double ratio = Contrast.ratio(pair.text(), pair.background());
            if (ratio < needed) {
                failures.add(String.format("%s: %.2f, needs %.1f", pair.name(), ratio, needed));
            }
        }
        assertTrue(failures.isEmpty(), "contrast too low: " + failures);
    }

    @Test
    void nfr05_ac1_everySquareFillAndCounterIsChecked() {
        // A new square colour or counter without a checked pair would slip past the test above.
        List<Color> backgrounds = Palette.TEXT_PAIRS.stream().map(Palette.Pair::background).toList();
        for (Color fill : List.of(Palette.POLLUTING, Palette.GREEN, Palette.NATURE, Palette.RESILIENCE,
                Palette.GLIDER, Palette.CLIMATE_CARD, Palette.HEADER, Palette.CARD, Palette.BUTTON)) {
            assertTrue(backgrounds.contains(fill), "no text pair checks " + fill);
        }
        assertTrue(backgrounds.containsAll(Palette.COUNTERS));
    }

    @Test
    void bandColours_coverEveryBandAndClampBeyond() {
        assertEquals(Palette.BANDS.get(0), Palette.band(1));
        assertEquals(Palette.BANDS.get(3), Palette.band(4));
        assertEquals(Palette.BANDS.get(3), Palette.band(9));
        assertEquals(Palette.BANDS.get(0), Palette.band(0));
    }
}
