package app.testero.service;

import app.testero.dto.AssessmentQuestionsResponse.OptionDto;
import app.testero.dto.AssessmentQuestionsResponse.QuestionDto;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.*;

class QuestionPrepServiceTest {

    private final QuestionPrepService service = new QuestionPrepService();

    // ── Helpers ────────────────────────────────────────────────────

    private static QuestionDto mc(String id, List<OptionDto> options) {
        return new QuestionDto(id, "multiple", "Text " + id, null, options);
    }

    private static QuestionDto mc(String id) {
        return mc(id, List.of(
                new OptionDto("opt-" + id + "-1", "A", false),
                new OptionDto("opt-" + id + "-2", "B", false),
                new OptionDto("opt-" + id + "-3", "C", false)
        ));
    }

    private static QuestionDto open(String id) {
        return new QuestionDto(id, "open", "Text " + id, null, null);
    }

    // ── Pool selection ─────────────────────────────────────────────

    @Nested
    @DisplayName("pool selection")
    class PoolSelection {

        @Test
        @DisplayName("selects correct number when pool > count")
        void poolLargerThanCount() {
            List<QuestionDto> pool = new ArrayList<>(List.of(mc("1"), mc("2"), mc("3"), mc("4"), mc("5")));

            List<QuestionDto> result = service.prepare(pool, 3);

            assertThat(result).hasSize(3);
        }

        @Test
        @DisplayName("returns all questions when pool == count")
        void poolEqualToCount() {
            List<QuestionDto> pool = new ArrayList<>(List.of(mc("1"), mc("2"), mc("3")));

            List<QuestionDto> result = service.prepare(pool, 3);

            assertThat(result).hasSize(3);
            assertThat(result).extracting(QuestionDto::id)
                    .containsExactlyInAnyOrder("1", "2", "3");
        }

        @Test
        @DisplayName("returns all questions when pool < count (no error)")
        void poolSmallerThanCount() {
            List<QuestionDto> pool = new ArrayList<>(List.of(mc("1"), mc("2")));

            List<QuestionDto> result = service.prepare(pool, 5);

            assertThat(result).hasSize(2);
        }
    }

    // ── Question ordering ──────────────────────────────────────────

    @Nested
    @DisplayName("question ordering")
    class QuestionOrdering {

        @Test
        @DisplayName("open questions are placed after MC questions")
        void openAfterMc() {
            List<QuestionDto> pool = new ArrayList<>(List.of(
                    open("o1"), mc("m1"), open("o2"), mc("m2")
            ));

            List<QuestionDto> result = service.prepare(pool, 4);

            // First two should be MC (in any order), last two should be open
            assertThat(result.subList(0, 2)).allMatch(q -> "multiple".equals(q.type()));
            assertThat(result.subList(2, 4)).allMatch(q -> "open".equals(q.type()));
        }

        @RepeatedTest(10)
        @DisplayName("MC questions contain same elements (possibly shuffled)")
        void mcQuestionsShuffled() {
            List<QuestionDto> pool = new ArrayList<>(List.of(
                    mc("1"), mc("2"), mc("3"), mc("4")
            ));

            List<QuestionDto> result = service.prepare(pool, 4);

            assertThat(result).extracting(QuestionDto::id)
                    .containsExactlyInAnyOrder("1", "2", "3", "4");
        }

        @Test
        @DisplayName("all open questions — no error, all returned")
        void allOpen() {
            List<QuestionDto> pool = new ArrayList<>(List.of(open("o1"), open("o2"), open("o3")));

            List<QuestionDto> result = service.prepare(pool, 3);

            assertThat(result).hasSize(3);
            assertThat(result).allMatch(q -> "open".equals(q.type()));
        }
    }

    // ── Option shuffling ───────────────────────────────────────────

    @Nested
    @DisplayName("option shuffling")
    class OptionShuffling {

        @RepeatedTest(10)
        @DisplayName("MC options contain same elements (possibly reordered)")
        void optionsPreserved() {
            List<QuestionDto> pool = new ArrayList<>(List.of(mc("1")));

            List<QuestionDto> result = service.prepare(pool, 1);

            assertThat(result.get(0).options())
                    .extracting(OptionDto::id)
                    .containsExactlyInAnyOrder("opt-1-1", "opt-1-2", "opt-1-3");
        }

        @RepeatedTest(20)
        @DisplayName("fallback option always stays last")
        void fallbackStaysLast() {
            List<OptionDto> options = List.of(
                    new OptionDto("a", "Option A", false),
                    new OptionDto("b", "Option B", false),
                    new OptionDto("c", "Option C", false),
                    new OptionDto("fb", "Nessuna delle precedenti", true)
            );
            List<QuestionDto> pool = new ArrayList<>(List.of(mc("1", options)));

            List<QuestionDto> result = service.prepare(pool, 1);

            List<OptionDto> resultOpts = result.get(0).options();
            assertThat(resultOpts).hasSize(4);
            assertThat(resultOpts.get(3).id()).isEqualTo("fb");
            assertThat(resultOpts.get(3).isFallback()).isTrue();
        }

        @Test
        @DisplayName("open questions have null options — untouched")
        void openOptionsNull() {
            List<QuestionDto> pool = new ArrayList<>(List.of(open("o1")));

            List<QuestionDto> result = service.prepare(pool, 1);

            assertThat(result.get(0).options()).isNull();
        }
    }

    // ── Edge cases ─────────────────────────────────────────────────

    @Nested
    @DisplayName("edge cases")
    class EdgeCases {

        @Test
        @DisplayName("empty list returns empty")
        void emptyList() {
            List<QuestionDto> result = service.prepare(new ArrayList<>(), 5);

            assertThat(result).isEmpty();
        }
    }
}
