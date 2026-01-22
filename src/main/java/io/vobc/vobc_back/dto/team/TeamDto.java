package io.vobc.vobc_back.dto.team;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TeamDto {

    private Long id;
    private String name;
    private String description;
    private String icon;
    private int displayOrder;
    private List<TeamMemberDto> members = new ArrayList<>();

    public TeamDto(Long id, String name, String description, String icon, int displayOrder) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.icon = icon;
        this.displayOrder = displayOrder;
    }
}
