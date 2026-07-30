package app.testero.service.assessment;

import app.testero.dto.assessment.AssessmentQuestionsResponse.OptionDto;
import app.testero.dto.assessment.AssessmentQuestionsResponse.QuestionDto;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

@Service
public class QuestionPrepService {

    /**
     * Builds the question set for a single delivery: an optional random subset, question
     * ordering and option ordering. All randomness is driven by {@code seed}, so the same
     * seed always yields the identical paper — the caller passes the seed frozen on the
     * submission, making a reload or resume reproducible.
     *
     * <p>{@code shuffleQuestions} / {@code shuffleOptions} come from the snapshot: when a flag
     * is off the corresponding order stays canonical. With question shuffling off the subset
     * is the first {@code count} questions in canonical order, so the paper is identical for
     * everyone (an in-class EXAM); with it on the subset is drawn deterministically from the
     * seed.
     */
    public List<QuestionDto> prepare(List<QuestionDto> questions, int count,
                                     boolean shuffleQuestions, boolean shuffleOptions, long seed) {
        Random rnd = new Random(seed);
        List<QuestionDto> pool = new ArrayList<>(questions);

        // Select subset if pool > count. Deterministic per seed; canonical (first N) when
        // question shuffling is off, so everyone sits the same paper.
        if (pool.size() > count) {
            if (shuffleQuestions) {
                Collections.shuffle(pool, rnd);
            }
            pool = new ArrayList<>(pool.subList(0, count));
        }

        // Separate MC from open, order only MC, append open at end.
        List<QuestionDto> mc = pool.stream()
                .filter(q -> "multiple".equals(q.type()))
                .collect(Collectors.toCollection(ArrayList::new));
        List<QuestionDto> open = pool.stream()
                .filter(q -> "open".equals(q.type()))
                .collect(Collectors.toList());

        if (shuffleQuestions) {
            Collections.shuffle(mc, rnd);
        }

        List<QuestionDto> result = new ArrayList<>(mc.size() + open.size());
        for (QuestionDto q : mc) {
            result.add(new QuestionDto(q.id(), q.type(), q.text(), q.code(),
                    orderOptions(q.options(), shuffleOptions, seed, q.id()), q.points(), q.subjects()));
        }
        result.addAll(open);

        return result;
    }

    /**
     * Orders a question's options. When shuffling is off the canonical order is kept as-is.
     * When on, regular options are shuffled with a per-question sub-seed derived from the
     * submission seed and the question id hash — so each question's option order is stable and
     * independent of the others — and fallback options ("none of the above") are pinned last.
     */
    private List<OptionDto> orderOptions(List<OptionDto> options, boolean shuffle,
                                         long seed, String questionId) {
        if (options == null || options.isEmpty() || !shuffle) {
            return options;
        }

        // Separate regular options from fallback ("Nessuna delle precedenti")
        List<OptionDto> regular = new ArrayList<>();
        List<OptionDto> fallback = new ArrayList<>();

        for (OptionDto opt : options) {
            if (Boolean.TRUE.equals(opt.isFallback())) {
                fallback.add(opt);
            } else {
                regular.add(opt);
            }
        }

        Collections.shuffle(regular, new Random(seed ^ questionId.hashCode()));

        List<OptionDto> result = new ArrayList<>(regular);
        result.addAll(fallback);
        return result;
    }
}
