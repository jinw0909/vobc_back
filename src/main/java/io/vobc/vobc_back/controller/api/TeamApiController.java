package io.vobc.vobc_back.controller.api;

import io.vobc.vobc_back.domain.LanguageCode;
import io.vobc.vobc_back.domain.team.Team;
import io.vobc.vobc_back.dto.team.TeamDto;
import io.vobc.vobc_back.dto.team.TeamMemberDto;
import io.vobc.vobc_back.service.team.TeamQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/team")
@RequiredArgsConstructor
public class TeamApiController {

    private final TeamQueryService teamQueryService;

    @GetMapping("/all-members")
    public Page<TeamMemberDto> all(@PageableDefault(size = 50, sort = "rolePriority", direction = Sort.Direction.ASC) Pageable pageable,
                      @RequestParam String lang) {
        LanguageCode languageCode = LanguageCode.from(lang);
        return teamQueryService.getAllMembers(pageable, languageCode);
    }

    @GetMapping("/members")
    public String members(@PageableDefault Pageable pageable,
                          @RequestParam String team) {

        Page<Team> members = teamQueryService.getMembersOfTeam(pageable, team);
        return "";
    }


    @GetMapping("/all")
    public List<TeamDto> all(@RequestParam String lang) {
        LanguageCode languageCode = LanguageCode.from(lang);
        return teamQueryService.getAll(languageCode);
    }
}
