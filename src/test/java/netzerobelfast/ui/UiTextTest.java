package netzerobelfast.ui;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Text going into Swing labels: escaped, and long web addresses split so they fit (NFR-10 AC-3). */
class UiTextTest {

    @Test
    void namesCantInjectHtml() {
        String html = Ui.html("<b>Aoife</b> & co", 100);
        assertTrue(html.contains("&lt;b&gt;Aoife&lt;/b&gt; &amp; co"), html);
    }

    @Test
    void aLongWebAddressIsSplitWithoutLosingCharacters() {
        String url = "(https://metoffice.gov.uk/about-us/news-and-media/media-centre/weather-and-climate-news/2025/"
                + "annual-climate-stocktake-shows-weather-records-and-extremes-now-the-norm-in-uk-climate)";
        String text = "Source: Met Office " + url;
        String broken = Ui.breakLongWords(text);
        assertEquals(text, broken.replace("\n", ""));
        for (String line : broken.split("\n")) {
            assertTrue(line.length() <= 70, "too long: " + line);
        }
    }

    @Test
    void ordinaryTextIsLeftAlone() {
        String text = "Polluting squares are cheap and pay £7M a round.";
        assertEquals(text, Ui.breakLongWords(text));
    }
}
