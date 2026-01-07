package io.vobc.vobc_back.dto;

import io.vobc.vobc_back.domain.LanguageCode;
import io.vobc.vobc_back.domain.post.Translation;
import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Getter
@Setter
public class TranslationForm {
    private Long postId;
    private String title;
    private String content;
    private String summary;
    private String author;
    private LanguageCode languageCode;

    private List<MultipartFile> files;

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
