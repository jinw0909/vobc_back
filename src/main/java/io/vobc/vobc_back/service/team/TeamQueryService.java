package io.vobc.vobc_back.service.team;

import io.vobc.vobc_back.domain.LanguageCode;
import io.vobc.vobc_back.domain.team.Team;
import io.vobc.vobc_back.domain.team.TeamRole;
import io.vobc.vobc_back.dto.team.ResumeDto;
import io.vobc.vobc_back.dto.team.TeamDto;
import io.vobc.vobc_back.dto.team.TeamMemberDto;
import io.vobc.vobc_back.repository.team.TeamQueryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class TeamQueryService {

    private final TeamQueryRepository teamQueryRepository;
    private final MessageSource messageSource;

    @Transactional(readOnly = true)
    public Page<Team> getMembersOfTeam(Pageable pageable, String team) {
        return null;
    }

    @Transactional(readOnly = true)
    public Page<TeamMemberDto> getAllMembers(Pageable pageable, LanguageCode languageCode) {

        Locale locale = languageCode.toLocale();

        Page<TeamMemberDto> memberList = teamQueryRepository.findAllMembers(pageable, languageCode);

        List<Long> teamMemberIds = memberList.stream().map(TeamMemberDto::getId).toList();

        List<ResumeDto> resumeList = teamQueryRepository.findResumesInIds(teamMemberIds, languageCode);
        Map<Long, List<ResumeDto>> resumeMap = resumeList.stream().collect(Collectors.groupingBy(ResumeDto::getTeamMemberId));

        return memberList.map(dto -> {
            TeamRole role = dto.getRole();
            if (role != null) {
                String label = messageSource.getMessage(role.messageKey(), null, locale);
                dto.setTeamRoleLabel(label);
            }
            dto.setResumes(resumeMap.getOrDefault(dto.getId(), Collections.emptyList()));
            return dto;
        });

    }

    @Transactional(readOnly = true)
    public List<TeamDto> getAll(LanguageCode languageCode) {
        return teamQueryRepository.findAll(languageCode);
    }
}
