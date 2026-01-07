package io.vobc.vobc_back.dto.post;

import io.vobc.vobc_back.domain.post.PostTag;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PostTagResponse {
    private Long tagId;
    private String tagName;
    private Integer sortOrder;
    private Long postId;
    private Boolean primaryTag;

    public PostTagResponse(Long tagId, String tagName, Integer sortOrder) {
        this.tagId = tagId;
        this.tagName = tagName;
        this.sortOrder = sortOrder;
    }

    public PostTagResponse(Integer sortOrder, Boolean primaryTag, String tagName) {
        this.sortOrder = sortOrder;
        this.primaryTag = primaryTag;
        this.tagName = tagName;
    }


    public PostTagResponse(Long tagId, String tagName, Integer sortOrder, Long postId) {
        this(tagId, tagName, sortOrder);
        this.postId = postId;
    }

    public static PostTagResponse from(PostTag postTag) {
        return new PostTagResponse(
                postTag.getTag().getId(),
                postTag.getTag().getName(),
                postTag.getSortOrder(),
                postTag.getPost().getId()
        );
    }
}
