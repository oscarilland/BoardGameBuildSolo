package netzerobelfast.model;

import netzerobelfast.config.CardDef.Effect;
import netzerobelfast.model.Game.Phase;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * FR-10 AC-4 and AC-7: choice cards. C01: "Take the deal" (+£6M, meter +2) or "Retrofit your
 * offices" (−£2M, +1 GP). C08: "Act now" is −£3M and meter −2.
 */
class ChoiceCardTest {

    private static Game draw(String id) {
        Game game = Games.scripted();
        Games.nextCard(game, id);
        Games.arrive(game, 2);
        return game;
    }

    @Test
    void fr10_ac4_bothOptionsAreShownWithTheirEffectsBeforeChoosing() {
        Game game = draw("C01");
        assertEquals(Phase.AWAITING_CHOICE, game.phase());
        ChoiceCard c01 = (ChoiceCard) game.pendingCard().orElseThrow();
        assertEquals("Take the deal", c01.option(0).label());
        assertEquals(new Effect(6, 0, 2), c01.option(0).effect());
        assertEquals(new Effect(-2, 1, 0), c01.option(1).effect());
        assertEquals(2, c01.options().size());
        assertEquals(List.of(0, 1), game.affordableOptions());
    }

    @Test
    void fr10_ac4_optionAAppliesMoneyAndTheMeterAtOnce() {
        Game game = draw("C01");
        game.chooseOption(0);
        assertEquals(46, game.currentPlayer().money());
        assertEquals(52, game.meter().value(), "the meter moves straight away");
        assertEquals(Phase.MOVE_DONE, game.phase());

        CardResult result = game.lastCard().orElseThrow();
        assertEquals(0, result.option());
        assertEquals(6, result.moneyChange());
        assertEquals(2, result.meterChange());
    }

    @Test
    void fr10_ac4_optionBAppliesItsCostAndGP() {
        Game game = draw("C01");
        game.chooseOption(1);
        assertEquals(38, game.currentPlayer().money());
        assertEquals(9, game.currentPlayer().greenPoints());
        assertEquals(50, game.meter().value());
    }

    @Test
    void actingNowLowersTheMeter() {
        Game game = draw("C08");
        game.chooseOption(1);
        assertEquals(48, game.meter().value());
        assertEquals(37, game.currentPlayer().money());
    }

    @Test
    void fr10_ac7_anUnaffordableOptionIsShownButCantBeChosen() {
        Game game = Games.scripted();
        Games.setMoney(game.currentPlayer(), 1);
        Games.nextCard(game, "C01");
        Games.arrive(game, 2);

        assertEquals(List.of(0), game.affordableOptions());
        IllegalActionException e = assertThrows(IllegalActionException.class, () -> game.chooseOption(1));
        assertEquals("That option isn't available.", e.getMessage());
        assertEquals(1, game.currentPlayer().money());
        assertEquals(Phase.AWAITING_CHOICE, game.phase());

        game.chooseOption(0);
        assertEquals(7, game.currentPlayer().money());
    }

    @Test
    void anOptionThatDoesntExistIsRefused() {
        Game game = draw("C01");
        assertThrows(IllegalActionException.class, () -> game.chooseOption(2));
    }
}
