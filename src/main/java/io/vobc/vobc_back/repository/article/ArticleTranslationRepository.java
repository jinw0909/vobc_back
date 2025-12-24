package io.vobc.vobc_back.repository.article;

import io.vobc.vobc_back.domain.LanguageCode;
import io.vobc.vobc_back.domain.article.ArticleTranslation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface ArticleTranslationRepository extends JpaRepository<ArticleTranslation, Long> {

    @Query("""
    select t
    from ArticleTranslation t
    where t.article.id in :articleIds
        and t.languageCode = :languageCode
    """)
    List<ArticleTranslation> findByArticleIdsAndLanguageCode(@Param("articleIds") List<Long> articleIds,
                                                             @Param("languageCode") LanguageCode languageCode);

    @Query("select distinct t.languageCode from ArticleTranslation t where t.article.id = :id")
    Set<LanguageCode> findLanguageCodesByArticleId(Long id);

    Optional<ArticleTranslation> findByArticleIdAndLanguageCode(Long id, LanguageCode languageCode);
}
