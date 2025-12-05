package io.vobc.vobc_back.dto;

import io.vobc.vobc_back.domain.LanguageCode;
import io.vobc.vobc_back.domain.Post;
import io.vobc.vobc_back.domain.Translation;
import jakarta.annotation.Nullable;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@AllArgsConstructor
public class PostResponse {

    private Long id;

    private String title;
    private String content;
    private String author;
    private String summary;

    private String requestedLanguage;
    private String effectiveLanguage;
    private boolean translated;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private List<TagForm> tags;

    // 번역이 없는 경우
    public static PostResponse from(Post post, LanguageCode requestedLanguage) {
        return of(post, null, requestedLanguage);

    }

    public static PostResponse of(Post post, @Nullable Translation tr, LanguageCode requestedLanguage) {
        String title = tr != null && tr.getTitle() != null ? tr.getTitle() : post.getTitle();
        String content = tr != null && tr.getContent() != null ? tr.getContent() : post.getContent();
        String summary = tr != null && tr.getSummary() != null ? tr.getSummary() : post.getSummary();
        String author = tr != null && tr.getAuthor() != null ? tr.getAuthor() : post.getAuthor();

        boolean translated = tr != null;
        String effectiveLanguage = translated ? tr.getLanguageCode().getCode() : null;

        List<TagForm> tags = post.getPostTags().stream()
                .map(pt -> new TagForm(pt.getTag().getId(), pt.getTag().getName()))
                .toList();

        return new PostResponse(post.getId(), title, content, author, summary, requestedLanguage.getCode(), effectiveLanguage, translated, post.getCreatedAt(), post.getUpdatedAt(), tags);

    }

}
