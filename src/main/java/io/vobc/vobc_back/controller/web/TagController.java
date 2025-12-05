package io.vobc.vobc_back.controller.web;

import io.vobc.vobc_back.domain.Post;
import io.vobc.vobc_back.domain.Tag;
import io.vobc.vobc_back.exception.DuplicateTagException;
import io.vobc.vobc_back.service.TagService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/tag")
@RequiredArgsConstructor
public class TagController {

    private final TagService tagService;

    @PostMapping("/create")
    public String createTag(@RequestParam String name, Model model) {
        try {
            tagService.createTag(name);
            return "redirect:/tag/list";
        } catch (DuplicateTagException e) {
            model.addAttribute("errorMessage", e.getMessage());
            model.addAttribute("name", name);
            return "tag/create";
        }
    }

    @GetMapping("/list")
    public String tagList(Model model) {
        List<Tag> tags = tagService.getAllTags();
        model.addAttribute("tags", tags);
        return "tag/list";
    }

    @GetMapping("/create")
    public String createTagView(Model model) {
        if (!model.containsAttribute("name")) {
            model.addAttribute("name", "");
        }
        return "tag/create";
    }

    @GetMapping("/detail")
    public String tagDetail(@RequestParam Long id, Model model) {
        Tag tag = tagService.getTag(id);
        model.addAttribute("tag", tag);
        return "tag/detail";
    }

    @GetMapping("/delete")
    public String deleteTag(@RequestParam Long id) {
        tagService.deleteTag(id);
        return "redirect:/tag/list";
    }

    @PostMapping("/update")
    public String updateTag(@RequestParam Long id, @RequestParam String name, RedirectAttributes redirectAttributes) {
        try {
            tagService.updateTag(id, name);
            return "redirect:/tag/detail?id=" + id;
        } catch (DuplicateTagException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            redirectAttributes.addFlashAttribute("name", name);
            return "redirect:/tag/detail?id=" + id;
        }
    }

    @GetMapping("/posts")
    public String getPostsByTag(@RequestParam Long id, Model model) {
        Tag tag = tagService.getTag(id);
        List<Post> posts = tagService.getPostsByTagId(id);

        model.addAttribute("tag", tag);
        model.addAttribute("posts", posts);

        return "tag/posts";
    }

}
