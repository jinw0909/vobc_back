package io.vobc.vobc_back.dto.post;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.vobc.vobc_back.domain.LanguageCode;
import io.vobc.vobc_back.domain.Post;
import io.vobc.vobc_back.domain.PostTag;
import io.vobc.vobc_back.domain.Translation;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class PostDto {

    private Long id;
    private String title;
    private String content;
    private String author;
    private String summary;
    private LocalDate releaseDate;
    private String thumbnail;

    private List<TagDto> postTags;
    @JsonIgnore
    private List<TranslationDto> translations;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public PostDto(Post post) {
        this.id = post.getId();
        this.title = post.getTitle();
        this.content = post.getContent();
        this.author = post.getAuthor();
        this.summary = post.getSummary();
        this.releaseDate = post.getReleaseDate();
        this.thumbnail = post.getThumbnail();
        this.createdAt = post.getCreatedAt();
        this.updatedAt = post.getUpdatedAt();

        this.postTags = post.getPostTags().stream()
                .map(TagDto::new)
                .toList();

        this.translations = post.getTranslations().stream()
                .map(TranslationDto::new)
                .toList();
    }

    @Data
    static class TagDto {
        private Long id;
        private String name;

        public TagDto(PostTag postTag) {
            this.id = postTag.getId();
            this.name = postTag.getTag().getName();
        }
    }

    @Data
    static class TranslationDto {
        private Long id;
        private String title;
        private String content;
        private String summary;
        private String author;
        private LanguageCode languageCode;

        public TranslationDto(Translation translation) {
            this.id = translation.getId();
            this.title = translation.getTitle();
            this.content = translation.getContent();
            this.summary = translation.getSummary();
            this.author = translation.getAuthor();
            this.languageCode = translation.getLanguageCode();
        }
    }
}
