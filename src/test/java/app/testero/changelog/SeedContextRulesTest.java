package app.testero.changelog;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.yaml.snakeyaml.Yaml;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Structural rules over the Liquibase changelogs.
 *
 * <p>A changeset that declares no {@code context} runs under <em>every</em> context,
 * production included. That makes it dangerously easy to ship development seed data
 * to prod by simply forgetting an attribute. These tests pin the convention so the
 * mistake is caught for every seed we add from now on, not just the current ones.
 *
 * <ul>
 *   <li>{@code db/seed/dev/**} is developer-only data → must be gated on {@code dev}.</li>
 *   <li>{@code db/seed/content/**} is product content → must ship to {@code prod}.</li>
 * </ul>
 */
class SeedContextRulesTest {

    private static final Path CHANGELOG_ROOT =
            Path.of("src/main/resources/db/changelog");

    /** A changeset paired with the seed files it applies. */
    private record SeedChangeSet(String file, String id, String contexts, List<String> seedPaths) {

        boolean referencesSeedDir(String dir) {
            return seedPaths.stream().anyMatch(p -> p.startsWith(dir));
        }

        List<String> contextList() {
            return contexts == null || contexts.isBlank()
                    ? List.of()
                    : Stream.of(contexts.split(",")).map(String::trim).toList();
        }
    }

    @Test
    @DisplayName("every changeset seeding db/seed/dev/** is gated on the dev context")
    void devSeedsAreDevOnly() {
        var offenders = seedChangeSets().stream()
                .filter(cs -> cs.referencesSeedDir("db/seed/dev/"))
                .filter(cs -> !cs.contextList().equals(List.of("dev")))
                .map(cs -> cs.file() + "::" + cs.id() + " → context=" + cs.contexts())
                .toList();

        assertThat(offenders)
                .as("developer seed data must never reach a non-dev environment; "
                        + "declare `context: dev` on these changesets")
                .isEmpty();
    }

    @Test
    @DisplayName("every changeset seeding db/seed/content/** ships to the prod context")
    void contentSeedsReachProd() {
        var contentSeeds = seedChangeSets().stream()
                .filter(cs -> cs.referencesSeedDir("db/seed/content/"))
                .toList();

        assertThat(contentSeeds)
                .as("expected at least one product-content seed changeset")
                .isNotEmpty();

        var offenders = contentSeeds.stream()
                .filter(cs -> !cs.contextList().contains("prod"))
                .map(cs -> cs.file() + "::" + cs.id() + " → context=" + cs.contexts())
                .toList();

        assertThat(offenders)
                .as("product content must ship to production; declare `prod` in the context")
                .isEmpty();
    }

    @Test
    @DisplayName("every seed file referenced by a changeset exists on disk")
    void seedFilesExist() {
        var missing = seedChangeSets().stream()
                .flatMap(cs -> cs.seedPaths().stream())
                .distinct()
                .filter(p -> !Files.exists(Path.of("src/main/resources").resolve(p)))
                .toList();

        assertThat(missing).as("changelogs reference seed files that do not exist").isEmpty();
    }

    // ── changelog parsing ──────────────────────────────────────────

    /** All changesets that apply at least one sqlFile under db/seed/. */
    private static List<SeedChangeSet> seedChangeSets() {
        var result = new ArrayList<SeedChangeSet>();

        try (Stream<Path> files = Files.walk(CHANGELOG_ROOT)) {
            for (Path file : files.filter(p -> p.toString().endsWith(".yaml")).toList()) {
                for (Map<String, Object> changeSet : changeSetsIn(file)) {
                    var seedPaths = seedPathsIn(changeSet);
                    if (!seedPaths.isEmpty()) {
                        result.add(new SeedChangeSet(
                                CHANGELOG_ROOT.relativize(file).toString(),
                                String.valueOf(changeSet.get("id")),
                                (String) changeSet.get("context"),
                                seedPaths));
                    }
                }
            }
        } catch (IOException e) {
            throw new IllegalStateException("cannot read changelogs under " + CHANGELOG_ROOT, e);
        }

        assertThat(result).as("no seed changesets found — is the changelog path still correct?")
                .isNotEmpty();
        return result;
    }

    @SuppressWarnings("unchecked")
    private static List<Map<String, Object>> changeSetsIn(Path file) throws IOException {
        try (InputStream in = Files.newInputStream(file)) {
            Map<String, Object> root = new Yaml().load(in);
            if (root == null || !(root.get("databaseChangeLog") instanceof List<?> entries)) {
                return List.of();
            }
            return entries.stream()
                    .map(e -> (Map<String, Object>) e)
                    .filter(e -> e.containsKey("changeSet"))
                    .map(e -> (Map<String, Object>) e.get("changeSet"))
                    .toList();
        }
    }

    /** The `path` of every sqlFile change pointing under db/seed/. */
    @SuppressWarnings("unchecked")
    private static List<String> seedPathsIn(Map<String, Object> changeSet) {
        if (!(changeSet.get("changes") instanceof List<?> changes)) {
            return List.of();
        }
        return changes.stream()
                .map(c -> (Map<String, Object>) c)
                .filter(c -> c.get("sqlFile") instanceof Map)
                .map(c -> (Map<String, Object>) c.get("sqlFile"))
                .map(sqlFile -> (String) sqlFile.get("path"))
                .filter(path -> path != null && path.startsWith("db/seed/"))
                .toList();
    }
}
