package io.vobc.vobc_back.controller.api;

import io.vobc.vobc_back.domain.LanguageCode;
import io.vobc.vobc_back.domain.Post;
import io.vobc.vobc_back.dto.PagedResponse;
import io.vobc.vobc_back.dto.post.PostResponse;
import io.vobc.vobc_back.service.TagService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/tag")
public class TagApiController {

    private final TagService tagService;

    @GetMapping("/posts")
    public PagedResponse<PostResponse> posts(@RequestParam Long id,
                                             @RequestParam(defaultValue = "en") String lang,
                                             @PageableDefault(size = 10) Pageable pageable
                                             ) {

        LanguageCode languageCode = LanguageCode.from(lang);
        return tagService.getPostsByTagId(id, languageCode, pageable);
    }
}
