package io.vobc.vobc_back.dto.team;

import io.vobc.vobc_back.domain.team.TeamMemberTranslation;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TeamMemberTranslationForm {

    private Long teamMemberId;
    private Long id;
    private String languageCode;
    private String name;
    private String introduction;
    private List<ResumeTranslationForm> resumes = new ArrayList<>();

    public TeamMemberTranslationForm(TeamMemberTranslation teamMemberTranslation) {
        this.id = teamMemberTranslation.getId();
        this.name = teamMemberTranslation.getName();
        this.introduction = teamMemberTranslation.getIntroduction();
        this.languageCode = teamMemberTranslation.getLanguageCode().name();
    }
}
