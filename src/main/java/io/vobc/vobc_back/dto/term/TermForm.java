package io.vobc.vobc_back.dto.term;

import io.vobc.vobc_back.domain.term.Term;
import io.vobc.vobc_back.domain.term.TermCode;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter @Setter
public class TermForm {

    private Long termId;
    private TermCode termCode;
    private String title;
    private String content;
    private LocalDate proposeDate;

    public static TermForm from(Term term) {
        TermForm termForm = new TermForm();
        termForm.setTermId(term.getId());
        termForm.setTermCode(term.getTermCode());
        termForm.setTitle(term.getTitle());
        termForm.setContent(term.getContent());
        termForm.setProposeDate(term.getProposeDate());
        return termForm;
    }
}
