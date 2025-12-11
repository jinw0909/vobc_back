package io.vobc.vobc_back.dto.postTag;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

@Data
public class PostTagQueryDto {

    @JsonIgnore
    private Long postId;
    private String tagName;
    private int sortOrder;
    private boolean primaryTag;

    public PostTagQueryDto(Long postId, String tagName, int sortOrder, boolean primaryTag) {
        this.postId = postId;
        this.tagName = tagName;
        this.sortOrder = sortOrder;
        this.primaryTag = primaryTag;
    }
}
