package io.vobc.vobc_back.dto.term;

import io.vobc.vobc_back.domain.LanguageCode;
import io.vobc.vobc_back.domain.term.TermTranslation;
import lombok.Data;

@Data
public class TermTranslationForm {

    private Long id;
    private Long termId;
    private LanguageCode languageCode;
//    private TermCode termCode;
    private String title;
    private String content;
//    private LocalDate proposeDate;

    public static TermTranslationForm from(TermTranslation termTranslation) {
        TermTranslationForm termTranslationForm = new TermTranslationForm();
        termTranslationForm.setId(termTranslation.getId());
        termTranslationForm.setTermId(termTranslation.getTerm().getId());
        termTranslationForm.setLanguageCode(termTranslation.getLanguageCode());
//        termTranslationForm.setTermCode(termTranslation.getTerm().getTermCode());
        termTranslationForm.setTitle(termTranslation.getTitle());
        termTranslationForm.setContent(termTranslation.getContent());
//        termTranslationForm.setProposeDate(termTranslation.getTerm().getProposeDate());
        return termTranslationForm;
    }

    public static TermTranslationForm empty(Long termId, LanguageCode languageCode) {
        TermTranslationForm form = new TermTranslationForm();
        form.setTermId(termId);
        form.setLanguageCode(languageCode);
        return form;
    }


}
