package netzerobelfast.model;

import org.junit.jupiter.api.Test;

import static netzerobelfast.model.Games.own;
import static netzerobelfast.model.Games.player;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * FR-11 step 1: what squares and co-funding deals pay at the end of the round. Squares used:
 * 1 Waste Incinerator (Polluting, NORTH), 4 Motorway Expansion (Polluting, NORTH), 6 Harbour Gas
 * Plant (Polluting, HARBOUR), 5 Heat Pump Retrofit (Green, NORTH), 0 Cave Hill Woodland (Green,
 * nature, NORTH). Players: Aoife Industrialist £40M 8 GP, Ben Tech Entrepreneur £36M 10 GP,
 * Chloe Council Planner, Dev Farmer £36M 13 GP. Carbon tax is checked separately in {@link CarbonTaxTest}.
 */
class IncomeTest {

    @Test
    void fr11_ac3_aPollutingSquarePaysSeven() {
        Game game = Games.quietRound();
        Player aoife = player(game, "Aoife");
        own(game, 1, aoife);
        game.endRound();
        assertEquals(47, aoife.money());
    }

    @Test
    void fr11_ac3_aGreenProjectPaysTwoAndTwoGP() {
        Game game = Games.quietRound();
        Player ben = player(game, "Ben");
        own(game, 5, ben);
        game.endRound();
        assertEquals(38, ben.money());
        assertEquals(12, ben.greenPoints());
    }

    @Test
    void fr06_ac1_ac2_aLobbiedSquareEarnsNoMoneyThisRoundOnly() {
        Game game = Games.withScript(new ScriptedRandom().firstPlayer(0).noDisaster().noDisaster());
        Player ben = player(game, "Ben");
        own(game, 1, ben);
        Games.lobby(game, 1, player(game, "Aoife"));

        game.endRound();
        assertEquals(36, ben.money(), "lobbied: no £7M");
        game.endRound();
        assertEquals(43, ben.money(), "the next round it pays again");
    }

    @Test
    void fr07_ac4_ac5_theIndustrialistEarnsExtraOnSquaresTheyConverted() {
        Game game = Games.quietRound();
        Player aoife = player(game, "Aoife");
        Player ben = player(game, "Ben");
        own(game, 1, aoife);
        Games.convert(game, 1, aoife);
        own(game, 4, ben);
        Games.convert(game, 4, ben);
        own(game, 5, aoife); // bought as green, not converted

        game.endRound();
        assertEquals(40 + 4 + 2, aoife.money(), "converted: £2M + £2M bonus; bought green: £2M");
        assertEquals(38, ben.money(), "converted by the Tech Entrepreneur: no bonus");
        assertEquals(8 + 2 + 2, aoife.greenPoints(), "AC-4: a converted square earns GP like any Green Project");
    }

    @Test
    void theFarmerEarnsOneMoreGPPerNatureProject() {
        Game game = Games.quietRound();
        Player dev = player(game, "Dev");
        own(game, 0, dev); // nature
        own(game, 5, dev); // not nature
        game.endRound();
        assertEquals(13 + 3 + 2, dev.greenPoints());
        assertEquals(40, dev.money());
    }

    @Test
    void fr08_ac6_aDroughtStopsNatureGPButNotMoney() {
        Game game = Games.quietRound();
        Player dev = player(game, "Dev");
        own(game, 0, dev);
        own(game, 5, dev);
        game.disasters().apply(Disaster.DROUGHT, 1, game.board(), game.players());

        game.endRound();
        assertEquals(13 + 2, dev.greenPoints(), "no GP from the nature project, and no Farmer bonus");
        assertEquals(36 + 4, dev.money(), "both still pay money");
    }

    @Test
    void fr08_ac6_aFloodedDistrictEarnsNothing() {
        // The flood picks a district: value 0 is NORTH, the first with owned squares.
        Game game = Games.withScript(new ScriptedRandom().firstPlayer(0).values(0).noDisaster());
        Player aoife = player(game, "Aoife");
        own(game, 1, aoife); // NORTH
        own(game, 6, aoife); // HARBOUR
        game.disasters().apply(Disaster.FLOOD, 1, game.board(), game.players());
        assertEquals(39, aoife.money(), "£1M for her one NORTH square");

        game.endRound();
        assertEquals(39 + 7, aoife.money(), "only the Harbour square pays");
    }

    @Test
    void fr05_ac5_aStormClosedSquareAndItsDealPayNothing() {
        Game game = Games.withScript(new ScriptedRandom().firstPlayer(0).values(0).noDisaster());
        Player aoife = player(game, "Aoife");
        Player ben = player(game, "Ben");
        own(game, 5, ben);
        Games.deal(aoife, 5);
        game.disasters().apply(Disaster.STORM, 1, game.board(), game.players());
        assertEquals(34, ben.money(), "£2M repairs in band 1");

        game.endRound();
        assertEquals(34, ben.money(), "closed: no income");
        assertEquals(10, ben.greenPoints(), "closed: no GP, and no co-funding GP");
        assertEquals(8, aoife.greenPoints(), "the funder gets nothing either");
    }

    @Test
    void fr05_ac3_aDealGivesTheFunderAndTheOwnerOneGPEach() {
        Game game = Games.quietRound();
        Player aoife = player(game, "Aoife");
        Player ben = player(game, "Ben");
        own(game, 5, ben);
        Games.deal(aoife, 5);
        game.endRound();
        assertEquals(9, aoife.greenPoints());
        assertEquals(10 + 2 + 1, ben.greenPoints());
    }

    @Test
    void fr13_ac7_fr05_ac6_aBankruptOwnerGetsGPButNoMoneyAndDealsContinue() {
        Game game = Games.quietRound();
        Player aoife = player(game, "Aoife");
        Player ben = player(game, "Ben");
        own(game, 5, ben);
        own(game, 6, ben);
        Games.deal(aoife, 5);
        Games.bankrupt(ben);

        game.endRound();
        assertEquals(0, ben.money(), "no money from either square");
        assertEquals(10 + 2 + 1 - 1, ben.greenPoints(), "+2 green, +1 deal, -1 carbon tax");
        assertEquals(9, aoife.greenPoints(), "the deal still pays the funder");
    }
}
