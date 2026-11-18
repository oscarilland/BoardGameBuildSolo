package netzerobelfast.config;

import netzerobelfast.config.BoardDef.SquareDef;
import netzerobelfast.config.BoardDef.SquareType;
import netzerobelfast.config.CardDef.Effect;
import netzerobelfast.config.CardDef.Option;
import netzerobelfast.config.CardDef.Source;
import netzerobelfast.config.Rules.CharacterDef;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Turns each data file's JSON tree into records. It checks that every required key is there
 * and has the right type; {@link ConfigValidator} then checks that the values make sense
 * together.
 */
final class ConfigParser {

    private ConfigParser() {}

    // ---- config.json --------------------------------------------------------

    static Rules rules(JsonNode root) {
        JsonNode game = root.get("game");
        JsonNode squares = root.get("squares");
        JsonNode polluting = squares.get("polluting");
        JsonNode green = squares.get("greenProject");
        JsonNode resilience = squares.get("resilience");
        JsonNode meter = root.get("climateMeter");
        JsonNode disasters = root.get("disasters");
        JsonNode bankruptcy = root.get("bankruptcy");
        JsonNode scoring = root.get("scoring");

        List<CharacterDef> characters = new ArrayList<>();
        for (JsonNode character : root.get("characters").list()) {
            characters.add(character(character));
        }

        List<Rules.InterimTarget> targets = new ArrayList<>();
        for (JsonNode target : meter.get("interimTargets").list()) {
            targets.add(new Rules.InterimTarget(
                    target.get("year").asInt(), target.get("round").asInt(), target.get("maxMeter").asInt()));
        }

        List<Rules.DangerBand> bands = new ArrayList<>();
        for (JsonNode band : meter.get("dangerBands").list()) {
            // The top band has "maxMeter": null, meaning no upper limit.
            Integer maxMeter = band.has("maxMeter") ? band.get("maxMeter").asInt() : null;
            bands.add(new Rules.DangerBand(band.get("band").asInt(), maxMeter));
        }

        return new Rules(
                new Rules.Game(
                        game.get("minPlayers").asInt(),
                        game.get("maxPlayers").asInt(),
                        game.get("maxNameLength").asInt(),
                        game.get("rounds").asInt(),
                        game.get("startYear").asInt(),
                        game.get("yearsPerRound").asInt(),
                        game.get("endYear").asInt()),
                List.copyOf(characters),
                new Rules.Polluting(
                        priceRange(polluting).get(0),
                        priceRange(polluting).get(1),
                        polluting.get("incomePerRound").asInt(),
                        polluting.get("carbonTaxGPPerRound").asInt(),
                        polluting.get("meterPerRound").asInt()),
                new Rules.Green(
                        priceRange(green).get(0),
                        priceRange(green).get(1),
                        green.get("incomePerRound").asInt(),
                        green.get("gpPerRound").asInt(),
                        green.get("meterPerRound").asInt()),
                new Rules.Actions(
                        squares.get("convert").get("cost").asInt(),
                        squares.get("convert").get("maxPerTurn").asInt(),
                        squares.get("coFunding").get("cost").asInt(),
                        squares.get("coFunding").get("gpPerRoundEach").asInt(),
                        squares.get("coFunding").get("maxActiveDealsPerPlayer").asInt(),
                        squares.get("lobbying").get("cost").asInt(),
                        squares.get("glider").get("fare").asInt()),
                new Rules.Resilience(
                        resilience.get("levelCosts").intList(),
                        resilience.get("disasterCostCut").numberList(),
                        resilience.get("maxLevel").asInt()),
                squares.get("climateCard").get("quizCorrectGP").asInt(),
                new Rules.Meter(
                        meter.get("start").asInt(),
                        meter.get("basePerRound").asInt(),
                        meter.get("netZeroAtOrBelow").asInt(),
                        List.copyOf(targets),
                        meter.get("interimMissPenaltyGPPerPolluting").asInt(),
                        List.copyOf(bands),
                        meter.get("belowZeroUsesBand").asInt()),
                new Rules.Disasters(
                        disasters.get("FLOOD").get("costPerOwnedSquarePerBand").asInt(),
                        disasters.get("FLOOD").get("lostIncomeRounds").asInt(),
                        disasters.get("STORM").get("repairCostPerBand").asInt(),
                        disasters.get("STORM").get("closedRounds").asInt(),
                        disasters.get("HEATWAVE").get("costPerPlayerPerBand").asInt(),
                        disasters.get("DROUGHT").get("lostGPRounds").asInt(),
                        explanations(disasters)),
                new Rules.Bankruptcy(
                        bankruptcy.get("escapeRoll").asInt(),
                        bankruptcy.get("maxRounds").asInt(),
                        bankruptcy.get("loanOnReturn").asInt(),
                        bankruptcy.get("repayAtEnd").asInt()),
                new Rules.Scoring(
                        scoring.get("missedPollutingPenaltyGP").asInt(),
                        scoring.get("moneyDivisor").asInt()));
    }

    private static CharacterDef character(JsonNode node) {
        Map<String, Integer> numbers = new LinkedHashMap<>();
        Map<String, Boolean> flags = new LinkedHashMap<>();
        JsonNode params = node.get("params");
        for (String key : params.keys()) {
            JsonNode value = params.get(key);
            if (value.isBoolean()) {
                flags.put(key, value.asBoolean());
            } else {
                numbers.put(key, value.asInt());
            }
        }
        return new CharacterDef(
                node.get("id").asText(),
                node.get("name").asText(),
                node.get("startMoney").asInt(),
                node.get("startGP").asInt(),
                node.get("ability").asText(),
                Map.copyOf(numbers),
                Map.copyOf(flags));
    }

    /** Each disaster's explanation and sources (FR-17). */
    private static Map<String, Rules.Explanation> explanations(JsonNode disasters) {
        Map<String, Rules.Explanation> explanations = new LinkedHashMap<>();
        for (String type : new String[] {"FLOOD", "STORM", "HEATWAVE", "DROUGHT"}) {
            JsonNode disaster = disasters.get(type);
            explanations.put(type, new Rules.Explanation(disaster.get("explanation").asText(), sources(disaster)));
        }
        return Map.copyOf(explanations);
    }

    /** A {@code sources} list: each entry has a title and a web address. */
    private static List<Source> sources(JsonNode parent) {
        List<Source> sources = new ArrayList<>();
        for (JsonNode source : parent.get("sources").list()) {
            sources.add(new Source(source.get("title").asText(), source.get("url").asText()));
        }
        return List.copyOf(sources);
    }

    private static List<Integer> priceRange(JsonNode section) {
        JsonNode range = section.get("priceRange");
        List<Integer> values = range.intList();
        if (values.size() != 2) {
            throw range.error("should be [lowest, highest]");
        }
        return values;
    }

    // ---- board.json ---------------------------------------------------------

    static BoardDef board(JsonNode root) {
        Map<String, String> districts = new LinkedHashMap<>();
        JsonNode districtNode = root.get("districts");
        for (String code : districtNode.keys()) {
            districts.put(code, districtNode.get(code).asText());
        }

        JsonNode route = root.get("cityCentreRoute");
        JsonNode connects = route.get("connects");

        List<SquareDef> squares = new ArrayList<>();
        for (JsonNode square : root.get("squares").list()) {
            SquareType type = square.get("type").asEnum(SquareType.class);
            boolean nature = false;
            if (square.has("isNature")) {
                JsonNode flag = square.get("isNature");
                if (type != SquareType.GREEN_PROJECT) {
                    throw flag.error("is only allowed on GREEN_PROJECT squares");
                }
                nature = flag.asBoolean();
            }
            squares.add(new SquareDef(
                    square.get("id").asInt(),
                    square.get("name").asText(),
                    type,
                    square.get("district").asText(),
                    square.has("price") ? square.get("price").asInt() : null,
                    nature));
        }

        return new BoardDef(
                Map.copyOf(districts),
                root.get("loop").get("squareIds").intList(),
                route.get("squareIds").intList(),
                connects.get("westJunction").asInt(),
                connects.get("eastJunction").asInt(),
                root.get("junctions").intList(),
                root.get("gliderStops").intList(),
                route.get("direction").asText(),
                List.copyOf(squares));
    }

    // ---- climate_cards.json -------------------------------------------------

    static List<CardDef> cards(JsonNode root) {
        List<CardDef> cards = new ArrayList<>();
        for (JsonNode card : root.get("cards").list()) {
            cards.add(card(card));
        }
        return List.copyOf(cards);
    }

    private static CardDef card(JsonNode node) {
        CardDef.Type type = node.get("type").asEnum(CardDef.Type.class);

        List<String> answers = List.of();
        int correctIndex = -1;
        Integer reward = null;
        List<Option> options = List.of();
        Effect effect = null;

        switch (type) {
            case QUIZ -> {
                List<String> texts = new ArrayList<>();
                for (JsonNode answer : node.get("answers").list()) {
                    texts.add(answer.asText());
                }
                answers = List.copyOf(texts);
                correctIndex = node.get("correctIndex").asInt();
                // Optional: a card without a reward uses the config default (Q-13).
                if (node.has("reward")) {
                    reward = node.get("reward").get("gp").asInt();
                }
            }
            case CHOICE -> {
                List<Option> parsed = new ArrayList<>();
                for (JsonNode option : node.get("options").list()) {
                    parsed.add(new Option(option.get("label").asText(), effect(option)));
                }
                options = List.copyOf(parsed);
            }
            case EVENT -> effect = effect(node.get("effect"));
        }

        List<Source> sources = sources(node);

        return new CardDef(
                node.get("id").asText(),
                type,
                node.get("text").asText(),
                answers,
                correctIndex,
                reward,
                options,
                effect,
                node.get("fact").asText(),
                sources);
    }

    private static Effect effect(JsonNode node) {
        return new Effect(node.get("money").asInt(), node.get("gp").asInt(), node.get("meter").asInt());
    }
}
