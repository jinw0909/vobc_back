package io.vobc.vobc_back.service.media;

import io.vobc.vobc_back.domain.article.Article;
import io.vobc.vobc_back.domain.article.ArticleTranslation;
import io.vobc.vobc_back.domain.media.ContentType;
import io.vobc.vobc_back.domain.media.Media;
import io.vobc.vobc_back.domain.post.Post;
import io.vobc.vobc_back.domain.post.Translation;
import io.vobc.vobc_back.exception.ImageUploadException;
import io.vobc.vobc_back.repository.MediaRepository;
import io.vobc.vobc_back.repository.PostRepository;
import io.vobc.vobc_back.repository.article.ArticleRepository;
import io.vobc.vobc_back.service.S3Uploader;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class MediaService {

    private final S3Uploader s3Uploader;
    private final MediaRepository mediaRepository;
    private final ArticleRepository articleRepository;
    private final PostRepository postRepository;

    public String replaceImagesAndSave(Post post, String content, List<MultipartFile> files) {
        Map<String, MultipartFile> fileMap = buildFileMap(files);

        Set<String> existing = new HashSet<>(mediaRepository.findAssetIdsByPostId(post.getId()));

        return replaceAndPersist(
                post.getId(),
                content,
                fileMap,
                existing::contains,
                (assetId, uploadResult) -> {
                    Media media = Media.create(assetId, uploadResult.url(), uploadResult.s3Key(), ContentType.IMAGE, post);
                    mediaRepository.save(media);
                    existing.add(assetId);
                    return media;
                }
        );
    }

    public String replaceImagesAndSave(Article article, String content, List<MultipartFile> files) {
        Map<String, MultipartFile> fileMap = buildFileMap(files);

        Set<String> existing = new HashSet<>(mediaRepository.findAssetIdsByArticleId(article.getId()));

        return replaceAndPersist(
                article.getId(),
                content,
                fileMap,
                existing::contains,
                (assetId, uploadResult) -> {
                    Media media = Media.create(assetId, uploadResult.url(), uploadResult.s3Key(), ContentType.IMAGE, article);
                    mediaRepository.save(media);
                    return media;
                }
        );
    }

    private Map<String, MultipartFile> buildFileMap(List<MultipartFile> files) {
        HashMap<String, MultipartFile> map = new HashMap<>();
        if (files == null) return map;

        for (MultipartFile f : files) {
            if (f == null || f.isEmpty()) continue;
            String originalFilename = f.getOriginalFilename();
            if (originalFilename == null || originalFilename.isBlank()) continue;

            String assetId = stripExtension(originalFilename).trim();
            if (!assetId.isEmpty()) { map.put(assetId, f); }
        }

        return map;
    }

    private String stripExtension(String originalFilename) {
        int dot = originalFilename.lastIndexOf('.');
        return (dot > 0) ? originalFilename.substring(0, dot) : originalFilename;
    }

    private String replaceAndPersist(Long id,
                                     String content,
                                     Map<String, MultipartFile> fileMap,
                                     ExistsByAssetId existsByAssetId,
                                     CreateAndSaveMedia createAndSaveMedia
    ) {
        if (content == null) content = "";
        Document doc = Jsoup.parseBodyFragment(content);

        for (Element img : doc.select("img")) {
            String mediaIdStr = img.attr("data-media-id").trim();
            String assetId = img.attr("data-asset-id").trim();
            String src = img.attr("src").trim();
            boolean hasMediaId = !mediaIdStr.isEmpty();
            boolean hasAssetId = !assetId.isEmpty();

            // 1) 기존 이미지
            if (hasMediaId) {
                if (hasAssetId) {
                    img.removeAttr("data-asset-id");
                    continue;
                }
            }

            // 2) asset-id가 없으면 관리 대상 아님
            if (!hasAssetId) continue;

            // 3) DB에는 있는데 HTML이 media-id를 못 들고 있는 레거시
            if (existsByAssetId.exists(assetId)) {
                img.removeAttr("data-asset-id");
                continue;
            }

            // 4) 신규: 파일 매칭 필요 (assetId -> file)
            MultipartFile file = fileMap.get(assetId);
            if (file == null || file.isEmpty()) {
                if (looksLikeUrl(src)) {
                    log.warn("Skip upload: assetId exists neither in DB nor fileMap, but src looks like URL. postId={}, assetId={}, src={}", id, assetId, src);
                    img.removeAttr("data-asset-id");
                    continue;
                }
                if (src.startsWith("blob:") || src.startsWith("data:")) {
                    throw new IllegalArgumentException("Missing upload file for assetId: " + assetId);
                }
                throw new IllegalArgumentException("Missing upload file for assetId: " + assetId + ", src=" + src);
            }

            // 5) 업로드 + Media 저장 + HTML 치환
            try {
                S3Uploader.UploadResult uploadResult = s3Uploader.uploadToS3(id, assetId, file);
                Media media = createAndSaveMedia.createAndSave(assetId, uploadResult);
                img.attr("src", uploadResult.url());
                img.attr("data-media-id", String.valueOf(media.getId()));

                // 업로드 완료 . asset-id 제거 (다음 수정 시 신규로 오해 방지)
                img.removeAttr("data-asset-id");
            } catch (Exception e) {
                log.error("Failed to upload image. assetId={}, postId={}", assetId, id, e);
                throw new ImageUploadException("Failed to upload image: " + assetId, e);
            }

        }

        return doc.body().html();
    }

    private boolean looksLikeUrl(String src) {
        return src != null && (src.startsWith("http://") || src.startsWith("https://"));
    }

    public void cleanUp(Long postId, String finalContent) {
        cleanupUnused(
                postId,
                finalContent,
                () -> mediaRepository.findAllByPostIdAndDeletedFalse(postId)
        );
    }

    private void cleanupUnused(Long postId, String finalContent, LoadAllMedia loadAllMedia) {
        Document doc = Jsoup.parseBodyFragment(finalContent == null ? "" : finalContent);

        Set<Long> aliveIds = doc.select("img[data-media-id]").stream()
                .map(el -> el.attr("data-media-id"))
                .filter(s -> !s.isBlank())
                .map(Long::valueOf)
                .collect(Collectors.toSet());

        List<Media> all = loadAllMedia.load();

        if (aliveIds.isEmpty()) {
            for (Media media : all) deleteOne(postId, media);
            return;
        }

        for (Media m : all) {
            if (!aliveIds.contains(m.getId())) {
                deleteOne(postId, m);
            }
        }
    }

    private void deleteOne(Long postId, Media m) {
        try {
            // 1) S3 삭제
            s3Uploader.delete(m.getS3Key());
            // 2) DB 삭제
            mediaRepository.delete(m);
        } catch (Exception e) {
            log.error("Failed to delete media. postId={}, mediaId={}", postId, m.getId(), e);
            throw new RuntimeException("Failed to delete media: " + m.getId(), e);
        }
    }

    public void cleanUpArticleConsideringAllContents(Long articleId) {
        Article article = articleRepository.findWithTranslationById(articleId).orElseThrow();

        StringBuilder combined = new StringBuilder((article.getContent() == null ? "" : article.getContent()));

        for (ArticleTranslation tr : article.getTranslations()) {
            if (tr.getContent() != null) combined.append("\n").append(tr.getContent());
        }

        cleanupUnused(
                articleId,
                combined.toString(),
                () -> mediaRepository.findAllByArticleIdAndDeletedFalse(articleId)
        );
    }

    public void cleanUpPostConsideringAllContents(Long postId) {
        Post post = postRepository.findWithTranslationById(postId).orElseThrow();

        StringBuilder combined = new StringBuilder((post.getContent() == null ? "" : post.getContent()));

        for (Translation tr : post.getTranslations()) {
            if (tr.getContent() != null) combined.append("\n").append(tr.getContent());
        }

        cleanupUnused(
                postId,
                combined.toString(),
                () -> mediaRepository.findAllByPostIdAndDeletedFalse(postId)
        );
    }


    @FunctionalInterface
    private interface ExistsByAssetId {
        boolean exists(String assetId);
    }

    @FunctionalInterface
    private interface CreateAndSaveMedia {
        Media createAndSave(String assetId, S3Uploader.UploadResult uploadResult);
    }

    @FunctionalInterface
    private interface LoadAllMedia {
        List<Media> load();
    }


}
