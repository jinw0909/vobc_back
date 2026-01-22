package io.vobc.vobc_back.repository.team;

import io.vobc.vobc_back.domain.LanguageCode;
import io.vobc.vobc_back.domain.team.ResumeTranslation;
import io.vobc.vobc_back.domain.team.TeamMember;
import io.vobc.vobc_back.domain.team.TeamMemberTranslation;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface TeamMemberRepository extends JpaRepository<TeamMember, Long> {
    Optional<TeamMember> findByIdAndTeamId(Long memberId, Long teamId);

    @EntityGraph(attributePaths = {"resumes", "team"})
    Optional<TeamMember> findWithResumesById(Long teamMemberId);

    @EntityGraph(attributePaths = {"resumes", "team"})
    Optional<TeamMember> findWithTeamAndResumesById(Long id);

    @Query("select distinct tmt.languageCode from TeamMemberTranslation tmt where tmt.teamMember.id = :teamMemberId")
    Set<LanguageCode> findLanguageCodesById(@Param("teamMemberId") Long teamMemberId);

    @Query("select tmt from TeamMemberTranslation tmt where tmt.teamMember.id = :teamMemberId and tmt.languageCode = :languageCode")
    Optional<TeamMemberTranslation> findByLanguageCodeAndById(@Param("languageCode") LanguageCode languageCode,
                                                              @Param("teamMemberId") Long teamMemberId);

    @Query("select tr from ResumeTranslation tr join fetch tr.resume r where tr.resume.id in :resumeIds and tr.languageCode = :languageCode")
    List<ResumeTranslation> findByLanguageCodeAndInIds(@Param("languageCode") LanguageCode languageCode,
                                                       @Param("resumeIds") List<Long> resumeIds);
}
