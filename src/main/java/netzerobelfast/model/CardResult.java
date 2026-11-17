package netzerobelfast.model;

import netzerobelfast.config.CardDef.Source;

import java.util.List;

/**
 * What happened when a Climate Card was resolved, for the screen to show afterwards with the
 * card's fact and sources (FR-10 AC-6).
 *
 * @param card        the card
 * @param answer      the quiz answer chosen, or -1 for other cards
 * @param correct     true if the quiz answer was right; false for other cards
 * @param option      the choice option taken (0 for A, 1 for B), or -1 for other cards
 * @param moneyChange how the player's money changed, after any amount written off
 * @param gpChange    how the player's GP changed
 * @param meterChange how the Climate Meter changed
 * @param bankrupt    true if this card made the player bankrupt
 */
public record CardResult(
        ClimateCard card,
        int answer,
        boolean correct,
        int option,
        int moneyChange,
        int gpChange,
        int meterChange,
        boolean bankrupt) {

    public String fact() {
        return card.fact();
    }

    public List<Source> sources() {
        return card.sources();
    }
}
