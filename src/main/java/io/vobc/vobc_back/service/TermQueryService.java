package io.vobc.vobc_back.service;

import io.vobc.vobc_back.domain.term.Term;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class TermQueryService {

    @Transactional(readOnly = true)
    public Term findByIdWithTranslations(Long id) {
        return null;
    }
}
