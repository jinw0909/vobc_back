package io.vobc.vobc_back.repository.publisher;

import io.vobc.vobc_back.domain.LanguageCode;
import io.vobc.vobc_back.domain.publisher.PublisherTranslation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Set;

public interface PublisherTranslationRepository extends JpaRepository<PublisherTranslation, Long> {

    @Query("select p.languageCode from PublisherTranslation p where p.publisher.id = :publisherId")
    Set<LanguageCode> findLanguageCodesByPublisherId(Long publisherId);

    PublisherTranslation findByPublisherIdAndLanguageCode(Long publisherId, LanguageCode languageCode);

    @Query("select pt from PublisherTranslation pt where pt.publisher.id in :publisherIds and pt.languageCode = :languageCode")
    List<PublisherTranslation> findByPublisherIdsAndLanguageCode(@Param("publisherIds") List<Long> publisherIds, LanguageCode languageCode);
}
