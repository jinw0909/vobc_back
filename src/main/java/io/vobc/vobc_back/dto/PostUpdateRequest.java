package io.vobc.vobc_back.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter @Setter
public class PostUpdateRequest {
    @NotBlank
    private String title;
    @NotBlank
    private String content;
    private String summary;
    private String author;
    private List<Long> tagIds;
}
