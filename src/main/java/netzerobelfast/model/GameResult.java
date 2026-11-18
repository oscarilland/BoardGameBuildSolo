package netzerobelfast.model;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * The end of the game (FR-14, FR-16): whether Belfast reached net zero, every player's score in
 * ranking order, the single winner and how they were decided, and the data the end-of-game
 * report draws.
 *
 * @param netZero         true if the meter is at or below the net zero value after the last round
 * @param finalMeter      the meter's final value
 * @param ranking         every player, best first
 * @param winner          the one winner (FR-14 AC-5)
 * @param decidedBy       what separated the winner from the next player
 * @param rollOff         if a roll-off was needed, each player's rolls in order; otherwise empty
 * @param meterHistory    the meter at the start and after each round (FR-16 AC-1)
 * @param meterLines      lines to mark on the graph: the interim targets and net zero
 * @param pollutingShares each player's share of the city's polluting-square-rounds, as a percentage (FR-16 AC-2)
 */
public record GameResult(
        boolean netZero,
        int finalMeter,
        List<PlayerScore> ranking,
        Player winner,
        TieBreak decidedBy,
        Map<Player, List<Integer>> rollOff,
        List<Integer> meterHistory,
        List<MeterLine> meterLines,
        Map<Player, BigDecimal> pollutingShares) {

    /** What decided the winner. */
    public enum TieBreak {
        /** The highest score, with no tie. */
        SCORE,
        /** Tied on score; the most Green Projects owned. */
        GREEN_PROJECTS,
        /** Tied on score and Green Projects; the fewest Polluting squares owned (Q-01). */
        FEWER_POLLUTING,
        /** Still tied; a die roll-off, re-rolling ties (Q-01). */
        ROLL_OFF
    }

    /**
     * One player's result.
     *
     * @param greenPoints    GP at the end, before any missed-net-zero penalty
     * @param money          money at the end, before loan repayments
     * @param pollutingOwned Polluting squares owned at the end
     * @param greenOwned     Green Projects owned at the end
     */
    public record PlayerScore(
            Player player,
            int greenPoints,
            int money,
            int loanRepayments,
            int pollutingOwned,
            int greenOwned,
            LegacyScore score) {}

    /** A horizontal line on the meter graph, such as "2030 target" at 26. Year is null for net zero. */
    public record MeterLine(String label, Integer year, int value) {}

    public PlayerScore scoreOf(Player player) {
        return ranking.stream().filter(s -> s.player() == player).findFirst().orElseThrow();
    }
}
