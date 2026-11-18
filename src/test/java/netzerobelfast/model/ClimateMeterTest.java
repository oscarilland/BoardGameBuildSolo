package netzerobelfast.model;

import netzerobelfast.config.Rules;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** The Climate Meter: start value, immediate changes, the round update, bands and targets (FR-11, FR-12, Q-02). */
class ClimateMeterTest {

    private final Rules rules = Games.CONFIG.rules();

    private ClimateMeter meterAt(int value) {
        ClimateMeter meter = new ClimateMeter(rules);
        meter.adjust(value - meter.value());
        return meter;
    }

    @Test
    void startsAtTheValueInTheData() {
        assertEquals(50, Games.scripted().meter().value());
        assertEquals(List.of(50), new ClimateMeter(rules).history());
    }

    @Test
    void movesBothWaysAndCanGoBelowZero() {
        ClimateMeter meter = meterAt(3);
        meter.adjust(2);
        assertEquals(5, meter.value());
        meter.adjust(-9);
        assertEquals(-4, meter.value(), "Q-02: no floor at 0");
    }

    @Test
    void fr11_ac5_theRoundUpdateUsesTheDataValues() {
        ClimateMeter meter = new ClimateMeter(rules);
        meter.update(3, 5); // +3 base, +2 × 3, −1 × 5
        assertEquals(54, meter.value());
        meter.update(0, 10);
        assertEquals(47, meter.value());
        assertEquals(List.of(50, 54, 47), meter.history(), "AC-8: one value per round, after the start");
    }

    @Test
    void fr12_ac1_dangerBands() {
        int[][] cases = {{-3, 1}, {0, 1}, {10, 1}, {11, 2}, {25, 2}, {26, 3}, {40, 3}, {41, 4}, {90, 4}};
        for (int[] c : cases) {
            assertEquals(c[1], meterAt(c[0]).dangerBand(), "meter " + c[0]);
        }
    }

    @Test
    void interimTargetsAndNetZero() {
        Rules.InterimTarget target2030 = new ClimateMeter(rules).targetFor(3).orElseThrow();
        assertEquals(26, target2030.maxMeter());
        assertEquals(11, new ClimateMeter(rules).targetFor(8).orElseThrow().maxMeter());
        assertTrue(new ClimateMeter(rules).targetFor(4).isEmpty());

        assertTrue(meterAt(26).meetsTarget(target2030));
        assertFalse(meterAt(27).meetsTarget(target2030));
        assertTrue(meterAt(0).netZero());
        assertTrue(meterAt(-2).netZero());
        assertFalse(meterAt(1).netZero());
    }
}
