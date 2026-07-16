package app.testero.service;

import app.testero.dto.TagResponse;
import app.testero.dto.TaggedQuestionResponse;
import app.testero.entity.assessment.QuestionTemplate;
import app.testero.entity.tag.QuestionTag;
import app.testero.entity.tag.QuestionTagId;
import app.testero.entity.tag.Tag;
import app.testero.exception.ConflictException;
import app.testero.exception.ResourceNotFoundException;
import app.testero.repository.QuestionTagRepository;
import app.testero.repository.QuestionTemplateRepository;
import app.testero.repository.TagRepository;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Manages a teacher's private tag vocabulary and the tagging of their question bank.
 *
 * <p>Ownership is enforced through {@link AccessService}: a teacher may only touch tags they
 * own ({@code requireCanManageTag}) and only tag questions in templates they own
 * ({@code requireCanManageAssessment}). An admin may act on anything.
 */
@Service
public class TagService {

    private final TagRepository tagRepository;
    private final QuestionTagRepository questionTagRepository;
    private final QuestionTemplateRepository questionTemplateRepository;
    private final AccessService accessService;

    public TagService(TagRepository tagRepository,
                      QuestionTagRepository questionTagRepository,
                      QuestionTemplateRepository questionTemplateRepository,
                      AccessService accessService) {
        this.tagRepository = tagRepository;
        this.questionTagRepository = questionTagRepository;
        this.questionTemplateRepository = questionTemplateRepository;
        this.accessService = accessService;
    }

    // ── Tag CRUD ───────────────────────────────────────────────────

    @Transactional
    public TagResponse create(UUID userId, String name) {
        accessService.requireAnyRole(userId, AccessService.ROLE_TEACHER, AccessService.ROLE_ADMIN);
        String trimmed = name.trim();
        if (tagRepository.existsByTeacherIdAndName(userId, trimmed)) {
            throw new ConflictException("You already have a tag named '" + trimmed + "'");
        }
        Tag tag = new Tag();
        tag.setId(UUID.randomUUID());
        tag.setTeacherId(userId);
        tag.setName(trimmed);
        return TagResponse.from(tagRepository.save(tag));
    }

    @Transactional(readOnly = true)
    public List<TagResponse> listOwn(UUID userId) {
        return tagRepository.findByTeacherIdOrderByName(userId).stream()
                .map(TagResponse::from)
                .toList();
    }

    @Transactional
    public TagResponse rename(UUID userId, UUID tagId, String name) {
        Tag tag = accessService.requireCanManageTag(userId, tagId);
        String trimmed = name.trim();
        if (!trimmed.equals(tag.getName())
                && tagRepository.existsByTeacherIdAndName(tag.getTeacherId(), trimmed)) {
            throw new ConflictException("You already have a tag named '" + trimmed + "'");
        }
        tag.setName(trimmed);
        return TagResponse.from(tagRepository.save(tag));
    }

    @Transactional
    public void delete(UUID userId, UUID tagId) {
        Tag tag = accessService.requireCanManageTag(userId, tagId);
        // question_tag rows cascade at the DB level (ON DELETE CASCADE).
        tagRepository.delete(tag);
    }

    // ── Tagging questions ──────────────────────────────────────────

    @Transactional
    public void attach(UUID userId, UUID questionId, UUID tagId) {
        authorizeTagging(userId, questionId, tagId);
        if (!questionTagRepository.existsByQuestionTemplateIdAndTagId(questionId, tagId)) {
            questionTagRepository.save(new QuestionTag(questionId, tagId));
        }
    }

    @Transactional
    public void detach(UUID userId, UUID questionId, UUID tagId) {
        authorizeTagging(userId, questionId, tagId);
        questionTagRepository.deleteById(new QuestionTagId(questionId, tagId));
    }

    /** The caller must own both the tag and the template the question belongs to. */
    private void authorizeTagging(UUID userId, UUID questionId, UUID tagId) {
        accessService.requireCanManageTag(userId, tagId);
        QuestionTemplate question = questionTemplateRepository.findById(questionId)
                .orElseThrow(() -> new ResourceNotFoundException("Question not found"));
        accessService.requireCanManageAssessment(userId, question.getAssessmentId());
    }

    // ── Filter ─────────────────────────────────────────────────────

    /** The caller's own questions carrying the given tag, each with its full tag list. */
    @Transactional(readOnly = true)
    public List<TaggedQuestionResponse> questionsByTag(UUID userId, UUID tagId) {
        accessService.requireCanManageTag(userId, tagId);
        List<QuestionTemplate> questions = questionTemplateRepository.findByTagAndOwner(tagId, userId);
        if (questions.isEmpty()) {
            return List.of();
        }

        List<UUID> questionIds = questions.stream().map(QuestionTemplate::getId).toList();

        // questionId → its tag ids
        Map<UUID, List<UUID>> tagIdsByQuestion = questionTagRepository
                .findByQuestionTemplateIdIn(questionIds).stream()
                .collect(Collectors.groupingBy(
                        QuestionTag::getQuestionTemplateId,
                        Collectors.mapping(QuestionTag::getTagId, Collectors.toList())));

        // load every tag referenced, once
        List<UUID> allTagIds = tagIdsByQuestion.values().stream()
                .flatMap(List::stream).distinct().toList();
        Map<UUID, TagResponse> tagsById = tagRepository.findByIdInOrderByName(allTagIds).stream()
                .collect(Collectors.toMap(Tag::getId, TagResponse::from));

        return questions.stream()
                .map(q -> new TaggedQuestionResponse(
                        q.getId().toString(),
                        q.getType(),
                        q.getText(),
                        q.getPosition(),
                        tagIdsByQuestion.getOrDefault(q.getId(), List.of()).stream()
                                .map(tagsById::get)
                                .filter(t -> t != null)
                                .toList()))
                .toList();
    }
}
