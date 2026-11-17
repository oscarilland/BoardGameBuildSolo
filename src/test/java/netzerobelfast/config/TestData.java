package netzerobelfast.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.Consumer;

/**
 * Test helper: copies the real data files into a temporary folder, so a test can change or
 * break one value and load the result with {@link GameConfig#load(Path)}.
 *
 * <p>Making each broken file from the real one keeps every test to a single, visible change.
 */
final class TestData {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();

    private TestData() {}

    /** Copies the three packaged data files into {@code folder} and returns it. */
    static Path copyInto(Path folder) {
        for (String name : new String[] {GameConfig.CONFIG_FILE, GameConfig.BOARD_FILE, GameConfig.CARDS_FILE}) {
            try (InputStream in = TestData.class.getResourceAsStream("/data/" + name)) {
                Files.write(folder.resolve(name), in.readAllBytes());
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        }
        return folder;
    }

    /** Reads one file as a JSON object, applies {@code change}, and writes it back. */
    static void edit(Path folder, String file, Consumer<JsonObject> change) {
        try {
            Path path = folder.resolve(file);
            JsonObject root = JsonParser.parseString(Files.readString(path, StandardCharsets.UTF_8)).getAsJsonObject();
            change.accept(root);
            Files.writeString(path, GSON.toJson(root), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    /** Replaces a file's whole text, for malformed-JSON tests. */
    static void write(Path folder, String file, String text) {
        try {
            Files.writeString(folder.resolve(file), text, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    // Small shortcuts for reaching into the JSON.

    static JsonObject obj(JsonObject root, String... path) {
        JsonObject node = root;
        for (String key : path) {
            node = node.getAsJsonObject(key);
        }
        return node;
    }

    static JsonObject square(JsonObject board, int index) {
        return board.getAsJsonArray("squares").get(index).getAsJsonObject();
    }

    static JsonObject card(JsonObject cards, int index) {
        return cards.getAsJsonArray("cards").get(index).getAsJsonObject();
    }

    static JsonObject character(JsonObject config, int index) {
        return config.getAsJsonArray("characters").get(index).getAsJsonObject();
    }
}
