package io.vobc.vobc_back.dto.translation;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.vobc.vobc_back.domain.LanguageCode;
import lombok.Data;

@Data
public class TranslationQueryDto {

    @JsonIgnore
    private Long postId;
    private String title;
    private String content;
    private String summary;
    private String author;
    private LanguageCode languageCode;

    public TranslationQueryDto(Long postId, String title, String content, String summary, String author, LanguageCode languageCode) {
        this.postId = postId;
        this.title = title;
        this.content = content;
        this.summary = summary;
        this.author = author;
        this.languageCode = languageCode;
    }
}
