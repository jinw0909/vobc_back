package io.vobc.vobc_back.controller.web;

import io.vobc.vobc_back.domain.LanguageCode;
import io.vobc.vobc_back.dto.TranslationForm;
import io.vobc.vobc_back.dto.publisher.PublisherForm;
import io.vobc.vobc_back.dto.publisher.PublisherResponse;
import io.vobc.vobc_back.dto.publisher.PublisherTranslationForm;
import io.vobc.vobc_back.service.PublisherService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Set;

@Slf4j
@Controller
@RequiredArgsConstructor
@RequestMapping("/publisher")
public class PublisherController {

    private final PublisherService publisherService;

    @GetMapping("/save")
    public String saveForm(Model model) {
        model.addAttribute("form", new PublisherForm());
        return "publisher/form";
    }

    @PostMapping("/save")
    public String save(@ModelAttribute PublisherForm form, Model model) {
        Long id = publisherService.savePublisher(form);
        return "redirect:/publisher/" + id;
    }

    @GetMapping("/list")
    public String publishers(@PageableDefault(size = 10, sort = "id", direction = Sort.Direction.DESC) Pageable pageable,
                             Model model
    ) {
        Page<PublisherForm> page = publisherService.getPublishers(pageable);
        model.addAttribute("publishers", page.getContent());
        model.addAttribute("page", page);
        return "publisher/list";
    }

    @GetMapping("/{id}")
    public String publisher(@PathVariable Long id, Model model) {
        PublisherForm publisherForm = publisherService.getPublisher(id);
        Set<LanguageCode> langs = publisherService.getLanguageCodes(id);
        model.addAttribute("publisher", publisherForm);
        model.addAttribute("langs", langs);
        return "publisher/publisher";
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        PublisherForm publisherForm = publisherService.getPublisher(id);
        model.addAttribute("form", publisherForm);
        return "publisher/form";
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id) {
        publisherService.deletePublisher(id);
        return "redirect:/publisher/list";
    }

    @GetMapping("/{id}/translate")
    public String translationForm(@PathVariable Long id,
                                  @RequestParam String lang,
                                  Model model
    ) {
        LanguageCode languageCode = LanguageCode.from(lang);
        PublisherForm publisher = publisherService.getPublisher(id);
        PublisherTranslationForm translation = publisherService.getTranslation(id, languageCode);
        model.addAttribute("publisher", publisher);
        model.addAttribute("translation", translation != null ? translation : new PublisherTranslationForm());

        return "publisher/translation";
    }

    @PostMapping("/{id}/translate")
    public String translate(@PathVariable Long id,
                            @ModelAttribute PublisherTranslationForm form) {
        log.info("Translate publisher {} to {}", id, form.getLanguageCode());
        log.info("Translation form: {}", form);
        LanguageCode languageCode = publisherService.translate(form, id);
        return "redirect:/publisher/" + id + "/translate?lang=" + languageCode.getCode();
    }

    @PostMapping("/{id}/translate/{translationId}/delete-translation")
    public String deleteTranslation(@PathVariable Long id,
                                    @PathVariable Long translationId,
                                    @RequestParam String lang) {
        publisherService.deleteTranslation(translationId);
        return "redirect:/publisher/translation/" + id + "?lang=" + lang;
    }
}
