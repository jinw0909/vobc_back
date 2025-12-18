package io.vobc.vobc_back.controller.web;

import io.vobc.vobc_back.domain.LanguageCode;
import io.vobc.vobc_back.domain.Post;
import io.vobc.vobc_back.domain.PostTag;
import io.vobc.vobc_back.domain.Translation;
import io.vobc.vobc_back.dto.*;
import io.vobc.vobc_back.dto.post.*;
import io.vobc.vobc_back.security.CustomUserDetails;
import io.vobc.vobc_back.service.PostService;
import io.vobc.vobc_back.service.TagService;
import io.vobc.vobc_back.service.TranslationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/post")
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;
    private final TagService tagService;
    private final TranslationService translationService;

    @GetMapping("/list")
    public String list(Model model, @PageableDefault(size = 10) Pageable pageable) {

        List<PostForm> posts = postService.getPosts(pageable).stream()
                .map(PostForm::from)
                .toList();

        model.addAttribute("posts", posts);
        return "post/list";
    }

    @GetMapping("/detail")
    public String detail(@RequestParam Long id, Model model) {

//        Post post = postService.findWithTagsById(id);
        model.addAttribute("post", postService.getPost(id));
        model.addAttribute("allTags", tagService.getAllTags());

        List<String> langs = postService.getLanguageCodesById(id).stream()
                .map(lc -> lc.getCode().toLowerCase())
                .toList();

        model.addAttribute("langs", langs);
        return "post/detail";
    }

    @GetMapping("/create")
    public String createForm(Model model) {
        model.addAttribute("post", new Post());
        model.addAttribute("allTags", tagService.getAllTags());
        return "post/form";
    }

    @GetMapping("/update")
    public String editForm(@RequestParam Long id, Model model) {
        model.addAttribute("post", postService.getPost(id));
        model.addAttribute("allTags", tagService.getAllTags());
        return "post/form";
    }

    @PostMapping("/create")
    public String create(@AuthenticationPrincipal CustomUserDetails userDetails, @Valid PostCreateRequest request) {
        Long memberId = userDetails.getId();
        postService.createPost(memberId, request);
        return "redirect:/post/list";
    }

    @PostMapping("/update")
    public String update(@RequestParam Long id, @Valid PostUpdateRequest request) {
        Post post = postService.updatePost(id, request);
        return "redirect:/post/detail?id=" + post.getId();
    }

    @PostMapping("/delete")
    public String delete(@RequestParam Long id) {
        postService.deletePost(id);
        return "redirect:/post/list";
    }

    @PostMapping("/tag/add")
    @ResponseBody
    public List<PostTagResponse> addTagToPost(@RequestParam Long postId,
                                              @RequestParam Long tagId) {
        List<PostTag> list = postService.addTagToPost(postId, tagId);
        return list.stream()
                .map(PostTagResponse::from)
                .collect(Collectors.toList());
    }

    @PostMapping("/tag/remove")
    @ResponseBody
    public List<PostTagResponse> removeTagFromPost(@RequestParam Long postId,
                                                   @RequestParam Long tagId) {

        List<PostTag> list = postService.removeTagFromPost(postId, tagId);
        return list.stream()
                .map(PostTagResponse::from)
                .collect(Collectors.toList());
    }

    @PostMapping("/tag/order")
    @ResponseBody
    public List<PostTagResponse> updateTagOrder(@RequestParam Long postId,
                                                @RequestParam Long tagId,
                                                @RequestParam Integer sortOrder) {

        List<PostTag> list = postService.updateTagOrder(postId, tagId, sortOrder);
        return list.stream()
                .map(PostTagResponse::from)
                .collect(Collectors.toList());

    }

    @GetMapping("/related")
    @ResponseBody
    public List<PostResponse> related(@RequestParam Long id, @RequestParam String languageCode) {
        List<Post> relatedPosts = postService.findRelatedPosts(id);
        LanguageCode language = LanguageCode.from(languageCode);
        return relatedPosts.stream()
                .map(p -> PostResponse.of(p, null, language))
                .toList();
    }

    @GetMapping("/translate")
    public String translationForm(@RequestParam Long id,
                                  @RequestParam String languageCode,
                                  Model model) {

        Post post = postService.getPost(id);
        LanguageCode language = LanguageCode.from(languageCode);
        Optional<Translation> existing = translationService.findByPostIdAndLanguageCode(id, language);

        TranslationForm form = existing
                .map(TranslationForm::from)
                .orElseGet(() -> {
                    TranslationForm tf = TranslationForm.empty(id, language);
                    tf.setTitle(post.getTitle());
                    tf.setSummary(post.getSummary());
                    tf.setContent(post.getContent());
                    tf.setAuthor(post.getAuthor());
                    return tf;
                });

        model.addAttribute("post", post);
        model.addAttribute("translationForm", form);
        model.addAttribute("language", language);
        model.addAttribute("hasTranslation", existing.isPresent());

        return "post/translationForm";
    }

    @PostMapping("/translate")
    public String saveTranslation(@ModelAttribute TranslationForm form) {
        translationService.saveOrUpdate(form);
        return "redirect:/post/detail?id=" + form.getPostId();
    }

    @PostMapping("/translate/delete")
    public String deleteTranslation(@RequestParam Long postId,
                                    @RequestParam String lang) {

        LanguageCode languageCode = LanguageCode.from(lang);

        translationService.deleteByPostIdAndLanguageCode(postId, languageCode);

        return "redirect:/post/detail?id=" + postId;


    }
}

