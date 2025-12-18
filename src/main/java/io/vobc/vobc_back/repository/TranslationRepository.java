package io.vobc.vobc_back.repository;

import io.vobc.vobc_back.domain.LanguageCode;
import io.vobc.vobc_back.domain.Translation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface TranslationRepository extends JpaRepository<Translation, Long> {

    Optional<Translation> findByPostIdAndLanguageCode(Long postId, LanguageCode languageCode);

    List<Translation> findAllByPostIdInAndLanguageCode(List<Long> postIds, LanguageCode languageCode);

    void deleteByPost_IdAndLanguageCode(Long postId, LanguageCode languageCode);

    @Query("""
            select distinct t.languageCode
            from Translation t
            where t.post.id = :postId
            """)
    Set<LanguageCode> findLanguageCodesByPostId(@Param("postId") Long postId);
}
