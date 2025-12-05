package io.vobc.vobc_back.controller.api;

import io.vobc.vobc_back.domain.LanguageCode;
import io.vobc.vobc_back.domain.Post;
import io.vobc.vobc_back.dto.PagedResponse;
import io.vobc.vobc_back.dto.PostResponse;
import io.vobc.vobc_back.service.PostService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/post")
@RequiredArgsConstructor
public class PostApiController {

    private final PostService postService;

    @GetMapping("/list")
    public PagedResponse<PostResponse> list(@RequestParam(defaultValue = "en") String lang,
                                            @RequestParam(defaultValue = "0") int page,
                                            @RequestParam(defaultValue = "10") int size
    ) {
        LanguageCode language = LanguageCode.from(lang);
        return postService.getPosts(language, page, size);
    }

}
