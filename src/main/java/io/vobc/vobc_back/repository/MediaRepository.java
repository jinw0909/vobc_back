package io.vobc.vobc_back.repository;

import io.vobc.vobc_back.domain.media.Media;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MediaRepository extends JpaRepository<Media, Long> {

    Optional<Media> findByAssetId(String assetId);
    List<Media> findAllByArticleId(Long articleId);
    boolean existsByArticleIdAndAssetId(Long articleId, String assetId);
    List<Media> findAllByArticleIdAndDeletedFalse(Long articleId);
}
