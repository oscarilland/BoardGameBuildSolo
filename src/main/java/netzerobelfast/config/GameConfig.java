package netzerobelfast.config;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.Strictness;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.List;

/**
 * The game's data, loaded once at start-up from the three JSON files (NFR-07, ADR-004).
 *
 * <p>Loading has three steps: Gson reads each file into a JSON tree (strictly, so a typo in
 * a file is an error, not a guess); {@link ConfigParser} turns the trees into records,
 * checking every key; {@link ConfigValidator} checks that the values make sense together.
 * Any problem throws a {@link ConfigException} naming the file and key.
 *
 * <p>Gson is used only inside this package. The rest of the game sees plain records.
 */
public final class GameConfig {

    public static final String CONFIG_FILE = "config.json";
    public static final String BOARD_FILE = "board.json";
    public static final String CARDS_FILE = "climate_cards.json";

    /** Where the working data files live inside the jar ({@code src/main/resources/data/}). */
    private static final String CLASSPATH_FOLDER = "/data/";

    private final Rules rules;
    private final BoardDef board;
    private final List<CardDef> cards;

    private GameConfig(Rules rules, BoardDef board, List<CardDef> cards) {
        this.rules = rules;
        this.board = board;
        this.cards = cards;
    }

    /** Loads the data files packaged with the game. */
    public static GameConfig load() {
        return load(name -> {
            try (InputStream in = GameConfig.class.getResourceAsStream(CLASSPATH_FOLDER + name)) {
                return in == null ? null : new String(in.readAllBytes(), StandardCharsets.UTF_8);
            }
        });
    }

    /** Loads the data files from a folder; used by tests to try changed or broken copies. */
    public static GameConfig load(Path folder) {
        return load(name -> {
            try {
                return Files.readString(folder.resolve(name), StandardCharsets.UTF_8);
            } catch (NoSuchFileException e) {
                return null;
            }
        });
    }

    private static GameConfig load(DataSource source) {
        Rules rules = ConfigParser.rules(parse(source, CONFIG_FILE));
        BoardDef board = ConfigParser.board(parse(source, BOARD_FILE));
        List<CardDef> cards = ConfigParser.cards(parse(source, CARDS_FILE));
        ConfigValidator.validate(rules, board, cards);
        return new GameConfig(rules, board, cards);
    }

    /** Reads one file into a JSON tree, strictly: no comments, unquoted keys or trailing text. */
    private static JsonNode parse(DataSource source, String file) {
        String text;
        try {
            text = source.read(file);
        } catch (IOException e) {
            throw new ConfigException(file, "can't be read (" + e.getMessage() + ")");
        }
        if (text == null) {
            throw new ConfigException(file, "not found");
        }
        try (JsonReader reader = new JsonReader(new StringReader(text))) {
            reader.setStrictness(Strictness.STRICT);
            JsonElement tree = new Gson().getAdapter(JsonElement.class).read(reader);
            if (reader.peek() != JsonToken.END_DOCUMENT) {
                throw new ConfigException(file, "is not valid JSON: unexpected text after the end");
            }
            return JsonNode.root(file, tree);
        } catch (IOException | JsonParseException | IllegalStateException e) {
            throw new ConfigException(file, "is not valid JSON: " + e.getMessage());
        }
    }

    public Rules rules() {
        return rules;
    }

    public BoardDef board() {
        return board;
    }

    public List<CardDef> cards() {
        return cards;
    }

    /** The GP for answering this quiz card correctly: its own reward, or the config default (Q-13). */
    public int quizReward(CardDef card) {
        return card.rewardGP() != null ? card.rewardGP() : rules.quizCorrectGP();
    }

    /** A one-line description of what was loaded, shown by the empty window and by {@code --check}. */
    public String summary() {
        return "Loaded " + board.squares().size() + " squares, " + cards.size() + " cards, "
                + rules.characters().size() + " characters";
    }

    /** Reads a data file's text by name; returns null if there's no such file. */
    @FunctionalInterface
    private interface DataSource {
        String read(String fileName) throws IOException;
    }
}
