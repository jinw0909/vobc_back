package io.vobc.vobc_back.controller.web;

import io.vobc.vobc_back.domain.LanguageCode;
import io.vobc.vobc_back.domain.article.Article;
import io.vobc.vobc_back.domain.article.Category;
import io.vobc.vobc_back.domain.publisher.Publisher;
import io.vobc.vobc_back.dto.article.ArticleForm;
import io.vobc.vobc_back.dto.article.ArticleTranslationForm;
import io.vobc.vobc_back.exception.ImageUploadException;
import io.vobc.vobc_back.repository.article.ArticleRepository;
import io.vobc.vobc_back.repository.publisher.PublisherRepository;
import io.vobc.vobc_back.service.ArticleService;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.Map;

@Controller
@RequestMapping("/article")
@RequiredArgsConstructor
public class ArticleController {

    private final ArticleService articleService;
    private final ArticleRepository articleRepository;
    private final PublisherRepository publisherRepository;

    @GetMapping("/create")
    public String createForm(Model model) {
        model.addAttribute("form", new ArticleForm());
        model.addAttribute("mode", "create");
        model.addAttribute("publishers", articleService.getPublishers());

        return "article/form";
    }

    @GetMapping("/edit")
    public String editForm(@RequestParam("id") Long id, Model model) {
        ArticleForm articleForm = articleService.findById(id);
        Map<Long, String> publishers = articleService.getPublishers();
        model.addAttribute("form", articleForm);
        model.addAttribute("mode", "edit");
        model.addAttribute("articleId", id);
        model.addAttribute("publishers", publishers);
        return "article/form";
    }

    @GetMapping("/detail")
    public String detail(@RequestParam("id") Long id, Model model) {
        // TODO: articleService.findById(id) 등으로 조회
        model.addAttribute("article", articleService.findById(id));
        model.addAttribute("langs", articleService.getLanguages(id));
        model.addAttribute("allLangs", io.vobc.vobc_back.domain.LanguageCode.values());
        return "article/detail";
    }

    @GetMapping("/list")
    public String list(Model model, @PageableDefault(size = 10, sort = {"createdAt", "id"}, direction = Sort.Direction.DESC) Pageable pageable) {
        Page<Article> page = articleService.findAll(pageable);
        model.addAttribute("articles", page.getContent());
        model.addAttribute("page", page); // ✅ 페이징 위해 추가
        return "article/list";
    }

    @PostMapping(value="/save", consumes=MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseBody
    public Map<String, Long> saveOrUpdate(
            @RequestParam(value="id", required=false) Long id,
            @ModelAttribute("form") ArticleForm form
    ) {
        Long savedId = articleService.saveOrUpdate(id, form);
        return Map.of("id", savedId);
    }

    @GetMapping("/translate/{id}")
    public String translationForm(@PathVariable Long id,
                                  @RequestParam("lang") String lang,
                                  Model model) {

        LanguageCode languageCode = LanguageCode.from(lang);

        ArticleForm article = articleService.findById(id);

        // 기존 번역 조회 (없으면 id=null인 빈 폼이 올 것)
        ArticleTranslationForm tr = articleService.getTranslation(id, languageCode);

        boolean hasTranslation = tr.getId() != null;

        // ✅ 번역이 없으면: 원문값으로 폼 기본값 채움(placeholder처럼)
        if (!hasTranslation) {
            // languageCode는 유지
            tr.setLanguageCode(languageCode);

            // "보여주기용 기본값" 채우기
            tr.setTitle(article.getTitle());
            tr.setAuthor(article.getAuthor());
            tr.setDescription(article.getDescription());
            tr.setSummary(article.getSummary());
            tr.setContent(article.getContent());
        }

        model.addAttribute("article", article);
        model.addAttribute("translation", tr);
        model.addAttribute("lang", languageCode.getCode());
        model.addAttribute("hasTranslation", hasTranslation);

        // ✅ langs 필요 없으면 제거
        // model.addAttribute("langs", articleService.getLanguages(id));

        return "article/translation-form";
    }



    @PostMapping(value = "/translate/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public String translate(@PathVariable Long id,
                            @RequestParam("lang") String lang,
                            @ModelAttribute("translation") ArticleTranslationForm form,
                            RedirectAttributes ra
    ) {

        form.setLanguageCode(LanguageCode.from(lang));

        try {
            articleService.saveTranslation(id, form);

            ra.addFlashAttribute("flashType", "success");
            ra.addFlashAttribute("flashMsg", "저장 완료");

        } catch (DataIntegrityViolationException e) {
            FlashError fe = FlashError.from(e);

            ra.addFlashAttribute("flashType", "danger");
            ra.addFlashAttribute("flashMsg", fe.userMessage());
            ra.addFlashAttribute("detail", fe.detail());
        } catch (Exception e) {
            ra.addFlashAttribute("flashType", "danger");
            ra.addFlashAttribute("flashMsg", "저장 중 오류가 발생했습니다.");
            ra.addFlashAttribute("flashDetail", rootMessage(e));
        }

        return "redirect:/article/translate/" + id + "?lang=" + form.getLanguageCode().getCode();
    }

    @GetMapping("/feed")
    public void feed() {

        Publisher herald = publisherRepository.findById(1L).orElseThrow();

        for (int i = 0; i < 24; i++) {
            String releaseDate = i < 8 ? "2025-12-12" : i < 16 ? "2024-11-11" : "2023-10-10";
            Article article = Article.create(
                    "BlockSquare Seoul to Enhance Market Intelligence Service for Crypto Investors",
                    "<p><img src=\"https://vobc-image-bucket.s3.ap-northeast-2.amazonaws.com/articles/7/40bb4fa2-a348-4d13-bf4c-32de35772650/cacdfe37-5bed-4783-a0c6-ea3846bddba4.jpg\" alt=\"\" data-media-id=\"13\"></p>\n" +
                            "<p style=\"margin-right: 0px; margin-bottom: 40px; margin-left: 0px; padding: 0px; border: 0px; text-size-adjust: 100%; vertical-align: baseline; line-height: 30px; word-break: keep-all;\">Korea's crypto data aggregator BlockSquare Seoul said Thursday that it plans to update its market analysis features in July to offer more detailed investment information for its users.</p>\n" +
                            "<p style=\"margin-right: 0px; margin-bottom: 40px; margin-left: 0px; padding: 0px; border: 0px; text-size-adjust: 100%; vertical-align: baseline; line-height: 30px; word-break: keep-all;\">Currently, the company provides market intelligence service under the name Retri, suggesting the best time to buy and sell cryptocurrencies by reading their price movements and transaction flows.</p>\n" +
                            "<p style=\"margin-right: 0px; margin-bottom: 40px; margin-left: 0px; padding: 0px; border: 0px; text-size-adjust: 100%; vertical-align: baseline; line-height: 30px; word-break: keep-all;\">Following the July update, the Retri service will offer more sophisticated real-time data updates on price projections. The current service is based on hourly data but the new version will offer more recent figures.</p>\n" +
                            "<p style=\"margin-right: 0px; margin-bottom: 40px; margin-left: 0px; padding: 0px; border: 0px; text-size-adjust: 100%; vertical-align: baseline; line-height: 30px; word-break: keep-all;\">Another update is the “Duo” feature which suggests two projected prices of a cryptocurrency, reflecting the fluctuations of coin prices following a momentary price surge or plunge. The firm stressed the feature would be useful, in particular, in the volatile crypto market.</p>\n" +
                            "<p style=\"margin-right: 0px; margin-bottom: 40px; margin-left: 0px; padding: 0px; border: 0px; text-size-adjust: 100%; vertical-align: baseline; line-height: 30px; word-break: keep-all;\">The update further presents functions that can help investors understand the overall crypto market. A total count of the price movements of over 200 cryptocurrencies will be provided, dividing them into two categories based on whether the coin prices have increased or decreased in the past five minutes.</p>",
                    "Korea's crypto data aggregator BlockSquare Seoul said Thursday that it plans to update its market analysis features in July to offer more detailed investment information for its users.",
                    "The Goyabot character for BlockSquare Seoul's Retri market intelligence service (BlockSquare Seoul)",
                    "Im Eun-byel",
                    LocalDate.parse(releaseDate),
                    "https://vobc-image-bucket.s3.ap-northeast-2.amazonaws.com/thumbnails/3d496da9-75de-4a17-8372-8d21efe01edd.jpg",
                    "https://www.koreaherald.com/article/3412452",
                    Category.event
            );

            article.setPublisher(herald);
            articleRepository.save(article);
        }
    }


    @ExceptionHandler(ImageUploadException.class)
    public String handleImageFail(ImageUploadException e, RedirectAttributes redirectAttributes) {
        redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        return "redirect:/article/create";
    }

    record FlashError(String userMessage, String detail) {

        static FlashError from(DataIntegrityViolationException e) {
                String root = rootMessage(e);
                if (root.contains("ORA-00001")) {
                    return new FlashError(
                            "이미 해당 언어 번역이 존재합니다. (중복 저장)",
                            root
                    );
                }
                return new FlashError("DB 제작조건 위반으로 저장에 실패했습니다.", root);
            }
        }

    private static String rootMessage(Throwable t) {
        Throwable r = t;
        while (r.getCause() != null) { r = r.getCause(); }
        String msg = r.getMessage();
        return r.getClass().getSimpleName() + (msg == null ? "" : ": " + msg);
    }
}
