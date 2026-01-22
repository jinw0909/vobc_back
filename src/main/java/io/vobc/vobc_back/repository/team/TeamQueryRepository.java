package io.vobc.vobc_back.repository.team;

import io.vobc.vobc_back.domain.LanguageCode;
import io.vobc.vobc_back.domain.team.Team;
import io.vobc.vobc_back.dto.team.ResumeDto;
import io.vobc.vobc_back.dto.team.TeamMemberDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface TeamQueryRepository extends JpaRepository<Team, Long> {

    @Query(
            value = """
        select new io.vobc.vobc_back.dto.team.TeamMemberDto(
            t.id, tm.id,
            coalesce(tmt.name, tm.name),
            tm.email,
            coalesce(tmt.introduction, tm.introduction),
            tm.role, tm.photo, tm.rolePriority
        )
        from TeamMember tm
        join tm.team t
        left join TeamMemberTranslation tmt
          on tmt.teamMember = tm and tmt.languageCode = :languageCode
      """,
            countQuery = """
        select count(tm)
        from TeamMember tm
      """
    )
    Page<TeamMemberDto> findAllMembers(Pageable pageable, @Param("languageCode") LanguageCode languageCode);

    @Query("""
        select new io.vobc.vobc_back.dto.team.ResumeDto(
            r.teamMember.id, r.id,
            coalesce(rt.content, r.content)
        )
        from Resume r
        left join ResumeTranslation rt
            on rt.resume = r and rt.languageCode = :languageCode
        where r.teamMember.id in :teamMemberIds
    """)
    List<ResumeDto> findResumesInIds(@Param("teamMemberIds") List<Long> teamMemberIds,
                                     @Param("languageCode") LanguageCode languageCode);
}
