package netzerobelfast.config;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

/**
 * A position in a parsed JSON file: the Gson element plus the file name and the path to it,
 * such as {@code squares[7].price}.
 *
 * <p>Every read checks the type and throws a {@link ConfigException} naming the file and
 * path if it's wrong or missing. This is why the loader walks Gson's tree instead of letting
 * Gson fill records directly: Gson would silently put 0 or null into a missing field.
 */
final class JsonNode {

    private final String file;
    private final String path;
    private final JsonElement element;

    private JsonNode(String file, String path, JsonElement element) {
        this.file = file;
        this.path = path;
        this.element = element;
    }

    static JsonNode root(String file, JsonElement element) {
        return new JsonNode(file, "", element);
    }

    String file() {
        return file;
    }

    String path() {
        return path.isEmpty() ? "(top level)" : path;
    }

    ConfigException error(String problem) {
        return new ConfigException(file, path(), problem);
    }

    /** The child at {@code key}; throws if it's missing or null. */
    JsonNode get(String key) {
        JsonElement child = object().get(key);
        if (child == null || child.isJsonNull()) {
            throw new ConfigException(file, childPath(key), "is missing");
        }
        return new JsonNode(file, childPath(key), child);
    }

    /** True if {@code key} is present and not null. */
    boolean has(String key) {
        JsonElement child = object().get(key);
        return child != null && !child.isJsonNull();
    }

    /**
     * The object's keys, in sorted order, without comment keys. Keys starting with
     * {@code _} (such as {@code _note}) are comments in the data files.
     */
    Set<String> keys() {
        Set<String> keys = new TreeSet<>();
        for (String key : object().keySet()) {
            if (!key.startsWith("_")) {
                keys.add(key);
            }
        }
        return keys;
    }

    List<JsonNode> list() {
        if (!element.isJsonArray()) {
            throw error("should be a list");
        }
        JsonArray array = element.getAsJsonArray();
        List<JsonNode> items = new ArrayList<>(array.size());
        for (int i = 0; i < array.size(); i++) {
            items.add(new JsonNode(file, path + "[" + i + "]", array.get(i)));
        }
        return items;
    }

    List<Integer> intList() {
        List<Integer> values = new ArrayList<>();
        for (JsonNode item : list()) {
            values.add(item.asInt());
        }
        return List.copyOf(values);
    }

    List<Double> numberList() {
        List<Double> values = new ArrayList<>();
        for (JsonNode item : list()) {
            values.add(item.asNumber());
        }
        return List.copyOf(values);
    }

    boolean isBoolean() {
        return element.isJsonPrimitive() && element.getAsJsonPrimitive().isBoolean();
    }

    int asInt() {
        BigDecimal value = number("should be a whole number");
        try {
            return value.intValueExact();
        } catch (ArithmeticException e) {
            throw error("should be a whole number, but is " + value.toPlainString());
        }
    }

    double asNumber() {
        return number("should be a number").doubleValue();
    }

    boolean asBoolean() {
        if (isBoolean()) {
            return element.getAsBoolean();
        }
        throw error("should be true or false");
    }

    /** Text that may be empty. */
    String asString() {
        if (element.isJsonPrimitive() && element.getAsJsonPrimitive().isString()) {
            return element.getAsString();
        }
        throw error("should be text");
    }

    /** Text that must not be empty or only spaces. */
    String asText() {
        String text = asString();
        if (text.isBlank()) {
            throw error("should not be empty");
        }
        return text;
    }

    /** Reads an enum constant by its exact name, listing the allowed names if it doesn't match. */
    <E extends Enum<E>> E asEnum(Class<E> type) {
        String name = asString();
        for (E constant : type.getEnumConstants()) {
            if (constant.name().equals(name)) {
                return constant;
            }
        }
        List<String> allowed = new ArrayList<>();
        for (E constant : type.getEnumConstants()) {
            allowed.add(constant.name());
        }
        throw error("is '" + name + "', expected one of " + allowed);
    }

    private BigDecimal number(String problem) {
        if (element.isJsonPrimitive()) {
            JsonPrimitive primitive = element.getAsJsonPrimitive();
            if (primitive.isNumber()) {
                return primitive.getAsBigDecimal();
            }
        }
        throw error(problem);
    }

    private JsonObject object() {
        if (!element.isJsonObject()) {
            throw error("should be an object");
        }
        return element.getAsJsonObject();
    }

    private String childPath(String key) {
        return path.isEmpty() ? key : path + "." + key;
    }
}
