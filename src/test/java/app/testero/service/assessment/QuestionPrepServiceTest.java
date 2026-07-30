package app.testero.service.assessment;

import app.testero.dto.assessment.AssessmentQuestionsResponse.OptionDto;
import app.testero.dto.assessment.AssessmentQuestionsResponse.QuestionDto;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.*;

class QuestionPrepServiceTest {

    private static final long SEED = 7L;

    private final QuestionPrepService service = new QuestionPrepService();

    // ── Helpers ────────────────────────────────────────────────────

    /** Prepare with both shuffle flags on and a fixed seed (the common case). */
    private List<QuestionDto> prep(List<QuestionDto> pool, int count) {
        return service.prepare(pool, count, true, true, SEED);
    }

    private static QuestionDto mc(String id, List<OptionDto> options) {
        return new QuestionDto(id, "multiple", "Text " + id, null, options, null, List.of());
    }

    private static QuestionDto mc(String id) {
        return mc(id, List.of(
                new OptionDto("opt-" + id + "-1", "A", false),
                new OptionDto("opt-" + id + "-2", "B", false),
                new OptionDto("opt-" + id + "-3", "C", false)
        ));
    }

    private static QuestionDto open(String id) {
        return new QuestionDto(id, "open", "Text " + id, null, null, null, List.of());
    }

    private static List<QuestionDto> mcPool(String... ids) {
        List<QuestionDto> pool = new ArrayList<>();
        for (String id : ids) {
            pool.add(mc(id));
        }
        return pool;
    }

    private static List<String> ids(List<QuestionDto> qs) {
        return qs.stream().map(QuestionDto::id).toList();
    }

    private static List<String> optionIds(List<QuestionDto> qs) {
        return qs.stream()
                .flatMap(q -> q.options() == null ? Stream.empty() : q.options().stream())
                .map(OptionDto::id).toList();
    }

    // ── Pool selection ─────────────────────────────────────────────

    @Nested
    @DisplayName("pool selection")
    class PoolSelection {

        @Test
        @DisplayName("selects correct number when pool > count")
        void poolLargerThanCount() {
            List<QuestionDto> pool = mcPool("1", "2", "3", "4", "5");

            List<QuestionDto> result = prep(pool, 3);

            assertThat(result).hasSize(3);
        }

        @Test
        @DisplayName("returns all questions when pool == count")
        void poolEqualToCount() {
            List<QuestionDto> pool = mcPool("1", "2", "3");

            List<QuestionDto> result = prep(pool, 3);

            assertThat(result).hasSize(3);
            assertThat(result).extracting(QuestionDto::id)
                    .containsExactlyInAnyOrder("1", "2", "3");
        }

        @Test
        @DisplayName("returns all questions when pool < count (no error)")
        void poolSmallerThanCount() {
            List<QuestionDto> pool = mcPool("1", "2");

            List<QuestionDto> result = prep(pool, 5);

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

            List<QuestionDto> result = prep(pool, 4);

            // First two should be MC (in any order), last two should be open
            assertThat(result.subList(0, 2)).allMatch(q -> "multiple".equals(q.type()));
            assertThat(result.subList(2, 4)).allMatch(q -> "open".equals(q.type()));
        }

        @Test
        @DisplayName("MC questions contain the same elements (possibly shuffled)")
        void mcQuestionsShuffled() {
            List<QuestionDto> pool = mcPool("1", "2", "3", "4");

            List<QuestionDto> result = prep(pool, 4);

            assertThat(result).extracting(QuestionDto::id)
                    .containsExactlyInAnyOrder("1", "2", "3", "4");
        }

        @Test
        @DisplayName("all open questions — no error, all returned")
        void allOpen() {
            List<QuestionDto> pool = new ArrayList<>(List.of(open("o1"), open("o2"), open("o3")));

            List<QuestionDto> result = prep(pool, 3);

            assertThat(result).hasSize(3);
            assertThat(result).allMatch(q -> "open".equals(q.type()));
        }
    }

    // ── Option shuffling ───────────────────────────────────────────

    @Nested
    @DisplayName("option shuffling")
    class OptionShuffling {

        @Test
        @DisplayName("MC options contain the same elements (possibly reordered)")
        void optionsPreserved() {
            List<QuestionDto> pool = new ArrayList<>(List.of(mc("1")));

            List<QuestionDto> result = prep(pool, 1);

            assertThat(result.get(0).options())
                    .extracting(OptionDto::id)
                    .containsExactlyInAnyOrder("opt-1-1", "opt-1-2", "opt-1-3");
        }

        @Test
        @DisplayName("fallback option always stays last when options are shuffled")
        void fallbackStaysLast() {
            List<OptionDto> options = List.of(
                    new OptionDto("a", "Option A", false),
                    new OptionDto("b", "Option B", false),
                    new OptionDto("c", "Option C", false),
                    new OptionDto("fb", "Nessuna delle precedenti", true)
            );
            List<QuestionDto> pool = new ArrayList<>(List.of(mc("1", options)));

            List<QuestionDto> result = prep(pool, 1);

            List<OptionDto> resultOpts = result.get(0).options();
            assertThat(resultOpts).hasSize(4);
            assertThat(resultOpts.get(3).id()).isEqualTo("fb");
            assertThat(resultOpts.get(3).isFallback()).isTrue();
        }

        @Test
        @DisplayName("open questions have null options — untouched")
        void openOptionsNull() {
            List<QuestionDto> pool = new ArrayList<>(List.of(open("o1")));

            List<QuestionDto> result = prep(pool, 1);

            assertThat(result.get(0).options()).isNull();
        }
    }

    // ── Seed determinism ───────────────────────────────────────────

    @Nested
    @DisplayName("seed determinism")
    class SeedDeterminism {

        @Test
        @DisplayName("same seed produces an identical paper — question order and option order")
        void identicalForSameSeed() {
            List<QuestionDto> first = service.prepare(mcPool("1", "2", "3", "4", "5"), 3, true, true, 12345L);
            List<QuestionDto> second = service.prepare(mcPool("1", "2", "3", "4", "5"), 3, true, true, 12345L);

            assertThat(ids(first)).isEqualTo(ids(second));
            assertThat(optionIds(first)).isEqualTo(optionIds(second));
        }

        @Test
        @DisplayName("the seed actually drives the order — different seeds vary it")
        void seedDrivesOrder() {
            Set<List<String>> distinctOrders = new HashSet<>();
            for (long s = 0; s < 50; s++) {
                distinctOrders.add(ids(service.prepare(mcPool("1", "2", "3", "4"), 4, true, true, s)));
            }
            // A 4-element shuffle across 50 seeds virtually always yields more than one order.
            assertThat(distinctOrders.size()).isGreaterThan(1);
        }

        @Test
        @DisplayName("option order is independent per question (per-question sub-seed)")
        void optionOrderPerQuestion() {
            // Same option ids on two questions; a single global RNG would couple their order.
            List<OptionDto> optsA = List.of(
                    new OptionDto("x1", "A", false), new OptionDto("x2", "B", false),
                    new OptionDto("x3", "C", false), new OptionDto("x4", "D", false));
            List<OptionDto> optsB = List.of(
                    new OptionDto("y1", "A", false), new OptionDto("y2", "B", false),
                    new OptionDto("y3", "C", false), new OptionDto("y4", "D", false));
            List<QuestionDto> pool = new ArrayList<>(List.of(mc("qa", optsA), mc("qb", optsB)));

            List<QuestionDto> result = service.prepare(pool, 2, false, true, 999L);

            List<String> orderA = result.stream().filter(q -> q.id().equals("qa"))
                    .findFirst().orElseThrow().options().stream().map(o -> o.id().substring(1)).toList();
            List<String> orderB = result.stream().filter(q -> q.id().equals("qb"))
                    .findFirst().orElseThrow().options().stream().map(o -> o.id().substring(1)).toList();

            // Independent sub-seeds make it overwhelmingly likely the two orders differ.
            assertThat(orderA).isNotEqualTo(orderB);
        }
    }

    // ── Shuffle flags ──────────────────────────────────────────────

    @Nested
    @DisplayName("shuffle flags")
    class ShuffleFlags {

        @Test
        @DisplayName("shuffleQuestions=false keeps canonical question order")
        void questionsNotShuffled() {
            List<QuestionDto> pool = mcPool("1", "2", "3", "4");

            List<QuestionDto> result = service.prepare(pool, 4, false, true, SEED);

            assertThat(ids(result)).containsExactly("1", "2", "3", "4");
        }

        @Test
        @DisplayName("shuffleQuestions=false takes the first N in canonical order")
        void subsetIsFirstNWhenNotShuffled() {
            List<QuestionDto> pool = mcPool("1", "2", "3", "4", "5");

            List<QuestionDto> result = service.prepare(pool, 3, false, true, SEED);

            assertThat(ids(result)).containsExactly("1", "2", "3");
        }

        @Test
        @DisplayName("shuffleOptions=false keeps canonical option order")
        void optionsNotShuffled() {
            List<QuestionDto> pool = new ArrayList<>(List.of(mc("1")));

            List<QuestionDto> result = service.prepare(pool, 1, true, false, SEED);

            assertThat(result.get(0).options())
                    .extracting(OptionDto::id)
                    .containsExactly("opt-1-1", "opt-1-2", "opt-1-3");
        }
    }

    // ── Edge cases ─────────────────────────────────────────────────

    @Nested
    @DisplayName("edge cases")
    class EdgeCases {

        @Test
        @DisplayName("empty list returns empty")
        void emptyList() {
            List<QuestionDto> result = prep(new ArrayList<>(), 5);

            assertThat(result).isEmpty();
        }
    }
}
