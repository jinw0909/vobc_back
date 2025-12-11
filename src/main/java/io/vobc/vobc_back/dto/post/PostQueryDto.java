package io.vobc.vobc_back.dto.post;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.vobc.vobc_back.dto.postTag.PostTagQueryDto;
import io.vobc.vobc_back.dto.translation.TranslationQueryDto;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;
import java.util.List;

@Data
@EqualsAndHashCode(of = "postId")
public class PostQueryDto {

    private Long postId;
    private String title;
    private String content;
    private String summary;
    private String author;
    private String thumbnail;
    private LocalDate releaseDate;
    private List<PostTagQueryDto> postTags;
    @JsonIgnore
    private List<TranslationQueryDto> translations;

    public PostQueryDto(Long postId, String title, String content, String summary, String author, String thumbnail, LocalDate releaseDate) {
        this.postId = postId;
        this.title = title;
        this.content = content;
        this.summary = summary;
        this.author = author;
        this.thumbnail = thumbnail;
        this.releaseDate = releaseDate;
    }
}
