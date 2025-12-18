package io.vobc.vobc_back.repository;

import io.vobc.vobc_back.domain.LanguageCode;
import io.vobc.vobc_back.domain.term.TermTranslation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.Set;

public interface TermTranslationRepository extends JpaRepository<TermTranslation, Long> {
    Optional<TermTranslation> findByTermIdAndLanguageCode(Long termId, LanguageCode languageCode);

    @Query("select distinct t.languageCode from TermTranslation t where t.term.id = :termId")
    Set<LanguageCode> findAllLanguageCodesByTermId(@Param("termId") Long termId);
}
