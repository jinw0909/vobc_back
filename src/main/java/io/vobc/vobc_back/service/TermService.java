package io.vobc.vobc_back.service;

import io.vobc.vobc_back.domain.LanguageCode;
import io.vobc.vobc_back.domain.term.Term;
import io.vobc.vobc_back.domain.term.TermCode;
import io.vobc.vobc_back.domain.term.TermTranslation;
import io.vobc.vobc_back.dto.term.TermDto;
import io.vobc.vobc_back.dto.term.TermForm;
import io.vobc.vobc_back.dto.term.TermResponse;
import io.vobc.vobc_back.dto.term.TermTranslationForm;
import io.vobc.vobc_back.repository.term.TermRepository;
import io.vobc.vobc_back.repository.term.TermTranslationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class TermService {

    private final TermRepository termRepository;
    private final TermTranslationRepository termTranslationRepository;

    @Transactional(readOnly = true)
    public Term findById(Long id) {
        return termRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Term not found: " + id));
    }

    @Transactional
    public void upsertTranslation (TermTranslationForm request) {
        Long termId = request.getTermId();
        LanguageCode languageCode = request.getLanguageCode();

        Optional<TermTranslation> opt = termTranslationRepository.findByTermIdAndLanguageCode(termId, languageCode);

        if (opt.isPresent()) {
            TermTranslation translation = opt.get();
            translation.setTitle(request.getTitle());
            translation.setContent(request.getContent());
            return;
        }

        Term term = termRepository.findById(termId).orElseThrow(() -> new IllegalArgumentException("Term not found: " + termId));
        TermTranslation termTranslation = TermTranslation.create(request.getTitle(), request.getContent(), languageCode);
        term.addTranslation(termTranslation);
        termRepository.save(term);
    }

//    @Transactional
//    public Long saveOrUpdate(TermForm form) {
//        if (form.getTermId() == null) {
//            Term term = Term.create(form.getTermCode(), form.getTitle(), form.getContent(), form.getProposeDate());
//            termRepository.save(term);
//            return term.getId();
//        } else {
//            Term term = termRepository.findById(form.getTermId()).orElseThrow(() -> new IllegalArgumentException("Term not found: " + form.getTermId()));
//            term.setTitle(form.getTitle());
//            term.setContent(form.getContent());
//            term.setTermCode(form.getTermCode());
//            term.setProposeDate(form.getProposeDate());
//            return term.getId();
//        }
//    }
    @Transactional
    public Long saveOrUpdate(TermForm form) {
        if (form.getTermId() == null) {
            Term term = Term.create(
                    form.getTermCode(),
                    form.getTitle(),
                    form.getContent(),
                    form.getProposeDate()
            );
            return termRepository.save(term).getId();
        }

        Term term = termRepository.findById(form.getTermId())
                .orElseThrow(() -> new IllegalArgumentException("Term not found: " + form.getTermId()));

        term.setTitle(form.getTitle());
        term.setContent(form.getContent());
        term.setTermCode(form.getTermCode());   // 변경 허용할거면 유지
        term.setProposeDate(form.getProposeDate());
        return term.getId();
    }


    @Transactional(readOnly = true)
    public List<TermForm> findAll() {
//        return termRepository.findAll().stream().map(TermForm::from).toList();
        return termRepository.findAllByOrderByIdAsc().stream().map(TermForm::from).toList();
    }

    @Transactional(readOnly = true)
    public TermDto findWithTranslationsById(Long id) {
        Term term = termRepository.findWithTranslationsById(id).orElseThrow(() -> new IllegalArgumentException("Could not find term with id: " + id));
        return new TermDto(term);
    }

    @Transactional(readOnly = true)
    public TermResponse getLocalizedTerm(TermCode termCode, LanguageCode languageCode) {
        Term term = termRepository.findByTermCode(termCode).orElseThrow(() -> new IllegalArgumentException("Term not found: " + termCode));
        return new TermResponse(term, languageCode);
    }

    public Set<LanguageCode> findLanguageCodesById(Long id) {
        return termTranslationRepository.findAllLanguageCodesByTermId(id);
    }

    @Transactional
    public void removeTranslation(Long translationId) {
        TermTranslation termTranslation = termTranslationRepository.findById(translationId).orElseThrow(() -> new IllegalArgumentException("Translation not found: " + translationId));
        termTranslationRepository.delete(termTranslation);
    }
}
