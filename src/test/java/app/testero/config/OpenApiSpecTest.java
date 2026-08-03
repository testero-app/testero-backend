package app.testero.config;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.TestRestTemplate;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureTestRestTemplate;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.context.ImportTestcontainers;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.containers.PostgreSQLContainer;

/**
 * Guards the published API contract and keeps {@code docs/openapi.json} in step with the code.
 *
 * <p>The frontend generates its TypeScript types from that file (testero-web#134), so a DTO
 * change that is not reflected in the spec is a silent contract break. The file is rewritten on
 * every run and CI fails on an uncommitted diff — so {@code ./mvnw test} is all a developer has
 * to remember.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureTestRestTemplate
@ActiveProfiles("integration")
@ImportTestcontainers
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class OpenApiSpecTest {

    /** Request DTOs whose every field may legitimately be omitted. */
    private static final List<String> SCHEMAS_WITHOUT_REQUIRED = List.of("UpdateProfileRequest");

    private static final Path SPEC_FILE = Path.of("docs", "openapi.json");

    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @Autowired
    TestRestTemplate rest;

    private final ObjectMapper mapper = new ObjectMapper()
            .enable(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS)
            .enable(SerializationFeature.INDENT_OUTPUT);

    @Test
    @DisplayName("every schema declares what a client can rely on")
    void schemasDeclareRequiredFields() throws IOException {
        Map<String, Object> schemas = schemas(fetchSpec());

        List<String> withoutRequired = new ArrayList<>();
        schemas.forEach((name, schema) -> {
            Map<String, Object> model = asMap(schema);
            if (model.containsKey("properties") && !model.containsKey("required")) {
                withoutRequired.add(name);
            }
        });

        assertThat(withoutRequired).containsExactlyInAnyOrderElementsOf(SCHEMAS_WITHOUT_REQUIRED);
    }

    @Test
    @DisplayName("a response field that is always sent is required, even when it may be null")
    void responseFieldsAreRequiredAndNullabilityIsExplicit() throws IOException {
        Map<String, Object> schemas = schemas(fetchSpec());

        Map<String, Object> feedback = asMap(schemas.get("SubmissionFeedbackResponse"));
        assertThat(required(feedback)).contains(
                "id", "user_id", "assessment_snapshot_id", "started_at", "submitted_at",
                "score", "max_score", "passed", "passing_score", "answers", "subject_scores");

        // Null while the submission is in progress, or when the snapshot carries no threshold —
        // present in the payload either way.
        assertThat(property(feedback, "score")).containsEntry("nullable", true);
        assertThat(property(feedback, "passed")).containsEntry("nullable", true);
        assertThat(property(feedback, "id")).doesNotContainKey("nullable");
    }

    @Test
    @DisplayName("optional request fields stay optional")
    void requestFieldsMayBeOmitted() throws IOException {
        Map<String, Object> schemas = schemas(fetchSpec());

        assertThat(required(asMap(schemas.get("SaveAnswerRequest")))).containsExactly("type");
        assertThat(required(asMap(schemas.get("LoginRequest"))))
                .containsExactlyInAnyOrder("username", "password");
    }

    @Test
    @DisplayName("the spec does not depend on the host it was generated from")
    void specIsHostIndependent() throws IOException {
        String spec = mapper.writeValueAsString(fetchSpec());

        // Left to springdoc, "servers" carries the URL of the request that produced the spec —
        // a random port under test. That would make the committed file differ on every run and
        // turn the CI freshness check into noise.
        assertThat(spec).doesNotContain("localhost");
        assertThat(asMap(((List<?>) fetchSpec().get("servers")).get(0)))
                .containsEntry("url", "/api");
    }

    @Test
    @DisplayName("the committed spec matches the running application")
    void specFileIsUpToDate() throws IOException {
        String spec = mapper.writeValueAsString(fetchSpec()) + "\n";

        Files.createDirectories(SPEC_FILE.getParent());
        Files.writeString(SPEC_FILE, spec, StandardCharsets.UTF_8);

        // Nothing to assert here: the file is the assertion. CI runs `git diff --exit-code` on it,
        // which fails when a DTO changed without the spec being regenerated and committed.
        assertThat(SPEC_FILE).exists();
    }

    private Map<String, Object> fetchSpec() throws IOException {
        String body = rest.getForObject("/v3/api-docs", String.class);
        assertThat(body).isNotBlank();
        return mapper.readValue(body, new TypeReference<Map<String, Object>>() {});
    }

    private Map<String, Object> schemas(Map<String, Object> spec) {
        return asMap(asMap(spec.get("components")).get("schemas"));
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> asMap(Object value) {
        return (Map<String, Object>) value;
    }

    @SuppressWarnings("unchecked")
    private List<String> required(Map<String, Object> schema) {
        return (List<String>) schema.getOrDefault("required", List.of());
    }

    private Map<String, Object> property(Map<String, Object> schema, String name) {
        return asMap(asMap(schema.get("properties")).get(name));
    }
}
