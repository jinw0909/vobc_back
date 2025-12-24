package io.vobc.vobc_back.service;

import io.vobc.vobc_back.domain.LanguageCode;
import io.vobc.vobc_back.domain.term.TermTranslation;
import io.vobc.vobc_back.repository.term.TermTranslationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TermTranslationService {

    private final TermTranslationRepository termTranslationRepository;


    public TermTranslation findByTermIdAndLanguageCode(Long termId, LanguageCode languageCode) {
        return termTranslationRepository.findByTermIdAndLanguageCode(termId, languageCode).orElse(null);
    }
}
