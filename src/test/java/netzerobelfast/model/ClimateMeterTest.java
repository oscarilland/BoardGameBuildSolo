package netzerobelfast.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/** The Climate Meter so far: its start value from the data, and immediate changes (FR-10 AC-4, Q-02). */
class ClimateMeterTest {

    @Test
    void startsAtTheValueInTheData() {
        assertEquals(50, Games.scripted().meter().value());
    }

    @Test
    void movesBothWaysAndCanGoBelowZero() {
        ClimateMeter meter = new ClimateMeter(3);
        meter.adjust(2);
        assertEquals(5, meter.value());
        meter.adjust(-9);
        assertEquals(-4, meter.value(), "Q-02: no floor at 0");
    }
}
