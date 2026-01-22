package io.vobc.vobc_back.service.team;

import io.vobc.vobc_back.domain.LanguageCode;
import io.vobc.vobc_back.domain.team.Team;
import io.vobc.vobc_back.domain.team.TeamMember;
import io.vobc.vobc_back.domain.team.TeamRole;
import io.vobc.vobc_back.domain.team.TeamTranslation;
import io.vobc.vobc_back.dto.team.TeamForm;
import io.vobc.vobc_back.dto.team.TeamMemberForm;
import io.vobc.vobc_back.dto.team.TeamTranslationForm;
import io.vobc.vobc_back.repository.team.TeamMemberRepository;
import io.vobc.vobc_back.repository.team.TeamRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TeamService {

    private final TeamRepository teamRepository;
    private final TeamMemberRepository teamMemberRepository;


    @Transactional(readOnly = true)
    public Team getTeamById(Long id) {
        return teamRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Team not found: " + id));
    }

    @Transactional(readOnly = true)
    public Team getTeamWithMembersById(Long id) {
        return teamRepository.findWithTeamMembersById(id);
    }

    @Transactional
    public Long saveTeam(TeamForm form) {
        Team team = Team.create(form.getName(), form.getDescription(), form.getIcon(), form.getDisplayOrder());

        for (TeamMemberForm tmf : form.getMembers()) {
            TeamMember member = TeamMember.create(team, tmf.getName(), TeamRole.fromOrDefault(tmf.getRole(), TeamRole.ASSOCIATE), tmf.getPhoto());
            team.addMember(member); // 연관관계 주인 쪽(teamMember.team)까지 세팅됨
        }

        Team saved = teamRepository.save(team); // Team + members 함께 저장
        return saved.getId();
    }

    @Transactional
    public void updateTeam(TeamForm form) {
        Team team = teamRepository.findWithTeamMembersById(form.getId());
        team.setName(form.getName());
        team.setDescription(form.getDescription());
        team.setIcon(form.getIcon());
        team.setDisplayOrder(form.getDisplayOrder());

        // 선택된 멤버 ID만 추출 (null 방어)
        Set<Long> selectedIds = (form.getMembers() == null ? Set.<Long>of() :
                form.getMembers().stream()
                        .map(TeamMemberForm::getId)
                        .filter(Objects::nonNull)
                        .collect(Collectors.toSet())
        );

        // 현재 이팀에 속한 멤버들
        List<TeamMember> currentMembers = new ArrayList<>(team.getMembers());

        // 1) 현재 멤버들 중에서 선택 해제된 애들은 팀에서 제외
        for (TeamMember member : currentMembers) {
            if (!selectedIds.contains(member.getId())) {
                team.removeMember(member);
            }
        }

        // 2) 선택된 멤버 중에서 아직 팀에 없는 애들은 팀에 추가
        if (!selectedIds.isEmpty()) {
            List<TeamMember> newMembers = teamMemberRepository.findAllById(selectedIds);

            for (TeamMember member : newMembers) {
                //이미 이 팀이면 스킵
                if (member.getTeam() != null && Objects.equals(member.getTeam().getId(), team.getId())) continue;
                //다른 팀 소속이면 그 팀에서 제거
                if (member.getTeam() != null) member.getTeam().removeMember(member);
                team.addMember(member);
            }
        }
    }

    @Transactional(readOnly = true)
    public Page<Team> getAllTeam(Pageable pageable) {
        return teamRepository.findAll(pageable);
    }

    @Transactional
    public void delete(Long teamId) {
        Team team = teamRepository.findById(teamId).orElseThrow(() -> new IllegalArgumentException("Team not found: " + teamId));
        teamRepository.delete(team);
    }

    @Transactional
    public Long createMember(Long teamId, TeamMemberForm form) {
        Team team = teamRepository.findById(teamId).orElseThrow(() -> new IllegalArgumentException("Team not found: " + teamId));
        TeamMember member = TeamMember.create(team, form.getName(), TeamRole.fromOrDefault(form.getRole(), TeamRole.ASSOCIATE), form.getPhoto());
        return teamMemberRepository.save(member).getId();
    }

    @Transactional(readOnly = true)
    public Page<TeamMember> getAllMemberByTeamId(Long teamId, Pageable pageable) {
        return teamRepository.findAllMemberByTeamId(teamId, pageable);
    }

    @Transactional(readOnly = true)
    public TeamMember getMemberById(Long memberId, Long teamId) {
        return teamMemberRepository.findByIdAndTeamId(memberId, teamId)
                .orElseThrow(() -> new IllegalArgumentException("TeamMember not found: " + memberId));
    }

    @Transactional
    public TeamMemberForm updateTeamMember(Long teamId, Long memberId, TeamMemberForm form) {
        TeamMember teamMember = teamMemberRepository.findByIdAndTeamId(memberId, teamId)
                .orElseThrow(() -> new IllegalArgumentException("TeamMember not found: " + memberId));
        return new TeamMemberForm(teamMember);
    }

    @Transactional(readOnly = true)
    public Page<TeamMember> getAllTeamMembers(Pageable pageable) {
        return teamMemberRepository.findAll(pageable);
    }

    @Transactional(readOnly = true)
    public List<Team> getAll() {
        return teamRepository.findAll();
    }

    @Transactional
    public Long save(TeamMemberForm form) {
        Team team = teamRepository.findById(form.getTeamId()).orElseThrow(() -> new IllegalArgumentException("Team not found: " + form.getTeamId()));
        TeamMember teamMember = TeamMember.create(team, form.getName(), TeamRole.fromOrDefault(form.getRole(), TeamRole.ASSOCIATE), form.getPhoto());
        teamMemberRepository.save(teamMember);
        return teamMember.getId();
    }

    @Transactional(readOnly = true)
    public Set<LanguageCode> getLanguageCodes(Long teamId) {
        return teamRepository.findAllLanguageCodes(teamId);
    }


    @Transactional(readOnly = true)
    public TeamTranslation getTranslationByLanguageCodeAndTeamId(LanguageCode languageCode, Long teamId) {
        return teamRepository.findTranslationByLanguageCodeAndTeamId(languageCode, teamId).orElse(null);
    }

    @Transactional(readOnly = true)
    public Team getTeamWithTranslationsById(Long teamId) {
        return teamRepository.findWithTranslationsById(teamId).orElseThrow();
    }

    @Transactional
    public void translate(TeamTranslationForm form) {
        LanguageCode languageCode = form.getLanguageCode();
        Team team = teamRepository.findById(form.getTeamId()).orElseThrow(() -> new IllegalArgumentException("Team not found: " + form.getTeamId()));
        TeamTranslation teamTranslation = team.getTranslations().stream().filter(t -> t.getLanguageCode().equals(languageCode)).findFirst().orElse(null);

        if (teamTranslation == null) {
            TeamTranslation.create(team, languageCode, form.getName(), form.getDescription());
        } else {
            teamTranslation.setName(form.getName());
            teamTranslation.setDescription(form.getDescription());
        }

    }
}
