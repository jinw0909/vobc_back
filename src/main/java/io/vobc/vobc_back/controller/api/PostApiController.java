package io.vobc.vobc_back.controller.api;

import io.vobc.vobc_back.domain.LanguageCode;
import io.vobc.vobc_back.dto.PagedResponse;
import io.vobc.vobc_back.dto.post.PostDto;
import io.vobc.vobc_back.dto.post.PostQueryDto;
import io.vobc.vobc_back.dto.post.PostResponse;
import io.vobc.vobc_back.dto.post.PostTranslatedResponse;
import io.vobc.vobc_back.service.PostQueryService;
import io.vobc.vobc_back.service.PostService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/post")
@RequiredArgsConstructor
public class PostApiController {

    private final PostService postService;
    private final PostQueryService postQueryService;

    @GetMapping("/list")
    public PagedResponse<PostResponse> list(@RequestParam(defaultValue = "en") String lang,
                                            @PageableDefault(size = 10) Pageable pageable
    ) {
        LanguageCode language = LanguageCode.from(lang);
        return postService.getPosts(language, pageable);
    }

    @GetMapping("/{id}")
    public PostResponse getPost(@PathVariable Long id,
                                @RequestParam(defaultValue = "en") String lang) {
        LanguageCode languageCode = LanguageCode.from(lang);

//        return postService.getPostDetail(id, languageCode);
        return postService.getTranslatedPost(id, languageCode);
    }


    @GetMapping("/query/list")
    public List<PostQueryDto> queryList() {
        return postQueryService.findAllByDto();
    }

    @GetMapping("/query/page")
    public Page<PostQueryDto> queryPage(@RequestParam(required = false) String tagName,
                                        @RequestParam(defaultValue = "en") String lang,
                                        @PageableDefault(size = 10, sort = "id", direction = Sort.Direction.DESC) Pageable pageable ) {
        LanguageCode languageCode = LanguageCode.from(lang);
        return postQueryService.findAllByDto(pageable, languageCode, tagName);
    }


    @GetMapping("/query/{id:\\d+}")
    public PostDto querySingle(@PathVariable Long id,
                               @RequestParam(required = false) String lang) {
        LanguageCode languageCode = LanguageCode.from(lang);
        return postQueryService.findOneById(id, languageCode);
    }

    @GetMapping("/query/featured")
    public PostTranslatedResponse featuredPost(@RequestParam(defaultValue = "en") String lang) {
        LanguageCode languageCode = LanguageCode.from(lang);
        return postQueryService.getFeatured(languageCode);
    }

    @GetMapping("/query/rest")
    public Page<PostTranslatedResponse> rest(@RequestParam(defaultValue = "en") String lang,
                                             @RequestParam(required = false) Long featuredId,
                                             @PageableDefault(size = 10) Pageable pageable
    ) {
        LanguageCode languageCode = LanguageCode.from(lang);
        return postQueryService.getRest(pageable, featuredId, languageCode);
    }

    @GetMapping("/{postId}/related")
    public List<PostTranslatedResponse> related(@PathVariable Long postId,
                                                @RequestParam(defaultValue = "en") String lang) {
        LanguageCode languageCode = LanguageCode.from(lang);
        return postQueryService.getRelatedPosts(postId, languageCode);
    }
}
