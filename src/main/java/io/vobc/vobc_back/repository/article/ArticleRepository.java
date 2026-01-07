package io.vobc.vobc_back.repository.article;

import io.vobc.vobc_back.domain.LanguageCode;
import io.vobc.vobc_back.domain.article.Article;
import io.vobc.vobc_back.dto.article.ArticleResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface ArticleRepository extends JpaRepository<Article, Long> {

    @EntityGraph(attributePaths = {"publisher"})
    Page<Article> findAllByOrderByCreatedAtDesc(Pageable pageable);

    @EntityGraph(attributePaths = {"publisher"})
    Optional<Article> findWithPublisherById(Long id);

    @Query("""
        select new io.vobc.vobc_back.dto.article.ArticleResponse(
            a.id,
            coalesce(at.title, a.title),
            coalesce(at.content, a.content),
            coalesce(at.summary, a.summary),
            coalesce(at.description, a.description),
            coalesce(at.author, a.author),
            a.releaseDate,
            coalesce(pt.name, p.name),
            coalesce(pt.introduction, p.introduction),
            a.thumbnail,
            a.link
        )
        from Article a
        join a.publisher p
        left join ArticleTranslation at
          on at.article = a
         and at.languageCode = :languageCode
        left join PublisherTranslation pt
          on pt.publisher = p
         and pt.languageCode = :languageCode
        order by a.releaseDate desc
    """)
    Page<ArticleResponse> findAllWithTranslations(
            Pageable pageable,
            @Param("languageCode") LanguageCode languageCode
    );

    @Query("""
        select new io.vobc.vobc_back.dto.article.ArticleResponse(
            a.id,
            coalesce(at.title, a.title),
            coalesce(at.content, a.content),
            coalesce(at.summary, a.summary),
            coalesce(at.description, a.description),
            coalesce(at.author, a.author),
            a.releaseDate,
            coalesce(pt.name, p.name),
            coalesce(pt.introduction, p.introduction),
            a.thumbnail,
            a.link
        )
        from Article a
        join a.publisher p
        left join ArticleTranslation at
          on at.article = a
         and at.languageCode = :languageCode
        left join PublisherTranslation pt
          on pt.publisher = p
         and pt.languageCode = :languageCode
        where a.id = :articleId
    """)
    Optional<ArticleResponse> findOneWithTranslations(
            @Param("articleId") Long articleId,
            @Param("languageCode") LanguageCode languageCode
    );


    @EntityGraph(attributePaths = {"translations"})
    Optional<Article> findWithTranslationById(Long articleId);
}