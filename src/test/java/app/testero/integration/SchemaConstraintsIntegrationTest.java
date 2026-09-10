package app.testero.integration;

import app.testero.entity.assessment.AssessmentType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.context.ImportTestcontainers;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.containers.PostgreSQLContainer;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Schema invariants that no application code enforces, and whose absence is silent.
 *
 * <p>Both were real defects. The seed scripts guard their inserts with
 * {@code ON CONFLICT DO NOTHING}, which only fires against a unique index: with
 * uniqueness declared on the primary key alone and ids generated per statement,
 * the conflict never happened and every re-run duplicated the same subjects and
 * topics. And {@code type} defaulted to {@code 'CERTIFICATION'}, a value absent
 * from {@link AssessmentType}, so any row inserted without an explicit type could
 * not be read back by the application.
 */
@SpringBootTest
@ActiveProfiles("integration")
@ImportTestcontainers
class SchemaConstraintsIntegrationTest {

    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @Autowired
    JdbcTemplate jdbc;

    @Test
    @DisplayName("subject.label and topic.title are unique, so seed re-runs cannot duplicate them")
    void naturalKeysAreUnique() {
        assertThat(uniqueColumns("subject")).as("unique columns on subject").contains("label");
        assertThat(uniqueColumns("topic")).as("unique columns on topic").contains("title");
    }

    @Test
    @DisplayName("every default on an assessment type column is a value of AssessmentType")
    void typeDefaultsAreValidEnumValues() {
        for (String table : List.of("assessment_template", "assessment_snapshot")) {
            String raw = jdbc.queryForObject("""
                    SELECT column_default FROM information_schema.columns
                    WHERE table_name = ? AND column_name = 'type'
                    """, String.class, table);

            assertThat(raw).as("%s.type has a default", table).isNotNull();

            // il default arriva come "'VALORE'::character varying"
            String value = raw.replaceAll("^'|'::.*$", "");
            assertThat(AssessmentType.values())
                    .as("%s.type default '%s' must be an AssessmentType", table, value)
                    .anyMatch(t -> t.name().equals(value));
        }
    }

    private List<String> uniqueColumns(String table) {
        return jdbc.queryForList("""
                SELECT a.attname
                FROM pg_index i
                JOIN pg_class t ON t.oid = i.indrelid
                JOIN pg_attribute a ON a.attrelid = t.oid AND a.attnum = ANY (i.indkey)
                WHERE i.indisunique AND t.relname = ?
                """, String.class, table);
    }
}
