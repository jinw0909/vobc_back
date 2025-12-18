package io.vobc.vobc_back.dto.term;

import io.vobc.vobc_back.domain.LanguageCode;
import io.vobc.vobc_back.domain.term.Term;
import io.vobc.vobc_back.domain.term.TermCode;
import io.vobc.vobc_back.domain.term.TermTranslation;
import lombok.Data;

import java.time.LocalDate;

@Data
public class TermResponse {

    private Long id;
    private LanguageCode languageCode;
    private TermCode termCode;
    private String title;
    private String content;
    private LocalDate proposeDate;

    public TermResponse(Term term, LanguageCode languageCode) {
        this.id = term.getId();
        this.termCode = term.getTermCode();
        this.proposeDate = term.getProposeDate();

        // 기본값: 원문
        this.languageCode = languageCode;
        this.title = term.getTitle();
        this.content = term.getContent();

        if (languageCode == null) {
            return;
        }

        TermTranslation tr = term.getTranslations().stream()
                .filter(t -> t.getLanguageCode() == languageCode)
                .findFirst()
                .orElse(null);

        if (tr != null) {
            this.title = tr.getTitle();
            this.content = tr.getContent();
        }
    }
}
