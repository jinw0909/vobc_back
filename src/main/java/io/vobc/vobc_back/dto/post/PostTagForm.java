package io.vobc.vobc_back.dto.post;

import io.vobc.vobc_back.domain.post.PostTag;
import io.vobc.vobc_back.dto.TagForm;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Data
@ToString
@Getter @Setter
@NoArgsConstructor
public class PostTagForm {

    private Long postTagId;
    private Long postId;
    private Long tagId;
    private Integer sortOrder;
    private Boolean primaryTag;

    private String tagName;

    public PostTagForm(PostTag postTag) {
        this.postTagId = postTag.getId();
        this.postId = postTag.getPost().getId();
        this.tagId = postTag.getTag().getId();
        this.sortOrder = postTag.getSortOrder();
        this.primaryTag = postTag.getPrimaryTag();
        this.tagName = postTag.getTag().getName();
    }

}
