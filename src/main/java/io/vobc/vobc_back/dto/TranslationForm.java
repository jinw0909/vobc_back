package io.vobc.vobc_back.dto;

import io.vobc.vobc_back.domain.LanguageCode;
import io.vobc.vobc_back.domain.Translation;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TranslationForm {
    private Long postId;
    private LanguageCode languageCode;
    private String content;
    private String title;
    private String summary;
    private String author;

    public static TranslationForm from(Translation t) {
        TranslationForm f = new TranslationForm();
        f.setPostId(t.getPost().getId());
        f.setLanguageCode(t.getLanguageCode());
        f.setContent(t.getContent());
        f.setTitle(t.getTitle());
        f.setSummary(t.getSummary());
        f.setAuthor(t.getAuthor());
        return f;
    }

    public static TranslationForm empty(Long postId, LanguageCode languageCode) {
        TranslationForm f = new TranslationForm();
        f.setPostId(postId);
        f.setLanguageCode(languageCode);
        return f;
    }
}
