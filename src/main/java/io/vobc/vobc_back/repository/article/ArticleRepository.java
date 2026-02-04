package io.vobc.vobc_back.repository.article;

import io.vobc.vobc_back.domain.LanguageCode;
import io.vobc.vobc_back.domain.article.Article;
import io.vobc.vobc_back.domain.article.ArticleTopic;
import io.vobc.vobc_back.dto.article.ArticleResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ArticleRepository extends JpaRepository<Article, Long> {

    @EntityGraph(attributePaths = {"publisher"})
    Page<Article> findAllByOrderByCreatedAtDesc(Pageable pageable);

    @EntityGraph(attributePaths = {"publisher", "topics", "topics.topic"})
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

    @Query("""
    select at from ArticleTopic at
    join fetch at.topic
    where at.article.id = :articleId
    order by at.primaryTopic desc, at.sortOrder asc, at.id asc
    """)
    List<ArticleTopic> findArticleTopicsByArticleId(@Param("articleId") Long articleId);



    @Query("""
    select new io.vobc.vobc_back.dto.article.ArticleResponse(
        a.id,
        coalesce(tr.title, a.title),
        coalesce(ptr.name, p.name),
        a.releaseDate,
        a.thumbnail
    )
    from Article a
    join a.publisher p
    join ArticleTopic atp
      on atp.article = a
     and atp.topic.id = :topicId
     and atp.primaryTopic = true
    left join ArticleTranslation tr
      on tr.article = a
     and tr.languageCode = :languageCode
    left join PublisherTranslation ptr
      on ptr.publisher = p
     and ptr.languageCode = :languageCode
    where a.id <> :articleId
    order by a.releaseDate desc
""")
    List<ArticleResponse> findRelatedByPrimaryTopic(
            @Param("articleId") Long articleId,
            @Param("topicId") Long topicId,
            @Param("languageCode") LanguageCode languageCode,
            org.springframework.data.domain.Pageable pageable
    );

    @Query("""
    select new io.vobc.vobc_back.dto.article.ArticleResponse(
        a.id,
        coalesce(tr.title, a.title),
        coalesce(ptr.name, p.name),
        a.releaseDate,
        a.thumbnail
    )
    from Article a
    join a.publisher p
    join ArticleTopic atp
      on atp.article = a
     and atp.topic.id = :topicId
    left join ArticleTranslation tr
      on tr.article = a
     and tr.languageCode = :languageCode
    left join PublisherTranslation ptr
      on ptr.publisher = p
     and ptr.languageCode = :languageCode
    where a.id <> :articleId
    order by atp.sortOrder asc, a.releaseDate desc
    """)
    List<ArticleResponse> findRelatedByTopic(
            @Param("articleId") Long articleId,
            @Param("topicId") Long topicId,
            @Param("languageCode") LanguageCode languageCode,
            org.springframework.data.domain.Pageable pageable
    );



}