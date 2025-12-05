package io.vobc.vobc_back.dto;

import io.vobc.vobc_back.domain.PostTag;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PostTagResponse {
    private Long tagId;
    private String tagName;
    private Integer sortOrder;

    public PostTagResponse(Long tagId, String tagName, Integer sortOrder) {
        this.tagId = tagId;
        this.tagName = tagName;
        this.sortOrder = sortOrder;
    }

    public static PostTagResponse from(PostTag postTag) {
        return new PostTagResponse(
                postTag.getTag().getId(),
                postTag.getTag().getName(),
                postTag.getSortOrder());
    }
}
