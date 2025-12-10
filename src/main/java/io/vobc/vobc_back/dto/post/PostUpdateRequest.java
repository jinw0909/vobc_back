package io.vobc.vobc_back.dto.post;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;

@Getter @Setter
public class PostUpdateRequest {
    @NotBlank
    private String title;
    @NotBlank
    private String content;
    private String summary;
    private String author;
    private LocalDate releaseDate;
    private String thumbnail;
    private List<Long> tagIds;
}
