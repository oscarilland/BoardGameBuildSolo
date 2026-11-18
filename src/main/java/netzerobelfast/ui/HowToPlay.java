package netzerobelfast.ui;

import netzerobelfast.config.Rules;

import java.util.List;

/**
 * The "How to play" tutorial: five short pages, readable in under two minutes (NFR-02 AC-1).
 * No Swing here, so it can be tested headless.
 *
 * <p>The sentences are fixed, but every number in them comes from the data, so a re-tune of
 * {@code config.json} changes the tutorial too (NFR-07).
 */
public final class HowToPlay {

    /** What the page's picture shows; the Swing screen draws it. */
    public enum Picture { METER, DICE, SQUARES, BANDS, SCORE }

    /** One page: a heading, a few short paragraphs, and a picture. */
    public record Page(String title, List<String> paragraphs, Picture picture) {
        public int words() {
            int words = title.split("\\s+").length;
            for (String paragraph : paragraphs) {
                words += paragraph.split("\\s+").length;
            }
            return words;
        }
    }

    private HowToPlay() {}

    public static List<Page> pages(Rules rules) {
        Rules.Game game = rules.game();
        Rules.Meter meter = rules.meter();
        Rules.InterimTarget first = meter.interimTargets().get(0);
        return List.of(
                new Page("The goal", List.of(
                        "It's " + game.startYear() + ". Belfast must reach net zero by " + game.endYear()
                                + ". You have " + game.rounds() + " rounds, " + game.yearsPerRound() + " years each.",
                        "The Climate Meter starts at " + meter.start() + ". If it's at " + meter.netZeroAtOrBelow()
                                + " or below at the end, Belfast reached net zero.",
                        "Everyone shares the meter. Polluting helps you now, but it hurts the whole city."),
                        Picture.METER),
                new Page("Your turn", List.of(
                        "Roll the dice and move round the board. At a junction you can cut through the City Centre.",
                        "Then act on your square, if you want to and can afford it: the buttons show what you can do and what it costs.",
                        "When you're done, press End turn. Keys: R roll, E end turn, Tab to move between buttons."),
                        Picture.DICE),
                new Page("The squares", List.of(
                        "Polluting squares are cheap and pay £" + rules.polluting().incomePerRound() + "M a round, but cost "
                                + rules.polluting().carbonTaxGP() + " GP in carbon tax and push the meter up "
                                + rules.polluting().meterPerRound() + ".",
                        "Green Projects cost more and pay £" + rules.green().incomePerRound() + "M, but earn "
                                + rules.green().gpPerRound() + " GP and bring the meter down " + -rules.green().meterPerRound() + ".",
                        "You can convert your own Polluting square to green for £" + rules.actions().convertCost()
                                + "M. Resilience protects you from disasters. Card squares draw a Climate Card."),
                        Picture.SQUARES),
                new Page("Disasters and targets", List.of(
                        "The higher the meter, the higher the danger band, and the more likely and costly floods, storms, heatwaves and droughts are.",
                        "In " + first.year() + " the meter must be " + first.maxMeter() + " or below, and later targets follow. "
                                + "Missing one costs polluters " + meter.interimMissPenaltyGP() + " GP per Polluting square.",
                        "If you can't pay, you're bankrupt until you roll a " + rules.bankruptcy().escapeRoll() + "."),
                        Picture.BANDS),
                new Page("Who wins", List.of(
                        "If Belfast reaches net zero, your score is your GP plus your money ÷ " + rules.scoring().moneyDivisor() + ".",
                        "If it doesn't, money is worthless: your score is your GP, less "
                                + rules.scoring().missedPollutingPenaltyGP() + " for each Polluting square you own.",
                        "So: get rich, but not at the city's expense. Good luck!"),
                        Picture.SCORE));
    }
}
