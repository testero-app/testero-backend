package app.testero.service.assessment;

import app.testero.dto.assessment.TopicChaptersResponse;
import app.testero.dto.assessment.TopicChaptersResponse.AvailableQuestions;
import app.testero.dto.assessment.TopicListResponse;
import app.testero.dto.assessment.TopicListResponse.ChapterItem;
import app.testero.dto.assessment.TopicListResponse.QuestionCounts;
import app.testero.dto.assessment.TopicListResponse.TopicItem;
import app.testero.exception.ResourceNotFoundException;
import app.testero.entity.assessment.Difficulty;
import app.testero.entity.assessment.Topic;
import app.testero.entity.assessment.TopicSubject;
import app.testero.entity.assessment.QuestionTemplateSubject;
import app.testero.entity.assessment.QuestionTemplate;
import app.testero.entity.assessment.Subject;
import app.testero.repository.assessment.QuestionTemplateRepository;
import app.testero.repository.assessment.QuestionTemplateSubjectRepository;
import app.testero.repository.assessment.SubjectRepository;
import app.testero.repository.assessment.TopicRepository;
import app.testero.repository.assessment.TopicSubjectRepository;
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
    private final QuestionTemplateSubjectRepository questionTemplateSubjectRepository;
    private final QuestionTemplateRepository questionTemplateRepository;

    public TopicService(TopicRepository topicRepository,
                        TopicSubjectRepository topicSubjectRepository,
                        SubjectRepository subjectRepository,
                        QuestionTemplateSubjectRepository questionTemplateSubjectRepository,
                        QuestionTemplateRepository questionTemplateRepository) {
        this.topicRepository = topicRepository;
        this.topicSubjectRepository = topicSubjectRepository;
        this.subjectRepository = subjectRepository;
        this.questionTemplateSubjectRepository = questionTemplateSubjectRepository;
        this.questionTemplateRepository = questionTemplateRepository;
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
        List<QuestionTemplateSubject> allQs = questionTemplateSubjectRepository
                .findBySubjectIdIn(new ArrayList<>(allSubjectIds));

        // Collect all question IDs and fetch questions for difficulty info
        Set<UUID> allQuestionIds = allQs.stream()
                .map(QuestionTemplateSubject::getQuestionTemplateId)
                .collect(Collectors.toSet());
        Map<UUID, Difficulty> questionDifficultyMap = questionTemplateRepository
                .findAllById(allQuestionIds).stream()
                .collect(Collectors.toMap(QuestionTemplate::getId,
                        q -> q.getDifficulty() != null
                                ? q.getDifficulty() : Difficulty.BEGINNER));

        // Group question-subject links by subject
        Map<UUID, List<QuestionTemplateSubject>> qsBySubject = allQs.stream()
                .collect(Collectors.groupingBy(QuestionTemplateSubject::getSubjectId));

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
                if (subject == null) {
                    continue;
                }

                List<QuestionTemplateSubject> qs = qsBySubject.getOrDefault(link.getSubjectId(), List.of());
                int base = 0;
                int inter = 0;
                int adv = 0;
                for (QuestionTemplateSubject q : qs) {
                    Difficulty d = questionDifficultyMap.getOrDefault(q.getQuestionTemplateId(), Difficulty.BEGINNER);
                    switch (d) {
                        case BEGINNER -> base++;
                        case INTERMEDIATE -> inter++;
                        case ADVANCED -> adv++;
                        default -> base++;
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

    @Transactional(readOnly = true)
    public TopicChaptersResponse getTopicChapters(UUID topicId) {
        Topic topic = topicRepository.findById(topicId)
                .filter(Topic::isEnabled)
                .orElseThrow(() -> new ResourceNotFoundException("Topic not found"));

        List<TopicSubject> links = topicSubjectRepository.findByTopicIdOrderByPositionAsc(topicId);
        Set<UUID> subjectIds = links.stream()
                .map(TopicSubject::getSubjectId)
                .collect(Collectors.toSet());

        Map<UUID, Subject> subjectMap = subjectRepository.findAllById(subjectIds)
                .stream()
                .collect(Collectors.toMap(Subject::getId, s -> s));

        List<QuestionTemplateSubject> allQs = questionTemplateSubjectRepository
                .findBySubjectIdIn(new ArrayList<>(subjectIds));
        Set<UUID> questionIds = allQs.stream()
                .map(QuestionTemplateSubject::getQuestionTemplateId)
                .collect(Collectors.toSet());
        Map<UUID, Difficulty> diffMap = questionTemplateRepository.findAllById(questionIds)
                .stream()
                .collect(Collectors.toMap(QuestionTemplate::getId,
                        q -> q.getDifficulty() != null ? q.getDifficulty() : Difficulty.BEGINNER));

        Map<UUID, List<QuestionTemplateSubject>> qsBySubject = allQs.stream()
                .collect(Collectors.groupingBy(QuestionTemplateSubject::getSubjectId));

        int totalBase = 0;
        int totalInter = 0;
        int totalAdv = 0;
        List<ChapterItem> chapters = new ArrayList<>();
        for (TopicSubject link : links) {
            Subject subject = subjectMap.get(link.getSubjectId());
            if (subject == null) {
                continue;
            }

            List<QuestionTemplateSubject> qs = qsBySubject.getOrDefault(link.getSubjectId(), List.of());
            int base = 0;
            int inter = 0;
            int adv = 0;
            for (QuestionTemplateSubject q : qs) {
                Difficulty d = diffMap.getOrDefault(q.getQuestionTemplateId(), Difficulty.BEGINNER);
                switch (d) {
                    case BEGINNER -> base++;
                    case INTERMEDIATE -> inter++;
                    case ADVANCED -> adv++;
                    default -> base++;
                }
            }
            totalBase += base;
            totalInter += inter;
            totalAdv += adv;
            chapters.add(new ChapterItem(subject.getId().toString(), subject.getLabel(),
                    new QuestionCounts(base, inter, adv)));
        }

        return new TopicChaptersResponse(
                topic.getId().toString(),
                topic.getTitle(),
                chapters,
                new AvailableQuestions(totalBase, totalInter, totalAdv,
                        totalBase + totalInter + totalAdv)
        );
    }
}
