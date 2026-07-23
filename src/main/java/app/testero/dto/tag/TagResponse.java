package app.testero.dto.tag;

import app.testero.entity.tag.Tag;

public record TagResponse(String id, String name) {

    public static TagResponse from(Tag tag) {
        return new TagResponse(tag.getId().toString(), tag.getName());
    }
}
