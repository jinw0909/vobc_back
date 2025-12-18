package io.vobc.vobc_back.dto.term;

import io.vobc.vobc_back.domain.LanguageCode;
import io.vobc.vobc_back.domain.term.Term;
import io.vobc.vobc_back.domain.term.TermCode;
import io.vobc.vobc_back.domain.term.TermTranslation;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDate;
import java.util.List;

@Data
public class TermDto {

    private Long termId;
    private TermCode termCode;
    private String title;
    private String content;
    private LocalDate proposeDate;
    private List<TermTranslationDto> translations;

    public TermDto(Term term) {
        termId = term.getId();
        termCode = term.getTermCode();
        title = term.getTitle();
        content = term.getContent();
        proposeDate = term.getProposeDate();
        translations = term.getTranslations().stream().map(TermTranslationDto::new).toList();
    }

    @Data
    static class TermTranslationDto {
        private LanguageCode languageCode;
        private String title;
        private String content;

        public TermTranslationDto(TermTranslation termTranslation) {
            languageCode = termTranslation.getLanguageCode();
            title = termTranslation.getTitle();
            content = termTranslation.getContent();
        }
    }

}
