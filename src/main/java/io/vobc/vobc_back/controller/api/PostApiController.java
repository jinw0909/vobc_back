package io.vobc.vobc_back.controller.api;

import io.vobc.vobc_back.domain.LanguageCode;
import io.vobc.vobc_back.domain.Post;
import io.vobc.vobc_back.dto.PagedResponse;
import io.vobc.vobc_back.dto.post.PostResponse;
import io.vobc.vobc_back.service.PostService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/post")
@RequiredArgsConstructor
public class PostApiController {

    private final PostService postService;

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

        return postService.getPostDetail(id, languageCode);
    }

}
