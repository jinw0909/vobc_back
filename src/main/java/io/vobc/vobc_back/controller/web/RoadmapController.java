package io.vobc.vobc_back.controller.web;

import io.vobc.vobc_back.domain.LanguageCode;
import io.vobc.vobc_back.domain.roadmap.Roadmap;
import io.vobc.vobc_back.domain.roadmap.RoadmapTranslation;
import io.vobc.vobc_back.dto.roadmap.RoadmapForm;
import io.vobc.vobc_back.dto.roadmap.RoadmapTranslationForm;
import io.vobc.vobc_back.service.RoadmapService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Set;

@Controller
@RequiredArgsConstructor
@RequestMapping("/roadmap")
public class RoadmapController {

    private final RoadmapService roadmapService;

    @GetMapping("/list")
    public String roadmapList(@PageableDefault(size = 10) Pageable pageable,
                              Model model) {
        Page<Roadmap> list = roadmapService.getRoadmapList(pageable);
        model.addAttribute("roadmapList", list);
        return "roadmap/list";
    }

    @GetMapping("/{roadmapId}")
    public String roadmapDetail(@PathVariable Long roadmapId,
                                Model model) {
        Roadmap roadmap = roadmapService.getRoadmapById(roadmapId);
        model.addAttribute("roadmap", roadmap);
        return "roadmap/detail";
    }

    @GetMapping("/new")
    public String createForm(Model model) {
        RoadmapForm roadmapForm = new RoadmapForm();
        model.addAttribute("form", roadmapForm);
        return "roadmap/createForm";
    }

    @GetMapping("/edit")
    public String editForm(@RequestParam Long roadmapId,
                           Model model) {
        Roadmap roadmap = roadmapService.getRoadmapById(roadmapId);
        RoadmapForm roadmapForm = new RoadmapForm(roadmap);
        model.addAttribute("form", roadmapForm);
        return "roadmap/editForm";
    }

    @PostMapping("/{roadmapId}/new")
    public String create(@PathVariable Long roadmapId,
                         @ModelAttribute RoadmapForm form) {
        Long createdId = roadmapService.create(roadmapId, form);
        return "redirect:/roadmap/" + createdId;
    }

    @PostMapping("/{roadmapId}/edit")
    public String edit(@PathVariable Long roadmapId,
                       @ModelAttribute RoadmapForm form) {
        Long editedId = roadmapService.edit(roadmapId, form);
        return "redirect:/roadmap.css/" + editedId;
    }

    @GetMapping("/translate")
    public String translateForm(@RequestParam Long roadmapId,
                                @RequestParam String lang,
                                Model model) {
        LanguageCode languageCode = LanguageCode.from(lang);

        Roadmap roadmap = roadmapService.getRoadmapById(roadmapId);
        RoadmapForm roadmapForm = new RoadmapForm(roadmap);

        RoadmapTranslation roadmapTranslation = roadmapService.getTranslationByIdAndLangCode(roadmapId, languageCode);
        RoadmapTranslationForm roadmapTranslationForm = new RoadmapTranslationForm(roadmapTranslation);
        Set<LanguageCode> languageCodes = roadmapService.getAllLangCodeById(roadmapId);

        model.addAttribute("lang", languageCode.getCode());
        model.addAttribute("languageCodes", languageCodes);
        model.addAttribute("roadmapForm", roadmapForm);
        model.addAttribute("translationForm", roadmapTranslationForm);

        return "roadmap/translationForm";
    }
}
