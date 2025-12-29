package io.vobc.vobc_back.domain.article;

import io.vobc.vobc_back.domain.LanguageCode;
import io.vobc.vobc_back.dto.article.ArticleTranslationForm;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@EntityListeners(AuditingEntityListener.class)
@Getter @Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
        uniqueConstraints = @UniqueConstraint(columnNames = {"article_id", "language_code"})
)
public class ArticleTranslation {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "article_id", nullable = false)
    private Article article;

    @Enumerated(EnumType.STRING)
    private LanguageCode languageCode;

    private String title;

    @Lob
    private String content;

    @Lob
    private String summary;
    @Column(length = 1024)
    private String description;
    private String author;

    public static ArticleTranslation create(Article article,
                                            LanguageCode languageCode,
                                            String title,
                                            String content,
                                            String summary,
                                            String description,
                                            String author
    ) {
        ArticleTranslation articleTranslation = new ArticleTranslation();
        article.addTranslation(articleTranslation);
        articleTranslation.setLanguageCode(languageCode);
        articleTranslation.setTitle(title);
        articleTranslation.setContent(content);
        articleTranslation.setSummary(summary);
        articleTranslation.setDescription(description);
        articleTranslation.setAuthor(author);
        return articleTranslation;
    }

}
