package io.vobc.vobc_back.repository.team;

import io.vobc.vobc_back.domain.LanguageCode;
import io.vobc.vobc_back.domain.team.Team;
import io.vobc.vobc_back.domain.team.TeamMember;
import io.vobc.vobc_back.domain.team.TeamTranslation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.Set;

public interface TeamRepository extends JpaRepository<Team, Long> {

    @EntityGraph(attributePaths = {"members"})
    Team findWithTeamMembersById(Long id);

    @Query("select tm from TeamMember tm where tm.team.id = :teamId")
    Page<TeamMember> findAllMemberByTeamId(@Param("teamId") Long teamId, Pageable pageable);

    @Query("select distinct tt.languageCode from TeamTranslation tt where tt.team.id = :teamId")
    Set<LanguageCode> findAllLanguageCodes(Long teamId);

    @Query("select tt from TeamTranslation tt join fetch Team t where tt.team.id = :teamId and tt.languageCode = :languageCode")
    Optional<TeamTranslation> findTranslationByLanguageCodeAndTeamId(LanguageCode languageCode, Long teamId);

    @EntityGraph(attributePaths = {"translations"})
    Optional<Team> findWithTranslationsById(Long teamId);
}
