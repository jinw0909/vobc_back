package io.vobc.vobc_back.controller.web;

import io.vobc.vobc_back.domain.LanguageCode;
import io.vobc.vobc_back.domain.team.*;
import io.vobc.vobc_back.dto.team.ResumeForm;
import io.vobc.vobc_back.dto.team.ResumeTranslationForm;
import io.vobc.vobc_back.dto.team.TeamMemberForm;
import io.vobc.vobc_back.dto.team.TeamMemberTranslationForm;
import io.vobc.vobc_back.service.team.TeamMemberService;
import io.vobc.vobc_back.service.team.TeamService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/team-member")
@RequiredArgsConstructor
public class TeamMemberController {

    private final TeamMemberService teamMemberService;
    private final TeamService teamService;

    @GetMapping("/create")
    public String createForm(Model model) {
        TeamMemberForm teamMemberForm = new TeamMemberForm();
        List<Team> list = teamService.getAll();

        model.addAttribute("form", teamMemberForm);
        model.addAttribute("teams", list);

        return "team-member/createForm";
    }

    @PostMapping("/create")
    public String create(@ModelAttribute TeamMemberForm form) {
        Long teamMemberId = teamService.save(form);
        return "redirect:/team-member/" + teamMemberId;
    }

    @GetMapping("/{teamMemberId}")
    public String teamMemberDetail(@PathVariable Long teamMemberId,
                                   Model model) {
        TeamMember teamMember = teamMemberService.getWithResumesById(teamMemberId);
        Set<LanguageCode> languageCodes = teamMemberService.getLanguageCodesById(teamMemberId);

        model.addAttribute("teamMember", teamMember);
        model.addAttribute("languageCodes", languageCodes);

        return "team-member/detail";
    }

//    @GetMapping("/list")
//    public String list(@PageableDefault(size = 10, sort = "rolePriority", direction = Sort.Direction.ASC) Pageable pageable,
//                       Model model) {
//        Page<TeamMember> list = teamMemberService.getAll(pageable);
//        List<Team> teams = teamService.getAll();
//        model.addAttribute("teamMembers", list);
//        model.addAttribute("teams", teams);
//        return "team-member/list";
//    }

    @GetMapping("/{teamMemberId}/edit")
    public String editForm(@PathVariable Long teamMemberId,
                           Model model) {

        TeamMember teamMember = teamMemberService.getWithResumesById(teamMemberId);
        TeamMemberForm teamMemberForm = new TeamMemberForm(teamMember);

        List<ResumeForm> resumes = teamMember.getResumes().stream().map(ResumeForm::new).toList();
        teamMemberForm.setResumes(resumes);
        List<Team> teams = teamService.getAll();

        model.addAttribute("form", teamMemberForm);
        model.addAttribute("teams", teams);

        return "team-member/editForm";
    }

    @PostMapping("/{teamMemberId}/edit")
    public String edit(@PathVariable Long teamMemberId,
                       @ModelAttribute TeamMemberForm form) {

        if (!teamMemberId.equals(form.getId())) {
            throw new IllegalArgumentException("Path id and form id mismatch");
        }

        teamMemberService.updateTeamMember(form);
        return "redirect:/team-member/" + teamMemberId;
    }

    @PostMapping("/{teamMemberId}/delete")
    public String delete(@PathVariable Long teamMemberId) {
        teamMemberService.delete(teamMemberId);
        return "redirect:/team-member/list";
    }

    @GetMapping("/{teamMemberId}/translate")
    public String translationForm(@PathVariable Long teamMemberId,
                                  @RequestParam String lang,
                                  Model model) {

        LanguageCode languageCode = LanguageCode.from(lang);

        TeamMember teamMember = teamMemberService.getWithResumesById(teamMemberId);

        TeamMemberTranslation tmt = teamMemberService.getTranslation(teamMemberId, languageCode);

        TeamMemberTranslationForm form;
        if (tmt != null) {
            form = new TeamMemberTranslationForm(tmt);
            form.setTeamMemberId(teamMemberId);
            form.setLanguageCode(languageCode.name());
        } else {
            form = new TeamMemberTranslationForm();
            form.setId(null);
            form.setTeamMemberId(teamMemberId);
            form.setLanguageCode(languageCode.name());
            form.setName(teamMember.getName());
            form.setIntroduction(teamMember.getIntroduction());
        }

        // 번역들 조회
        List<Long> resumeIds = teamMember.getResumes().stream().map(Resume::getId).toList();
        List<ResumeTranslation> resumeTranslations =
                teamMemberService.getResumesByLanguageCodeInIds(languageCode, resumeIds);

        // resumeId -> translation
        Map<Long, ResumeTranslation> trMap = resumeTranslations.stream()
                .collect(Collectors.toMap(rt -> rt.getResume().getId(), Function.identity()));

        // ✅ 원본 resume 기준으로 폼 리스트를 완성
        List<ResumeTranslationForm> resumeForms = teamMember.getResumes().stream()
                .map(resume -> {
                    ResumeTranslation rt = trMap.get(resume.getId());
                    ResumeTranslationForm rf;
                    if (rt != null) {
                        // 기존 번역
                        rf = new ResumeTranslationForm(rt);
                        rf.setResumeId(resume.getId()); // 안전하게 보장
                        rf.setLanguageCode(languageCode.name());
                    } else {
                        // 번역 없으면 원본으로 초기값
                        rf = new ResumeTranslationForm();
                        rf.setId(null); // 번역 row id
                        rf.setResumeId(resume.getId()); // ⭐ 이게 핵심
                        rf.setLanguageCode(languageCode.name());
                        rf.setContent(resume.getContent()); // 기본값 = 원본
                    }
                    return rf;
                })
                .toList();

        form.setResumes(resumeForms);

        model.addAttribute("teamMember", teamMember);
        model.addAttribute("lang", languageCode.name().toLowerCase());
        model.addAttribute("teamMemberTranslationForm", form);

        return "team-member/translationForm";
    }


    @PostMapping("/{teamMemberId}/translate")
    public String translate(@PathVariable Long teamMemberId,
                            @ModelAttribute TeamMemberTranslationForm form) {

        teamMemberService.translate(form);
        return "redirect:/team-member/" + teamMemberId + "/translate?lang=" + form.getLanguageCode();
    }

    @PostMapping("/reorder")
    public String reOrder() {
        teamMemberService.reOrder();
        return "redirect:/team-member/list";
    }


    @GetMapping("/list")
    public String list(
            @PageableDefault(size = 10, sort = "rolePriority", direction = Sort.Direction.ASC) Pageable pageable,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) List<Long> teamId,
            @RequestParam(required = false) List<String> role,
            Model model
    ) {
        List<TeamRole> roles = (role == null) ? List.of() :
                role.stream().map(TeamRole::from).filter(Objects::nonNull).toList();

        Page<TeamMember> list = teamMemberService.search(
                pageable,
                (name == null || name.isBlank()) ? null : name,
                (teamId == null || teamId.isEmpty()) ? null : teamId,
                (roles.isEmpty()) ? null : roles
        );

        model.addAttribute("teamMembers", list);
        model.addAttribute("teams", teamService.getAll());

        // ✅ 템플릿에서 항상 안전하게 contains 호출 가능
        model.addAttribute("qName", name);
        model.addAttribute("qTeamIds", teamId == null ? List.of() : teamId);
        model.addAttribute("qRoles", roles);

        // pagination에서 role 유지할 때 쓰기 좋음
        model.addAttribute("qRoleNames", roles.stream().map(Enum::name).toList());

        return "team-member/list";
    }


}
