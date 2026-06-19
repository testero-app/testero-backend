package app.testero.service;

import app.testero.dto.TopicListResponse;
import app.testero.dto.TopicListResponse.ChapterItem;
import app.testero.dto.TopicListResponse.QuestionCounts;
import app.testero.dto.TopicListResponse.TopicItem;
import app.testero.entity.assessment.Difficulty;
import app.testero.entity.assessment.Topic;
import app.testero.entity.assessment.TopicSubject;
import app.testero.entity.assessment.QuestionSubject;
import app.testero.entity.assessment.Question;
import app.testero.entity.assessment.Subject;
import app.testero.repository.QuestionRepository;
import app.testero.repository.QuestionSubjectRepository;
import app.testero.repository.SubjectRepository;
import app.testero.repository.TopicRepository;
import app.testero.repository.TopicSubjectRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class TopicService {

    private final TopicRepository topicRepository;
    private final TopicSubjectRepository topicSubjectRepository;
    private final SubjectRepository subjectRepository;
    private final QuestionSubjectRepository questionSubjectRepository;
    private final QuestionRepository questionRepository;

    public TopicService(TopicRepository topicRepository,
                        TopicSubjectRepository topicSubjectRepository,
                        SubjectRepository subjectRepository,
                        QuestionSubjectRepository questionSubjectRepository,
                        QuestionRepository questionRepository) {
        this.topicRepository = topicRepository;
        this.topicSubjectRepository = topicSubjectRepository;
        this.subjectRepository = subjectRepository;
        this.questionSubjectRepository = questionSubjectRepository;
        this.questionRepository = questionRepository;
    }

    @Transactional(readOnly = true)
    public TopicListResponse getTopics() {
        List<Topic> topics = topicRepository.findByEnabledTrueOrderByPositionAsc();
        if (topics.isEmpty()) {
            return new TopicListResponse(List.of());
        }

        // Batch-fetch all topic-subject links
        List<UUID> topicIds = topics.stream().map(Topic::getId).toList();
        List<TopicSubject> allLinks = topicSubjectRepository.findByTopicIdInOrderByPositionAsc(topicIds);

        // Collect all subject IDs
        Set<UUID> allSubjectIds = allLinks.stream()
                .map(TopicSubject::getSubjectId)
                .collect(Collectors.toSet());

        // Batch-fetch subjects
        Map<UUID, Subject> subjectMap = subjectRepository.findAllById(allSubjectIds)
                .stream()
                .collect(Collectors.toMap(Subject::getId, s -> s));

        // Batch-fetch all question-subject links for these subjects
        List<QuestionSubject> allQs = questionSubjectRepository.findBySubjectIdIn(new ArrayList<>(allSubjectIds));

        // Collect all question IDs and fetch questions for difficulty info
        Set<UUID> allQuestionIds = allQs.stream()
                .map(QuestionSubject::getQuestionId)
                .collect(Collectors.toSet());
        Map<UUID, Difficulty> questionDifficultyMap = questionRepository.findAllById(allQuestionIds)
                .stream()
                .collect(Collectors.toMap(Question::getId, q -> q.getDifficulty() != null ? q.getDifficulty() : Difficulty.BEGINNER));

        // Group question-subject links by subject
        Map<UUID, List<QuestionSubject>> qsBySubject = allQs.stream()
                .collect(Collectors.groupingBy(QuestionSubject::getSubjectId));

        // Group topic-subject links by topic
        Map<UUID, List<TopicSubject>> linksByTopic = new LinkedHashMap<>();
        for (TopicSubject link : allLinks) {
            linksByTopic.computeIfAbsent(link.getTopicId(), k -> new ArrayList<>()).add(link);
        }

        // Build response
        List<TopicItem> items = topics.stream().map(topic -> {
            List<TopicSubject> links = linksByTopic.getOrDefault(topic.getId(), List.of());
            int totalQuestions = 0;

            List<ChapterItem> chapters = new ArrayList<>();
            for (TopicSubject link : links) {
                Subject subject = subjectMap.get(link.getSubjectId());
                if (subject == null) continue;

                List<QuestionSubject> qs = qsBySubject.getOrDefault(link.getSubjectId(), List.of());
                int base = 0, inter = 0, adv = 0;
                for (QuestionSubject q : qs) {
                    Difficulty d = questionDifficultyMap.getOrDefault(q.getQuestionId(), Difficulty.BEGINNER);
                    switch (d) {
                        case BEGINNER -> base++;
                        case INTERMEDIATE -> inter++;
                        case ADVANCED, EXPERT -> adv++;
                    }
                }
                totalQuestions += base + inter + adv;
                chapters.add(new ChapterItem(
                        subject.getId().toString(),
                        subject.getLabel(),
                        new QuestionCounts(base, inter, adv)
                ));
            }

            return new TopicItem(
                    topic.getId().toString(),
                    topic.getTitle(),
                    topic.getAbbreviation(),
                    topic.getDescription(),
                    topic.isEnabled(),
                    chapters,
                    chapters.size(),
                    totalQuestions
            );
        }).toList();

        return new TopicListResponse(items);
    }
}
