package io.vobc.vobc_back.service;

import io.vobc.vobc_back.domain.LanguageCode;
import io.vobc.vobc_back.domain.article.Article;
import io.vobc.vobc_back.domain.article.ArticleTopic;
import io.vobc.vobc_back.domain.article.ArticleTranslation;
import io.vobc.vobc_back.domain.article.Topic;
import io.vobc.vobc_back.domain.publisher.Publisher;
import io.vobc.vobc_back.dto.article.*;
import io.vobc.vobc_back.dto.publisher.PublisherResponse;
import io.vobc.vobc_back.dto.publisher.PublisherTranslationResponse;
import io.vobc.vobc_back.repository.TopicRepository;
import io.vobc.vobc_back.repository.article.ArticleRepository;
import io.vobc.vobc_back.repository.MediaRepository;
import io.vobc.vobc_back.repository.article.ArticleTranslationRepository;
import io.vobc.vobc_back.repository.publisher.PublisherRepository;
import io.vobc.vobc_back.repository.publisher.PublisherTranslationRepository;
import io.vobc.vobc_back.service.media.MediaService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
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
    private final MediaService mediaService;
    private final TopicRepository topicRepository;

    public Long save(ArticleForm form) {

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

        publisherRepository.findById(form.getPublisherId())
                .ifPresent(article::setPublisher);

        articleRepository.save(article);

        // ✅ content 처리
        String finalContent =
                mediaService.replaceImagesAndSave(article, form.getContent(), form.getFiles());
        article.changeContent(finalContent);

        // ✅ topics 처리 (여기!)
        syncArticleTopics(article, form);

        log.info("=== Called Save(new) Article ===");

        return article.getId();
    }


    public Long update(Long id, ArticleForm form) {

        Article article = articleRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Article not found: " + id));

        article.setTitle(form.getTitle());
        article.setSummary(form.getSummary());
        article.setDescription(form.getDescription());
        article.setAuthor(form.getAuthor());
        article.setReleaseDate(form.getReleaseDate());
        article.setThumbnail(form.getThumbnail());
        article.setLink(form.getLink());
        article.setCategory(form.getCategory());

        publisherRepository.findById(form.getPublisherId())
                .ifPresent(article::setPublisher);

        String finalContent =
                mediaService.replaceImagesAndSave(article, form.getContent(), form.getFiles());
        article.changeContent(finalContent);

        boolean hasFiles =
                form.getFiles() != null && form.getFiles().stream().anyMatch(f -> !f.isEmpty());
        if (hasFiles) {
            mediaService.cleanUpArticleConsideringAllContents(article.getId());
        }

        // ✅ topics 동기화 (여기!)
        syncArticleTopics(article, form);

        log.info("=== Called Update Article ===");

        return article.getId();
    }


    private void syncArticleTopics(Article article, ArticleForm form) {
        // 기존 관계 제거
        article.getTopics().clear();

        if (form.getArticleTopics() == null || form.getArticleTopics().isEmpty()) {
            return;
        }

        for (ArticleTopicForm tf : form.getArticleTopics()) {
            Topic topic = topicRepository.findById(tf.getTopicId())
                    .orElseThrow(() -> new IllegalArgumentException(
                            "Topic not found: " + tf.getTopicId()
                    ));

            ArticleTopic at = new ArticleTopic();
            at.setArticle(article);
            at.setTopic(topic);
            at.setPrimaryTopic(tf.isPrimaryTopic());
            at.setSortOrder(tf.getSortOrder());

            article.getTopics().add(at);
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

//    @Transactional
//    public Long saveTranslation(Long id, ArticleTranslationForm form) {
//
//        Article article = articleRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("article not found"));
//
//        if (form.getId() == null) {
//            ArticleTranslation articleTranslation = ArticleTranslation.create(
//                    article,
//                    form.getLanguageCode(),
//                    form.getTitle(),
//                    form.getContent(),
//                    form.getSummary(),
//                    form.getDescription(),
//                    form.getAuthor()
//            );
//            ArticleTranslation saved = articleTranslationRepository.save(articleTranslation);
//            return saved.getId();
//        } else {
//            ArticleTranslation articleTranslation = articleTranslationRepository.findById(form.getId()).orElseThrow(() -> new IllegalArgumentException("article translation not found"));
//            articleTranslation.setArticle(article);
//            article.addTranslation(articleTranslation);
//            articleTranslation.setLanguageCode(form.getLanguageCode());
//            articleTranslation.setTitle(form.getTitle());
//            articleTranslation.setContent(form.getContent());
//            articleTranslation.setSummary(form.getSummary());
//            articleTranslation.setDescription(form.getDescription());
//            articleTranslation.setAuthor(form.getAuthor());
//
//            return articleTranslation.getId();
//        }
//    }
    @Transactional
    public Long saveTranslation(Long id, ArticleTranslationForm form) {

        Article article = articleRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("article not found"));

        // ✅ 1) content의 img(data-asset-id)들을 S3 url + data-media-id로 치환 + Media 저장
        String replacedContent = mediaService.replaceImagesAndSave(
                article,
                form.getContent(),
                form.getFiles()
        );

        boolean hasFiles = form.getFiles() != null && !form.getFiles().isEmpty();

        if (form.getId() == null) {
            ArticleTranslation articleTranslation = ArticleTranslation.create(
                    article,
                    form.getLanguageCode(),
                    form.getTitle(),
                    replacedContent, // ✅ 여기
                    form.getSummary(),
                    form.getDescription(),
                    form.getAuthor()
            );

            ArticleTranslation saved = articleTranslationRepository.save(articleTranslation);

            // ✅ 2) 최신 상태 보장(선택이지만 추천)
    //        articleTranslationRepository.flush();



            // ✅ 3) 전체콘텐츠 기준 cleanup (원문+모든번역 기준)
            if (hasFiles) mediaService.cleanUpArticleConsideringAllContents(article.getId());

            log.info("=== Called Save Translation ===");

            return saved.getId();

        } else {
            ArticleTranslation articleTranslation = articleTranslationRepository.findById(form.getId())
                    .orElseThrow(() -> new IllegalArgumentException("article translation not found"));

            // ✅ 연관관계 설정은 한 번만(둘 중 하나만)
            articleTranslation.setArticle(article); // 보통 이걸로 충분
            // article.addTranslation(articleTranslation); // ⚠️ 이미 포함되어있다면 중복 add 가능

            articleTranslation.setLanguageCode(form.getLanguageCode());
            articleTranslation.setTitle(form.getTitle());
            articleTranslation.setContent(replacedContent); // ✅ 여기
            articleTranslation.setSummary(form.getSummary());
            articleTranslation.setDescription(form.getDescription());
            articleTranslation.setAuthor(form.getAuthor());

    //        articleTranslationRepository.flush();
            if (hasFiles) mediaService.cleanUpArticleConsideringAllContents(article.getId());

            log.info("=== Called Update Translation ===");

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

    @Transactional(readOnly = true)
    public List<ArticleTopic> getTopics(Long id) {
        return articleRepository.findArticleTopicsByArticleId(id);
    }


    @Transactional(readOnly = true)
    public List<ArticleResponse> getRelatedArticles(Long articleId, LanguageCode languageCode) {

        // 1) 해당 article의 topic 목록(Primary 우선, 그 다음 sortOrder 우선)
        List<ArticleTopic> ats = articleRepository.findArticleTopicsByArticleId(articleId);
        if (ats == null || ats.isEmpty()) return List.of();

        // 결과: 중복 제거 + 순서 유지
        Map<Long, ArticleResponse> picked = new LinkedHashMap<>();

        // 유틸: 남은 개수
        final int LIMIT = 3;

        // Primary topic 찾기
        ArticleTopic primary = ats.stream()
                .filter(ArticleTopic::getPrimaryTopic)
                .findFirst()
                .orElse(null);

        // 2) primary topic으로 동일 primary 가진 article을 releaseDate desc로 최대 5개
        if (primary != null) {
            Long primaryTopicId = primary.getTopic().getId();
            List<ArticleResponse> top = articleRepository.findRelatedByPrimaryTopic(
                    articleId,
                    primaryTopicId,
                    languageCode,
                    PageRequest.of(0, LIMIT)
            );
            for (ArticleResponse r : top) {
                if (picked.size() >= LIMIT) break;
                picked.putIfAbsent(r.getId(), r);
            }
        }

        // 3) 5개 이상이면 끝
        if (picked.size() >= LIMIT) return new ArrayList<>(picked.values());

        // 4~5) 다른 topic들을 sortOrder 낮은 순으로 하나씩 돌면서 채우기
        //    - primary topic은 이미 처리했으니 제외
        Long primaryTopicId = (primary != null) ? primary.getTopic().getId() : null;

        // 순회할 topicId 리스트(중복 제거)
        List<Long> topicIdsInOrder = ats.stream()
                .map(at -> at.getTopic().getId())
                .filter(tid -> !tid.equals(primaryTopicId))
                .distinct()
                .toList();

        for (Long topicId : topicIdsInOrder) {
            int remaining = LIMIT - picked.size();
            if (remaining <= 0) break;

            // 중복 때문에 몇 개 더 가져오는 게 안전 (remaining만 가져오면 다 중복일 수 있음)
            int fetchSize = Math.max(remaining * 2, remaining);

            List<ArticleResponse> more = articleRepository.findRelatedByTopic(
                    articleId,
                    topicId,
                    languageCode,
                    PageRequest.of(0, fetchSize)
            );

            for (ArticleResponse r : more) {
                if (picked.size() >= LIMIT) break;
                picked.putIfAbsent(r.getId(), r);
            }
        }

        // 6) 모자라면 그대로 리턴
        return new ArrayList<>(picked.values());
    }

    @Transactional(readOnly = true)
    public Page<Article> findAllByKeyword(String keyword, Pageable pageable) {
        return articleRepository.findAllByKeyword(keyword, pageable);
    }

    @Transactional(readOnly = true)
    public Page<Article> findAllByReleaseDate(LocalDate start, LocalDate end, Pageable pageable) {

        if (start != null && end != null) {
            if (end.isBefore(start)) {
                // 필요하면 swap 또는 예외 처리
                LocalDate tmp = start; start = end; end = tmp;
            }
            return articleRepository.findByReleaseDateBetween(start, end, pageable);
        }
        if (start != null) {
            return articleRepository.findByReleaseDateGreaterThanEqual(start, pageable);
        }
        if (end != null) {
            return articleRepository.findByReleaseDateLessThanEqual(end, pageable);
        }
        return articleRepository.findAll(pageable);
    }

    @Transactional(readOnly = true)
    public Page<Article> search(String keyword, String publisher, LocalDate startDate, LocalDate endDate, Pageable pageable) {
        return articleRepository.search(keyword, publisher, startDate, endDate, pageable);
    }
}
