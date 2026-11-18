package netzerobelfast.model;

import netzerobelfast.config.Rules;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

/**
 * Checks for and applies disasters (analysis model: {@code DisasterManager}), and remembers their
 * lasting effects: a flooded district earns nothing, a storm-closed square earns nothing, and a
 * Drought stops nature projects' GP, for the number of rounds the data gives (1: "next round").
 *
 * <p>Every random choice (the die, the type, which district or square) uses the game's {@link Dice},
 * so a seed replays it.
 */
public final class DisasterManager {

    /** The result of the end-of-round roll: the die, and the disaster if it was at or below the band. */
    public record Check(int roll, Disaster disaster) {}

    private final Rules rules;
    private final Dice dice;

    // Lasting effects, counted in rounds still to run. They take effect at the next end of round.
    private final Map<String, Integer> floodedDistricts = new HashMap<>();
    private final Map<Integer, Integer> closedSquares = new HashMap<>();
    private int droughtRounds;

    DisasterManager(Rules rules, Dice dice) {
        this.rules = rules;
        this.dice = dice;
    }

    /** FR-12 AC-2 and AC-3: roll one die; a disaster if it's at or below the band, of a type chosen at random. */
    Check check(int band) {
        int roll = dice.rollOne();
        if (roll > band) {
            return new Check(roll, null);
        }
        Disaster[] types = Disaster.values();
        return new Check(roll, types[dice.random().nextInt(types.length)]);
    }

    /** Applies a disaster (FR-12 AC-4 to AC-8). Costs are paid now; lasting effects start at the next end of round. */
    DisasterResult apply(Disaster disaster, int band, Board board, List<Player> players) {
        Rules.Disasters data = rules.disasters();
        Map<Player, Integer> payments = new LinkedHashMap<>();
        List<Player> bankrupted = new ArrayList<>();
        String district = null;
        int square = -1;

        switch (disaster) {
            case FLOOD -> {
                List<String> districts = districtsWithOwnedSquares(board);
                if (!districts.isEmpty()) {
                    district = districts.get(dice.random().nextInt(districts.size()));
                    // One payment per owner for all their squares there, cut once by their Resilience.
                    Map<Player, Integer> squaresOwned = new LinkedHashMap<>();
                    for (Square s : board.squares()) {
                        if (s instanceof OwnableSquare o && o.owner() != null && s.district().equals(district)) {
                            squaresOwned.merge(o.owner(), 1, Integer::sum);
                        }
                    }
                    for (Map.Entry<Player, Integer> owner : squaresOwned.entrySet()) {
                        charge(owner.getKey(), data.floodCostPerSquarePerBand() * band * owner.getValue(), payments, bankrupted);
                    }
                    floodedDistricts.put(district, data.floodLostIncomeRounds());
                }
            }
            case STORM -> {
                List<OwnableSquare> owned = ownedSquares(board);
                if (!owned.isEmpty()) {
                    OwnableSquare hit = owned.get(dice.random().nextInt(owned.size()));
                    square = hit.id();
                    charge(hit.owner(), data.stormRepairCostPerBand() * band, payments, bankrupted);
                    closedSquares.put(square, data.stormClosedRounds());
                }
            }
            case HEATWAVE -> {
                for (Player player : players) {
                    charge(player, data.heatwaveCostPerPlayerPerBand() * band, payments, bankrupted);
                }
            }
            case DROUGHT -> droughtRounds = data.droughtLostGPRounds();
        }

        Rules.Explanation explanation = data.explanations().get(disaster.name());
        return new DisasterResult(disaster, band, district, square, payments, bankrupted,
                explanation.text(), explanation.sources());
    }

    /** A compulsory disaster payment, cut by the player's Resilience (FR-08 AC-5); it can bankrupt (FR-12 AC-8). */
    private static void charge(Player player, int amount, Map<Player, Integer> payments, List<Player> bankrupted) {
        boolean wasBankrupt = player.bankrupt();
        payments.put(player, player.payCompulsory(player.disasterCost(amount)));
        if (!wasBankrupt && player.bankrupt()) {
            bankrupted.add(player);
        }
    }

    /** Districts with at least one owned square, in board order. */
    private static List<String> districtsWithOwnedSquares(Board board) {
        LinkedHashSet<String> districts = new LinkedHashSet<>();
        for (OwnableSquare square : ownedSquares(board)) {
            districts.add(square.district());
        }
        return new ArrayList<>(districts);
    }

    private static List<OwnableSquare> ownedSquares(Board board) {
        List<OwnableSquare> owned = new ArrayList<>();
        for (Square square : board.squares()) {
            if (square instanceof OwnableSquare o && o.owner() != null) {
                owned.add(o);
            }
        }
        return owned;
    }

    // ---- Lasting effects ------------------------------------------------------

    /** True if this square earns nothing this round: storm-closed, or in a flooded district (Q-15). */
    public boolean shut(Square square) {
        return closedSquares.containsKey(square.id()) || floodedDistricts.containsKey(square.district());
    }

    /** True if nature projects earn no GP this round. */
    public boolean drought() {
        return droughtRounds > 0;
    }

    /** After the round's income has been paid, each lasting effect has one round fewer to run. */
    void endOfIncome() {
        floodedDistricts.replaceAll((district, rounds) -> rounds - 1);
        floodedDistricts.values().removeIf(rounds -> rounds <= 0);
        closedSquares.replaceAll((square, rounds) -> rounds - 1);
        closedSquares.values().removeIf(rounds -> rounds <= 0);
        droughtRounds = Math.max(0, droughtRounds - 1);
    }
}
