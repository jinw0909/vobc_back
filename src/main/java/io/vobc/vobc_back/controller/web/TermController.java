package io.vobc.vobc_back.controller.web;

import io.vobc.vobc_back.domain.LanguageCode;
import io.vobc.vobc_back.domain.Tag;
import io.vobc.vobc_back.domain.term.Term;
import io.vobc.vobc_back.domain.term.TermTranslation;
import io.vobc.vobc_back.dto.term.TermDto;
import io.vobc.vobc_back.dto.term.TermForm;
import io.vobc.vobc_back.dto.term.TermResponse;
import io.vobc.vobc_back.dto.term.TermTranslationForm;
import io.vobc.vobc_back.service.TagService;
import io.vobc.vobc_back.service.TermQueryService;
import io.vobc.vobc_back.service.TermService;
import io.vobc.vobc_back.service.TermTranslationService;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/term")
@RequiredArgsConstructor
public class TermController {

    private final TermService termService;
    private final TermTranslationService termTranslationService;

    @GetMapping("/create")
    public String createForm(Model model) {
        model.addAttribute("form", new TermForm());
        return "term/form";
    }

    @GetMapping("/list")
    public String list(Model model) {
        List<TermForm> list = termService.findAll();
        model.addAttribute("terms", list);
        return "term/list";
    }

    @GetMapping("/update")
    public String updateForm(@RequestParam Long id, Model model) {
        Term term = termService.findById(id);
        model.addAttribute("form", TermForm.from(term));
        return "term/form";
    }

    @GetMapping("/detail")
    public String detailTerm(@RequestParam Long id, Model model) {
        Term term = termService.findById(id);
        TermForm form = TermForm.from(term);
        List<String> langs = termService.findLanguageCodesById(id).stream()
                .map(lc -> lc.getCode().toLowerCase())
                .toList();
        model.addAttribute("term", form);
        model.addAttribute("langs", langs);
        return "term/detail";
    }

    @PostMapping("/save")
    public String save(@ModelAttribute("form") TermForm form,
                       BindingResult bindingResult) {
        try {
            termService.saveOrUpdate(form);
            return "redirect:/term/list";
        } catch (DataIntegrityViolationException e) {
            bindingResult.rejectValue("termCode", "duplicate", "이미 존재하는 Term Code 입니다.");
            return "term/form";
        }

    }

    @GetMapping("/translate")
    public String editTranslation(@RequestParam Long termId, @RequestParam String lang, Model model) {
        LanguageCode languageCode = LanguageCode.from(lang);

        Term term = termService.findById(termId);
        TermTranslation termTranslation = termTranslationService.findByTermIdAndLanguageCode(termId, languageCode);

        TermForm form = TermForm.from(term);
        TermTranslationForm termTranslationForm = (termTranslation == null) ? TermTranslationForm.empty(termId, languageCode) : TermTranslationForm.from(termTranslation);

        model.addAttribute("form", form);
        model.addAttribute("translationForm", termTranslationForm);

        return "term/translationForm";
    }

    @PostMapping("/add-translation")
    public String addTranslation(@ModelAttribute TermTranslationForm termTranslationForm) {
        termService.upsertTranslation(termTranslationForm);
        return "redirect:/term/translate?termId=" + termTranslationForm.getTermId() + "&lang=" + termTranslationForm.getLanguageCode().getCode();
    }

    @PostMapping("/remove-translation")
    public String removeTranslation(@RequestParam Long termId,
                                    @RequestParam String lang,
                                    @RequestParam Long translationId) {

        termService.removeTranslation(translationId);

        return "redirect:/term/translate?termId=" + termId + "&lang=" + lang;

    }
}
