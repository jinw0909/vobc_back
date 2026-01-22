package io.vobc.vobc_back.dto.team;

import io.vobc.vobc_back.domain.team.TeamMember;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TeamMemberForm {

    private Long teamId;
    private Long id;
    private String name;
    private String role;
    private String photo;
    private String email;
    private String introduction;
    private List<ResumeForm> resumes = new ArrayList<>();

    public TeamMemberForm(TeamMember teamMember) {
        this.teamId = teamMember.getTeam().getId();
        this.id = teamMember.getId();
        this.name = teamMember.getName();
        this.role = teamMember.getRole().name();
        this.photo = teamMember.getPhoto();
        this.introduction = teamMember.getIntroduction();
    }

}
