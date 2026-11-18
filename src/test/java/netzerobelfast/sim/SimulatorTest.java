package netzerobelfast.sim;

import netzerobelfast.config.GameConfig;
import netzerobelfast.model.GameResult;
import netzerobelfast.model.GameResult.PlayerScore;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** NFR-06 AC-2 and AC-3: 20 complete games with computer players, and repeatable from a seed. */
class SimulatorTest {

    private static final GameConfig CONFIG = GameConfig.load();
    private static final String[] IDS = {"INDUSTRIALIST", "TECH_ENTREPRENEUR", "COUNCIL_PLANNER", "FARMER",
            "STUDENT_COMMUTER", "COMMUNITY_ACTIVIST"};

    /** {@code count} seats with characters rotated by {@code seed} and strategies mixed in turn. */
    private static List<Simulator.Seat> seats(int count, long seed) {
        List<Simulator.Seat> seats = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            Strategy strategy = switch (i % 3) {
                case 0 -> Strategies.random(0.6);
                case 1 -> Strategies.green();
                default -> Strategies.polluter();
            };
            seats.add(new Simulator.Seat("P" + i, IDS[(int) ((i + seed) % IDS.length)], strategy));
        }
        return seats;
    }

    @Test
    void nfr06_ac2_ac3_twentyCompleteGamesWithAMixOfStrategies() {
        for (long seed = 0; seed < 20; seed++) {
            int count = 4 + (int) (seed % 3);
            Simulator.Outcome outcome = Simulator.play(CONFIG, seed, seats(count, seed));
            GameResult result = outcome.result();
            String context = "game " + seed;

            assertEquals(count, result.ranking().size(), context);
            assertSame(result.ranking().get(0).player(), result.winner(), context + ": exactly one winner, first in the ranking");
            for (PlayerScore score : result.ranking()) {
                assertTrue(score.money() >= 0, context + ": " + score.player() + " has negative money before repayments");
            }
            assertEquals(13, result.meterHistory().size(), context);
            assertTrue(outcome.turns() >= 12 * count, context + ": every player had 12 turns");
        }
    }

    @Test
    void theSameSeedReplaysTheSameGame() {
        Simulator.Outcome first = Simulator.play(CONFIG, 99, seats(5, 99));
        Simulator.Outcome second = Simulator.play(CONFIG, 99, seats(5, 99));
        assertEquals(first.result().winner().name(), second.result().winner().name());
        assertEquals(first.result().meterHistory(), second.result().meterHistory());
        assertEquals(first.result().ranking().stream().map(s -> s.score().display()).toList(),
                second.result().ranking().stream().map(s -> s.score().display()).toList());
        assertEquals(first.disasters(), second.disasters());
    }
}
