package io.vobc.vobc_back.dto.roadmap;

import io.vobc.vobc_back.domain.LanguageCode;
import io.vobc.vobc_back.domain.roadmap.RoadmapTranslation;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Data
@Getter @Setter
@NoArgsConstructor
public class RoadmapTranslationForm {

    private Long roadmapId;
    private Long id;
    private LanguageCode languageCode;
    private String title;
    private String description;
    private String content;

    public RoadmapTranslationForm(RoadmapTranslation translation) {
        this.id = translation.getId();
        this.title = translation.getTitle();
        this.description = translation.getDescription();
        this.content = translation.getContent();
    }

}
