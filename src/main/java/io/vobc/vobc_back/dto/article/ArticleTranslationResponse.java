package io.vobc.vobc_back.dto.article;

import io.vobc.vobc_back.domain.LanguageCode;
import io.vobc.vobc_back.domain.article.ArticleTranslation;
import lombok.Data;

@Data
public class ArticleTranslationResponse {

    private Long articleId;
    private Long id;
    private String title;
    private String content;
    private String summary;
    private String description;
    private String author;
    private LanguageCode languageCode;

    public static ArticleTranslationResponse create(Long articleId, ArticleTranslation translation) {
        ArticleTranslationResponse articleTranslationResponse = new ArticleTranslationResponse();
        articleTranslationResponse.articleId = articleId;
        articleTranslationResponse.id = translation.getId();
        articleTranslationResponse.title = translation.getTitle();
        articleTranslationResponse.content = translation.getContent();
        articleTranslationResponse.summary = translation.getSummary();
        articleTranslationResponse.description = translation.getDescription();
        articleTranslationResponse.author = translation.getAuthor();
        articleTranslationResponse.languageCode = translation.getLanguageCode();
        return articleTranslationResponse;
    }

}
