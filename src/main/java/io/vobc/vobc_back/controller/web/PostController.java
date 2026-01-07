package io.vobc.vobc_back.controller.web;

import io.vobc.vobc_back.domain.LanguageCode;
import io.vobc.vobc_back.domain.post.Post;
import io.vobc.vobc_back.domain.post.PostTag;
import io.vobc.vobc_back.domain.post.Translation;
import io.vobc.vobc_back.dto.*;
import io.vobc.vobc_back.dto.post.*;
import io.vobc.vobc_back.security.CustomUserDetails;
import io.vobc.vobc_back.service.PostService;
import io.vobc.vobc_back.service.TagService;
import io.vobc.vobc_back.service.TranslationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/post")
@RequiredArgsConstructor
@Slf4j
public class PostController {

    private final PostService postService;
    private final TagService tagService;
    private final TranslationService translationService;


    @GetMapping("/list")
    public String postList(Model model,
                           @PageableDefault(
                                   size = 10,
                                   sort = "releaseDate",
                                   direction = Sort.Direction.DESC
                           ) Pageable pageable) {
        Page<Post> posts = postService.getAllPosts(pageable);
        model.addAttribute("posts", posts);
        return "post/list";
    }


    @GetMapping("/detail")
    public String detail(@RequestParam Long id, Model model) {

//        Post post = postService.findWithTagsById(id);
        model.addAttribute("post", postService.getPostWithTags(id));
        model.addAttribute("allTags", tagService.getAllTags());

        List<String> langs = postService.getLanguageCodesById(id).stream()
                .map(lc -> lc.getCode().toLowerCase())
                .toList();

        model.addAttribute("langs", langs);
        return "post/detail";
    }

    @GetMapping("/create")
    public String createForm(Model model) {
        model.addAttribute("post", new PostForm());
        model.addAttribute("allTags", tagService.getAllTags());
        return "post/form";
    }

    @GetMapping("/update")
    public String editForm(@RequestParam Long id, Model model) {
        Post post = postService.getPostWithTags(id);
        Long postId = post.getId();
        List<PostTagForm> list = post.getPostTags().stream().map(pt -> {
            PostTagForm postTagForm = new PostTagForm();
            postTagForm.setPostId(postId);
            postTagForm.setTagId(pt.getTag().getId());
            postTagForm.setSortOrder(pt.getSortOrder());
            postTagForm.setPrimaryTag(pt.getPrimaryTag());
            postTagForm.setPostTagId(pt.getId());
            return postTagForm;
        }).toList();

        Map<Long, PostTagForm> postTagByTagId = list.stream()
                .collect(java.util.stream.Collectors.toMap(PostTagForm::getTagId, x -> x));

        PostForm postForm = new PostForm();
        postForm.setId(post.getId());
        postForm.setTitle(post.getTitle());
        postForm.setSummary(post.getSummary());
        postForm.setContent(post.getContent());
        postForm.setAuthor(post.getAuthor());
        postForm.setThumbnail(post.getThumbnail());
        postForm.setReleaseDate(post.getReleaseDate());
        postForm.setPostTags(list);
        model.addAttribute("post", postForm);
        model.addAttribute("allTags", tagService.getAllTags());
        model.addAttribute("postTagByTagId", postTagByTagId);
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

    @PostMapping(value = "/save", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseBody
    public Map<String, Long> save(@ModelAttribute PostForm form) {
        log.info("PostForm: {}", form);
        Long savedId = postService.save(form.getId(), form);
        log.info("=== SAVE DONE postId={} ===", savedId);
        return Map.of("id", savedId);
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

//    @PostMapping(value = "/translate/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
//    public String saveTranslation(@PathVariable Long id,
//                                  @ModelAttribute TranslationForm form) {
//        translationService.saveTranslation(form);
//        return "redirect:/post/translate?id=" + form.getPostId() + "&languageCode=" + form.getLanguageCode().getCode();
//    }

    @PostMapping(value="/translate/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseBody
    public Map<String, Object> saveTranslation(@PathVariable Long id,
                                               @RequestParam("lang") String lang,
                                               @ModelAttribute TranslationForm form) {
        form.setLanguageCode(LanguageCode.from(lang));
        Long savedId = translationService.saveTranslation(form);

        log.info("=== TRANSLATION DONE translationId={} ===", savedId);

        return Map.of(
                "ok", true,
                "postId", form.getPostId(),
                "lang", form.getLanguageCode().getCode(),
                "id", savedId
        );
    }


    @PostMapping("/translate/delete")
    public String deleteTranslation(@RequestParam Long postId,
                                    @RequestParam String lang) {

        LanguageCode languageCode = LanguageCode.from(lang);

        translationService.deleteByPostIdAndLanguageCode(postId, languageCode);

        return "redirect:/post/detail?id=" + postId;


    }
}

