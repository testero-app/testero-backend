package app.testero.dto.common;

import com.fasterxml.jackson.annotation.JsonProperty;

public record PaginationMetadata(
        @JsonProperty("total_elements") long totalElements,
        @JsonProperty("total_pages") int totalPages,
        int page,
        int size
) {}
