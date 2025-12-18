package io.vobc.vobc_back.controller.api;

import io.vobc.vobc_back.domain.LanguageCode;
import io.vobc.vobc_back.domain.term.TermCode;
import io.vobc.vobc_back.dto.term.TermResponse;
import io.vobc.vobc_back.service.TermService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/term")
@RequiredArgsConstructor
public class TermApiController {

    private final TermService termService;

    @GetMapping
    public TermResponse findOne(
            @RequestParam(defaultValue = "en") String lang,
            @RequestParam String code
    ) {
        LanguageCode languageCode = LanguageCode.from(lang);
        TermCode termCode = TermCode.from(code);

        return termService.getLocalizedTerm(termCode, languageCode);
    }
}
