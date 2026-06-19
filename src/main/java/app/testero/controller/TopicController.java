package app.testero.controller;

import app.testero.dto.TopicListResponse;
import app.testero.service.TopicService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/topics")
@Tag(name = "Topics")
public class TopicController {

    private final TopicService topicService;

    public TopicController(TopicService topicService) {
        this.topicService = topicService;
    }

    @GetMapping
    public ResponseEntity<TopicListResponse> getTopics() {
        return ResponseEntity.ok(topicService.getTopics());
    }
}
