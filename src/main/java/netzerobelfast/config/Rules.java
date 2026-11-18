package netzerobelfast.config;

import java.util.List;
import java.util.Map;

/**
 * Everything in {@code config.json}: the game's balance values (NFR-07).
 *
 * <p>Text-only keys in the file, such as {@code scoring.netZero} or {@code disasters.roll},
 * describe a rule for people to read. They aren't loaded: the model implements the rule and
 * reads only the numbers here.
 */
public record Rules(
        Game game,
        List<CharacterDef> characters,
        Polluting polluting,
        Green green,
        Actions actions,
        Resilience resilience,
        int quizCorrectGP,
        Meter meter,
        Disasters disasters,
        Bankruptcy bankruptcy,
        Scoring scoring) {

    /**
     * {@code game}: players, rounds and years.
     *
     * @param maxNameLength the longest display name allowed, after trimming (FR-01 AC-3)
     */
    public record Game(
            int minPlayers, int maxPlayers, int maxNameLength, int rounds, int startYear, int yearsPerRound, int endYear) {}

    /**
     * One entry of {@code characters}. Ability parameters vary by character, so they're kept
     * as named numbers and named flags, for example {@code greenProjectDiscount} or
     * {@code lobbyCancelsMeter}.
     */
    public record CharacterDef(
            String id,
            String name,
            int startMoney,
            int startGP,
            String ability,
            Map<String, Integer> numbers,
            Map<String, Boolean> flags) {}

    /** {@code squares.polluting}. */
    public record Polluting(int minPrice, int maxPrice, int incomePerRound, int carbonTaxGP, int meterPerRound) {}

    /** {@code squares.greenProject}. The meter value is negative: green projects lower the meter. */
    public record Green(int minPrice, int maxPrice, int incomePerRound, int gpPerRound, int meterPerRound) {}

    /** {@code squares.convert}, {@code coFunding}, {@code lobbying} and {@code glider}. */
    public record Actions(
            int convertCost,
            int maxConversionsPerTurn,
            int coFundCost,
            int coFundGPEach,
            int maxCoFundDeals,
            int lobbyCost,
            int gliderFare) {}

    /** {@code squares.resilience}: cost of each level, and the share of disaster costs it removes. */
    public record Resilience(List<Integer> levelCosts, List<Double> disasterCostCut, int maxLevel) {}

    /** {@code climateMeter}. */
    public record Meter(
            int start,
            int basePerRound,
            int netZeroAtOrBelow,
            List<InterimTarget> interimTargets,
            int interimMissPenaltyGP,
            List<DangerBand> dangerBands,
            int belowZeroBand) {}

    /** One interim target, such as at most 26 in round 3 (2030). */
    public record InterimTarget(int year, int round, int maxMeter) {}

    /** A danger band and the highest meter value in it; {@code maxMeter} is null for the top band. */
    public record DangerBand(int band, Integer maxMeter) {}

    /**
     * The numbers in {@code disasters}; each cost is multiplied by the danger band. Each disaster
     * also has a one-line climate explanation with sources (FR-17, NFR-10 AC-2), keyed by the
     * disaster's name ({@code FLOOD}, {@code STORM}, {@code HEATWAVE}, {@code DROUGHT}).
     */
    public record Disasters(
            int floodCostPerSquarePerBand,
            int floodLostIncomeRounds,
            int stormRepairCostPerBand,
            int stormClosedRounds,
            int heatwaveCostPerPlayerPerBand,
            int droughtLostGPRounds,
            Map<String, Explanation> explanations) {}

    /** A disaster's climate explanation and where it comes from. */
    public record Explanation(String text, List<CardDef.Source> sources) {}

    /** {@code bankruptcy}. */
    public record Bankruptcy(int escapeRoll, int maxRounds, int loanOnReturn, int repayAtEnd) {}

    /** {@code scoring}. */
    public record Scoring(int missedPollutingPenaltyGP, int moneyDivisor) {}
}
