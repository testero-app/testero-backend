package app.testero.service;

import app.testero.dto.TopicListResponse;
import app.testero.entity.assessment.Difficulty;
import app.testero.entity.assessment.Question;
import app.testero.entity.assessment.QuestionSubject;
import app.testero.entity.assessment.Subject;
import app.testero.entity.assessment.Topic;
import app.testero.entity.assessment.TopicSubject;
import app.testero.repository.QuestionRepository;
import app.testero.repository.QuestionSubjectRepository;
import app.testero.repository.SubjectRepository;
import app.testero.repository.TopicRepository;
import app.testero.repository.TopicSubjectRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TopicServiceTest {

    @Mock TopicRepository topicRepository;
    @Mock TopicSubjectRepository topicSubjectRepository;
    @Mock SubjectRepository subjectRepository;
    @Mock QuestionSubjectRepository questionSubjectRepository;
    @Mock QuestionRepository questionRepository;

    @InjectMocks TopicService topicService;

    private static final UUID TOPIC_ID = UUID.randomUUID();
    private static final UUID SUBJECT_1 = UUID.randomUUID();
    private static final UUID SUBJECT_2 = UUID.randomUUID();
    private static final UUID Q1 = UUID.randomUUID();
    private static final UUID Q2 = UUID.randomUUID();
    private static final UUID Q3 = UUID.randomUUID();

    private Topic topic;

    @BeforeEach
    void setUp() {
        topic = new Topic();
        topic.setId(TOPIC_ID);
        topic.setTitle("Fondamenti Python I");
        topic.setAbbreviation("Py");
        topic.setDescription("Variabili e tipi");
        topic.setEnabled(true);
        topic.setPosition(0);
    }

    @Test
    @DisplayName("getTopics returns topics with chapters and question counts")
    void getTopicsWithChapters() {
        when(topicRepository.findByEnabledTrueOrderByPositionAsc())
                .thenReturn(List.of(topic));

        TopicSubject ts1 = new TopicSubject();
        ts1.setTopicId(TOPIC_ID);
        ts1.setSubjectId(SUBJECT_1);
        ts1.setPosition(0);
        TopicSubject ts2 = new TopicSubject();
        ts2.setTopicId(TOPIC_ID);
        ts2.setSubjectId(SUBJECT_2);
        ts2.setPosition(1);
        when(topicSubjectRepository.findByTopicIdInOrderByPositionAsc(any()))
                .thenReturn(List.of(ts1, ts2));

        Subject s1 = new Subject();
        s1.setId(SUBJECT_1);
        s1.setLabel("Variabili e tipi");
        Subject s2 = new Subject();
        s2.setId(SUBJECT_2);
        s2.setLabel("Cicli");
        when(subjectRepository.findAllById(any())).thenReturn(List.of(s1, s2));

        QuestionSubject qs1 = new QuestionSubject();
        qs1.setQuestionId(Q1);
        qs1.setSubjectId(SUBJECT_1);
        QuestionSubject qs2 = new QuestionSubject();
        qs2.setQuestionId(Q2);
        qs2.setSubjectId(SUBJECT_1);
        QuestionSubject qs3 = new QuestionSubject();
        qs3.setQuestionId(Q3);
        qs3.setSubjectId(SUBJECT_2);
        when(questionSubjectRepository.findBySubjectIdIn(any()))
                .thenReturn(List.of(qs1, qs2, qs3));

        Question q1 = new Question();
        q1.setId(Q1);
        q1.setDifficulty(Difficulty.BEGINNER);
        Question q2 = new Question();
        q2.setId(Q2);
        q2.setDifficulty(Difficulty.INTERMEDIATE);
        Question q3 = new Question();
        q3.setId(Q3);
        q3.setDifficulty(Difficulty.ADVANCED);
        when(questionRepository.findAllById(any())).thenReturn(List.of(q1, q2, q3));

        TopicListResponse response = topicService.getTopics();

        assertThat(response.topics()).hasSize(1);
        var t = response.topics().getFirst();
        assertThat(t.title()).isEqualTo("Fondamenti Python I");
        assertThat(t.abbreviation()).isEqualTo("Py");
        assertThat(t.totalChapters()).isEqualTo(2);
        assertThat(t.totalQuestions()).isEqualTo(3);

        var ch1 = t.chapters().get(0);
        assertThat(ch1.label()).isEqualTo("Variabili e tipi");
        assertThat(ch1.questionCounts().base()).isEqualTo(1);
        assertThat(ch1.questionCounts().intermediate()).isEqualTo(1);
        assertThat(ch1.questionCounts().advanced()).isEqualTo(0);

        var ch2 = t.chapters().get(1);
        assertThat(ch2.label()).isEqualTo("Cicli");
        assertThat(ch2.questionCounts().advanced()).isEqualTo(1);
    }

    @Test
    @DisplayName("getTopics returns empty when no topics exist")
    void emptyTopics() {
        when(topicRepository.findByEnabledTrueOrderByPositionAsc())
                .thenReturn(List.of());

        TopicListResponse response = topicService.getTopics();
        assertThat(response.topics()).isEmpty();
    }
}
