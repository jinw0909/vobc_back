package io.vobc.vobc_back.service;

import io.vobc.vobc_back.domain.LanguageCode;
import io.vobc.vobc_back.domain.article.Article;
import io.vobc.vobc_back.domain.article.ArticleTranslation;
import io.vobc.vobc_back.domain.media.ContentType;
import io.vobc.vobc_back.domain.media.Media;
import io.vobc.vobc_back.domain.publisher.Publisher;
import io.vobc.vobc_back.dto.article.ArticleForm;
import io.vobc.vobc_back.dto.article.ArticleResponse;
import io.vobc.vobc_back.dto.article.ArticleTranslationForm;
import io.vobc.vobc_back.dto.article.ArticleTranslationResponse;
import io.vobc.vobc_back.dto.publisher.PublisherResponse;
import io.vobc.vobc_back.dto.publisher.PublisherTranslationResponse;
import io.vobc.vobc_back.exception.ImageUploadException;
import io.vobc.vobc_back.repository.article.ArticleRepository;
import io.vobc.vobc_back.repository.MediaRepository;
import io.vobc.vobc_back.repository.article.ArticleTranslationRepository;
import io.vobc.vobc_back.repository.publisher.PublisherRepository;
import io.vobc.vobc_back.repository.publisher.PublisherTranslationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ArticleService {

    private final ArticleRepository articleRepository;
    private final S3Uploader s3Uploader;
    private final MediaRepository mediaRepository;
    private final PublisherRepository publisherRepository;
    private final ArticleTranslationRepository articleTranslationRepository;
    private final PublisherTranslationRepository publisherTranslationRepository;

    public Long save(ArticleForm form) {
        // 1) Article 먼저 저장해서 ID 확보
        Article article = Article.create(
                form.getTitle(),
                "",
                form.getSummary(),
                form.getDescription(),
                form.getAuthor(),
                form.getReleaseDate(),
                form.getThumbnail(),
                form.getLink(),
                form.getCategory()
        );

        Publisher publisher = publisherRepository.findById(form.getPublisherId()).orElse(null);
        article.setPublisher(publisher);

        articleRepository.save(article);

        // 2) files를 assetId -> file로 매핑 (파일명 = assetId.ext)
        Map<String, MultipartFile> fileMap = buildFileMap(form.getFiles());

        // 3) content의 img[data-asset-id]를 찾아 신규만 업로드/치환
        String finalContent = replaceImagesAndSaveMedia(article, form.getContent(), fileMap);

        // 4) 최종 content 저장
        article.changeContent(finalContent);


        return article.getId();
    }

    public Long update(Long id, ArticleForm form) {
        Article article = articleRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Article not found: " + id));

        article.setTitle(form.getTitle());
        article.setSummary(form.getSummary());
        article.setDescription(form.getDescription());
        article.setAuthor(form.getAuthor());
        article.setReleaseDate(form.getReleaseDate());
        article.setThumbnail(form.getThumbnail());
        article.setLink(form.getLink());
        article.setCategory(form.getCategory());

        Publisher publisher = publisherRepository.findById(form.getPublisherId()).orElse(null);
        article.setPublisher(publisher);

        // 2) files 맵
        Map<String, MultipartFile> fileMap = buildFileMap(form.getFiles());

        // 3) 신규 이미지만 업로드/치환
        String finalContent = replaceImagesAndSaveMedia(article, form.getContent(), fileMap);

        article.changeContent(finalContent);

        //5) 본문에서 사라진 이미지 deleted 처리
        cleanupUnusedMedia(article.getId(), finalContent);

        return article.getId();
    }

    private String replaceImagesAndSaveMedia(
            Article article,
            String content,
            Map<String, MultipartFile> fileMap
    ) {
        if (content == null) content = "";

        Document doc = Jsoup.parseBodyFragment(content);

        for (Element img : doc.select("img")) {

            String mediaIdStr = img.attr("data-media-id").trim();
            String assetId = img.attr("data-asset-id").trim();
            String src = img.attr("src").trim();

            boolean hasMediaId = !mediaIdStr.isEmpty();
            boolean hasAssetId = !assetId.isEmpty();

            // 1) ✅ 기존(이미 DB 매핑된) 이미지: 절대 업로드 하지 않음
            if (hasMediaId) {
                // 혹시 레거시로 asset-id까지 남아있으면 제거만
                if (hasAssetId) img.removeAttr("data-asset-id");
                continue;
            }

            // 2) asset-id가 없다면 우리가 관리하는 이미지가 아님 (외부 URL 등)
            if (!hasAssetId) {
                continue;
            }

            // 3) asset-id가 있는데 media-id가 없다면 "신규 or 레거시"
            //    우선 DB에 이미 있는 assetId인지 확인
            boolean exists = mediaRepository.existsByArticleIdAndAssetId(article.getId(), assetId);
            if (exists) {
                // ✅ DB에는 있는데 HTML이 media-id를 못 들고 있는 레거시 상태
                // 업로드는 하지 않고 asset-id만 제거해서 다음부터 신규로 오해하지 않게 정리
                img.removeAttr("data-asset-id");
                continue;
            }

            // 4) 신규: 파일이 있어야 함
            MultipartFile file = fileMap.get(assetId);

            if (file == null || file.isEmpty()) {
                // 레거시/오류 케이스 분기
                if (looksLikeS3Url(src)) {
                    // ✅ 이미 S3 url인데 data-asset-id만 남아있던 이상한 케이스
                    // 일단 저장은 통과시키고 asset-id 제거(안전)
                    log.warn("Skip upload: assetId exists neither in DB nor fileMap, but src looks like S3. articleId={}, assetId={}, src={}",
                            article.getId(), assetId, src);
                    img.removeAttr("data-asset-id");
                    continue;
                }

                if (src.startsWith("blob:") || src.startsWith("data:")) {
                    // blob/data URL인데 파일이 없으면 진짜 문제(프론트가 파일을 못 보냈거나 삭제됨)
                    throw new IllegalArgumentException("Missing upload file for NEW assetId: " + assetId);
                }

                // 그 외는 애매하지만 업로드 못하니 일단 예외로 두는 게 안전
                throw new IllegalArgumentException("Missing upload file for assetId: " + assetId + ", src=" + src);
            }

            // 5) ✅ 정상 신규 업로드 플로우
            try {
                S3Uploader.UploadResult up = s3Uploader.uploadToS3(article.getId(), assetId, file);

                Media media = Media.create(
                        assetId,
                        up.url(),
                        up.s3Key(),
                        ContentType.IMAGE,
                        article
                );
                mediaRepository.save(media);

                img.attr("src", up.url());
                img.attr("data-media-id", String.valueOf(media.getId()));

                // ✅ 업로드 완료: asset-id는 제거(다음 수정 때 신규로 오해 방지)
                img.removeAttr("data-asset-id");

            } catch (Exception e) {
                log.error("Failed to upload image. articleId={}, assetId={}", article.getId(), assetId, e);
                throw new ImageUploadException("Image upload failed (assetId=" + assetId + ")", e);
            }
        }

        return doc.body().html();
    }

    private boolean looksLikeS3Url(String src) {
        if (src == null) return false;
        // 네 환경에 맞게 더 엄격하게(예: bucket 도메인/CloudFront) 바꿔도 됨
        return src.startsWith("http://") || src.startsWith("https://");
    }

    private Map<String, MultipartFile> buildFileMap(List<MultipartFile> files) {
        Map<String, MultipartFile> map = new HashMap<>();
        if (files == null) {
            return map;
        }

        for (MultipartFile f : files) {
            if (f == null || f.isEmpty()) continue;
            String original = f.getOriginalFilename();
            if (original == null || original.isBlank()) continue;

            String assetId = stripExtension(original).trim();
            if (!assetId.isEmpty()) {
                map.put(assetId, f);
            }
        }
        return map;
    }

    private String stripExtension(String filename) {
        int dot = filename.lastIndexOf('.');
        return (dot > 0) ? filename.substring(0, dot) : filename;
    }

    private void cleanupUnusedMedia(Long articleId, String finalContent) {
        // 1) 현재 본문에 남아있는 mediaId 수집
        Document doc = Jsoup.parseBodyFragment(finalContent);

        var aliveIds = doc.select("img[data-media-id]").stream()
                .map(el -> el.attr("data-media-id"))
                .filter(s -> !s.isBlank())
                .map(Long::valueOf)
                .collect(java.util.stream.Collectors.toSet());

        if (aliveIds.isEmpty()) {
            log.warn("No data-media-id found in content. Skip cleanup. articleId={}", articleId);
            return;
        }

        // 2) DB에 등록된 미디어 전체 조회
        List<Media> all = mediaRepository.findAllByArticleIdAndDeletedFalse(articleId);

        // 3) 본문에 없는 것들 = 삭제 대상
        for (Media m : all) {
            if (!aliveIds.contains(m.getId())) {
                try {
                    // S3 삭제
                    s3Uploader.delete(m.getS3Key());

                    // DB 삭제(또는 soft delete)
                    // 1) hard delete
                    mediaRepository.delete(m);

                    // 2) soft delete 하고 싶으면:
                    // m.setDeleted(true);

                } catch (Exception e) {
                    // 여기서 예외를 터뜨릴지/로그만 남길지 정책 선택
                    log.error("Failed to delete unused media. articleId={}, mediaId={}, s3Key={}",
                            articleId, m.getId(), m.getS3Key(), e);
                    // 보수적으로는 롤백시키는 게 안전:
                    throw new RuntimeException("Failed to delete unused media: " + m.getId(), e);
                }
            }
        }
    }

    @Transactional(readOnly = true)
    public ArticleForm findById(Long id) {
        Article article = articleRepository.findWithPublisherById(id).orElseThrow();
        return new ArticleForm(article);
    }

    @Transactional
    public Long saveOrUpdate(Long id, ArticleForm form) {
        if (id == null) {
            return save(form);     // 지금 네 save()
        }
        return update(id, form);   // 지금 네 update()
    }

    @Transactional(readOnly = true)
    public Page<Article> findAll(Pageable pageable) {
        return articleRepository.findAll(pageable);
    }

    @Transactional(readOnly = true)
    public Map<Long, String> getPublishers() {
        return publisherRepository.findAllByOrderByName().stream()
                .collect(Collectors.toMap(
                        PublisherRepository.PublisherIdName::getId,
                        PublisherRepository.PublisherIdName::getName,
                        (a, b) -> a,
                        LinkedHashMap::new
                ));
    }


    @Transactional(readOnly = true)
    public Page<ArticleResponse> getArticlesWithPublisherTranslation(Pageable pageable, LanguageCode languageCode) {
        Page<Article> page = articleRepository.findAllByOrderByCreatedAtDesc(pageable);
        List<Article> articles = page.getContent();
        if (articles.isEmpty()) {
            return page.map(ArticleResponse::create);
        }

        // 1) articleIds 추출
        List<Long> articleIds = articles.stream().map(Article::getId).toList();

        // 2) publisherIds 추출
        List<Long> publisherIds = articles.stream()
                .map(Article::getPublisher)
                .filter(Objects::nonNull)
                .map(Publisher::getId)
                .distinct().toList();

        // 3) 번역들 한 번에 조회
        Map<Long, List<ArticleTranslationResponse>> articleTrMap = articleTranslationRepository.findByArticleIdsAndLanguageCode(articleIds, languageCode)
                .stream()
                .map(at -> ArticleTranslationResponse.create(at.getArticle().getId(), at))
                .collect(Collectors.groupingBy(ArticleTranslationResponse::getArticleId, Collectors.toList()));
        Map<Long, List<PublisherTranslationResponse>> publisherTrMap = publisherIds.isEmpty() ? Map.of() :
                publisherTranslationRepository.findByPublisherIdsAndLanguageCode(publisherIds, languageCode).stream()
                        .map(pt -> PublisherTranslationResponse.create(pt.getPublisher().getId(), pt))
                        .collect(Collectors.groupingBy(PublisherTranslationResponse::getPublisherId, Collectors.toList()));

        return page.map(article -> {

            ArticleResponse articleResponse = ArticleResponse.create(article);

            List<ArticleTranslationResponse> articleTranslations = articleTrMap.getOrDefault(article.getId(), List.of());
            articleResponse.setTranslations(articleTranslations);

            PublisherResponse pr = articleResponse.getPublisher();
            if (pr != null) {
                List<PublisherTranslationResponse> publisherTranslations = publisherTrMap.getOrDefault(pr.getId(), List.of());
                pr.setTranslations(publisherTranslations);
            }

            return ArticleResponse.translate(articleResponse, languageCode);

        });
    }

    @Transactional(readOnly = true)
    public Set<LanguageCode> getLanguages(Long id) {
        return articleTranslationRepository.findLanguageCodesByArticleId(id);
    }

    @Transactional(readOnly = true)
    public ArticleTranslationForm getTranslation(Long id, LanguageCode languageCode) {
        return articleTranslationRepository.findByArticleIdAndLanguageCode(id, languageCode)
                .map(ArticleTranslationForm::new)
                .orElseGet(() -> ArticleTranslationForm.empty(languageCode));
    }

    @Transactional
    public Long saveTranslation(Long id, ArticleTranslationForm form) {

        Article article = articleRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("article not found"));

        if (form.getId() == null) {
            ArticleTranslation articleTranslation = ArticleTranslation.create(
                    article,
                    form.getLanguageCode(),
                    form.getTitle(),
                    form.getContent(),
                    form.getSummary(),
                    form.getDescription(),
                    form.getAuthor()
            );
            ArticleTranslation saved = articleTranslationRepository.save(articleTranslation);
            return saved.getId();
        } else {
            ArticleTranslation articleTranslation = articleTranslationRepository.findById(form.getId()).orElseThrow(() -> new IllegalArgumentException("article translation not found"));
            articleTranslation.setArticle(article);
            article.addTranslation(articleTranslation);
            articleTranslation.setLanguageCode(form.getLanguageCode());
            articleTranslation.setTitle(form.getTitle());
            articleTranslation.setContent(form.getContent());
            articleTranslation.setSummary(form.getSummary());
            articleTranslation.setDescription(form.getDescription());
            articleTranslation.setAuthor(form.getAuthor());

            return articleTranslation.getId();
        }
    }

    @Transactional(readOnly = true)
    public Page<ArticleResponse> getTranslatedPage(Pageable pageable, LanguageCode languageCode) {
        return articleRepository.findAllWithTranslations(pageable, languageCode);
    }

    @Transactional(readOnly = true)
    public ArticleResponse getTranslatedSingle(Long id, LanguageCode languageCode) {
        return articleRepository.findOneWithTranslations(id, languageCode).orElseThrow(() -> new IllegalArgumentException("article not found"));
    }
}
