package io.vobc.vobc_back.domain.article;

import io.vobc.vobc_back.domain.media.Media;
import io.vobc.vobc_back.domain.publisher.Publisher;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter @Setter
@EntityListeners(AuditingEntityListener.class)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Article {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "article_id")
    private Long id;

    private String title;

    @Lob
    private String content;

    @Lob
    private String summary;

    private String description;

    private LocalDate releaseDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "publisher_id", nullable = true)
    private Publisher publisher;

    private String author;

    @Column(length = 512)
    private String thumbnail;

    @Column(length = 512)
    private String link;

    @Enumerated(EnumType.STRING)
    private Category category;

    @OneToMany(mappedBy = "article", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Media> media = new ArrayList<>();

    @OneToMany(mappedBy = "article", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ArticleTranslation> translations = new ArrayList<>();

    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;

    //==생성 메서드==//
    public static Article create(String title, String content, String summary, String description, String author, LocalDate releaseDate, String thumbnail, String link, Category category) {
        Article article = new Article();
        article.title = title;
        article.content = content;
        article.summary = summary;
        article.description = description;
        article.author = author;
        article.releaseDate = releaseDate;
        article.thumbnail = thumbnail;
        article.link = link;
        article.category = category;
        return article;
    }

    //== 연관관계 편의 메서드 ==//
    public void setPublisher(Publisher publisher) {
        this.publisher = publisher;
        publisher.getArticles().add(this);
    }

    public void addMedia(Media media) {
        this.media.add(media);
        media.setArticle(this);
    }

    public void addTranslation(ArticleTranslation articleTranslation) {
        this.translations.add(articleTranslation);
        articleTranslation.setArticle(this);
    }

    public void removeMedia(Media m) {
        media.remove(m);
        m.setArticle(null);
    }


    public void changeContent(String content) {
        this.content = content;
    }

}
