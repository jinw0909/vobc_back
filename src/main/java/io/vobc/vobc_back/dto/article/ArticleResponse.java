package io.vobc.vobc_back.dto.article;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.vobc.vobc_back.domain.LanguageCode;
import io.vobc.vobc_back.domain.article.Article;
import io.vobc.vobc_back.domain.article.ArticleTranslation;
import io.vobc.vobc_back.domain.publisher.Publisher;
import io.vobc.vobc_back.domain.publisher.PublisherTranslation;
import io.vobc.vobc_back.dto.publisher.PublisherResponse;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;

@Getter @Setter
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ArticleResponse {
    private Long id;
    private String title;
    private String content;
    private String summary;
    private String description;
    private String author;
    private String thumbnail;
    private String link;
    private LocalDate releaseDate;
    private String publisherName;
    private String publisherIntroduction;

    @JsonIgnore
    private List<ArticleTranslationResponse> translations;
    @JsonIgnore
    private PublisherResponse publisher;

    public ArticleResponse(
            Long id,
            String title,
            String content,
            String summary,
            String description,
            String author,
            LocalDate releaseDate,
            String publisherName,
            String publisherIntroduction,
            String thumbnail,
            String link
    ) {
        this.id = id;
        this.title = title;
        this.content = content;
        this.summary = summary;
        this.description = description;
        this.author = author;
        this.releaseDate = releaseDate;
        this.publisherName = publisherName;
        this.publisherIntroduction = publisherIntroduction;
        this.thumbnail = thumbnail;
        this.link = link;
    }

    public ArticleResponse(
            Long id,
            String title,
            String publisherName,
            LocalDate releaseDate,
            String thumbnail
    ) {
        this.id = id;
        this.title = title;
        this.publisherName = publisherName;
        this.releaseDate = releaseDate;
        this.thumbnail = thumbnail;
    }

    public static ArticleResponse create(Article article) {
        ArticleResponse r = new ArticleResponse();
        r.id = article.getId();
        r.title = article.getTitle();
        r.content = article.getContent();
        r.summary = article.getSummary();
        r.description = article.getDescription();
        r.author = article.getAuthor();
        r.thumbnail = article.getThumbnail();
        r.link = article.getLink();
        r.releaseDate = article.getReleaseDate();
        // publisher 기본값
        Publisher publisher = article.getPublisher();
        r.publisher = PublisherResponse.create(publisher);
        r.publisherName = publisher == null ? null : publisher.getName();
        r.publisherIntroduction = publisher == null ? null : publisher.getIntroduction();
        return r;
    }

    public static ArticleResponse translate(ArticleResponse articleResponse, LanguageCode languageCode) {
        PublisherResponse publisher = articleResponse.getPublisher();
        if (publisher != null) {
            PublisherResponse translated = PublisherResponse.translate(publisher, languageCode);
            articleResponse.setPublisher(translated);
            articleResponse.setPublisherName(translated.getName());
            articleResponse.setPublisherIntroduction(translated.getIntroduction());
        }
        List<ArticleTranslationResponse> translations = articleResponse.getTranslations();
        if (translations != null) {
            translations.stream()
                    .filter(t -> t.getLanguageCode().equals(languageCode))
                    .findFirst()
                    .ifPresent( t -> {
                        articleResponse.setTitle(t.getTitle());
                        articleResponse.setContent(t.getContent());
                        articleResponse.setSummary(t.getSummary());
                        articleResponse.setDescription(t.getDescription());
                        articleResponse.setAuthor(t.getAuthor());
                    });
        }

        return articleResponse;
    }

}
