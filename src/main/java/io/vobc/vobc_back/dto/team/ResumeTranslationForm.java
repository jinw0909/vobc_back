package io.vobc.vobc_back.dto.team;

import io.vobc.vobc_back.domain.team.ResumeTranslation;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ResumeTranslationForm {

    private Long resumeId;
    private Long id;
    private String content;
    private String languageCode;

    public ResumeTranslationForm(ResumeTranslation resumeTranslation) {
        this.resumeId = resumeTranslation.getResume().getId();
        this.id = resumeTranslation.getId();
        this.content = resumeTranslation.getContent();
        this.languageCode = resumeTranslation.getLanguageCode().name();
    }

}
