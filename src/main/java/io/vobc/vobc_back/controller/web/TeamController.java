package io.vobc.vobc_back.controller.web;

import io.vobc.vobc_back.domain.LanguageCode;
import io.vobc.vobc_back.domain.team.Team;
import io.vobc.vobc_back.domain.team.TeamMember;
import io.vobc.vobc_back.domain.team.TeamTranslation;
import io.vobc.vobc_back.dto.team.TeamForm;
import io.vobc.vobc_back.dto.team.TeamMemberForm;
import io.vobc.vobc_back.dto.team.TeamTranslationForm;
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
import java.util.Set;
import java.util.stream.Collectors;

@Controller
@RequiredArgsConstructor
@RequestMapping("/team")
public class TeamController {

    private final TeamService teamService;

    @GetMapping("/list")
    public String teamList(@PageableDefault(size = 10, sort = {"displayOrder", "id"}, direction = Sort.Direction.ASC) Pageable pageable,
                           Model model) {
        Page<Team> page = teamService.getAllTeam(pageable);
        model.addAttribute("teams", page.getContent());
        return "team/list";
    }

    @GetMapping("/create")
    public String createForm(Model model) {
        TeamForm teamForm = new TeamForm();
        model.addAttribute("form", teamForm);
        return "team/createForm";
    }

    @GetMapping("/{teamId}")
    public String teamDetail(@PathVariable Long teamId,
                             Model model) {
        Team team = teamService.getTeamWithMembersById(teamId);
        Set<LanguageCode> languageCodes = teamService.getLanguageCodes(teamId);
        model.addAttribute("team", team);
        model.addAttribute("languageCodes", languageCodes);
        return "team/detail";
    }

    @GetMapping("/{teamId}/edit")
    public String editForm(@PathVariable Long teamId,
                           @PageableDefault(size = 10, sort = "name", direction = Sort.Direction.ASC) Pageable pageable,
                           Model model) {

        Team team = teamService.getTeamWithMembersById(teamId);

        TeamForm teamForm = new TeamForm(team);

        List<TeamMemberForm> list = team.getMembers().stream().map(TeamMemberForm::new).toList();
        teamForm.setMembers(list);

        Set<Long> selectedIds = team.getMembers().stream().map(TeamMember::getId).collect(Collectors.toSet());

        model.addAttribute("form", teamForm);
        model.addAttribute("selectedIds", selectedIds);

        return "team/editForm";
    }

    @PostMapping("/create")
    public String create(TeamForm form) {
        Long teamId = teamService.saveTeam(form);
        return "redirect:/team/" + teamId;
    }

    @PostMapping("/{teamId}/edit")
    public String edit(@PathVariable Long teamId,
                       @ModelAttribute("form") TeamForm form) {
        teamService.updateTeam(form);
        return "redirect:/team/" + teamId;
    }

    @PostMapping("/{teamId}/delete")
    public String delete(@PathVariable Long teamId) {
        teamService.delete(teamId);
        return "redirect:/team/list";
    }


    @GetMapping("/{teamId}/translate")
    public String translateForm(@PathVariable Long teamId,
                                @RequestParam String lang,
                                Model model) {
        LanguageCode languageCode = LanguageCode.from(lang);

        Team team = teamService.getTeamWithTranslationsById(teamId);
        TeamForm teamForm = new TeamForm(team);
        TeamTranslation teamTranslation = team.getTranslations().stream().filter(t -> t.getLanguageCode().equals(languageCode)).findFirst().orElse(null);

        TeamTranslationForm teamTranslationForm;
        if (teamTranslation == null) {
            teamTranslationForm = new TeamTranslationForm();
            teamTranslationForm.setTeamId(teamId);
            teamTranslationForm.setName(team.getName());
            teamTranslationForm.setDescription(team.getDescription());
            teamTranslationForm.setLanguageCode(languageCode);
        } else {
            teamTranslationForm = new TeamTranslationForm(teamTranslation);
        }

        model.addAttribute("teamForm", teamForm);
        model.addAttribute("translationForm", teamTranslationForm);

        return "team/translationForm";
    }

    @PostMapping("/{teamId}/translate")
    public String translate(@PathVariable Long teamId,
                            @ModelAttribute TeamTranslationForm form) {
        teamService.translate(form);
        return "redirect:/team/" + teamId + "/translate?lang=" + form.getLanguageCode().getCode();
    }

    @GetMapping("/member/create")
    public String createMemberForm(@RequestParam Long teamId,
                                   Model model) {
        Team team = teamService.getTeamById(teamId);
        model.addAttribute("team", team);
        TeamMemberForm teamMemberForm = new TeamMemberForm();
        model.addAttribute("form", teamMemberForm);
        return "team/member/createForm";
    }


    @PostMapping("/member/create")
    public String createMember(@RequestParam Long teamId,
                               @ModelAttribute TeamMemberForm form) {
        Long memberId = teamService.createMember(teamId, form);
        return "redirect:/team-member/" + memberId;
    }


    @GetMapping("/member/list")
    public String memberList(@RequestParam Long teamId,
                             @PageableDefault(size = 10, sort = "rolePriority", direction = Sort.Direction.ASC) Pageable pageable,
                             Model model) {
        Page<TeamMember> teamMembers = teamService.getAllMemberByTeamId(teamId, pageable);
        Team team = teamService.getTeamById(teamId);
        model.addAttribute("team", team);
        model.addAttribute("members", teamMembers);
        return "team/member/list";
    }

    @GetMapping("/member/{memberId}")
    public String memberDetail(@RequestParam Long teamId,
                               @PathVariable Long memberId,
                               Model model) {
        Team team = teamService.getTeamById(teamId);
        TeamMember teamMember = teamService.getMemberById(teamId, memberId);
        model.addAttribute("member", teamMember);
        model.addAttribute("team", team);
        return "team/member/detail";
    }

    @GetMapping("/member/{memberId}/edit")
    public String memberEditForm(@RequestParam Long teamId,
                                 @PathVariable Long memberId,
                                 @ModelAttribute TeamMemberForm form,
                                 Model model) {
        TeamMember teamMember = teamService.getMemberById(teamId, memberId);
        TeamMemberForm teamMemberForm = new TeamMemberForm(teamMember);
        model.addAttribute("form", teamMemberForm);
        Team team = teamService.getTeamById(teamId);
        model.addAttribute("team", team);
        return "redirect:/team/member/" + memberId;
    }


    @PostMapping("/member/{memberId}/edit")
    public String editMember(@RequestParam Long teamId,
                             @PathVariable Long memberId,
                             @ModelAttribute TeamMemberForm form,
                             Model model) {
        TeamMemberForm teamMember = teamService.updateTeamMember(teamId, memberId, form);
        model.addAttribute("form", teamMember);
        Team team = teamService.getTeamById(teamId);
        model.addAttribute("team", team);
        return "redirect:/team/member/" + memberId;
    }

    @GetMapping("/member/api/allmembers")
    @ResponseBody
    public Page<TeamMember> list(@PageableDefault(size = 10, sort = "rolePriority", direction = Sort.Direction.ASC) Pageable pageable) {
        return teamService.getAllTeamMembers(pageable);
    }

}
