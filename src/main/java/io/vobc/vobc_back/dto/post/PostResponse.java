package io.vobc.vobc_back.dto.post;

import io.vobc.vobc_back.domain.LanguageCode;
import io.vobc.vobc_back.domain.Post;
import io.vobc.vobc_back.domain.Translation;
import io.vobc.vobc_back.dto.TagForm;
import jakarta.annotation.Nullable;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDate;
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
    private LocalDate releaseDate;
    private String thumbnail;

    private String requestedLanguage;
    private String effectiveLanguage;
    private boolean translated;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private List<TagForm> tags;

    // 번역이 없는 경우 (상세/리스트 공용으로 쓸 수 있음)
    public static PostResponse from(Post post, LanguageCode requestedLanguage, List<TagForm> tags) {
        return ofForList(post, null, requestedLanguage, tags);
    }

    /**
     * 🔹 리스트용: 태그는 외부에서 계산해서 넣어주는 버전
     *  - 엔티티의 LAZY 컬렉션(postTags)을 직접 건드리지 않는다.
     */
    public static PostResponse ofForList(Post post,
                                         @Nullable Translation tr,
                                         LanguageCode requestedLanguage,
                                         List<TagForm> tags) {

        String title = tr != null && tr.getTitle() != null ? tr.getTitle() : post.getTitle();
        String content = tr != null && tr.getContent() != null ? tr.getContent() : post.getContent();
        String summary = tr != null && tr.getSummary() != null ? tr.getSummary() : post.getSummary();
        String author = tr != null && tr.getAuthor() != null ? tr.getAuthor() : post.getAuthor();

        boolean translated = tr != null;
        String effectiveLanguage = translated ? tr.getLanguageCode().getCode() : null;

        return new PostResponse(
                post.getId(),
                title,
                content,
                author,
                summary,
                post.getReleaseDate(),
                post.getThumbnail(),
                requestedLanguage.getCode(),
                effectiveLanguage,
                translated,
                post.getCreatedAt(),
                post.getUpdatedAt(),
                tags
        );
    }

    /**
     * 🔹 상세용: 이미 Post.postTags 가 fetch 되어 있는 경우에만 사용 (예: EntityGraph / fetch join).
     *  - list API에서는 이 메서드 사용 금지 (N+1 위험)
     */
    public static PostResponse of(Post post,
                                  @Nullable Translation tr,
                                  LanguageCode requestedLanguage) {

        String title = tr != null && tr.getTitle() != null ? tr.getTitle() : post.getTitle();
        String content = tr != null && tr.getContent() != null ? tr.getContent() : post.getContent();
        String summary = tr != null && tr.getSummary() != null ? tr.getSummary() : post.getSummary();
        String author = tr != null && tr.getAuthor() != null ? tr.getAuthor() : post.getAuthor();

        boolean translated = tr != null;
        String effectiveLanguage = translated ? tr.getLanguageCode().getCode() : null;

        // ❗ 이건 상세 전용 (이미 fetch된 상황)
        List<TagForm> tags = post.getPostTags().stream()
                .map(pt -> new TagForm(pt.getTag().getId(), pt.getTag().getName()))
                .toList();

        return new PostResponse(
                post.getId(),
                title,
                content,
                author,
                summary,
                post.getReleaseDate(),
                post.getThumbnail(),
                requestedLanguage.getCode(),
                effectiveLanguage,
                translated,
                post.getCreatedAt(),
                post.getUpdatedAt(),
                tags
        );
    }
}
