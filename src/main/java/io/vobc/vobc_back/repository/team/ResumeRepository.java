package io.vobc.vobc_back.repository.team;

import io.vobc.vobc_back.domain.LanguageCode;
import io.vobc.vobc_back.domain.team.Resume;
import io.vobc.vobc_back.domain.team.ResumeTranslation;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface ResumeRepository extends JpaRepository<Resume, Long> {

    @Query("select rt from ResumeTranslation rt where rt.resume.id = :resumeId and rt.languageCode = :languageCode")
    Optional<ResumeTranslation> findByResumeIdAndLanguageCode(@Param("resumeId") Long resumeId,
                                                              @Param("languageCode") LanguageCode languageCode);
}
