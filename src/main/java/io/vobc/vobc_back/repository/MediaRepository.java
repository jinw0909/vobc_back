package io.vobc.vobc_back.repository;

import io.vobc.vobc_back.domain.media.Media;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface MediaRepository extends JpaRepository<Media, Long> {

    Optional<Media> findByAssetId(String assetId);

    List<Media> findAllByArticleId(Long articleId);

    boolean existsByArticleIdAndAssetId(Long articleId, String assetId);

    List<Media> findAllByArticleIdAndDeletedFalse(Long articleId);

    List<Media> findAllByPostIdAndDeletedFalse(Long postId);

    boolean existsByPostIdAndAssetId(Long id, String assetId);

    @Query("select m.assetId from Media m where m.post.id = :postId and m.deleted = false")
    List<String> findAssetIdsByPostId(@Param("postId") Long postId);

    @Query("select m.assetId from Media m where m.article.id = :articleId and m.deleted = false")
    List<String> findAssetIdsByArticleId(@Param("articleId") Long articleId);
}
