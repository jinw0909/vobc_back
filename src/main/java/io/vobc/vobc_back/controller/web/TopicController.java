package io.vobc.vobc_back.controller.web;

import io.vobc.vobc_back.domain.article.Topic;
import io.vobc.vobc_back.dto.TopicForm;
import io.vobc.vobc_back.service.TopicService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/topic")
@RequiredArgsConstructor
public class TopicController {

    private final TopicService topicService;

    @GetMapping("/new")
    public String createForm(Model model) {
        TopicForm form = new TopicForm();
        model.addAttribute("form", form);
        return "topic/createForm";
    }

    @PostMapping("/new")
    public String create(@ModelAttribute TopicForm form) {
        Long topicId = topicService.save(form);
        return "redirect:/topic/" + topicId;
    }

    @GetMapping("/list")
    public String list(@PageableDefault(size = 10, sort = "id", direction = Sort.Direction.DESC) Pageable pageable,
                       Model model) {
        Page<Topic> list = topicService.getAll(pageable);
        model.addAttribute("list", list);
        return "topic/list";
    }

    @GetMapping("/{topicId}")
    public String detail(@PathVariable Long topicId,
                         Model model) {
        Topic topic = topicService.getOneById(topicId);
        model.addAttribute("topic", topic);
        return "topic/detail";
    }

    @GetMapping("/{topicId}/edit")
    public String editForm(@PathVariable Long topicId,
                           Model model) {
        Topic topic = topicService.getOne(topicId);
        TopicForm topicForm = new TopicForm(topic);
        model.addAttribute("form", topicForm);
        return "topic/editForm";
    }

    @PostMapping("/{topicId}/edit")
    public String edit(@PathVariable Long topicId,
                       @ModelAttribute TopicForm topicForm) {
        Long updatedId = topicService.update(topicId, topicForm);
        return "redirect:/topic/" + updatedId;
    }

}
