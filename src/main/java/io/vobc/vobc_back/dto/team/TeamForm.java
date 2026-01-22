package io.vobc.vobc_back.dto.team;

import io.vobc.vobc_back.domain.team.Team;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TeamForm {

    private Long id;
    private String name;
    private String description;
    private String icon;
    private int displayOrder;
    private List<TeamMemberForm> members = new ArrayList<>();

    public TeamForm(Team team) {
        this.id = team.getId();
        this.name = team.getName();
        this.description = team.getDescription();
        this.icon = team.getIcon();
        this.displayOrder = team.getDisplayOrder();
    }
}
