package io.vobc.vobc_back.dto.post;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter @Setter
@ToString
public class PostTagForm {

    private Long postTagId;
    private Long postId;
    private Long tagId;
    private Integer sortOrder;
    private Boolean primaryTag;

}
