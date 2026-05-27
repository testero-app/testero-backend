package app.testero.service;

import app.testero.dto.TestQuestionsResponse.OptionDto;
import app.testero.dto.TestQuestionsResponse.QuestionDto;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class QuestionPrepService {

    public List<QuestionDto> prepare(List<QuestionDto> questions, int count) {
        List<QuestionDto> pool = new ArrayList<>(questions);

        // Select random subset if pool > count
        if (pool.size() > count) {
            Collections.shuffle(pool);
            pool = new ArrayList<>(pool.subList(0, count));
        }

        // Separate MC from open, shuffle only MC, append open at end
        List<QuestionDto> mc = pool.stream()
                .filter(q -> "multiple".equals(q.type()))
                .collect(Collectors.toCollection(ArrayList::new));
        List<QuestionDto> open = pool.stream()
                .filter(q -> "open".equals(q.type()))
                .collect(Collectors.toList());

        Collections.shuffle(mc);

        List<QuestionDto> result = new ArrayList<>(mc.size() + open.size());
        for (QuestionDto q : mc) {
            result.add(new QuestionDto(q.id(), q.type(), q.text(), q.code(), shuffleOptions(q.options())));
        }
        for (QuestionDto q : open) {
            result.add(q);
        }

        return result;
    }

    private List<OptionDto> shuffleOptions(List<OptionDto> options) {
        if (options == null || options.isEmpty()) {
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

        Collections.shuffle(regular);

        List<OptionDto> result = new ArrayList<>(regular);
        result.addAll(fallback);
        return result;
    }
}
