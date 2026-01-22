package io.vobc.vobc_back.dto.team;

import io.vobc.vobc_back.domain.LanguageCode;
import io.vobc.vobc_back.domain.team.TeamTranslation;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TeamTranslationForm {

    private Long teamId;
    private Long id;
    private LanguageCode languageCode;
    private String name;
    private String description;

    public TeamTranslationForm(TeamTranslation teamTranslation) {
        this.teamId = teamTranslation.getTeam().getId();
        this.id = teamTranslation.getId();
        this.languageCode = teamTranslation.getLanguageCode();
        this.name = teamTranslation.getName();
        this.description = teamTranslation.getDescription();
    }
}
