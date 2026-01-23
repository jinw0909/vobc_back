package io.vobc.vobc_back.service.team;

import io.vobc.vobc_back.domain.LanguageCode;
import io.vobc.vobc_back.domain.team.*;
import io.vobc.vobc_back.dto.team.ResumeForm;
import io.vobc.vobc_back.dto.team.ResumeTranslationForm;
import io.vobc.vobc_back.dto.team.TeamMemberForm;
import io.vobc.vobc_back.dto.team.TeamMemberTranslationForm;
import io.vobc.vobc_back.repository.team.ResumeRepository;
import io.vobc.vobc_back.repository.team.TeamMemberRepository;
import io.vobc.vobc_back.repository.team.TeamRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TeamMemberService {

    private final TeamMemberRepository teamMemberRepository;
    private final TeamRepository teamRepository;
    private final ResumeRepository resumeRepository;

    @Transactional(readOnly = true)
    public TeamMember getWithResumesById(Long teamMemberId) {

        return teamMemberRepository.findWithResumesById(teamMemberId)
                .orElseThrow(() -> new IllegalArgumentException("TeamMember not found: " + teamMemberId));
    }

    @Transactional(readOnly = true)
    public Page<TeamMember> getAll(Pageable pageable) {
        return teamMemberRepository.findAll(pageable);
    }

    @Transactional
    public void delete(Long teamMemberId) {
        TeamMember teamMember = teamMemberRepository.findById(teamMemberId).orElseThrow(() -> new IllegalArgumentException("TeamMember not found: " + teamMemberId));
        teamMemberRepository.delete(teamMember);
    }

//    @Transactional
//    public void updateTeamMember(TeamMemberForm form) {
//        TeamMember teamMember = teamMemberRepository.findWithTeamAndResumesById(form.getId())
//                .orElseThrow(() -> new IllegalArgumentException("TeamMember not found: " + form.getId()));
//
//        // 기본 필드 업데이트
//        teamMember.setName(form.getName());
//        teamMember.setRole(TeamRole.fromOrDefault(form.getRole(), TeamRole.STAFF));
//        teamMember.setPhoto(form.getPhoto());
//        teamMember.setIntroduction(form.getIntroduction());
//
//        // 팀 변경
//        if (!Objects.equals(form.getTeamId(), teamMember.getTeam().getId())) {
//            Team team = teamRepository.findById(form.getTeamId())
//                    .orElseThrow(() -> new IllegalArgumentException("Team not found: " + form.getTeamId()));
//            teamMember.changeTeam(team); // 편의 메서드 권장
//        }
//
//        // --------------------
//        // Resume 처리 (추가/삭제만)
//        // --------------------
//
//        List<ResumeForm> forms = Optional.ofNullable(form.getResumes())
//                .orElseGet(List::of);
//
//        // 폼에서 넘어온 "기존 resume id" 목록
//        Set<Long> formIds = forms.stream()
//                .map(ResumeForm::getId)
//                .filter(Objects::nonNull)
//                .collect(Collectors.toSet());
//
//        // 1) 삭제: 기존에 있었는데 폼에 없는 것
//        teamMember.getResumes().removeIf(r ->
//                r.getId() != null && !formIds.contains(r.getId())
//        );
//
//        // 2) 추가: id == null 인 것들
//        for (ResumeForm rf : forms) {
//            if (rf.getId() == null) {
//                Resume.create(teamMember, rf.getContent());
//            }
//        }
//    }
//
    @Transactional
    public void updateTeamMember(TeamMemberForm form) {
        TeamMember teamMember = teamMemberRepository.findWithTeamAndResumesById(form.getId())
                .orElseThrow(() -> new IllegalArgumentException("TeamMember not found: " + form.getId()));

        // 기본 필드 업데이트
        teamMember.setName(form.getName());
        teamMember.setRole(TeamRole.fromOrDefault(form.getRole(), TeamRole.ASSOCIATE));
        teamMember.setPhoto(form.getPhoto());
        teamMember.setIntroduction(form.getIntroduction());

        // 팀 변경
        if (!Objects.equals(form.getTeamId(), teamMember.getTeam().getId())) {
            Team team = teamRepository.findById(form.getTeamId())
                    .orElseThrow(() -> new IllegalArgumentException("Team not found: " + form.getTeamId()));
            teamMember.changeTeam(team); // 편의 메서드 (기존 팀에서도 remove까지 해주면 더 좋음)
        }

        // --------------------
        // Resume 처리 (수정/추가/삭제)
        // --------------------
        List<ResumeForm> forms = Optional.ofNullable(form.getResumes()).orElseGet(List::of);

        // 기존 resumes를 id->엔티티 map으로
        Map<Long, Resume> existingById = teamMember.getResumes().stream()
                .filter(r -> r.getId() != null)
                .collect(Collectors.toMap(Resume::getId, r -> r));

        // 폼에서 넘어온 "유지할(=존재하는) id" 목록
        Set<Long> formIds = forms.stream()
                .map(ResumeForm::getId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        // 1) 수정/추가
        for (ResumeForm rf : forms) {
            if (rf.getId() == null) {
                // 신규 추가
                Resume.create(teamMember, rf.getContent());
            } else {
                // 기존 수정
                Resume existing = existingById.get(rf.getId());
                if (existing == null) {
                    // 선택지 A: 예외로 막기(권장 - 잘못된 요청)
                    throw new IllegalArgumentException("Resume not found: " + rf.getId());

                    // 선택지 B: 무시(원하면 위 throw 대신 continue)
                    // continue;
                }
                existing.setContent(rf.getContent());
            }
        }

        // 2) 삭제: 기존에 있었는데 폼에 없는 것
//        teamMember.getResumes().removeIf(r ->
//                r.getId() != null && !formIds.contains(r.getId())
//        );

        for (Iterator<Resume> it = teamMember.getResumes().iterator(); it.hasNext();) {
            Resume r = it.next();
            if (r.getId()!=null && !formIds.contains(r.getId())) {
                it.remove();
                r.setTeamMember(null);
            }
        }
    }

    @Transactional(readOnly = true)
    public Set<LanguageCode> getLanguageCodesById(Long teamMemberId) {
        return teamMemberRepository.findLanguageCodesById(teamMemberId);
    }

    @Transactional(readOnly = true)
    public TeamMemberTranslation getTranslation(Long teamMemberId, LanguageCode languageCode) {
        return teamMemberRepository.findByLanguageCodeAndById(languageCode, teamMemberId).orElse(null);
    }

    @Transactional(readOnly = true)
    public List<ResumeTranslation> getResumesByLanguageCodeInIds(LanguageCode languageCode, List<Long> resumeIds) {
        return teamMemberRepository.findByLanguageCodeAndInIds(languageCode, resumeIds);
    }

    @Transactional
    public void translate(TeamMemberTranslationForm form) {
        LanguageCode languageCode = LanguageCode.valueOf(form.getLanguageCode());

        // TeamMemberTranslation 처리
        TeamMember teamMember = teamMemberRepository.findById(form.getTeamMemberId()).orElseThrow(() -> new IllegalArgumentException("TeamMember not found: " + form.getTeamMemberId()));
        TeamMemberTranslation tmt = teamMemberRepository.findByLanguageCodeAndById(languageCode, form.getTeamMemberId()).orElse(null);

        if (tmt == null) {
            TeamMemberTranslation.create(teamMember, languageCode, form.getName(), form.getIntroduction());
            teamMemberRepository.save(teamMember);
        } else {
            tmt.setName(form.getName());
            tmt.setIntroduction(form.getIntroduction());
        }

        // ResumeTranslation 처리
        for (ResumeTranslationForm rtf : form.getResumes()) {
            Resume resume = resumeRepository.findById(rtf.getResumeId()).orElseThrow(() -> new IllegalArgumentException("Resume not found: " + rtf.getResumeId()));
            ResumeTranslation resumeTranslation = resumeRepository.findByResumeIdAndLanguageCode(resume.getId(), languageCode).orElse(null);

            if (resumeTranslation == null) {
                ResumeTranslation rt = ResumeTranslation.create(resume, languageCode, rtf.getContent());
            } else {
                resumeTranslation.setContent(rtf.getContent());
            }
        }

    }

    @Transactional
    public void reOrder() {
        List<TeamMember> all = teamMemberRepository.findAll();
        all.forEach(TeamMember::reOrder);
    }
}
