package io.vobc.vobc_back.dto.article;

import io.vobc.vobc_back.domain.LanguageCode;
import io.vobc.vobc_back.domain.article.ArticleTranslation;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Getter @Setter
@NoArgsConstructor
public class ArticleTranslationForm {

    private Long id;
    private String title;
    private String content;
    private String summary;
    private String author;
    private String description;
    private LanguageCode languageCode;

    private List<MultipartFile> files;

    public ArticleTranslationForm(ArticleTranslation translation) {
        this.id = translation.getId();
        this.title = translation.getTitle();
        this.content = translation.getContent();
        this.summary = translation.getSummary();
        this.author = translation.getAuthor();
        this.description = translation.getDescription();
        this.languageCode = translation.getLanguageCode();
    }

    public static ArticleTranslationForm empty(LanguageCode languageCode) {
        ArticleTranslationForm articleTranslationForm = new ArticleTranslationForm();
        articleTranslationForm.setLanguageCode(languageCode);
        return articleTranslationForm;
    }
}
