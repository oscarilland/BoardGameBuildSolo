package netzerobelfast.model;

/**
 * A co-funding deal: the funder paid to back another player's Green Project, and both gain GP
 * every round it's open (analysis model: {@code CoFundingDeal}). Each player has at most one;
 * starting a new deal ends the old one (FR-05). The GP is paid at the end of the round (plan 005).
 *
 * @param funder the player who paid
 * @param square the id of the Green Project they fund. An id rather than the square object,
 *               so the deal stays valid if the board swaps square objects.
 */
public record CoFundingDeal(Player funder, int square) {}
