package io.vobc.vobc_back.dto.team;

import io.vobc.vobc_back.domain.team.TeamRole;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
public class TeamMemberDto {

    private Long teamId;
    private Long id;
    private String name;
    private String email;
    private String introduction;
    private TeamRole role;
    private String photo;
    private int rolePriority;

    private List<ResumeDto> resumes = new ArrayList<>();
    private String teamRoleLabel;

    public TeamMemberDto(Long teamId, Long id, String name, String email, String introduction, TeamRole role, String photo, int rolePriority) {
        this.teamId = teamId;
        this.id = id;
        this.name = name;
        this.email = email;
        this.introduction = introduction;
        this.role = role;
        this.photo = photo;
        this.rolePriority = rolePriority;
    }
}
