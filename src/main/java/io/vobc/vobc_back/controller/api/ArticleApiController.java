package io.vobc.vobc_back.controller.api;

import io.vobc.vobc_back.domain.LanguageCode;
import io.vobc.vobc_back.dto.article.ArticleResponse;
import io.vobc.vobc_back.service.ArticleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/article")
public class ArticleApiController {

    private final ArticleService articleService;

    @GetMapping("/list")
    public Page<ArticleResponse> articles(@PageableDefault(size = 10) Pageable pageable,
                                          @RequestParam(defaultValue = "en") String lang
    ) {
        LanguageCode languageCode = LanguageCode.from(lang);
        return articleService.getArticlesWithPublisherTranslation(pageable, languageCode);
    }

    @GetMapping("/page")
    public Page<ArticleResponse> page(@PageableDefault(size = 10) Pageable pageable,
                                      @RequestParam(defaultValue = "en") String lang) {
        LanguageCode languageCode = LanguageCode.from(lang);
        return articleService.getTranslatedPage(pageable, languageCode);
    }

    @GetMapping("/{id}")
    public ArticleResponse one(@PathVariable Long id,
                               @RequestParam(defaultValue = "en") String lang
    ) {
        LanguageCode languageCode = LanguageCode.from(lang);
        return articleService.getTranslatedSingle(id, languageCode);
    }

    @GetMapping("/{articleId}/related")
    public List<ArticleResponse> related(@PathVariable Long articleId,
                                         @RequestParam(defaultValue = "en") String lang) {
        LanguageCode languageCode = LanguageCode.from(lang);
        return articleService.getRelatedArticles(articleId, languageCode);
    }

}
