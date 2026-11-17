import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

/**
 * WikiLint - mechanical checks for the Net Zero Belfast wiki in docs/.
 *
 *   java scripts/WikiLint.java
 *
 * Runs as a single-file Java program (Java 11 or later), so it needs no build tool.
 * Exits 0 when clean and 1 when it finds problems, so CI can fail on it.
 *
 * Ported from FeedWatchBuild's wiki-lint.php, without the public/private checks
 * (this repo is never published). It only checks structure. Judging content
 * (contradictions, stale claims, weak pages) is the agent's job: see docs/AGENTS.md.
 */
public class WikiLint {

    // Files that govern the wiki rather than being pages in it.
    static final Set<String> CONTROL_FILES =
            Set.of("index.md", "log.md", "AGENTS.md", "CLAUDE.md", "README.md", "_template.md");

    static final List<String> WIKI_STATUSES = List.of("draft", "agreed", "built");
    static final Map<String, List<String>> STATUSES_BY_TYPE = Map.ofEntries(
            Map.entry("overview", WIKI_STATUSES),
            Map.entry("context", WIKI_STATUSES),
            Map.entry("rules", WIKI_STATUSES),
            Map.entry("requirement", WIKI_STATUSES),
            Map.entry("architecture", WIKI_STATUSES),
            Map.entry("ux", WIKI_STATUSES),
            Map.entry("operation", WIKI_STATUSES),
            Map.entry("source", WIKI_STATUSES),
            Map.entry("decision", List.of("proposed", "accepted", "superseded", "rejected")),
            Map.entry("plan", List.of("draft", "approved", "done")));

    static final List<String> LOG_OPERATIONS = List.of(
            "ingest", "query", "spec", "decide", "plan", "build", "explain", "release", "lint", "repository");

    static final Pattern FRONTMATTER = Pattern.compile("\\A---\\R(.*?)\\R---\\R?(.*)\\z", Pattern.DOTALL);
    static final Pattern FIELD = Pattern.compile("^([a-z_]+):\\s*(.*?)\\s*(?:#.*)?$");
    static final Pattern FENCED_CODE = Pattern.compile("^(```|~~~).*?^\\1", Pattern.MULTILINE | Pattern.DOTALL);
    static final Pattern INLINE_CODE = Pattern.compile("`[^`\\n]*`");
    static final Pattern LINK = Pattern.compile("\\[[^\\]]*\\]\\(([^)\\s]+)(?:\\s+\"[^\"]*\")?\\)");
    static final Pattern EXTERNAL = Pattern.compile("^([a-z][a-z0-9+.-]*:|#)", Pattern.CASE_INSENSITIVE);

    record Page(Map<String, Object> fields, String body) {}

    final List<String> problems = new ArrayList<>();
    final Path repo;
    final Path docs;

    WikiLint(Path docs) {
        this.docs = docs;
        this.repo = docs.getParent();
    }

    public static void main(String[] args) throws IOException {
        Path docs = Paths.get("docs");
        if (!Files.isDirectory(docs)) {
            // Allow running from inside scripts/ as well as from the repo root.
            docs = Paths.get("..", "docs");
        }
        if (!Files.isDirectory(docs)) {
            System.err.println("docs/ folder not found. Run from the repo root: java scripts/WikiLint.java");
            System.exit(1);
        }
        WikiLint lint = new WikiLint(docs.toRealPath());
        int pageCount = lint.run();
        lint.problems.forEach(System.out::println);
        System.out.printf("wiki-lint: %d problem(s) across %d page(s)%n", lint.problems.size(), pageCount);
        System.exit(lint.problems.isEmpty() ? 0 : 1);
    }

    void report(String check, String path, String message) {
        problems.add(String.format("  [%s] %s: %s", check, path, message));
    }

    String rel(Path path) {
        return repo.relativize(path).toString().replace('\\', '/');
    }

    int run() throws IOException {
        Map<Path, Page> pages = new LinkedHashMap<>();
        Map<Path, List<Path>> linkedFrom = new HashMap<>();
        Path raw = docs.resolve("raw");

        List<Path> files;
        try (Stream<Path> walk = Files.walk(docs)) {
            files = walk.filter(p -> p.toString().endsWith(".md") && !p.startsWith(raw)).sorted().toList();
        }
        for (Path file : files) {
            pages.put(file.toRealPath(), parse(Files.readString(file, StandardCharsets.UTF_8)));
        }

        for (Map.Entry<Path, Page> entry : pages.entrySet()) {
            Path path = entry.getKey();
            Page page = entry.getValue();
            String name = path.getFileName().toString();
            String rel = rel(path);
            if (!CONTROL_FILES.contains(name)) {
                checkFrontmatter(rel, name, page.fields());
            }

            // --- Body: house style and links ---------------------------------
            String body = stripCode(page.body());
            if (body.contains("—")) {
                report("style", rel, "contains an em dash; use a hyphen");
            }
            if (body.contains("[[")) {
                report("links", rel, "uses [[wikilinks]]; use relative Markdown links");
            }
            Matcher m = LINK.matcher(body);
            while (m.find()) {
                String target = m.group(1);
                if (EXTERNAL.matcher(target).find()) {
                    continue; // external URL, mailto: or in-page anchor
                }
                String targetPath = URLDecoder.decode(target.replaceAll("[#?].*$", ""), StandardCharsets.UTF_8);
                Path resolved = path.getParent().resolve(targetPath).normalize();
                if (!Files.exists(resolved)) {
                    report("links", rel, "broken link -> " + target);
                    continue;
                }
                linkedFrom.computeIfAbsent(resolved.toRealPath(), k -> new ArrayList<>()).add(path);
            }
        }

        // --- Orphans and index coverage ---------------------------------------
        Path index = docs.resolve("wiki/index.md");
        for (Path path : pages.keySet()) {
            if (CONTROL_FILES.contains(path.getFileName().toString())) {
                continue;
            }
            List<Path> sources = linkedFrom.getOrDefault(path, List.of());
            if (sources.stream().noneMatch(from -> !from.equals(path))) {
                report("orphan", rel(path), "no other page links here");
            }
            if (!sources.contains(index)) {
                report("index", rel(path), "not listed in docs/wiki/index.md");
            }
        }

        checkLog(docs.resolve("wiki/log.md"));
        return pages.size();
    }

    void checkFrontmatter(String rel, String name, Map<String, Object> fields) {
        if (fields == null) {
            report("frontmatter", rel, "missing frontmatter block");
            return;
        }
        for (String key : List.of("title", "type", "status", "updated")) {
            Object value = fields.get(key);
            if (value == null || "".equals(value) || (value instanceof List<?> list && list.isEmpty())) {
                report("frontmatter", rel, "missing '" + key + "'");
            }
        }
        String type = fields.get("type") instanceof String s ? s : "";
        String status = fields.get("status") instanceof String s ? s : "";
        if (!type.isEmpty() && !STATUSES_BY_TYPE.containsKey(type)) {
            report("frontmatter", rel, "unknown type '" + type + "'");
        } else if (!type.isEmpty() && !status.isEmpty() && !STATUSES_BY_TYPE.get(type).contains(status)) {
            report("frontmatter", rel, "status '" + status + "' is not valid for type '" + type + "'");
        }
        if (fields.get("updated") instanceof String updated && !updated.isEmpty()) {
            try {
                LocalDate.parse(updated);
            } catch (DateTimeParseException e) {
                report("frontmatter", rel, "updated '" + updated + "' is not a valid YYYY-MM-DD date");
            }
        }
        String id = fields.get("id") instanceof String s ? s : "";
        if (type.equals("requirement")) {
            if (!id.matches("(FR|NFR)-\\d{2}")) {
                report("frontmatter", rel, "requirement id '" + id + "' should look like FR-01 or NFR-01");
            } else if (!name.startsWith(id.toLowerCase() + "-")) {
                report("naming", rel, "file name should start with " + id.toLowerCase() + "-");
            }
            if (!List.of("must", "should", "could").contains(fields.getOrDefault("priority", ""))) {
                report("frontmatter", rel, "priority must be 'must', 'should' or 'could'");
            }
        }
        for (String[] numbered : new String[][] {{"decision", "ADR"}, {"plan", "PLAN"}}) {
            if (type.equals(numbered[0])) {
                String number = name.length() >= 3 ? name.substring(0, 3) : "";
                if (!number.matches("\\d{3}") || !id.equals(numbered[1] + "-" + number)) {
                    report("frontmatter", rel, "id should be " + numbered[1] + "-NNN matching the file number");
                }
            }
        }
    }

    void checkLog(Path log) throws IOException {
        if (!Files.isRegularFile(log)) {
            report("log", "docs/wiki/log.md", "missing");
            return;
        }
        Pattern heading = Pattern.compile(
                "^## \\[(\\d{4}-\\d{2}-\\d{2})\\] (" + String.join("|", LOG_OPERATIONS) + ") \\| \\S.*$");
        String previous = "";
        List<String> lines = Files.readAllLines(log, StandardCharsets.UTF_8);
        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i);
            if (!line.startsWith("## ")) {
                continue;
            }
            Matcher m = heading.matcher(line);
            if (!m.matches()) {
                report("log", "docs/wiki/log.md:" + (i + 1), "heading should be '## [YYYY-MM-DD] operation | title'");
                continue;
            }
            if (m.group(1).compareTo(previous) < 0) {
                report("log", "docs/wiki/log.md:" + (i + 1), "entries must be in date order (append new entries at the end)");
            }
            previous = m.group(1);
        }
    }

    /**
     * Reads the YAML frontmatter block. Supports the subset the wiki uses:
     * {@code key: value} and {@code key: [a, b]}, with trailing {@code # comments} ignored.
     */
    static Page parse(String text) {
        Matcher m = FRONTMATTER.matcher(text);
        if (!m.matches()) {
            return new Page(null, text);
        }
        Map<String, Object> fields = new HashMap<>();
        for (String line : m.group(1).split("\\R")) {
            Matcher kv = FIELD.matcher(line);
            if (!kv.matches()) {
                continue;
            }
            String value = kv.group(2);
            if (value.startsWith("[") && value.endsWith("]")) {
                List<String> items = new ArrayList<>();
                for (String item : value.substring(1, value.length() - 1).split(",")) {
                    if (!item.isBlank()) {
                        items.add(item.trim());
                    }
                }
                fields.put(kv.group(1), items);
            } else {
                fields.put(kv.group(1), value);
            }
        }
        return new Page(fields, m.group(2));
    }

    /** Removes fenced code blocks and inline code so example links and syntax aren't checked. */
    static String stripCode(String body) {
        return INLINE_CODE.matcher(FENCED_CODE.matcher(body).replaceAll("")).replaceAll("");
    }
}
