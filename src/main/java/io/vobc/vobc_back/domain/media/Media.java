package io.vobc.vobc_back.domain.media;

import io.vobc.vobc_back.domain.article.Article;
import io.vobc.vobc_back.domain.post.Post;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@Getter @Setter
@EntityListeners(AuditingEntityListener.class)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(uniqueConstraints = @UniqueConstraint(columnNames = {"article_id", "asset_id"}))
public class Media {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "media_id")
    private Long id;

    @Column(nullable = false, length = 64)
    private String assetId;

    @Column(nullable = false, length = 1024)
    private String url;

    @Column(nullable = false, length = 512)
    private String s3Key;

    @Enumerated(EnumType.STRING)
    private ContentType contentType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "article_id")
    Article article;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id")
    Post post;


    private boolean deleted = false;

    public static Media create(String assetId, String url, String s3Key, ContentType contentType, Article article) {
        Media media = new Media();
        media.assetId = assetId;
        media.url = url;
        media.s3Key = s3Key;
        media.contentType = contentType;
        media.article = article;
        article.getMedia().add(media);
        return media;
    }

    public static Media create(String assetId, String url, String s3Key, ContentType contentType, Post post) {
        Media media = new Media();
        media.assetId = assetId;
        media.url = url;
        media.s3Key = s3Key;
        media.contentType = contentType;
        media.post = post;
        post.getMedia().add(media);
        return media;
    }



}
